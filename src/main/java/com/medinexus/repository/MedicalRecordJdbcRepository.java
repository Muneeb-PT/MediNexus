package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.MedicalRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordJdbcRepository {

    // ---------------------------------------------------------
    // FIND ALL RECORDS
    // ---------------------------------------------------------

    public List<MedicalRecord> findAll() {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = """
                SELECT record_id,
                       patient_id,
                       doctor_id,
                       appointment_id,
                       diagnosis,
                       symptoms,
                       treatment,
                       notes,
                       created_at
                FROM medical_records
                ORDER BY created_at DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                records.add(mapMedicalRecord(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medical records",
                    e
            );
        }

        return records;
    }

    // ---------------------------------------------------------
    // FIND RECORD BY ID
    // ---------------------------------------------------------

    public MedicalRecord findById(int recordId) {

        if (recordId <= 0) {
            return null;
        }

        String sql = """
                SELECT record_id,
                       patient_id,
                       doctor_id,
                       appointment_id,
                       diagnosis,
                       symptoms,
                       treatment,
                       notes,
                       created_at
                FROM medical_records
                WHERE record_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, recordId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapMedicalRecord(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find medical record " + recordId,
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // FIND RECORDS BY PATIENT
    // ---------------------------------------------------------

    public List<MedicalRecord> findByPatientId(
            int patientId) {

        if (patientId <= 0) {
            return new ArrayList<>();
        }

        List<MedicalRecord> records =
                new ArrayList<>();

        String sql = """
                SELECT record_id,
                       patient_id,
                       doctor_id,
                       appointment_id,
                       diagnosis,
                       symptoms,
                       treatment,
                       notes,
                       created_at
                FROM medical_records
                WHERE patient_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    records.add(
                            mapMedicalRecord(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medical records "
                            + "for patient " + patientId,
                    e
            );
        }

        return records;
    }

    // ---------------------------------------------------------
    // FIND RECORDS BY DOCTOR
    // ---------------------------------------------------------

    public List<MedicalRecord> findByDoctorId(
            int doctorId) {

        if (doctorId <= 0) {
            return new ArrayList<>();
        }

        List<MedicalRecord> records =
                new ArrayList<>();

        String sql = """
                SELECT record_id,
                       patient_id,
                       doctor_id,
                       appointment_id,
                       diagnosis,
                       symptoms,
                       treatment,
                       notes,
                       created_at
                FROM medical_records
                WHERE doctor_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    records.add(
                            mapMedicalRecord(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medical records "
                            + "for doctor " + doctorId,
                    e
            );
        }

        return records;
    }

    // ---------------------------------------------------------
    // FIND RECORDS BY DOCTOR + PATIENT
    // ---------------------------------------------------------

    /**
     * Returns only medical records that belong to BOTH
     * the specified doctor and patient.
     *
     * This is useful for enforcing clinical ownership
     * at the repository/database-query level.
     */
    public List<MedicalRecord> findByDoctorAndPatient(
            int doctorId,
            int patientId) {

        if (doctorId <= 0 || patientId <= 0) {
            return new ArrayList<>();
        }

        List<MedicalRecord> records =
                new ArrayList<>();

        String sql = """
                SELECT record_id,
                       patient_id,
                       doctor_id,
                       appointment_id,
                       diagnosis,
                       symptoms,
                       treatment,
                       notes,
                       created_at
                FROM medical_records
                WHERE doctor_id = ?
                  AND patient_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);
            statement.setInt(2, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    records.add(
                            mapMedicalRecord(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medical records "
                            + "for doctor " + doctorId
                            + " and patient " + patientId,
                    e
            );
        }

        return records;
    }

    // ---------------------------------------------------------
    // FIND RECORDS FOR PATIENT ACCESS
    // ---------------------------------------------------------

    /**
     * Patient-specific query.
     *
     * The patient ID is directly included in the SQL WHERE
     * clause so the repository never returns another
     * patient's medical records for this operation.
     */
    public List<MedicalRecord> findForPatient(
            int patientId) {

        return findByPatientId(patientId);
    }

    // ---------------------------------------------------------
    // FIND RECORDS FOR DOCTOR ACCESS
    // ---------------------------------------------------------

    /**
     * Doctor-specific query.
     *
     * The doctor ID is directly included in the SQL WHERE
     * clause.
     */
    public List<MedicalRecord> findForDoctor(
            int doctorId) {

        return findByDoctorId(doctorId);
    }

    // ---------------------------------------------------------
    // ADD MEDICAL RECORD
    // ---------------------------------------------------------

    public MedicalRecord add(MedicalRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Medical record cannot be null."
            );
        }

        if (record.getPatientId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (record.getDoctorId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        if (record.getAppointmentId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid appointment ID."
            );
        }

        String sql = """
                INSERT INTO medical_records (
                    patient_id,
                    doctor_id,
                    appointment_id,
                    diagnosis,
                    symptoms,
                    treatment,
                    notes,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
                    record.getPatientId()
            );

            statement.setInt(
                    2,
                    record.getDoctorId()
            );

            statement.setInt(
                    3,
                    record.getAppointmentId()
            );

            statement.setString(
                    4,
                    record.getDiagnosis()
            );

            statement.setString(
                    5,
                    record.getSymptoms()
            );

            statement.setString(
                    6,
                    record.getTreatment()
            );

            statement.setString(
                    7,
                    record.getNotes()
            );

            LocalDateTime createdAt =
                    record.getCreatedAt();

            if (createdAt != null) {

                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(createdAt)
                );

            } else {

                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );
            }

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException(
                        "Failed to create medical record."
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    int generatedId =
                            generatedKeys.getInt(1);

                    Timestamp savedTimestamp =
                            null;

                    /*
                     * Retrieve the actual created timestamp
                     * from the database so the returned object
                     * accurately represents the stored record.
                     */
                    String timestampSql = """
                            SELECT created_at
                            FROM medical_records
                            WHERE record_id = ?
                            """;

                    try (PreparedStatement timestampStatement =
                                 connection.prepareStatement(
                                         timestampSql
                                 )) {

                        timestampStatement.setInt(
                                1,
                                generatedId
                        );

                        try (ResultSet timestampResult =
                                     timestampStatement.executeQuery()) {

                            if (timestampResult.next()) {
                                savedTimestamp =
                                        timestampResult.getTimestamp(
                                                "created_at"
                                        );
                            }
                        }
                    }

                    LocalDateTime savedCreatedAt =
                            savedTimestamp != null
                                    ? savedTimestamp
                                    .toLocalDateTime()
                                    : createdAt;

                    return new MedicalRecord(
                            generatedId,
                            record.getPatientId(),
                            record.getDoctorId(),
                            record.getAppointmentId(),
                            record.getDiagnosis(),
                            record.getSymptoms(),
                            record.getTreatment(),
                            record.getNotes(),
                            savedCreatedAt
                    );
                }
            }

            throw new RuntimeException(
                    "Medical record was created, "
                            + "but its generated ID could not be retrieved."
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to add medical record",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // MAP DATABASE ROW TO MEDICAL RECORD
    // ---------------------------------------------------------

    private MedicalRecord mapMedicalRecord(
            ResultSet resultSet)
            throws SQLException {

        Timestamp timestamp =
                resultSet.getTimestamp("created_at");

        LocalDateTime createdAt =
                timestamp != null
                        ? timestamp.toLocalDateTime()
                        : null;

        return new MedicalRecord(
                resultSet.getInt("record_id"),
                resultSet.getInt("patient_id"),
                resultSet.getInt("doctor_id"),
                resultSet.getInt("appointment_id"),
                resultSet.getString("diagnosis"),
                resultSet.getString("symptoms"),
                resultSet.getString("treatment"),
                resultSet.getString("notes"),
                createdAt
        );
    }
}