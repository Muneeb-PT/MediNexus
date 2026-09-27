package com.medinexus.service.identity;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.VerificationStatus;
import com.medinexus.service.IdentityVerificationService;

public class DigiLockerVerificationHandoffService {

    private final DigiLockerIdentityService identityService;
    private final IdentityVerificationService verificationService;

    public DigiLockerVerificationHandoffService() {

        this.identityService =
                new DigiLockerIdentityService();

        this.verificationService =
                new IdentityVerificationService(
                        new com.medinexus.service.DemoVerificationProvider()
                );
    }

    public IdentityVerification process(
            String code,
            String state,
            String patientName) {

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

        /*
         * The OAuth flow has authenticated the DigiLocker
         * account, but MediNexus does not automatically
         * mark the patient as VERIFIED.
         *
         * The existing verification workflow remains the
         * authority for the final verification decision.
         */

        IdentityVerification verification =
                verificationService.submitVerification(
                        identity.patientId(),
                        patientName
                );

        if (verification == null) {
            throw new IllegalStateException(
                    "Unable to create identity verification request."
            );
        }

        return verification;
    }
}