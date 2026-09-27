package com.medinexus.service.identity;

public class DigiLockerIdentityService {

    private final DigiLockerCallbackService callbackService;
    private final DigiLockerUserService userService;

    public DigiLockerIdentityService() {

        this.callbackService =
                new DigiLockerCallbackService();

        this.userService =
                new DigiLockerUserService();
    }

    public IdentityResult processCallback(
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
         * This performs:
         *
         * 1. OAuth state validation
         * 2. OAuth state consumption
         * 3. PKCE verifier recovery
         * 4. Authorization-code exchange
         */
        DigiLockerCallbackService.CallbackResult callbackResult =
                callbackService.handleCallback(
                        code,
                        state
                );

        /*
         * Use the short-lived access token only for
         * retrieving the authenticated DigiLocker
         * account details.
         */
        DigiLockerUserService.UserDetails userDetails =
                userService.getUserDetails(
                        callbackResult.accessToken()
                );

        return new IdentityResult(
                callbackResult.patientId(),
                userDetails.digiLockerId(),
                userDetails.name(),
                userDetails.dateOfBirth(),
                userDetails.gender()
        );
    }

    public record IdentityResult(
            int patientId,
            String digiLockerId,
            String name,
            String dateOfBirth,
            String gender
    ) {
    }
}