package kiraririria.hychat.core.auth;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.common.HyChatConfig;
import kiraririria.hychat.core.utils.WebLinkUtil;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static kiraririria.hychat.HyChatPlugin.playerRef;

public class OpenRouterAuth
{
    public static void main(String[] args)
    {
        try
        {
            String codeVerifier = PkceGenerator.generateCodeVerifier();
            String codeChallenge = PkceGenerator.generateCodeChallenge(codeVerifier);

            String redirectUri = "http://localhost:8080/callback";
            String authUrl = "https://openrouter.ai/auth" +
                    "?callback_url=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&code_challenge=" + codeChallenge +
                    "&code_challenge_method=S256";

            System.out.println("Open OpenRouterAuth...");

            WebLinkUtil.openWebLink(authUrl);
            System.out.println("[HC] Open this URL: " + authUrl);

            String authCode = AuthCallbackServer.waitForAuthCode(8080);
            System.out.println("Accepted");
            String apiKey = exchangeCodeForToken(authCode, codeVerifier);
            System.out.println("API key: " + apiKey);

            HyChatConfig config = HyChatPlugin.getInstance().getConfig().get();
            config.setOpenrouterKey(apiKey);
            HyChatPlugin.getInstance().getConfig().save();

            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.raw("HyChat"),
                    Message.raw("OpenRouter Key Updated!"),
                    NotificationStyle.Success
            );
            System.out.println("Saved!");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String exchangeCodeForToken(String authCode, String codeVerifier) throws Exception
    {
        String url = "https://openrouter.ai/api/v1/auth/keys";
        HttpURLConnection conn = null;

        try
        {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            String requestBody = String.format(
                    "{\"code\": \"%s\", \"code_verifier\": \"%s\", \"code_challenge_method\": \"S256\"}",
                    authCode, codeVerifier
            );

            try (OutputStream os = conn.getOutputStream())
            {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
                in.close();

                String responseBody = response.toString();
                return parseApiKeyFromJson(responseBody);

            }
            else
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String inputLine;
                StringBuilder errorResponse = new StringBuilder();

                while ((inputLine = in.readLine()) != null)
                {
                    errorResponse.append(inputLine);
                }
                in.close();

                throw new RuntimeException("Error API: " + responseCode + " - " + errorResponse);
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

    private static String parseApiKeyFromJson(String json)
    {
        int keyIndex = json.indexOf("\"key\":\"");
        if (keyIndex != -1)
        {
            int start = keyIndex + 7;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        throw new RuntimeException("Error: " + json);
    }
}