package com.medinexus.service.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PkceUtil {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private PkceUtil() {
    }

    public static String generateCodeVerifier() {

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public static String generateCodeChallenge(
            String codeVerifier) {

        if (codeVerifier == null ||
                codeVerifier.isBlank()) {

            throw new IllegalArgumentException(
                    "Code verifier cannot be empty."
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            codeVerifier.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    e
            );
        }
    }
}