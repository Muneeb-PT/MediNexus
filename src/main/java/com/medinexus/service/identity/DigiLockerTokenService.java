package com.medinexus.service.identity;

import com.medinexus.config.DigiLockerConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DigiLockerTokenService {

    private final HttpClient httpClient;

    public DigiLockerTokenService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public TokenResponse exchangeCode(
            String authorizationCode,
            String codeVerifier) {

        if (authorizationCode == null
                || authorizationCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Authorization code cannot be empty."
            );
        }

        if (codeVerifier == null
                || codeVerifier.isBlank()) {

            throw new IllegalArgumentException(
                    "PKCE code verifier cannot be empty."
            );
        }

        if (!DigiLockerConfig.isConfigured()) {

            throw new IllegalStateException(
                    "DigiLocker integration is not configured."
            );
        }

        String body =
                "grant_type=authorization_code"
                        + "&code="
                        + encode(authorizationCode)
                        + "&client_id="
                        + encode(
                        DigiLockerConfig.CLIENT_ID
                )
                        + "&client_secret="
                        + encode(
                        DigiLockerConfig.CLIENT_SECRET
                )
                        + "&redirect_uri="
                        + encode(
                        DigiLockerConfig.REDIRECT_URI
                )
                        + "&code_verifier="
                        + encode(codeVerifier);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        DigiLockerConfig.TOKEN_URL
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        body
                                )
                        )
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "DigiLocker token exchange failed. HTTP "
                                + response.statusCode()
                );
            }

            return parseTokenResponse(
                    response.body()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "DigiLocker token exchange was interrupted.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to communicate with DigiLocker.",
                    e
            );
        }
    }

    private TokenResponse parseTokenResponse(
            String json) {

        if (json == null
                || json.isBlank()) {

            throw new IllegalStateException(
                    "DigiLocker returned an empty token response."
            );
        }

        String accessToken =
                extractJsonValue(
                        json,
                        "access_token"
                );

        String tokenType =
                extractJsonValue(
                        json,
                        "token_type"
                );

        String expiresIn =
                extractJsonValue(
                        json,
                        "expires_in"
                );

        if (accessToken == null
                || accessToken.isBlank()) {

            throw new IllegalStateException(
                    "DigiLocker response did not contain an access token."
            );
        }

        return new TokenResponse(
                accessToken,
                tokenType,
                expiresIn
        );
    }

    private String extractJsonValue(
            String json,
            String key) {

        String search =
                "\"" + key + "\"";

        int keyIndex =
                json.indexOf(search);

        if (keyIndex < 0) {
            return null;
        }

        int colonIndex =
                json.indexOf(
                        ':',
                        keyIndex + search.length()
                );

        if (colonIndex < 0) {
            return null;
        }

        int valueStart =
                colonIndex + 1;

        while (valueStart < json.length()
                && Character.isWhitespace(
                json.charAt(valueStart))) {

            valueStart++;
        }

        if (valueStart >= json.length()) {
            return null;
        }

        if (json.charAt(valueStart) == '"') {

            int firstQuote =
                    valueStart;

            int secondQuote =
                    findClosingQuote(
                            json,
                            firstQuote + 1
                    );

            if (secondQuote > firstQuote) {

                return json.substring(
                        firstQuote + 1,
                        secondQuote
                );
            }

            return null;
        }

        int commaIndex =
                json.indexOf(
                        ',',
                        valueStart
                );

        int closingBraceIndex =
                json.indexOf(
                        '}',
                        valueStart
                );

        int closingBracketIndex =
                json.indexOf(
                        ']',
                        valueStart
                );

        int endIndex =
                json.length();

        if (commaIndex >= 0) {

            endIndex =
                    Math.min(
                            endIndex,
                            commaIndex
                    );
        }

        if (closingBraceIndex >= 0) {

            endIndex =
                    Math.min(
                            endIndex,
                            closingBraceIndex
                    );
        }

        if (closingBracketIndex >= 0) {

            endIndex =
                    Math.min(
                            endIndex,
                            closingBracketIndex
                    );
        }

        return json.substring(
                valueStart,
                endIndex
        ).trim();
    }

    private int findClosingQuote(
            String json,
            int startIndex) {

        boolean escaped = false;

        for (int i = startIndex;
             i < json.length();
             i++) {

            char current =
                    json.charAt(i);

            if (escaped) {

                escaped = false;

                continue;
            }

            if (current == '\\') {

                escaped = true;

                continue;
            }

            if (current == '"') {

                return i;
            }
        }

        return -1;
    }

    private String encode(
            String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            String expiresIn
    ) {
    }
}