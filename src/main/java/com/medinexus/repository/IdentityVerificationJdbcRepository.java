package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.IdentityVerification;
import com.medinexus.model.VerificationStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IdentityVerificationJdbcRepository {

    // =========================================================
    // FIND LATEST VERIFICATION
    // =========================================================

    public IdentityVerification findLatestByPatientId(
            int patientId) {

        String sql = """
                SELECT verification_id,
                       patient_id,
                       provider,
                       status,
                       verified_at,
                       document_type,
                       document_reference
                FROM identity_verifications
                WHERE patient_id = ?
                ORDER BY verification_id DESC
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load identity verification.",
                    e
            );
        }

        return null;
    }


    // =========================================================
    // FIND BY STATUS
    // =========================================================

    public List<IdentityVerification> findByStatus(
            VerificationStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Verification status cannot be null."
            );
        }

        String sql = """
                SELECT verification_id,
                       patient_id,
                       provider,
                       status,
                       verified_at,
                       document_type,
                       document_reference
                FROM identity_verifications
                WHERE status = ?
                ORDER BY verification_id DESC
                """;

        List<IdentityVerification> verifications =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name()
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    verifications.add(
                            mapRow(rs)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load identity verification requests.",
                    e
            );
        }

        return verifications;
    }


    // =========================================================
    // ADD VERIFICATION
    // =========================================================

    public IdentityVerification add(
            IdentityVerification verification) {

        if (verification == null) {
            throw new IllegalArgumentException(
                    "Identity verification cannot be null."
            );
        }

        String sql = """
                INSERT INTO identity_verifications
                (
                    patient_id,
                    provider,
                    status,
                    verified_at,
                    document_type,
                    document_reference
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(
                    1,
                    verification.getPatientId()
            );

            statement.setString(
                    2,
                    verification.getProvider()
            );

            statement.setString(
                    3,
                    verification.getStatus().name()
            );

            if (verification.getVerifiedAt() == null) {

                statement.setNull(
                        4,
                        Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                        4,
                        Timestamp.valueOf(
                                verification.getVerifiedAt()
                        )
                );
            }

            statement.setString(
                    5,
                    verification.getDocumentType()
            );

            statement.setString(
                    6,
                    verification.getDocumentReference()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    verification.setVerificationId(
                            keys.getInt(1)
                    );
                }
            }

            return verification;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to create identity verification.",
                    e
            );
        }
    }


    // =========================================================
    // LEGACY UPDATE METHOD
    // =========================================================
    /*
     * Kept for compatibility with existing code/tests.
     *
     * New approval/rejection workflows should use
     * updateStatusWithAudit().
     */

    public void updateStatus(
            int verificationId,
            VerificationStatus status,
            LocalDateTime verifiedAt) {

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Verification status cannot be null."
            );
        }

        String sql = """
                UPDATE identity_verifications
                SET status = ?,
                    verified_at = ?
                WHERE verification_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name()
            );

            if (verifiedAt == null) {

                statement.setNull(
                        2,
                        Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(verifiedAt)
                );
            }

            statement.setInt(
                    3,
                    verificationId
            );

            int updated =
                    statement.executeUpdate();

            if (updated == 0) {

                throw new IllegalArgumentException(
                        "Identity verification request not found."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update identity verification status.",
                    e
            );
        }
    }
    public IdentityVerification addWithAudit(
            IdentityVerification verification,
            Integer performedBy,
            String action,
            String reason) {

        if (verification == null) {
            throw new IllegalArgumentException(
                    "Identity verification cannot be null."
            );
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action cannot be empty."
            );
        }

        String insertVerificationSql = """
            INSERT INTO identity_verifications
            (
                patient_id,
                provider,
                status,
                verified_at,
                document_type,
                document_reference
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String insertAuditSql = """
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
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int verificationId;

                /*
                 * 1. Create verification record.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertVerificationSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setInt(
                            1,
                            verification.getPatientId()
                    );

                    statement.setString(
                            2,
                            verification.getProvider()
                    );

                    statement.setString(
                            3,
                            verification.getStatus().name()
                    );

                    if (verification.getVerifiedAt() == null) {

                        statement.setNull(
                                4,
                                Types.TIMESTAMP
                        );

                    } else {

                        statement.setTimestamp(
                                4,
                                Timestamp.valueOf(
                                        verification.getVerifiedAt()
                                )
                        );
                    }

                    statement.setString(
                            5,
                            verification.getDocumentType()
                    );

                    statement.setString(
                            6,
                            verification.getDocumentReference()
                    );

                    statement.executeUpdate();

                    try (ResultSet keys =
                                 statement.getGeneratedKeys()) {

                        if (!keys.next()) {

                            throw new SQLException(
                                    "Failed to obtain generated verification ID."
                            );
                        }

                        verificationId =
                                keys.getInt(1);

                        verification.setVerificationId(
                                verificationId
                        );
                    }
                }

                /*
                 * 2. Create audit record in SAME transaction.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertAuditSql
                             )) {

                    statement.setInt(
                            1,
                            verificationId
                    );

                    statement.setInt(
                            2,
                            verification.getPatientId()
                    );

                    statement.setString(
                            3,
                            action
                    );

                    /*
                     * New verification has no previous status.
                     */
                    statement.setNull(
                            4,
                            Types.VARCHAR
                    );

                    statement.setString(
                            5,
                            verification.getStatus().name()
                    );

                    if (performedBy == null) {

                        statement.setNull(
                                6,
                                Types.BIGINT
                        );

                    } else {

                        statement.setInt(
                                6,
                                performedBy
                        );
                    }

                    statement.setString(
                            7,
                            verification.getProvider()
                    );

                    statement.setString(
                            8,
                            verification.getDocumentReference()
                    );

                    statement.setString(
                            9,
                            reason
                    );

                    statement.executeUpdate();
                }

                /*
                 * 3. Commit both operations together.
                 */
                connection.commit();

                return verification;

            } catch (Exception e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to create identity verification "
                            + "and audit record.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create identity verification "
                            + "and audit record.",
                    e
            );
        }
    }

    // =========================================================
    // ATOMIC STATUS UPDATE + AUDIT
    // =========================================================

    public void updateStatusWithAudit(
            int verificationId,
            VerificationStatus expectedCurrentStatus,
            VerificationStatus newStatus,
            LocalDateTime verifiedAt,
            int performedBy,
            String action,
            String reason) {

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        if (expectedCurrentStatus == null) {
            throw new IllegalArgumentException(
                    "Expected current status cannot be null."
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New verification status cannot be null."
            );
        }

        if (performedBy <= 0) {
            throw new IllegalArgumentException(
                    "Invalid performing user ID."
            );
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action cannot be empty."
            );
        }

        String selectSql = """
                SELECT verification_id,
                       patient_id,
                       provider,
                       status,
                       verified_at,
                       document_type,
                       document_reference
                FROM identity_verifications
                WHERE verification_id = ?
                FOR UPDATE
                """;

        String updateSql = """
                UPDATE identity_verifications
                SET status = ?,
                    verified_at = ?
                WHERE verification_id = ?
                """;

        String auditSql = """
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
                     DatabaseConnection.getConnection()) {

            /*
             * Everything from this point must succeed together.
             */
            connection.setAutoCommit(false);

            try {

                IdentityVerification current;

                /*
                 * FOR UPDATE locks this verification row
                 * until COMMIT or ROLLBACK.
                 */
                try (PreparedStatement selectStatement =
                             connection.prepareStatement(selectSql)) {

                    selectStatement.setInt(
                            1,
                            verificationId
                    );

                    try (ResultSet rs =
                                 selectStatement.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                    "Identity verification request not found."
                            );
                        }

                        current = mapRow(rs);
                    }
                }

                /*
                 * Verify that the state has not changed.
                 */
                if (current.getStatus()
                        != expectedCurrentStatus) {

                    throw new IllegalStateException(
                            "Identity verification state changed. "
                                    + "Expected "
                                    + expectedCurrentStatus
                                    + " but found "
                                    + current.getStatus()
                                    + "."
                    );
                }

                /*
                 * Prevent meaningless transitions.
                 */
                if (current.getStatus() == newStatus) {

                    throw new IllegalStateException(
                            "Identity verification is already in "
                                    + newStatus.name()
                                    + " state."
                    );
                }

                /*
                 * Update identity verification.
                 */
                try (PreparedStatement updateStatement =
                             connection.prepareStatement(updateSql)) {

                    updateStatement.setString(
                            1,
                            newStatus.name()
                    );

                    if (verifiedAt == null) {

                        updateStatement.setNull(
                                2,
                                Types.TIMESTAMP
                        );

                    } else {

                        updateStatement.setTimestamp(
                                2,
                                Timestamp.valueOf(
                                        verifiedAt
                                )
                        );
                    }

                    updateStatement.setInt(
                            3,
                            verificationId
                    );

                    int updated =
                            updateStatement.executeUpdate();

                    if (updated != 1) {

                        throw new SQLException(
                                "Unexpected identity verification "
                                        + "update count: "
                                        + updated
                        );
                    }
                }

                /*
                 * Insert audit record using THE SAME
                 * database transaction.
                 */
                try (PreparedStatement auditStatement =
                             connection.prepareStatement(auditSql)) {

                    auditStatement.setInt(
                            1,
                            verificationId
                    );

                    auditStatement.setInt(
                            2,
                            current.getPatientId()
                    );

                    auditStatement.setString(
                            3,
                            action
                    );

                    auditStatement.setString(
                            4,
                            current.getStatus().name()
                    );

                    auditStatement.setString(
                            5,
                            newStatus.name()
                    );

                    auditStatement.setInt(
                            6,
                            performedBy
                    );

                    auditStatement.setString(
                            7,
                            current.getProvider()
                    );

                    auditStatement.setString(
                            8,
                            current.getDocumentReference()
                    );

                    auditStatement.setString(
                            9,
                            reason
                    );

                    auditStatement.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {

                connection.rollback();

                throw e;
            } finally {

                connection.setAutoCommit(true);
            }

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            throw e;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update identity verification "
                            + "and create audit record.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to update identity verification "
                            + "and create audit record.",
                    e
            );
        }
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    public IdentityVerification findById(
            int verificationId) {

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        String sql = """
                SELECT verification_id,
                       patient_id,
                       provider,
                       status,
                       verified_at,
                       document_type,
                       document_reference
                FROM identity_verifications
                WHERE verification_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    verificationId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to retrieve identity verification.",
                    e
            );
        }
    }


    // =========================================================
    // MAP DATABASE ROW
    // =========================================================

    private IdentityVerification mapRow(
            ResultSet rs) throws SQLException {

        Timestamp verifiedTimestamp =
                rs.getTimestamp("verified_at");

        return new IdentityVerification(
                rs.getInt("verification_id"),
                rs.getInt("patient_id"),
                rs.getString("provider"),
                VerificationStatus.valueOf(
                        rs.getString("status")
                ),
                verifiedTimestamp == null
                        ? null
                        : verifiedTimestamp.toLocalDateTime(),
                rs.getString("document_type"),
                rs.getString("document_reference")
        );
    }
}