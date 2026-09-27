package com.medinexus.config;

public final class DigiLockerConfig {

    private DigiLockerConfig() {
    }

    /*
     * DigiLocker / Entity Locker OAuth authorization endpoint.
     */
    public static final String AUTHORIZATION_URL =
            System.getenv().getOrDefault(
                    "DIGILOCKER_AUTHORIZATION_URL",
                    "https://entity.digilocker.gov.in/public/oauth2/1/authorize"
            );

    /*
     * DigiLocker / Entity Locker OAuth token endpoint.
     */
    public static final String TOKEN_URL =
            System.getenv().getOrDefault(
                    "DIGILOCKER_TOKEN_URL",
                    "https://entity.digilocker.gov.in/public/oauth2/1/token"
            );

    /*
     * DigiLocker user-information endpoint.
     */
    public static final String USER_URL =
            System.getenv().getOrDefault(
                    "DIGILOCKER_USER_URL",
                    "https://entity.digilocker.gov.in/public/oauth2/1/user"
            );

    /*
     * OAuth client ID.
     *
     * Keep production credentials outside source code.
     */
    public static final String CLIENT_ID =
            System.getenv().getOrDefault(
                    "DIGILOCKER_CLIENT_ID",
                    ""
            );

    /*
     * OAuth client secret.
     *
     * NEVER hard-code the real production secret here.
     */
    public static final String CLIENT_SECRET =
            System.getenv().getOrDefault(
                    "DIGILOCKER_CLIENT_SECRET",
                    ""
            );

    /*
     * OAuth redirect URI registered with DigiLocker.
     */
    public static final String REDIRECT_URI =
            System.getenv().getOrDefault(
                    "DIGILOCKER_REDIRECT_URI",
                    ""
            );

    public static boolean isConfigured() {

        return !CLIENT_ID.isBlank()
                && !CLIENT_SECRET.isBlank()
                && !REDIRECT_URI.isBlank();
    }
}