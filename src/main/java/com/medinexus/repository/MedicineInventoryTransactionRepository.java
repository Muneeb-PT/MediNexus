package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicineInventoryTransactionRepository {

    /*
     * Atomically restock a medicine and create
     * the corresponding inventory transaction.
     *
     * Both operations succeed together or both
     * are rolled back.
     */
    public void restock(
            int medicineId,
            int quantity,
            long performedBy,
            String referenceType,
            Long referenceId,
            String notes) {

        if (medicineId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medicine ID."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Restock quantity must be greater than zero."
            );
        }

        if (performedBy <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        String selectSql = """
                SELECT quantity
                FROM medicines
                WHERE medicine_id = ?
                FOR UPDATE
                """;

        String updateSql = """
                UPDATE medicines
                SET quantity = quantity + ?,
                    available = TRUE
                WHERE medicine_id = ?
                """;

        String transactionSql = """
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
                VALUES (?, 'RESTOCK', ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int stockBefore;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     selectSql)) {

                    statement.setInt(
                            1,
                            medicineId
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException(
                                    "Medicine not found: "
                                            + medicineId
                            );
                        }

                        stockBefore =
                                resultSet.getInt(
                                        "quantity"
                                );
                    }
                }

                int stockAfter =
                        stockBefore + quantity;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updateSql)) {

                    statement.setInt(
                            1,
                            quantity
                    );

                    statement.setInt(
                            2,
                            medicineId
                    );

                    if (statement.executeUpdate() != 1) {

                        throw new IllegalStateException(
                                "Medicine stock could not be updated."
                        );
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     transactionSql)) {

                    statement.setInt(
                            1,
                            medicineId
                    );

                    statement.setInt(
                            2,
                            quantity
                    );

                    statement.setInt(
                            3,
                            stockBefore
                    );

                    statement.setInt(
                            4,
                            stockAfter
                    );

                    statement.setString(
                            5,
                            referenceType
                    );

                    if (referenceId == null) {

                        statement.setNull(
                                6,
                                java.sql.Types.BIGINT
                        );

                    } else {

                        statement.setLong(
                                6,
                                referenceId
                        );
                    }

                    statement.setLong(
                            7,
                            performedBy
                    );

                    statement.setString(
                            8,
                            notes
                    );

                    statement.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {

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
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is closing.
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to restock medicine "
                            + medicineId,
                    e
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to restock medicine "
                            + medicineId,
                    e
            );
        }
    }

    /*
     * Return complete inventory transaction history.
     */
    public List<InventoryTransaction> findAll() {

        String sql = """
                SELECT
                    transaction_id,
                    medicine_id,
                    transaction_type,
                    quantity,
                    stock_before,
                    stock_after,
                    reference_type,
                    reference_id,
                    performed_by,
                    notes,
                    created_at
                FROM medicine_inventory_transactions
                ORDER BY created_at DESC,
                         transaction_id DESC
                """;

        List<InventoryTransaction> transactions =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                transactions.add(
                        mapTransaction(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch inventory transactions.",
                    e
            );
        }

        return transactions;
    }

    /*
     * Find transaction history for one medicine.
     */
    public List<InventoryTransaction>
    findByMedicineId(int medicineId) {

        if (medicineId <= 0) {
            return List.of();
        }

        String sql = """
                SELECT
                    transaction_id,
                    medicine_id,
                    transaction_type,
                    quantity,
                    stock_before,
                    stock_after,
                    reference_type,
                    reference_id,
                    performed_by,
                    notes,
                    created_at
                FROM medicine_inventory_transactions
                WHERE medicine_id = ?
                ORDER BY created_at DESC,
                         transaction_id DESC
                """;

        List<InventoryTransaction> transactions =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    medicineId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    transactions.add(
                            mapTransaction(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch inventory history "
                            + "for medicine "
                            + medicineId,
                    e
            );
        }

        return transactions;
    }

    private InventoryTransaction mapTransaction(
            ResultSet resultSet)
            throws SQLException {

        long referenceId =
                resultSet.getLong("reference_id");

        Long nullableReferenceId =
                resultSet.wasNull()
                        ? null
                        : referenceId;

        return new InventoryTransaction(
                resultSet.getLong("transaction_id"),
                resultSet.getInt("medicine_id"),
                resultSet.getString(
                        "transaction_type"
                ),
                resultSet.getInt("quantity"),
                resultSet.getInt("stock_before"),
                resultSet.getInt("stock_after"),
                resultSet.getString(
                        "reference_type"
                ),
                nullableReferenceId,
                resultSet.getLong("performed_by"),
                resultSet.getString("notes"),
                resultSet.getTimestamp(
                        "created_at"
                ).toLocalDateTime()
        );
    }

    /*
     * Immutable representation of an inventory
     * transaction.
     */
    public static class InventoryTransaction {

        private final long transactionId;
        private final int medicineId;
        private final String transactionType;
        private final int quantity;
        private final int stockBefore;
        private final int stockAfter;
        private final String referenceType;
        private final Long referenceId;
        private final long performedBy;
        private final String notes;
        private final java.time.LocalDateTime createdAt;

        public InventoryTransaction(
                long transactionId,
                int medicineId,
                String transactionType,
                int quantity,
                int stockBefore,
                int stockAfter,
                String referenceType,
                Long referenceId,
                long performedBy,
                String notes,
                java.time.LocalDateTime createdAt) {

            this.transactionId =
                    transactionId;

            this.medicineId =
                    medicineId;

            this.transactionType =
                    transactionType;

            this.quantity =
                    quantity;

            this.stockBefore =
                    stockBefore;

            this.stockAfter =
                    stockAfter;

            this.referenceType =
                    referenceType;

            this.referenceId =
                    referenceId;

            this.performedBy =
                    performedBy;

            this.notes =
                    notes;

            this.createdAt =
                    createdAt;
        }

        public long getTransactionId() {
            return transactionId;
        }

        public int getMedicineId() {
            return medicineId;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getStockBefore() {
            return stockBefore;
        }

        public int getStockAfter() {
            return stockAfter;
        }

        public String getReferenceType() {
            return referenceType;
        }

        public Long getReferenceId() {
            return referenceId;
        }

        public long getPerformedBy() {
            return performedBy;
        }

        public String getNotes() {
            return notes;
        }

        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}