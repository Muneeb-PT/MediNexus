package com.medinexus.service.identity;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.VerificationStatus;
import com.medinexus.service.IdentityVerificationProvider;

public class DigiLockerVerificationProvider
        implements IdentityVerificationProvider {

    private final DigiLockerIdentityService identityService;

    public DigiLockerVerificationProvider() {
        this.identityService =
                new DigiLockerIdentityService();
    }

    /**
     * This method is intentionally not used to start OAuth.
     *
     * The DigiLocker flow requires an authorization code and
     * OAuth state, so the actual verification request is handled
     * by verifyAuthorizationCallback().
     */
    @Override
    public IdentityVerification verifyIdentity(
            int patientId,
            String patientName) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (patientName == null
                || patientName.isBlank()) {

            throw new IllegalArgumentException(
                    "Patient name cannot be empty."
            );
        }

        return new IdentityVerification(
                0,
                patientId,
                "DigiLocker",
                VerificationStatus.PENDING,
                null,
                "DigiLocker Identity",
                "PENDING-" + patientId
        );
    }

    /**
     * Completes the DigiLocker OAuth callback.
     *
     * OAuth authentication and identity verification are kept
     * separate. A successful callback creates a PENDING
     * MediNexus verification record rather than automatically
     * marking the patient VERIFIED.
     */
    public IdentityVerification verifyAuthorizationCallback(
            String code,
            String state,
            String patientName) {

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

        if (patientName == null
                || patientName.isBlank()) {

            throw new IllegalArgumentException(
                    "Patient name cannot be empty."
            );
        }

        DigiLockerIdentityService.IdentityResult identity =
                identityService.processCallback(
                        code,
                        state
                );

        if (identity == null) {
            throw new IllegalStateException(
                    "DigiLocker returned no identity result."
            );
        }

        if (identity.patientId() <= 0) {
            throw new IllegalStateException(
                    "DigiLocker returned an invalid patient reference."
            );
        }

        if (identity.digiLockerId() == null
                || identity.digiLockerId().isBlank()) {

            throw new IllegalStateException(
                    "DigiLocker did not return a valid identity reference."
            );
        }

        return new IdentityVerification(
                0,
                identity.patientId(),
                "DigiLocker",
                VerificationStatus.PENDING,
                null,
                "DigiLocker Identity",
                identity.digiLockerId()
        );
    }
}