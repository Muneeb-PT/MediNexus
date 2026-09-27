package com.medinexus.service;

import com.medinexus.model.IdentityVerification;

public interface IdentityVerificationProvider {

    IdentityVerification verifyIdentity(
            int patientId,
            String patientName
    );
}