package com.medinexus.service;

import com.medinexus.config.IdentityVerificationConfig;
import com.medinexus.service.identity.DigiLockerVerificationProvider;

public final class IdentityVerificationProviderFactory {

    private IdentityVerificationProviderFactory() {
    }

    public static IdentityVerificationProvider create() {

        if (IdentityVerificationConfig.isDigiLocker()) {
            return new DigiLockerVerificationProvider();
        }

        return new DemoVerificationProvider();
    }
}