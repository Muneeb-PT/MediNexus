package com.medinexus.service.identity;

import com.medinexus.config.DigiLockerConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DigiLockerOAuthService {

    /*
     * One state store is shared by all OAuth service instances
     * inside the running MediNexus application.
     *
     * This allows the service that starts authorization and the
     * callback service to access the same pending OAuth state.
     */
    private static final OAuthStateStore STATE_STORE =
            new OAuthStateStore();

    public DigiLockerOAuthService() {
    }

    public AuthorizationRequest createAuthorizationRequest(
            int patientId) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (!DigiLockerConfig.isConfigured()) {
            throw new IllegalStateException(
                    "DigiLocker integration is not configured."
            );
        }

        String state =
                patientId + "-" + UUID.randomUUID();

        String codeVerifier =
                PkceUtil.generateCodeVerifier();

        STATE_STORE.save(
                state,
                patientId,
                codeVerifier
        );

        String codeChallenge =
                PkceUtil.generateCodeChallenge(
                        codeVerifier
                );

        String authorizationUrl =
                DigiLockerConfig.AUTHORIZATION_URL
                        + "?response_type=code"
                        + "&client_id="
                        + encode(
                        DigiLockerConfig.CLIENT_ID
                )
                        + "&redirect_uri="
                        + encode(
                        DigiLockerConfig.REDIRECT_URI
                )
                        + "&state="
                        + encode(state)
                        + "&code_challenge="
                        + encode(codeChallenge)
                        + "&code_challenge_method=S256";

        return new AuthorizationRequest(
                authorizationUrl,
                state,
                codeVerifier
        );
    }

    public OAuthStateStore.OAuthState consumeAuthorizationState(
            String state) {

        return STATE_STORE.consume(state);
    }

    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    public record AuthorizationRequest(
            String authorizationUrl,
            String state,
            String codeVerifier
    ) {
    }
}