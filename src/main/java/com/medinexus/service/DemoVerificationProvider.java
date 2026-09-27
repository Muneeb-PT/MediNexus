package com.medinexus.service;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.VerificationStatus;

public class DemoVerificationProvider
        implements IdentityVerificationProvider {

    @Override
    public IdentityVerification verifyIdentity(
            int patientId,
            String patientName) {

        return new IdentityVerification(
                0,
                patientId,
                "Demo Verification Provider",
                VerificationStatus.PENDING,
                null,
                "Demo Digital Identity",
                "DEMO-" + patientId
        );
    }
}