package kiraririria.hychat.core.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kiraririria.hychat.HyChatPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class KoboldAI implements IModel
{
    private static final int TIMEOUT = 30000;
    public static String BASE_URL = "";
    private static KoboldAI instance;

    private KoboldAI()
    {
        BASE_URL = HyChatPlugin.getInstance().getConfig().get().getKoboldUrl();
    }
    public static KoboldAI getInstance()
    {
        if (instance == null)
        {
            instance = new KoboldAI();
        }
        return instance;
    }
    public String getModels() throws IOException
    {
        HttpURLConnection conn = createConnection(BASE_URL + "/v1/models", "GET", false);
        return readFullResponseWithRetry(conn);
    }

    public String getVersion() throws IOException
    {
        HttpURLConnection conn = createConnection(BASE_URL + "/v1/info/version", "GET", false);
        return readFullResponseWithRetry(conn);
    }

    public void generateTextStream(JsonObject object, Consumer<String> onChunk) throws IOException
    {
        generateTextStream(object, onChunk, HyChatPlugin.getInstance().getConfig().get().isStreamResponse());
    }

    @Override
    public void generateTextStream(JsonObject request, Consumer<String> onChunk, boolean useStreaming) throws IOException
    {
        HttpURLConnection conn = createConnection(BASE_URL + "/v1/completions", "POST", useStreaming);

        try (OutputStream os = conn.getOutputStream())
        {
            os.write(request.toString().getBytes(StandardCharsets.UTF_8));
        }

        try
        {
            if (useStreaming)
            {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())))
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        if (line.startsWith("data: "))
                        {
                            String json = line.substring(6).trim();
                            if (!json.isEmpty() && !json.equals("[DONE]"))
                            {
                                String text = extractTextFromJson(json);
                                if (text != null && !text.isEmpty())
                                {
                                    onChunk.accept(text);
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                String response = readFullResponseWithRetry(conn);
                String text = extractTextFromJson(response);
                onChunk.accept(text);
            }
        }
        catch (SocketException e)
        {
            System.out.println("Connection aborted by user");
        }
        finally
        {
            conn.disconnect();
        }

    }

    public static CompletableFuture<Void> abortGenerationAsync()
    {
        return CompletableFuture.runAsync(() ->
        {
            try
            {
                abortGeneration();
            }
            catch (IOException e)
            {
                System.err.println("Error aborting generation: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void abortGeneration() throws IOException
    {
        String abortUrl = BASE_URL + "/api/extra/abort";
        System.out.println("Sending abort request to: " + abortUrl);

        HttpURLConnection conn = null;
        try
        {
            conn = (HttpURLConnection) new URL(abortUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300)
            {
                System.out.println("Generation aborted successfully");
            }
            else
            {
                System.err.println("Abort request failed. Response code: " + responseCode);
            }
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    public int countTokens(String prompt)
    {
        String tokenCountUrl = BASE_URL + "/api/extra/tokencount";

        try
        {
            HttpURLConnection conn = createConnection(tokenCountUrl, "POST", false);
            conn.setRequestProperty("Accept", "application/json");

            JsonObject request = new JsonObject();
            request.addProperty("prompt", prompt);

            try (OutputStream os = conn.getOutputStream())
            {
                os.write(request.toString().getBytes(StandardCharsets.UTF_8));
            }

            String response = readFullResponseWithRetry(conn);
            JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();

            if (jsonResponse.has("value"))
            {
                return jsonResponse.get("value").getAsInt();
            }
            else
            {
                System.err.println("Invalid token count response: missing 'value' field");
                return -1;
            }
        }
        catch (Exception e)
        {
            System.err.println("Token count error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private String extractTextFromJson(String json)
    {
        try
        {
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            return jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject().get("text").getAsString();
        }
        catch (Exception e)
        {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
        return null;
    }

    private HttpURLConnection createConnection(String url, String method, boolean isStream) throws IOException
    {
        System.out.println("Creating connection to: " + url);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (isStream)
        {
            conn.setRequestProperty("Accept", "text/event-stream");
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(isStream ? 0 : TIMEOUT);
        return conn;
    }

    private String readFullResponseWithRetry(HttpURLConnection conn) throws IOException
    {
        int retries = 3;
        int delay = 2000;

        for (int i = 0; i < retries; i++)
        {
            try
            {
                return readFullResponse(conn);
            }
            catch (SocketTimeoutException e)
            {
                if (i == retries - 1) throw e;
                System.out.println("Timeout, retrying... (" + (i + 1) + "/" + retries + ")");
                try {Thread.sleep(delay);} catch (InterruptedException ie) {Thread.currentThread().interrupt();}
            }
        }
        return "";
    }

    private String readFullResponse(HttpURLConnection conn) throws IOException
    {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                response.append(line);
            }
        }
        return response.toString();
    }
}