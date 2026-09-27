package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Patient;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientJdbcRepository {

    // -----------------------------------------
    // Find all patients
    // -----------------------------------------

    public List<Patient> findAll() {

        List<Patient> patients = new ArrayList<>();

        String sql = """
                SELECT
                    patient_id,
                    user_id,
                    name,
                    age,
                    gender,
                    phone,
                    blood_group,
                    identity_verified,
                    verification_method,
                    verification_reference,
                    verified_at
                FROM patients
                ORDER BY patient_id
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load patients from MySQL",
                    e
            );
        }

        return patients;
    }

    // -----------------------------------------
    // Find patients assigned to a doctor
    // -----------------------------------------

    public List<Patient> findByDoctorId(int doctorId) {

        if (doctorId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        List<Patient> patients = new ArrayList<>();

        String sql = """
                SELECT
                    p.patient_id,
                    p.user_id,
                    p.name,
                    p.age,
                    p.gender,
                    p.phone,
                    p.blood_group,
                    p.identity_verified,
                    p.verification_method,
                    p.verification_reference,
                    p.verified_at
                FROM patients p
                WHERE EXISTS (
                    SELECT 1
                    FROM appointments a
                    WHERE a.patient_id = p.patient_id
                      AND a.doctor_id = ?
                )
                ORDER BY p.patient_id
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    patients.add(
                            mapPatient(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load patients for doctor "
                            + doctorId,
                    e
            );
        }

        return patients;
    }

    // -----------------------------------------
    // Check whether doctor has patient access
    // -----------------------------------------

    public boolean doctorHasPatientAccess(
            int doctorId,
            int patientId) {

        if (doctorId <= 0 || patientId <= 0) {
            return false;
        }

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM appointments
                    WHERE doctor_id = ?
                      AND patient_id = ?
                )
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, doctorId);
            statement.setInt(2, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to verify doctor-patient access.",
                    e
            );
        }

        return false;
    }

    // -----------------------------------------
    // Find patient by ID
    // -----------------------------------------

    public Patient findById(int patientId) {

        String sql = """
                SELECT
                    patient_id,
                    user_id,
                    name,
                    age,
                    gender,
                    phone,
                    blood_group,
                    identity_verified,
                    verification_method,
                    verification_reference,
                    verified_at
                FROM patients
                WHERE patient_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPatient(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find patient with ID "
                            + patientId,
                    e
            );
        }

        return null;
    }

    // -----------------------------------------
    // Find patient by user ID
    // -----------------------------------------

    public Patient findByUserId(int userId) {

        String sql = """
                SELECT
                    patient_id,
                    user_id,
                    name,
                    age,
                    gender,
                    phone,
                    blood_group,
                    identity_verified,
                    verification_method,
                    verification_reference,
                    verified_at
                FROM patients
                WHERE user_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPatient(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find patient for user ID "
                            + userId,
                    e
            );
        }

        return null;
    }

    // -----------------------------------------
    // Add patient
    // -----------------------------------------

    /**
     * Adds a patient and automatically creates
     * the corresponding PATIENT user account.
     */
    public void add(Patient patient) {

        String checkPatientSql = """
                SELECT COUNT(*)
                FROM patients
                WHERE patient_id = ?
                """;

        String nextUserIdSql = """
                SELECT COALESCE(MAX(user_id), 0) + 1
                FROM users
                """;

        String insertUserSql = """
                INSERT INTO users
                (
                    user_id,
                    username,
                    full_name,
                    role
                )
                VALUES (?, ?, ?, ?)
                """;

        String insertPatientSql = """
                INSERT INTO patients
                (
                    patient_id,
                    user_id,
                    name,
                    age,
                    gender,
                    phone,
                    blood_group
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection connection = null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            // 1. Check patient ID

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 checkPatientSql)) {

                statement.setInt(
                        1,
                        patient.getPatientId()
                );

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (resultSet.next()
                            && resultSet.getInt(1) > 0) {

                        throw new RuntimeException(
                                "Patient ID "
                                        + patient.getPatientId()
                                        + " already exists."
                        );
                    }
                }
            }

            // 2. Generate user ID

            int userId;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    nextUserIdSql);
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    throw new RuntimeException(
                            "Unable to generate a user ID."
                    );
                }

                userId =
                        resultSet.getInt(1);
            }

            // 3. Create PATIENT user

            String username =
                    "patient"
                            + patient.getPatientId();

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 insertUserSql)) {

                statement.setInt(
                        1,
                        userId
                );

                statement.setString(
                        2,
                        username
                );

                statement.setString(
                        3,
                        patient.getName()
                );

                statement.setString(
                        4,
                        "PATIENT"
                );

                statement.executeUpdate();
            }

            // 4. Create patient

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 insertPatientSql)) {

                statement.setInt(
                        1,
                        patient.getPatientId()
                );

                statement.setInt(
                        2,
                        userId
                );

                statement.setString(
                        3,
                        patient.getName()
                );

                statement.setInt(
                        4,
                        patient.getAge()
                );

                statement.setString(
                        5,
                        patient.getGender()
                );

                statement.setString(
                        6,
                        patient.getPhone()
                );

                statement.setString(
                        7,
                        patient.getBloodGroup()
                );

                statement.executeUpdate();
            }

            // 5. Commit

            connection.commit();

            patient.setUserId(userId);

        } catch (SQLException e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            throw new RuntimeException(
                    "Failed to add patient: "
                            + e.getMessage(),
                    e
            );

        } catch (RuntimeException e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            throw e;

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // -----------------------------------------
    // Update patient
    // -----------------------------------------

    public void update(Patient patient) {

        String sql = """
                UPDATE patients
                SET
                    name = ?,
                    age = ?,
                    gender = ?,
                    phone = ?,
                    blood_group = ?
                WHERE patient_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    patient.getName()
            );

            statement.setInt(
                    2,
                    patient.getAge()
            );

            statement.setString(
                    3,
                    patient.getGender()
            );

            statement.setString(
                    4,
                    patient.getPhone()
            );

            statement.setString(
                    5,
                    patient.getBloodGroup()
            );

            statement.setInt(
                    6,
                    patient.getPatientId()
            );

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected == 0) {

                throw new RuntimeException(
                        "Patient with ID "
                                + patient.getPatientId()
                                + " was not found."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update patient in MySQL",
                    e
            );
        }
    }

    // -----------------------------------------
    // Delete patient
    // -----------------------------------------

    public void delete(int patientId) {

        String sql = """
                DELETE FROM patients
                WHERE patient_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, patientId);

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected == 0) {

                throw new RuntimeException(
                        "Patient with ID "
                                + patientId
                                + " was not found."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete patient from MySQL",
                    e
            );
        }
    }

    // -----------------------------------------
    // Update identity verification
    // -----------------------------------------

    public void updateVerification(
            int patientId,
            boolean verified,
            String method,
            String reference) {

        String sql = """
                UPDATE patients
                SET
                    identity_verified = ?,
                    verification_method = ?,
                    verification_reference = ?,
                    verified_at = ?
                WHERE patient_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(
                    1,
                    verified
            );

            statement.setString(
                    2,
                    method
            );

            statement.setString(
                    3,
                    reference
            );

            if (verified) {

                statement.setTimestamp(
                        4,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );

            } else {

                statement.setTimestamp(
                        4,
                        null
                );
            }

            statement.setInt(
                    5,
                    patientId
            );

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 0) {

                throw new IllegalArgumentException(
                        "Patient not found: "
                                + patientId
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update patient identity verification.",
                    e
            );
        }
    }

    // -----------------------------------------
    // Map ResultSet → Patient
    // -----------------------------------------

    private Patient mapPatient(
            ResultSet resultSet)
            throws SQLException {

        Patient patient =
                new Patient(
                        resultSet.getInt(
                                "patient_id"
                        ),
                        resultSet.getInt(
                                "user_id"
                        ),
                        resultSet.getString(
                                "name"
                        ),
                        resultSet.getInt(
                                "age"
                        ),
                        resultSet.getString(
                                "gender"
                        ),
                        resultSet.getString(
                                "phone"
                        ),
                        resultSet.getString(
                                "blood_group"
                        )
                );

        patient.setIdentityVerified(
                resultSet.getBoolean(
                        "identity_verified"
                )
        );

        patient.setVerificationMethod(
                resultSet.getString(
                        "verification_method"
                )
        );

        patient.setVerificationReference(
                resultSet.getString(
                        "verification_reference"
                )
        );

        Timestamp verifiedAt =
                resultSet.getTimestamp(
                        "verified_at"
                );

        if (verifiedAt != null) {

            patient.setVerifiedAt(
                    verifiedAt.toLocalDateTime()
            );
        }

        return patient;
    }
}