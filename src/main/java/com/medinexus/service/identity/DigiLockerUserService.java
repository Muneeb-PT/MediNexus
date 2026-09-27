package com.medinexus.service.identity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DigiLockerUserService {

    private static final String USER_DETAILS_URL =
            "https://api.digilocker.gov.in/public/oauth2/1/user";

    private final HttpClient httpClient;

    public DigiLockerUserService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public UserDetails getUserDetails(
            String accessToken) {

        if (accessToken == null
                || accessToken.isBlank()) {

            throw new IllegalArgumentException(
                    "Access token cannot be empty."
            );
        }

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(USER_DETAILS_URL))
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .GET()
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 401) {

                throw new IllegalStateException(
                        "DigiLocker access token is invalid or expired."
                );
            }

            if (response.statusCode() == 403) {

                throw new IllegalStateException(
                        "DigiLocker access was not permitted for this request."
                );
            }

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "DigiLocker user-details request failed. HTTP "
                                + response.statusCode()
                );
            }

            return parseUserDetails(
                    response.body()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "DigiLocker user-details request was interrupted.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to communicate with DigiLocker.",
                    e
            );
        }
    }

    private UserDetails parseUserDetails(
            String json) {

        String digiLockerId =
                extractJsonValue(
                        json,
                        "digilockerid"
                );

        if (digiLockerId == null) {
            digiLockerId =
                    extractJsonValue(
                            json,
                            "digilocker_id"
                    );
        }

        String name =
                extractJsonValue(
                        json,
                        "name"
                );

        String dateOfBirth =
                extractJsonValue(
                        json,
                        "dob"
                );

        String gender =
                extractJsonValue(
                        json,
                        "gender"
                );

        if (digiLockerId == null
                || digiLockerId.isBlank()) {

            throw new IllegalStateException(
                    "DigiLocker response did not contain a DigiLocker ID."
            );
        }

        return new UserDetails(
                digiLockerId,
                name,
                dateOfBirth,
                gender
        );
    }

    private String extractJsonValue(
            String json,
            String key) {

        if (json == null || json.isBlank()) {
            return null;
        }

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

        int firstQuote =
                json.indexOf(
                        '"',
                        colonIndex + 1
                );

        if (firstQuote < 0) {
            return null;
        }

        int secondQuote =
                json.indexOf(
                        '"',
                        firstQuote + 1
                );

        if (secondQuote < 0) {
            return null;
        }

        return json.substring(
                firstQuote + 1,
                secondQuote
        );
    }

    public record UserDetails(
            String digiLockerId,
            String name,
            String dateOfBirth,
            String gender
    ) {
    }
}