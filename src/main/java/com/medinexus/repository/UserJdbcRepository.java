package com.medinexus.repository;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserJdbcRepository {


    /**
     * Finds a user by username.
     */
    public User findByUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = """
            SELECT
                user_id,
                username,
                full_name,
                role,
                password_hash
            FROM users
            WHERE username = ?
            LIMIT 1
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    username.trim()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to find user by username.",
                    e
            );
        }

        return null;
    }

    /**
     * Finds a user by ID.
     */
    public User findById(int userId) {

        String sql = """
            SELECT
                user_id,
                username,
                full_name,
                role,
                password_hash
            FROM users
            WHERE user_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to find user by ID.",
                    e
            );
        }

        return null;
    }

    /**
     * Stores a BCrypt password hash for an existing user.
     */
    public void updatePasswordHash(
            int userId,
            String passwordHash) {

        if (passwordHash == null
                || passwordHash.isBlank()) {

            throw new IllegalArgumentException(
                    "Password hash cannot be empty."
            );
        }

        String sql = """
            UPDATE users
            SET password_hash = ?
            WHERE user_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setInt(2, userId);

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {

                throw new IllegalArgumentException(
                        "User not found: " + userId
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to update password hash.",
                    e
            );
        }
    }

    private User mapUser(ResultSet resultSet)
            throws SQLException {

        Role role =
                Role.valueOf(
                        resultSet.getString("role")
                                .toUpperCase()
                );

        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("username"),
                resultSet.getString("full_name"),
                role,
                resultSet.getString("password_hash")
        );
    }


}
