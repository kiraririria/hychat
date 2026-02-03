package kiraririria.hychat.core.utils.png;

public class PngChunk
{
    public int length;
    public String type;
    public byte[] data;
    public int crc;

    public PngChunk(int length, String type, byte[] data, int crc)
    {
        this.length = length;
        this.type = type;
        this.data = data;
        this.crc = crc;
    }
}
