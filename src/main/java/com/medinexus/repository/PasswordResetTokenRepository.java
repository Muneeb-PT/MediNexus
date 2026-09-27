package com.medinexus.repository;

import com.medinexus.service.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PasswordResetTokenRepository {

    public void invalidateActiveTokens(int userId) {

        String sql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                  AND used_at IS NULL
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to invalidate password reset tokens.",
                    e
            );
        }
    }

    public void saveToken(
            int userId,
            String tokenHash,
            Timestamp expiresAt) {

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Token hash cannot be empty."
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Token expiry cannot be null."
            );
        }

        String sql = """
                INSERT INTO password_reset_tokens
                    (user_id, token_hash, expires_at)
                VALUES
                    (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, tokenHash);
            statement.setTimestamp(3, expiresAt);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to save password reset token.",
                    e
            );
        }
    }

    public Integer findValidUserIdByTokenHash(
            String tokenHash) {

        if (tokenHash == null || tokenHash.isBlank()) {
            return null;
        }

        String sql = """
                SELECT user_id
                FROM password_reset_tokens
                WHERE token_hash = ?
                  AND used_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tokenHash);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to validate password reset token.",
                    e
            );
        }

        return null;
    }

    public boolean markTokenUsed(
            String tokenHash) {

        if (tokenHash == null || tokenHash.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE token_hash = ?
                  AND used_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tokenHash);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to consume password reset token.",
                    e
            );
        }
    }

    /**
     * Atomically updates the user's password and consumes
     * the password reset token.
     *
     * The token row is locked before the password is changed.
     * If either operation fails, the complete transaction rolls back.
     */
    public boolean resetPasswordAtomically(
            int userId,
            String tokenHash,
            String passwordHash) {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Token hash cannot be empty."
            );
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be empty."
            );
        }

        String lockTokenSql = """
                SELECT user_id
                FROM password_reset_tokens
                WHERE token_hash = ?
                  AND used_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                FOR UPDATE
                """;

        String updatePasswordSql = """
                UPDATE users
                SET password_hash = ?
                WHERE user_id = ?
                """;

        String consumeTokenSql = """
                UPDATE password_reset_tokens
                SET used_at = CURRENT_TIMESTAMP
                WHERE token_hash = ?
                  AND user_id = ?
                  AND used_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            boolean originalAutoCommit =
                    connection.getAutoCommit();

            try {

                connection.setAutoCommit(false);

                /*
                 * Step 1:
                 * Lock and validate the reset token.
                 */
                int tokenUserId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     lockTokenSql)) {

                    statement.setString(
                            1,
                            tokenHash
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            connection.rollback();

                            return false;
                        }

                        tokenUserId =
                                resultSet.getInt(
                                        "user_id"
                                );
                    }
                }

                /*
                 * Prevent a token belonging to another
                 * account from being used.
                 */
                if (tokenUserId != userId) {

                    connection.rollback();

                    return false;
                }

                /*
                 * Step 2:
                 * Update the password.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updatePasswordSql)) {

                    statement.setString(
                            1,
                            passwordHash
                    );

                    statement.setInt(
                            2,
                            userId
                    );

                    int affectedRows =
                            statement.executeUpdate();

                    if (affectedRows != 1) {

                        connection.rollback();

                        return false;
                    }
                }

                /*
                 * Step 3:
                 * Consume the reset token.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     consumeTokenSql)) {

                    statement.setString(
                            1,
                            tokenHash
                    );

                    statement.setInt(
                            2,
                            userId
                    );

                    int affectedRows =
                            statement.executeUpdate();

                    if (affectedRows != 1) {

                        connection.rollback();

                        return false;
                    }
                }

                /*
                 * Step 4:
                 * Everything succeeded.
                 */
                connection.commit();

                return true;

            } catch (SQLException e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw e;

            } finally {

                try {
                    connection.setAutoCommit(
                            originalAutoCommit
                    );
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to reset password atomically.",
                    e
            );
        }
    }
}