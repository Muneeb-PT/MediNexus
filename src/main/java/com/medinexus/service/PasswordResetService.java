package com.medinexus.service;

import com.medinexus.model.User;
import com.medinexus.repository.PasswordResetTokenRepository;
import com.medinexus.repository.UserJdbcRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;

public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;

    private static final long TOKEN_VALIDITY_MINUTES = 15;

    private final UserJdbcRepository userRepository;

    private final PasswordResetTokenRepository tokenRepository;

    private final SecureRandom secureRandom;

    public PasswordResetService() {

        this.userRepository =
                new UserJdbcRepository();

        this.tokenRepository =
                new PasswordResetTokenRepository();

        this.secureRandom =
                new SecureRandom();
    }

    public String requestPasswordReset(
            String username) {

        if (username == null
                || username.trim().isEmpty()) {

            return null;
        }

        User user =
                userRepository.findByUsername(
                        username.trim()
                );

        /*
         * Do not reveal whether an account exists.
         */
        if (user == null) {
            return null;
        }

        /*
         * Invalidate previous active tokens.
         */
        tokenRepository.invalidateActiveTokens(
                user.getUserId()
        );

        String rawToken =
                generateSecureToken();

        String tokenHash =
                hashToken(rawToken);

        Timestamp expiresAt =
                Timestamp.from(
                        Instant.now()
                                .plusSeconds(
                                        TOKEN_VALIDITY_MINUTES
                                                * 60
                                )
                );

        tokenRepository.saveToken(
                user.getUserId(),
                tokenHash,
                expiresAt
        );

        /*
         * Development/demo only.
         *
         * In production this token will be delivered
         * through an approved email/SMS/notification provider.
         */
        return rawToken;
    }

    public void resetPassword(
            String rawToken,
            String newPassword) {

        if (rawToken == null
                || rawToken.isBlank()) {

            throw new IllegalArgumentException(
                    "Reset token is required."
            );
        }

        /*
         * Apply the same strong password policy
         * used during account registration.
         */
        PasswordPolicy.validate(
                newPassword
        );

        String tokenHash =
                hashToken(rawToken);

        /*
         * First locate the account associated with
         * the valid token.
         */
        Integer userId =
                tokenRepository
                        .findValidUserIdByTokenHash(
                                tokenHash
                        );

        if (userId == null) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset token."
            );
        }

        String passwordHash =
                PasswordService.hashPassword(
                        newPassword
                );

        /*
         * Password update and token consumption now
         * happen inside ONE database transaction.
         */
        boolean successful =
                tokenRepository.resetPasswordAtomically(
                        userId,
                        tokenHash,
                        passwordHash
                );

        if (!successful) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset token."
            );
        }
    }

    private String generateSecureToken() {

        byte[] bytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return toHex(bytes);
    }

    private String hashToken(
            String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return toHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable.",
                    e
            );
        }
    }

    private String toHex(
            byte[] bytes) {

        StringBuilder result =
                new StringBuilder(
                        bytes.length * 2
                );

        for (byte value : bytes) {

            result.append(
                    String.format(
                            "%02x",
                            value & 0xff
                    )
            );
        }

        return result.toString();
    }
}