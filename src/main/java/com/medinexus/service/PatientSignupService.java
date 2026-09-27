package com.medinexus.service;

import com.medinexus.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientSignupService {

    public void registerPatient(
            String username,
            String password,
            String fullName,
            int age,
            String gender,
            String phone,
            String bloodGroup) {

        /*
         * Validate everything BEFORE touching the database.
         *
         * This is important for both security and testability.
         */
        validateInput(
                username,
                password,
                fullName,
                age,
                gender,
                phone,
                bloodGroup
        );

        String userSql = """
                INSERT INTO users
                    (username, full_name, role, password_hash)
                VALUES
                    (?, ?, ?, ?)
                """;

        String patientSql = """
                INSERT INTO patients
                    (user_id, name, age, gender, phone, blood_group)
                VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        String passwordHash =
                PasswordService.hashPassword(password);

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int userId;

                /*
                 * Create the user account first.
                 */
                try (PreparedStatement userStatement =
                             connection.prepareStatement(
                                     userSql,
                                     java.sql.Statement.RETURN_GENERATED_KEYS
                             )) {

                    userStatement.setString(
                            1,
                            username.trim()
                    );

                    userStatement.setString(
                            2,
                            fullName.trim()
                    );

                    userStatement.setString(
                            3,
                            Role.PATIENT.name()
                    );

                    userStatement.setString(
                            4,
                            passwordHash
                    );

                    int affectedRows =
                            userStatement.executeUpdate();

                    if (affectedRows != 1) {
                        throw new SQLException(
                                "Unable to create user account."
                        );
                    }

                    /*
                     * Obtain the automatically generated user ID.
                     */
                    try (ResultSet keys =
                                 userStatement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Unable to obtain generated user ID."
                            );
                        }

                        userId = keys.getInt(1);
                    }
                }

                /*
                 * Create the patient profile using the
                 * generated user ID.
                 */
                try (PreparedStatement patientStatement =
                             connection.prepareStatement(
                                     patientSql
                             )) {

                    patientStatement.setInt(
                            1,
                            userId
                    );

                    patientStatement.setString(
                            2,
                            fullName.trim()
                    );

                    patientStatement.setInt(
                            3,
                            age
                    );

                    patientStatement.setString(
                            4,
                            gender.trim()
                    );

                    patientStatement.setString(
                            5,
                            phone.trim()
                    );

                    patientStatement.setString(
                            6,
                            bloodGroup.trim()
                    );

                    int affectedRows =
                            patientStatement.executeUpdate();

                    if (affectedRows != 1) {
                        throw new SQLException(
                                "Unable to create patient profile."
                        );
                    }
                }

                /*
                 * Both user and patient records must succeed.
                 */
                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;

            } finally {

                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection will be closed automatically.
                }
            }

        } catch (SQLException e) {

            String message =
                    e.getMessage() == null
                            ? ""
                            : e.getMessage().toLowerCase();

            if (message.contains("duplicate")
                    || message.contains("unique")) {

                throw new IllegalArgumentException(
                        "Username already exists."
                );
            }

            throw new RuntimeException(
                    "Unable to create patient account.",
                    e
            );
        }
    }

    private void validateInput(
            String username,
            String password,
            String fullName,
            int age,
            String gender,
            String phone,
            String bloodGroup) {

        /*
         * Username validation.
         */
        if (username == null
                || username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        if (username.trim().length() < 4) {

            throw new IllegalArgumentException(
                    "Username must contain at least 4 characters."
            );
        }

        /*
         * Password validation.
         *
         * This MUST happen before any database operation.
         */
        PasswordPolicy.validate(password);

        /*
         * Full name validation.
         */
        if (fullName == null
                || fullName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        /*
         * Age validation.
         */
        if (age < 1 || age > 120) {

            throw new IllegalArgumentException(
                    "Age must be between 1 and 120."
            );
        }

        /*
         * Gender validation.
         */
        if (gender == null
                || gender.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Gender is required."
            );
        }

        /*
         * Phone validation.
         */
        if (phone == null
                || phone.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        /*
         * Blood group validation.
         */
        if (bloodGroup == null
                || bloodGroup.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Blood group is required."
            );
        }
    }
}