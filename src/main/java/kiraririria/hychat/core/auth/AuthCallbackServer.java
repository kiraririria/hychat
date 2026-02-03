package kiraririria.hychat.core.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuthCallbackServer
{
    public static String waitForAuthCode(int port) throws Exception
    {
        final BlockingQueue<String> codeQueue = new LinkedBlockingQueue<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/callback", (HttpExchange exchange) ->
        {
            try
            {
                String query = exchange.getRequestURI().getQuery();
                String code = extractCodeFromQuery(query);

                String response = "Authorization successful! You can close this window and return to the application";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody())
                {
                    os.write(response.getBytes());
                }

                if (code != null)
                {
                    codeQueue.offer(code);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        server.start();
        System.out.println("Server is running at " + port + ", waiting for auth code...");

        String authCode = codeQueue.take();
        server.stop(0);

        return authCode;
    }

    private static String extractCodeFromQuery(String query)
    {
        if (query != null)
        {
            String[] pairs = query.split("&");
            for (String pair : pairs)
            {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && "code".equals(keyValue[0]))
                {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
}