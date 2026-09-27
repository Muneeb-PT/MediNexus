package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Bill;
import com.medinexus.model.Prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Handles the atomic database transaction for:
 *
 * DISPENSED prescriptions
 *        ↓
 *      BILL
 *        ↓
 * BILLED prescriptions
 *
 * Everything succeeds together or everything is rolled back.
 */
public class PrescriptionBillingRepository {

    /**
     * Creates one bill and marks all supplied prescriptions as BILLED
     * inside a single database transaction.
     *
     * Transaction:
     *
     * 1. Verify prescriptions are DISPENSED
     * 2. Insert bill
     * 3. Mark prescriptions BILLED
     * 4. Commit
     *
     * If anything fails:
     *
     * ROLLBACK
     */
    public void createBillAndMarkPrescriptionsBilled(
            Bill bill,
            List<Prescription> prescriptions) {

        if (bill == null) {
            throw new IllegalArgumentException(
                    "Bill cannot be null."
            );
        }

        if (prescriptions == null
                || prescriptions.isEmpty()) {

            throw new IllegalArgumentException(
                    "No prescriptions were supplied."
            );
        }

        String verifyPrescriptionSql = """
                SELECT status
                FROM prescriptions
                WHERE prescription_id = ?
                FOR UPDATE
                """;

        String insertBillSql = """
                INSERT INTO bills (
                    bill_id,
                    patient_id,
                    appointment_id,
                    consultation_charge,
                    medicine_charge,
                    other_charges,
                    total_amount,
                    payment_status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String updatePrescriptionSql = """
                UPDATE prescriptions
                SET status = 'BILLED'
                WHERE prescription_id = ?
                  AND status = 'DISPENSED'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // =================================================
                // 1. LOCK AND VERIFY ALL PRESCRIPTIONS
                // =================================================

                for (Prescription prescription :
                        prescriptions) {

                    if (prescription == null
                            || prescription.getPrescriptionId() <= 0) {

                        connection.rollback();

                        throw new IllegalArgumentException(
                                "Invalid prescription supplied."
                        );
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         verifyPrescriptionSql)) {

                        statement.setInt(
                                1,
                                prescription.getPrescriptionId()
                        );

                        try (ResultSet resultSet =
                                     statement.executeQuery()) {

                            if (!resultSet.next()) {

                                connection.rollback();

                                throw new IllegalStateException(
                                        "Prescription #"
                                                + prescription
                                                .getPrescriptionId()
                                                + " was not found."
                                );
                            }

                            String status =
                                    resultSet.getString("status");

                            if (!"DISPENSED"
                                    .equalsIgnoreCase(status)) {

                                connection.rollback();

                                throw new IllegalStateException(
                                        "Prescription #"
                                                + prescription
                                                .getPrescriptionId()
                                                + " is not DISPENSED."
                                );
                            }
                        }
                    }
                }

                // =================================================
                // 2. INSERT BILL
                // =================================================

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     insertBillSql)) {

                    statement.setInt(
                            1,
                            bill.getBillId()
                    );

                    statement.setInt(
                            2,
                            bill.getPatientId()
                    );

                    if (bill.getAppointmentId() > 0) {

                        statement.setInt(
                                3,
                                bill.getAppointmentId()
                        );

                    } else {

                        statement.setNull(
                                3,
                                java.sql.Types.BIGINT
                        );
                    }

                    statement.setDouble(
                            4,
                            bill.getConsultationCharge()
                    );

                    statement.setDouble(
                            5,
                            bill.getMedicineCharge()
                    );

                    statement.setDouble(
                            6,
                            bill.getOtherCharges()
                    );

                    statement.setDouble(
                            7,
                            bill.getTotalAmount()
                    );

                    statement.setString(
                            8,
                            bill.getPaymentStatus()
                    );

                    if (bill.getCreatedAt() != null) {

                        statement.setTimestamp(
                                9,
                                Timestamp.valueOf(
                                        bill.getCreatedAt()
                                )
                        );

                    } else {

                        statement.setTimestamp(
                                9,
                                new Timestamp(
                                        System.currentTimeMillis()
                                )
                        );
                    }

                    statement.executeUpdate();
                }

                // =================================================
                // 3. MARK ALL PRESCRIPTIONS AS BILLED
                // =================================================

                for (Prescription prescription :
                        prescriptions) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         updatePrescriptionSql)) {

                        statement.setInt(
                                1,
                                prescription.getPrescriptionId()
                        );

                        int updated =
                                statement.executeUpdate();

                        if (updated != 1) {

                            connection.rollback();

                            throw new IllegalStateException(
                                    "Prescription #"
                                            + prescription
                                            .getPrescriptionId()
                                            + " could not be marked as BILLED."
                            );
                        }
                    }
                }

                // =================================================
                // 4. COMMIT EVERYTHING
                // =================================================

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

                throw e;

            } catch (RuntimeException e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to create bill and update "
                            + "prescription statuses.",
                    e
            );
        }
    }
}