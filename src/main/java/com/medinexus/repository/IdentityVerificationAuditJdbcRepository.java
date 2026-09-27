package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IdentityVerificationAuditJdbcRepository {

    public void addAudit(
            int verificationId,
            int patientId,
            String action,
            String previousStatus,
            String newStatus,
            Integer performedBy,
            String provider,
            String documentReference,
            String reason) {

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action cannot be empty."
            );
        }

        String sql = """
                INSERT INTO identity_verification_audit
                (
                    verification_id,
                    patient_id,
                    action,
                    previous_status,
                    new_status,
                    performed_by,
                    provider,
                    document_reference,
                    reason
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    verificationId
            );

            statement.setInt(
                    2,
                    patientId
            );

            statement.setString(
                    3,
                    action
            );

            if (previousStatus == null) {
                statement.setNull(
                        4,
                        java.sql.Types.VARCHAR
                );
            } else {
                statement.setString(
                        4,
                        previousStatus
                );
            }

            if (newStatus == null) {
                statement.setNull(
                        5,
                        java.sql.Types.VARCHAR
                );
            } else {
                statement.setString(
                        5,
                        newStatus
                );
            }

            if (performedBy == null) {
                statement.setNull(
                        6,
                        java.sql.Types.BIGINT
                );
            } else {
                statement.setInt(
                        6,
                        performedBy
                );
            }

            statement.setString(
                    7,
                    provider
            );

            statement.setString(
                    8,
                    documentReference
            );

            statement.setString(
                    9,
                    reason
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to create identity verification audit record.",
                    e
            );
        }
    }
}