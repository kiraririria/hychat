package kiraririria.hychat.core.utils.png;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class PngMetadataHandler
{
    public static byte[] writePngMetadata(InputStream inputStream, String charaData) throws IOException
    {
        List<PngChunk> chunks = readAllChunks(inputStream);
        chunks.removeIf(chunk -> chunk.type.equals("tEXt") &&
                (getTextChunkKeyword(chunk.data).equalsIgnoreCase("chara") ||
                        getTextChunkKeyword(chunk.data).equalsIgnoreCase("ccv3")));

        byte[] charaChunk = createTextChunk("chara",
                Base64.getEncoder().encodeToString(charaData.getBytes(StandardCharsets.UTF_8)));

        try
        {
            String ccv3Data = convertToCCV3Format(charaData);
            byte[] ccv3Chunk = createTextChunk("ccv3",
                    Base64.getEncoder().encodeToString(ccv3Data.getBytes(StandardCharsets.UTF_8)));

            insertBeforeIEND(chunks, charaChunk);
            insertBeforeIEND(chunks, ccv3Chunk);
        }
        catch (Exception e)
        {
            insertBeforeIEND(chunks, charaChunk);
        }
        return assemblePng(chunks);
    }

    public static String readPngTextChunk(InputStream inputStream) throws IOException
    {
        List<PngChunk> chunks = readAllChunks(inputStream);

        for (PngChunk chunk : chunks)
        {
            if (chunk.type.equals("tEXt"))
            {
                String chunkKeyword = getTextChunkKeyword(chunk.data);
                if (Arrays.asList("chara", "ccv3").contains(chunkKeyword.toLowerCase()))
                {
                    byte[] textData = Arrays.copyOfRange(chunk.data,
                            chunkKeyword.length() + 1, chunk.data.length);
                    return new String(Base64.getDecoder().decode(textData), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private static List<PngChunk> readAllChunks(InputStream inputStream) throws IOException
    {
        List<PngChunk> chunks = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream)))
        {
            byte[] signature = new byte[8];
            dis.readFully(signature);
            if (!Arrays.equals(signature, new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}))
            {
                throw new IOException("Not a valid PNG file");
            }

            while (true)
            {
                int length = dis.readInt();
                byte[] typeBytes = new byte[4];
                dis.readFully(typeBytes);
                String type = new String(typeBytes, StandardCharsets.US_ASCII);

                byte[] data = new byte[length];
                if (length > 0) dis.readFully(data);

                int crc = dis.readInt();
                chunks.add(new PngChunk(length, type, data, crc));

                if (type.equals("IEND")) break;
            }
        }
        return chunks;
    }

    private static String getTextChunkKeyword(byte[] textChunkData)
    {
        for (int i = 0; i < textChunkData.length; i++)
        {
            if (textChunkData[i] == 0)
            {
                return new String(textChunkData, 0, i, StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static byte[] createTextChunk(String keyword, String text)
    {
        byte[] keywordBytes = keyword.getBytes(StandardCharsets.UTF_8);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(keywordBytes.length + 1 + textBytes.length);
        buffer.put(keywordBytes);
        buffer.put((byte) 0);
        buffer.put(textBytes);

        return buffer.array();
    }

    private static void insertBeforeIEND(List<PngChunk> chunks, byte[] textData)
    {
        int iendIndex = -1;
        for (int i = 0; i < chunks.size(); i++)
        {
            if (chunks.get(i).type.equals("IEND"))
            {
                iendIndex = i;
                break;
            }
        }

        if (iendIndex != -1)
        {
            int crc = calculateCRC("tEXt".getBytes(StandardCharsets.US_ASCII), textData);
            chunks.add(iendIndex, new PngChunk(textData.length, "tEXt", textData, crc));
        }
    }

    private static byte[] assemblePng(List<PngChunk> chunks) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10});

        for (PngChunk chunk : chunks)
        {
            writeInt(baos, chunk.length);
            baos.write(chunk.type.getBytes(StandardCharsets.US_ASCII));
            baos.write(chunk.data);
            writeInt(baos, chunk.crc);
        }

        return baos.toByteArray();
    }

    private static String convertToCCV3Format(String charaData) throws Exception
    {
        return "{\"spec\":\"chara_card_v3\",\"spec_version\":\"3.0\",\"data\":" + charaData + "}";
    }

    private static int calculateCRC(byte[] type, byte[] data)
    {
        int crc = 0xFFFFFFFF;

        for (byte b : type)
        {
            crc = updateCRC(crc, b);
        }

        for (byte b : data)
        {
            crc = updateCRC(crc, b);
        }
        return ~crc;
    }

    private static int updateCRC(int crc, byte b)
    {
        final int poly = 0xEDB88320;

        crc ^= (b & 0xFF);

        for (int i = 0; i < 8; i++)
        {
            if ((crc & 1) != 0)
            {
                crc = (crc >>> 1) ^ poly;
            }
            else
            {
                crc >>>= 1;
            }
        }

        return crc;
    }

    private static void writeInt(OutputStream os, int value) throws IOException
    {
        os.write((value >> 24) & 0xFF);
        os.write((value >> 16) & 0xFF);
        os.write((value >> 8) & 0xFF);
        os.write(value & 0xFF);
    }
}