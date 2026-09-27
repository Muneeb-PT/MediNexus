package com.medinexus.service.identity;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class OAuthCallbackServer {

    private final int port;
    private final String callbackPath;

    private HttpServer server;

    private BiConsumer<String, String> callbackHandler;

    public OAuthCallbackServer(
            int port,
            String callbackPath) {

        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(
                    "Invalid callback port."
            );
        }

        if (callbackPath == null
                || callbackPath.isBlank()
                || !callbackPath.startsWith("/")) {

            throw new IllegalArgumentException(
                    "Callback path must start with '/'."
            );
        }

        this.port = port;
        this.callbackPath = callbackPath;
    }

    public void setCallbackHandler(
            BiConsumer<String, String> callbackHandler) {

        if (callbackHandler == null) {
            throw new IllegalArgumentException(
                    "Callback handler cannot be null."
            );
        }

        this.callbackHandler = callbackHandler;
    }

    public void start() {

        if (server != null) {
            return;
        }

        try {

            server =
                    HttpServer.create(
                            new InetSocketAddress(
                                    "127.0.0.1",
                                    port
                            ),
                            0
                    );

            server.createContext(
                    callbackPath,
                    this::handleRequest
            );

            server.setExecutor(
                    Executors.newSingleThreadExecutor()
            );

            server.start();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to start OAuth callback server.",
                    e
            );
        }
    }

    public void stop() {

        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private void handleRequest(
            HttpExchange exchange) {

        try {

            URI requestUri =
                    exchange.getRequestURI();

            Map<String, String> parameters =
                    parseQuery(
                            requestUri.getRawQuery()
                    );

            String code =
                    parameters.get("code");

            String state =
                    parameters.get("state");

            String error =
                    parameters.get("error");

            String response;

            if (error != null && !error.isBlank()) {

                response =
                        """
                        <html>
                        <body>
                        <h2>MediNexus Identity Verification</h2>
                        <p>Authorization was not completed.</p>
                        <p>You may close this browser window.</p>
                        </body>
                        </html>
                        """;

            } else if (code == null
                    || code.isBlank()
                    || state == null
                    || state.isBlank()) {

                response =
                        """
                        <html>
                        <body>
                        <h2>MediNexus Identity Verification</h2>
                        <p>Invalid OAuth callback.</p>
                        <p>You may close this browser window.</p>
                        </body>
                        </html>
                        """;

            } else {

                if (callbackHandler != null) {

                    callbackHandler.accept(
                            code,
                            state
                    );
                }

                response =
                        """
                        <html>
                        <body>
                        <h2>MediNexus</h2>
                        <p>Identity authorization received.</p>
                        <p>You may close this browser window and return to MediNexus.</p>
                        </body>
                        </html>
                        """;
            }

            byte[] bytes =
                    response.getBytes(
                            StandardCharsets.UTF_8
                    );

            exchange.getResponseHeaders()
                    .set(
                            "Content-Type",
                            "text/html; charset=UTF-8"
                    );

            exchange.sendResponseHeaders(
                    200,
                    bytes.length
            );

            exchange.getResponseBody()
                    .write(bytes);

        } catch (Exception e) {

            try {

                byte[] bytes =
                        """
                        <html>
                        <body>
                        <h2>MediNexus</h2>
                        <p>Unable to process the authorization callback.</p>
                        </body>
                        </html>
                        """
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                exchange.sendResponseHeaders(
                        500,
                        bytes.length
                );

                exchange.getResponseBody()
                        .write(bytes);

            } catch (IOException ignored) {
                // Nothing else can be done with this HTTP exchange.
            }

        } finally {

            try {
                exchange.close();
            } catch (Exception ignored) {
                // Ignore close failure.
            }
        }
    }

    private Map<String, String> parseQuery(
            String query) {

        Map<String, String> result =
                new HashMap<>();

        if (query == null
                || query.isBlank()) {

            return result;
        }

        String[] pairs =
                query.split("&");

        for (String pair : pairs) {

            int separator =
                    pair.indexOf('=');

            if (separator < 0) {
                continue;
            }

            String key =
                    URLDecoder.decode(
                            pair.substring(
                                    0,
                                    separator
                            ),
                            StandardCharsets.UTF_8
                    );

            String value =
                    URLDecoder.decode(
                            pair.substring(
                                    separator + 1
                            ),
                            StandardCharsets.UTF_8
                    );

            result.put(
                    key,
                    value
            );
        }

        return result;
    }
}