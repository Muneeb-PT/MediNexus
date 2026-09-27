package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Prescription;
import com.medinexus.model.PrescriptionStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionJdbcRepository {

    // ---------------------------------------------------------
    // FIND ALL PRESCRIPTIONS
    // ---------------------------------------------------------

    public List<Prescription> findAll() {

        List<Prescription> prescriptions =
                new ArrayList<>();

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                ORDER BY prescription_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                prescriptions.add(
                        mapPrescription(resultSet)
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescriptions.",
                    e
            );
        }

        return prescriptions;
    }

    // ---------------------------------------------------------
    // FIND PRESCRIPTION BY ID
    // ---------------------------------------------------------

    public Prescription findById(
            int prescriptionId) {

        if (prescriptionId <= 0) {
            return null;
        }

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                WHERE prescription_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, prescriptionId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPrescription(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find prescription with ID: "
                            + prescriptionId,
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // FIND BY MEDICAL RECORD
    // ---------------------------------------------------------

    public List<Prescription> findByMedicalRecordId(
            int medicalRecordId) {

        if (medicalRecordId <= 0) {
            return new ArrayList<>();
        }

        List<Prescription> prescriptions =
                new ArrayList<>();

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                WHERE medical_record_id = ?
                ORDER BY prescription_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, medicalRecordId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    prescriptions.add(
                            mapPrescription(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescriptions for "
                            + "medical record: "
                            + medicalRecordId,
                    e
            );
        }

        return prescriptions;
    }

    // ---------------------------------------------------------
    // FIND BY PATIENT
    // ---------------------------------------------------------

    public List<Prescription> findByPatientId(
            int patientId) {

        if (patientId <= 0) {
            return new ArrayList<>();
        }

        List<Prescription> prescriptions =
                new ArrayList<>();

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                WHERE patient_id = ?
                ORDER BY prescription_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    prescriptions.add(
                            mapPrescription(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescriptions for "
                            + "patient: "
                            + patientId,
                    e
            );
        }

        return prescriptions;
    }

    // ---------------------------------------------------------
    // FIND BY DOCTOR
    // ---------------------------------------------------------

    public List<Prescription> findByDoctorId(
            int doctorId) {

        if (doctorId <= 0) {
            return new ArrayList<>();
        }

        List<Prescription> prescriptions =
                new ArrayList<>();

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                WHERE doctor_id = ?
                ORDER BY prescription_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    prescriptions.add(
                            mapPrescription(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescriptions for "
                            + "doctor: "
                            + doctorId,
                    e
            );
        }

        return prescriptions;
    }

    // ---------------------------------------------------------
    // FIND BY DOCTOR + PATIENT
    // ---------------------------------------------------------

    /**
     * Returns prescriptions belonging to both
     * the specified doctor and patient.
     *
     * The ownership restriction is enforced directly
     * by the SQL query.
     */
    public List<Prescription> findByDoctorAndPatient(
            int doctorId,
            int patientId) {

        if (doctorId <= 0 || patientId <= 0) {
            return new ArrayList<>();
        }

        List<Prescription> prescriptions =
                new ArrayList<>();

        String sql = """
                SELECT prescription_id,
                       medical_record_id,
                       patient_id,
                       doctor_id,
                       medicine_id,
                       quantity,
                       dosage,
                       frequency,
                       duration,
                       instructions,
                       status,
                       created_at
                FROM prescriptions
                WHERE doctor_id = ?
                  AND patient_id = ?
                ORDER BY prescription_id
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
                    prescriptions.add(
                            mapPrescription(resultSet)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescriptions for "
                            + "doctor "
                            + doctorId
                            + " and patient "
                            + patientId,
                    e
            );
        }

        return prescriptions;
    }

    // ---------------------------------------------------------
    // ADD PRESCRIPTION
    // ---------------------------------------------------------

    /**
     * Inserts a prescription without supplying
     * prescription_id.
     *
     * MySQL generates the ID using AUTO_INCREMENT.
     */
    public Prescription add(
            Prescription prescription) {

        if (prescription == null) {
            throw new IllegalArgumentException(
                    "Prescription cannot be null."
            );
        }

        String sql = """
                INSERT INTO prescriptions (
                    medical_record_id,
                    patient_id,
                    doctor_id,
                    medicine_id,
                    quantity,
                    dosage,
                    frequency,
                    duration,
                    instructions,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                    prescription.getMedicalRecordId()
            );

            statement.setInt(
                    2,
                    prescription.getPatientId()
            );

            statement.setInt(
                    3,
                    prescription.getDoctorId()
            );

            statement.setInt(
                    4,
                    prescription.getMedicineId()
            );

            statement.setInt(
                    5,
                    prescription.getQuantity()
            );

            statement.setString(
                    6,
                    prescription.getDosage()
            );

            statement.setString(
                    7,
                    prescription.getFrequency()
            );

            statement.setString(
                    8,
                    prescription.getDuration()
            );

            statement.setString(
                    9,
                    prescription.getInstructions()
            );

            statement.setString(
                    10,
                    prescription.getStatus().name()
            );

            if (prescription.getCreatedAt() != null) {

                statement.setTimestamp(
                        11,
                        Timestamp.valueOf(
                                prescription.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        11,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );
            }

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException(
                        "Failed to create prescription."
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Failed to retrieve generated "
                                    + "prescription ID."
                    );
                }

                int generatedId =
                        generatedKeys.getInt(1);

                prescription.setPrescriptionId(
                        generatedId
                );

                return prescription;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to add prescription.",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // UPDATE PRESCRIPTION
    // ---------------------------------------------------------

    public void update(
            Prescription prescription) {

        if (prescription == null) {
            throw new IllegalArgumentException(
                    "Prescription cannot be null."
            );
        }

        if (prescription.getPrescriptionId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }

        String sql = """
                UPDATE prescriptions
                SET medical_record_id = ?,
                    patient_id = ?,
                    doctor_id = ?,
                    medicine_id = ?,
                    quantity = ?,
                    dosage = ?,
                    frequency = ?,
                    duration = ?,
                    instructions = ?,
                    status = ?,
                    created_at = ?
                WHERE prescription_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    prescription.getMedicalRecordId()
            );

            statement.setInt(
                    2,
                    prescription.getPatientId()
            );

            statement.setInt(
                    3,
                    prescription.getDoctorId()
            );

            statement.setInt(
                    4,
                    prescription.getMedicineId()
            );

            statement.setInt(
                    5,
                    prescription.getQuantity()
            );

            statement.setString(
                    6,
                    prescription.getDosage()
            );

            statement.setString(
                    7,
                    prescription.getFrequency()
            );

            statement.setString(
                    8,
                    prescription.getDuration()
            );

            statement.setString(
                    9,
                    prescription.getInstructions()
            );

            statement.setString(
                    10,
                    prescription.getStatus().name()
            );

            if (prescription.getCreatedAt() != null) {

                statement.setTimestamp(
                        11,
                        Timestamp.valueOf(
                                prescription.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        11,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );
            }

            statement.setInt(
                    12,
                    prescription.getPrescriptionId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update prescription with ID: "
                            + prescription.getPrescriptionId(),
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // DELETE PRESCRIPTION
    // ---------------------------------------------------------

    public void delete(
            int prescriptionId) {

        if (prescriptionId <= 0) {
            return;
        }

        String sql = """
                DELETE FROM prescriptions
                WHERE prescription_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, prescriptionId);
            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete prescription with ID: "
                            + prescriptionId,
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // MAP DATABASE ROW
    // ---------------------------------------------------------

    private Prescription mapPrescription(
            ResultSet resultSet)
            throws SQLException {

        Prescription prescription =
                new Prescription();

        prescription.setPrescriptionId(
                resultSet.getInt(
                        "prescription_id"
                )
        );

        prescription.setMedicalRecordId(
                resultSet.getInt(
                        "medical_record_id"
                )
        );

        prescription.setPatientId(
                resultSet.getInt(
                        "patient_id"
                )
        );

        prescription.setDoctorId(
                resultSet.getInt(
                        "doctor_id"
                )
        );

        prescription.setMedicineId(
                resultSet.getInt(
                        "medicine_id"
                )
        );

        prescription.setQuantity(
                resultSet.getInt(
                        "quantity"
                )
        );

        prescription.setDosage(
                resultSet.getString(
                        "dosage"
                )
        );

        prescription.setFrequency(
                resultSet.getString(
                        "frequency"
                )
        );

        prescription.setDuration(
                resultSet.getString(
                        "duration"
                )
        );

        prescription.setInstructions(
                resultSet.getString(
                        "instructions"
                )
        );

        String status =
                resultSet.getString("status");

        if (status != null
                && !status.isBlank()) {

            prescription.setStatus(
                    PrescriptionStatus.valueOf(
                            status.toUpperCase()
                    )
            );

        } else {

            prescription.setStatus(
                    PrescriptionStatus.PENDING
            );
        }

        Timestamp timestamp =
                resultSet.getTimestamp(
                        "created_at"
                );

        if (timestamp != null) {

            prescription.setCreatedAt(
                    timestamp.toLocalDateTime()
            );
        }

        return prescription;
    }
}