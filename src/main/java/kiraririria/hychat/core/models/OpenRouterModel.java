package kiraririria.hychat.core.models;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kiraririria.hychat.HyChatPlugin;


public class OpenRouterModel implements IModel
{
    private static final String BASE_URL = "https://openrouter.ai/api/v1";
    private static final int TIMEOUT = 30000;

    private static OpenRouterModel instance;

    private OpenRouterModel()
    {

    }

    public static OpenRouterModel getInstance()
    {
        if (instance == null)
        {
            instance = new OpenRouterModel();
        }
        return instance;
    }

    @Override
    public void generateTextStream(JsonObject request, Consumer<String> onChunk, boolean useStreaming) throws IOException
    {
        HttpURLConnection conn = createConnection(BASE_URL + "/chat/completions", "POST", useStreaming);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(request.toString().getBytes(StandardCharsets.UTF_8));
        }

        try {
            if (useStreaming) {
                processStreamingResponse(conn, onChunk);
            } else {
                processNonStreamingResponse(conn, onChunk);
            }
        } catch (SocketException e) {
            System.out.println("Connection aborted by user");
        } finally {
            conn.disconnect();
        }
    }


    private void processNonStreamingResponse(HttpURLConnection conn, Consumer<String> onChunk) throws IOException {
        String response = readFullResponseWithRetry(conn);
        String content = extractContentFromJson(response);
        if (content != null && !content.isEmpty()) {
            onChunk.accept(content);
        }
    }

    private void processStreamingResponse(HttpURLConnection conn, Consumer<String> onChunk) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                if (line.startsWith("data: ")) {
                    String json = line.substring(6).trim();
                    if (json.equals("[DONE]")) {
                        break;
                    }
                    if (!json.isEmpty()) {
                        String content = extractContentFromStreamingJson(json);
                        if (content != null && !content.isEmpty()) {
                            onChunk.accept(content);
                        }
                    }
                }
            }
        }
    }

    private String extractContentFromStreamingJson(String json) {
        try {
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            if (jsonObject.has("choices")) {
                JsonArray choices = jsonObject.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    StringBuilder contentBuilder = new StringBuilder();

                    if (choice.has("delta")) {
                        JsonObject delta = choice.getAsJsonObject("delta");

                        if (delta.has("reasoning") && !delta.get("reasoning").isJsonNull()) {
                            String reasoning = delta.get("reasoning").getAsString();
                            if (reasoning != null && !reasoning.isEmpty()) {
                                System.out.println("[Reasoning] " + reasoning);
                            }
                        }

                        if (delta.has("content") && !delta.get("content").isJsonNull()) {
                            String regularContent = delta.get("content").getAsString();
                            if (regularContent != null && !regularContent.isEmpty()) {
                                contentBuilder.append(regularContent);
                            }
                        }
                    }

                    return contentBuilder.length() > 0 ? contentBuilder.toString() : null;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse streaming JSON: " + e.getMessage());
            System.err.println("Problematic JSON: " + json);
        }
        return null;
    }

    private String extractContentFromJson(String json) {
        try {
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            return jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
        return null;
    }

    private HttpURLConnection createConnection(String url, String method, boolean isStream) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " +HyChatPlugin.getInstance().getConfig().get().getOpenrouterKey());
        conn.setRequestProperty("HTTP-Referer", "arichat");
        conn.setRequestProperty("X-Title", "Arichat Classic");

        if (isStream) {
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Connection", "keep-alive");
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(isStream ? 60000 : TIMEOUT);
        return conn;
    }

    private String readFullResponseWithRetry(HttpURLConnection conn) throws IOException {
        int retries = 3;
        int delay = 2000;

        for (int i = 0; i < retries; i++) {
            try {
                return readFullResponse(conn);
            } catch (SocketTimeoutException e) {
                if (i == retries - 1) throw e;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "";
    }

    private String readFullResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    public static void abortGeneration() {
        System.out.println("Abort generation not supported by OpenRouter");
    }

    public int countTokens(String prompt) {
        System.out.println("Token count endpoint not available for OpenRouter");
        return -1;
    }

    public void fetchModelsAsync(Consumer<List<String>> callback) {
        new Thread(() -> {
            try {
                List<String> models = fetchModels();
                callback.accept(models);
            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(new ArrayList<>());
            }
        }).start();
    }

    private List<String> fetchModels() throws IOException {
        List<String> models = new ArrayList<>();
        HttpURLConnection connection = createModelsConnection();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = readFullResponse(connection);
            JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();
            JsonArray dataArray = jsonResponse.getAsJsonArray("data");

            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject model = dataArray.get(i).getAsJsonObject();
                String modelId = model.get("id").getAsString();
                models.add(modelId);
            }
        }
        return models;
    }

    public String getModels() {
        final List<String>[] result = new List[] {new ArrayList<>()};
        fetchModelsAsync(models -> result[0] = models);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return String.join(",", result[0]);
    }

    public List<String> getAllModels() {
        final List<String>[] result = new List[] {new ArrayList<>()};
        fetchModelsAsync(models -> result[0] = models);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

    public void checkConnectionAsync(Consumer<Boolean> callback) {
        // Implementation pending or remove if unnecessary
    }

    public void fetchModelsWithDetailsAsync(Consumer<List<ModelDetail>> callback) {
        new Thread(() -> {
            try {
                List<ModelDetail> models = fetchModelsWithDetails();
                callback.accept(models);
            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(new ArrayList<>());
            }
        }).start();
    }

    public List<ModelDetail> fetchModelsWithDetails() throws IOException {
        List<ModelDetail> models = new ArrayList<>();
        HttpURLConnection connection = createModelsConnection();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = readFullResponse(connection);
            JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();
            JsonArray dataArray = jsonResponse.getAsJsonArray("data");

            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject model = dataArray.get(i).getAsJsonObject();
                ModelDetail detail = new ModelDetail();
                detail.id = model.get("id").getAsString();

                if (model.has("pricing")) {
                    JsonObject pricing = model.getAsJsonObject("pricing");
                    if (pricing.has("prompt") && !pricing.get("prompt").isJsonNull()) {
                        detail.promptPrice = pricing.get("prompt").getAsDouble();
                    }
                    if (pricing.has("completion") && !pricing.get("completion").isJsonNull()) {
                        detail.completionPrice = pricing.get("completion").getAsDouble();
                    }
                }

                if (model.has("context_length") && !model.get("context_length").isJsonNull()) {
                    detail.contextLength = model.get("context_length").getAsInt();
                }

                models.add(detail);
            }
        }
        models.forEach(detail -> {
            System.out.println(detail.id);

        });
        return models;
    }

    private HttpURLConnection createModelsConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/models").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + HyChatPlugin.getInstance().getConfig().get().getOpenrouterKey());
        connection.setRequestProperty("HTTP-Referer", "arichat");
        connection.setRequestProperty("X-Title", "Arichat");
        return connection;
    }

    public static class ModelDetail {
        public String id;
        public double promptPrice;
        public double completionPrice;
        public int contextLength;

        public ModelDetail()
        {
            super();
        }

        public ModelDetail(String id, double promptPrice, double completionPrice, int contextLength)
        {
            this.id = id;
            this.promptPrice = promptPrice;
            this.completionPrice = completionPrice;
            this.contextLength = contextLength;
        }
    }
}