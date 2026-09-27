package com.medinexus.service.identity;

public class DigiLockerCallbackService {

    private final DigiLockerOAuthService oauthService;
    private final DigiLockerTokenService tokenService;

    public DigiLockerCallbackService() {
        this.oauthService =
                new DigiLockerOAuthService();

        this.tokenService =
                new DigiLockerTokenService();
    }

    public CallbackResult handleCallback(
            String code,
            String state) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Authorization code is missing."
            );
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "OAuth state is missing."
            );
        }

        /*
         * Consuming the state performs both:
         * 1. existence validation
         * 2. expiry validation
         *
         * The state is removed after successful retrieval,
         * preventing reuse.
         */
        OAuthStateStore.OAuthState storedState =
                oauthService.consumeAuthorizationState(
                        state
                );

        DigiLockerTokenService.TokenResponse token =
                tokenService.exchangeCode(
                        code,
                        storedState.codeVerifier()
                );

        return new CallbackResult(
                storedState.patientId(),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        );
    }

    public record CallbackResult(
            int patientId,
            String accessToken,
            String tokenType,
            String expiresIn
    ) {
    }
}