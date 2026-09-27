package com.medinexus.service;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordService {


    /*
     * BCrypt work factor.
     *
     * 12 provides a deliberately expensive password hash
     * while remaining practical for a desktop application.
     */
    private static final int BCRYPT_ROUNDS = 12;

    private PasswordService() {
        // Utility class.
    }

    /**
     * Creates a secure BCrypt hash for a password.
     */
    public static String hashPassword(String password) {

        validatePassword(password);

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(BCRYPT_ROUNDS)
        );
    }

    /**
     * Verifies a plaintext password against a BCrypt hash.
     */
    public static boolean verifyPassword(
            String password,
            String passwordHash) {

        if (password == null
                || passwordHash == null
                || password.isEmpty()
                || passwordHash.isEmpty()) {

            return false;
        }

        try {

            return BCrypt.checkpw(
                    password,
                    passwordHash
            );

        } catch (IllegalArgumentException e) {

            /*
             * Invalid/corrupted BCrypt hashes must never
             * crash the login process.
             */
            return false;
        }
    }

    private static void validatePassword(String password) {

        if (password == null
                || password.isEmpty()) {

            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        /*
         * BCrypt has a 72-byte input limit.
         * We use UTF-8 byte length rather than Java
         * character count.
         */
        int byteLength =
                password.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                ).length;

        if (byteLength > 72) {

            throw new IllegalArgumentException(
                    "Password is too long for BCrypt."
            );
        }
    }


}
