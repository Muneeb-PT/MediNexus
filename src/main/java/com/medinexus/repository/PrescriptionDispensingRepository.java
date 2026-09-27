package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrescriptionDispensingRepository {

    /**
     * Atomically dispenses a prescription.
     *
     * Transaction:
     *
     * 1. Verify logged-in pharmacy staff
     * 2. Lock prescription
     * 3. Verify prescription is PENDING
     * 4. Lock medicine
     * 5. Verify sufficient stock
     * 6. Reduce medicine stock
     * 7. Record DISPENSE inventory transaction
     * 8. Change prescription status to DISPENSED
     * 9. Commit
     *
     * If any step fails, everything is rolled back.
     */
    public boolean dispensePrescription(int prescriptionId) {

        if (prescriptionId <= 0) {
            return false;
        }

        // ---------------------------------------------------------
        // 0. Verify current user
        // ---------------------------------------------------------

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "You must be logged in to dispense medicines."
            );
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can dispense medicines."
            );
        }

        long performedBy =
                currentUser.getUserId();

        String prescriptionSql = """
                SELECT medicine_id, quantity, status
                FROM prescriptions
                WHERE prescription_id = ?
                FOR UPDATE
                """;

        String medicineSql = """
                SELECT quantity
                FROM medicines
                WHERE medicine_id = ?
                FOR UPDATE
                """;

        String reduceStockSql = """
                UPDATE medicines
                SET quantity = quantity - ?,
                    available = (quantity - ? > 0)
                WHERE medicine_id = ?
                  AND quantity >= ?
                """;

        String inventoryTransactionSql = """
                INSERT INTO medicine_inventory_transactions (
                    medicine_id,
                    transaction_type,
                    quantity,
                    stock_before,
                    stock_after,
                    reference_type,
                    reference_id,
                    performed_by,
                    notes
                )
                VALUES (
                    ?, 'DISPENSE', ?, ?, ?,
                    'PRESCRIPTION', ?, ?, ?
                )
                """;

        String updatePrescriptionSql = """
                UPDATE prescriptions
                SET status = 'DISPENSED'
                WHERE prescription_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int medicineId;
                int prescriptionQuantity;

                // -------------------------------------------------
                // 1. Lock and validate prescription
                // -------------------------------------------------

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     prescriptionSql)) {

                    statement.setInt(
                            1,
                            prescriptionId
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            connection.rollback();

                            return false;
                        }

                        medicineId =
                                resultSet.getInt(
                                        "medicine_id"
                                );

                        prescriptionQuantity =
                                resultSet.getInt(
                                        "quantity"
                                );

                        String status =
                                resultSet.getString(
                                        "status"
                                );

                        if (!"PENDING".equalsIgnoreCase(
                                status)) {

                            connection.rollback();

                            return false;
                        }
                    }
                }

                // -------------------------------------------------
                // 2. Validate prescription values
                // -------------------------------------------------

                if (medicineId <= 0
                        || prescriptionQuantity <= 0) {

                    connection.rollback();

                    return false;
                }

                // -------------------------------------------------
                // 3. Lock medicine row
                // -------------------------------------------------

                int stockBefore;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     medicineSql)) {

                    statement.setInt(
                            1,
                            medicineId
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            connection.rollback();

                            return false;
                        }

                        stockBefore =
                                resultSet.getInt(
                                        "quantity"
                                );
                    }
                }

                // -------------------------------------------------
                // 4. Check sufficient stock
                // -------------------------------------------------

                if (stockBefore < prescriptionQuantity) {

                    connection.rollback();

                    return false;
                }

                int stockAfter =
                        stockBefore
                                - prescriptionQuantity;

                // -------------------------------------------------
                // 5. Reduce medicine stock
                // -------------------------------------------------

                int stockUpdated;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     reduceStockSql)) {

                    statement.setInt(
                            1,
                            prescriptionQuantity
                    );

                    statement.setInt(
                            2,
                            prescriptionQuantity
                    );

                    statement.setInt(
                            3,
                            medicineId
                    );

                    statement.setInt(
                            4,
                            prescriptionQuantity
                    );

                    stockUpdated =
                            statement.executeUpdate();
                }

                if (stockUpdated != 1) {

                    connection.rollback();

                    return false;
                }

                // -------------------------------------------------
                // 6. Record inventory audit transaction
                // -------------------------------------------------

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     inventoryTransactionSql)) {

                    statement.setInt(
                            1,
                            medicineId
                    );

                    statement.setInt(
                            2,
                            prescriptionQuantity
                    );

                    statement.setInt(
                            3,
                            stockBefore
                    );

                    statement.setInt(
                            4,
                            stockAfter
                    );

                    statement.setLong(
                            5,
                            prescriptionId
                    );

                    statement.setLong(
                            6,
                            performedBy
                    );

                    statement.setString(
                            7,
                            "Medicine dispensed against prescription "
                                    + prescriptionId
                    );

                    int transactionInserted =
                            statement.executeUpdate();

                    if (transactionInserted != 1) {

                        connection.rollback();

                        return false;
                    }
                }

                // -------------------------------------------------
                // 7. Change prescription status
                // -------------------------------------------------

                int prescriptionUpdated;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updatePrescriptionSql)) {

                    statement.setInt(
                            1,
                            prescriptionId
                    );

                    prescriptionUpdated =
                            statement.executeUpdate();
                }

                if (prescriptionUpdated != 1) {

                    connection.rollback();

                    return false;
                }

                // -------------------------------------------------
                // 8. Everything succeeded
                // -------------------------------------------------

                connection.commit();

                return true;

            } catch (SQLException e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to dispense prescription "
                            + prescriptionId,
                    e
            );
        }
    }
}