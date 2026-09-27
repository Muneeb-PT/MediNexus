package com.medinexus.config;

public final class IdentityVerificationConfig {

    private IdentityVerificationConfig() {
    }

    /*
     * Keep DEMO while developing locally.
     *
     * Change to DIGILOCKER only after the official
     * DigiLocker/API Setu credentials and redirect URI
     * have been configured.
     */
    public static final String PROVIDER =
            System.getenv().getOrDefault(
                    "MEDINEXUS_IDENTITY_PROVIDER",
                    "DEMO"
            );

    public static boolean isDemo() {
        return "DEMO".equalsIgnoreCase(PROVIDER);
    }

    public static boolean isDigiLocker() {
        return "DIGILOCKER".equalsIgnoreCase(PROVIDER);
    }
}