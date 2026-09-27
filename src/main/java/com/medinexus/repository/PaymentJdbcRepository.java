package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Payment;
import com.medinexus.model.PaymentMethod;
import com.medinexus.model.PaymentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentJdbcRepository {

    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    public List<Payment> findAll() {

        List<Payment> payments = new ArrayList<>();

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            ORDER BY payment_id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                payments.add(mapPayment(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch payments.", e);
        }

        return payments;
    }


    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    public Payment findById(int paymentId) {

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE payment_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find payment with ID: "
                            + paymentId,
                    e
            );
        }

        return null;
    }


    // =========================================================
    // GET PAYMENTS BY BILL
    // =========================================================

    public List<Payment> findByBillId(int billId) {

        List<Payment> payments = new ArrayList<>();

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE bill_id = ?
            ORDER BY payment_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch payments for bill: "
                            + billId,
                    e
            );
        }

        return payments;
    }


    // =========================================================
    // GET PAYMENTS BY PATIENT
    // =========================================================

    public List<Payment> findByPatientId(int patientId) {

        List<Payment> payments = new ArrayList<>();

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE patient_id = ?
            ORDER BY payment_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch payments for patient: "
                            + patientId,
                    e
            );
        }

        return payments;
    }


    // =========================================================
    // FIND BILL ID FOR A PRESCRIPTION
    //
    // prescriptions
    //      ↓ medical_record_id
    // medical_records
    //      ↓ appointment_id
    // bills
    // =========================================================

    public int findBillIdForPrescription(int prescriptionId) {

        String sql = """
            SELECT b.bill_id
            FROM prescriptions p
            JOIN medical_records mr
                ON mr.record_id = p.medical_record_id
            JOIN bills b
                ON b.appointment_id = mr.appointment_id
               AND b.patient_id = p.patient_id
            WHERE p.prescription_id = ?
            ORDER BY b.bill_id DESC
            LIMIT 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, prescriptionId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("bill_id");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find bill for prescription: "
                            + prescriptionId,
                    e
            );
        }

        return 0;
    }


    // =========================================================
    // CHECK WHETHER BILL HAS SUCCESSFUL PAYMENT
    // =========================================================

    public boolean hasSuccessfulPayment(
            Connection connection,
            int billId) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM payments
            WHERE bill_id = ?
              AND payment_status = 'SUCCESS'
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }


    // =========================================================
    // ATOMIC PAYMENT TRANSACTION
    //
    // Bill:
    //      PENDING → PAID
    //
    // Prescriptions:
    //      BILLED → PAID
    //
    // Payment:
    //      INSERT SUCCESS
    //
    // Everything commits together.
    // =========================================================

    public Payment completeSuccessfulPayment(
            int billId,
            int patientId,
            double amount,
            PaymentMethod paymentMethod,
            String transactionId) {

        if (billId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid bill ID."
            );
        }

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Payment method cannot be null."
            );
        }

        if (transactionId == null
                || transactionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction ID cannot be empty."
            );
        }

        transactionId = transactionId.trim();

        Connection connection = null;

        try {

            connection =
                    DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            // -------------------------------------------------
            // 1. LOCK BILL
            // -------------------------------------------------

            String billSql = """
                SELECT bill_id,
                       patient_id,
                       total_amount,
                       payment_status
                FROM bills
                WHERE bill_id = ?
                FOR UPDATE
                """;

            int databasePatientId;
            double databaseAmount;
            String billStatus;
            int appointmentId;

            try (PreparedStatement statement =
                         connection.prepareStatement(billSql)) {

                statement.setInt(1, billId);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {

                        throw new IllegalStateException(
                                "Bill not found."
                        );
                    }

                    databasePatientId =
                            resultSet.getInt("patient_id");

                    databaseAmount =
                            resultSet.getDouble("total_amount");

                    billStatus =
                            resultSet.getString(
                                    "payment_status"
                            );

                    appointmentId =
                            getAppointmentIdForBill(
                                    connection,
                                    billId
                            );
                }
            }


            // -------------------------------------------------
            // 2. OWNERSHIP CHECK
            // -------------------------------------------------

            if (databasePatientId != patientId) {

                throw new SecurityException(
                        "Payment patient does not match bill patient."
                );
            }


            // -------------------------------------------------
            // 3. BILL STATUS CHECK
            // -------------------------------------------------

            if ("PAID".equalsIgnoreCase(billStatus)) {

                /*
                 * The bill is already paid.
                 *
                 * Before rejecting it, check whether this
                 * exact transaction is the successful payment.
                 *
                 * This makes repeated gateway callbacks safe.
                 */

                Payment existingTransaction =
                        findByTransactionId(
                                connection,
                                transactionId
                        );

                if (existingTransaction != null
                        && existingTransaction.getBillId() == billId
                        && existingTransaction.getPatientId() == patientId
                        && existingTransaction.getStatus()
                        == PaymentStatus.SUCCESS
                        && Math.abs(
                        existingTransaction.getAmount()
                                - amount
                ) < 0.01) {

                    connection.commit();
                    return existingTransaction;
                }

                throw new IllegalStateException(
                        "This bill has already been paid."
                );
            }

            if (!"PENDING".equalsIgnoreCase(billStatus)) {

                throw new IllegalStateException(
                        "Bill is not in a payable state."
                );
            }


            // -------------------------------------------------
            // 4. AMOUNT CHECK
            // -------------------------------------------------

            if (Math.abs(databaseAmount - amount) > 0.01) {

                throw new IllegalStateException(
                        "Payment amount does not match bill amount."
                );
            }


            // -------------------------------------------------
            // 5. CHECK EXISTING SUCCESSFUL PAYMENT
            // -------------------------------------------------

            if (hasSuccessfulPayment(
                    connection,
                    billId)) {

                /*
                 * If the exact transaction already exists,
                 * return it instead of creating another record.
                 */

                Payment existingTransaction =
                        findByTransactionId(
                                connection,
                                transactionId
                        );

                if (existingTransaction != null
                        && existingTransaction.getBillId() == billId
                        && existingTransaction.getPatientId() == patientId
                        && existingTransaction.getStatus()
                        == PaymentStatus.SUCCESS
                        && Math.abs(
                        existingTransaction.getAmount()
                                - amount
                ) < 0.01) {

                    connection.commit();
                    return existingTransaction;
                }

                throw new IllegalStateException(
                        "A successful payment already exists for this bill."
                );
            }


            // -------------------------------------------------
            // 6. CHECK TRANSACTION ID
            // -------------------------------------------------

            Payment existingTransaction =
                    findByTransactionId(
                            connection,
                            transactionId
                    );

            if (existingTransaction != null) {

                /*
                 * The transaction ID already exists.
                 *
                 * It may belong to the same payment or another
                 * bill. A transaction ID must never be reused
                 * for another payment.
                 */

                if (existingTransaction.getBillId() == billId
                        && existingTransaction.getPatientId() == patientId
                        && existingTransaction.getStatus()
                        == PaymentStatus.SUCCESS
                        && Math.abs(
                        existingTransaction.getAmount()
                                - amount
                ) < 0.01) {

                    connection.commit();
                    return existingTransaction;
                }

                throw new IllegalStateException(
                        "Transaction ID is already associated with another payment."
                );
            }


            // -------------------------------------------------
            // 7. MARK BILL PAID
            // -------------------------------------------------

            String updateBillSql = """
                UPDATE bills
                SET payment_status = 'PAID'
                WHERE bill_id = ?
                  AND payment_status = 'PENDING'
                """;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updateBillSql)) {

                statement.setInt(1, billId);

                int updated =
                        statement.executeUpdate();

                if (updated != 1) {

                    throw new IllegalStateException(
                            "Bill could not be marked as PAID."
                    );
                }
            }


            // -------------------------------------------------
            // 8. MARK RELATED BILLED PRESCRIPTIONS AS PAID
            // -------------------------------------------------

            String updatePrescriptionSql = """
                UPDATE prescriptions p
                JOIN medical_records mr
                    ON mr.record_id =
                       p.medical_record_id
                SET p.status = 'PAID'
                WHERE mr.appointment_id = ?
                  AND p.patient_id = ?
                  AND p.status = 'BILLED'
                """;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 updatePrescriptionSql)) {

                statement.setInt(1, appointmentId);
                statement.setInt(2, patientId);

                statement.executeUpdate();
            }


            // -------------------------------------------------
            // 9. INSERT SUCCESSFUL PAYMENT
            //
            // payment_id is generated by MySQL AUTO_INCREMENT.
            // -------------------------------------------------

            LocalDateTime now =
                    LocalDateTime.now();

            String insertPaymentSql = """
                INSERT INTO payments (
                    bill_id,
                    patient_id,
                    amount,
                    payment_method,
                    payment_status,
                    transaction_id,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            int paymentId;

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 insertPaymentSql,
                                 Statement.RETURN_GENERATED_KEYS)) {

                statement.setInt(1, billId);
                statement.setInt(2, patientId);
                statement.setDouble(3, amount);

                statement.setString(
                        4,
                        paymentMethod.name()
                );

                statement.setString(
                        5,
                        PaymentStatus.SUCCESS.name()
                );

                statement.setString(
                        6,
                        transactionId
                );

                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(now)
                );

                int inserted =
                        statement.executeUpdate();

                if (inserted != 1) {

                    throw new IllegalStateException(
                            "Payment record could not be created."
                    );
                }

                try (ResultSet generatedKeys =
                             statement.getGeneratedKeys()) {

                    if (!generatedKeys.next()) {

                        throw new IllegalStateException(
                                "Unable to retrieve generated payment ID."
                        );
                    }

                    paymentId =
                            generatedKeys.getInt(1);
                }
            }


            // -------------------------------------------------
            // 10. COMMIT EVERYTHING
            // -------------------------------------------------

            connection.commit();

            return new Payment(
                    paymentId,
                    billId,
                    patientId,
                    amount,
                    paymentMethod,
                    PaymentStatus.SUCCESS,
                    transactionId,
                    now
            );

        } catch (Exception e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {

                    e.addSuppressed(
                            rollbackException
                    );
                }
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(
                    "Payment transaction failed.",
                    e
            );

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {
                    // Connection cleanup.
                }
            }
        }
    }


    // =========================================================
    // GET APPOINTMENT ID FOR BILL
    // =========================================================

    private int getAppointmentIdForBill(
            Connection connection,
            int billId) throws SQLException {

        String sql = """
            SELECT appointment_id
            FROM bills
            WHERE bill_id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    throw new IllegalStateException(
                            "Appointment information not found for bill."
                    );
                }

                int appointmentId =
                        resultSet.getInt("appointment_id");

                if (resultSet.wasNull()
                        || appointmentId <= 0) {

                    throw new IllegalStateException(
                            "Bill is not linked to an appointment."
                    );
                }

                return appointmentId;
            }
        }
    }


    // =========================================================
    // FIND PAYMENT BY TRANSACTION ID
    //
    // Normal connection version.
    // =========================================================

    public Payment findByTransactionId(
            String transactionId) {

        if (transactionId == null
                || transactionId.isBlank()) {

            return null;
        }

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE transaction_id = ?
            LIMIT 1
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    transactionId.trim()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find payment by transaction ID.",
                    e
            );
        }

        return null;
    }


    // =========================================================
    // FIND SUCCESSFUL PAYMENT BY TRANSACTION ID
    // =========================================================

    public Payment findSuccessfulPaymentByTransactionId(
            String transactionId) {

        if (transactionId == null
                || transactionId.isBlank()) {

            return null;
        }

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE transaction_id = ?
              AND payment_status = 'SUCCESS'
            LIMIT 1
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    transactionId.trim()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find successful payment by transaction ID.",
                    e
            );
        }

        return null;
    }


    // =========================================================
    // FIND PAYMENT BY TRANSACTION ID
    // USING EXISTING TRANSACTION CONNECTION
    //
    // IMPORTANT:
    // This method participates in the SAME transaction.
    // =========================================================

    private Payment findByTransactionId(
            Connection connection,
            String transactionId)
            throws SQLException {

        if (transactionId == null
                || transactionId.isBlank()) {

            return null;
        }

        String sql = """
            SELECT payment_id,
                   bill_id,
                   patient_id,
                   amount,
                   payment_method,
                   payment_status,
                   transaction_id,
                   created_at
            FROM payments
            WHERE transaction_id = ?
            LIMIT 1
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    transactionId.trim()
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapPayment(resultSet);
                }
            }
        }

        return null;
    }


    // =========================================================
    // ADD PAYMENT
    // =========================================================

    public void add(Payment payment) {

        String sql = """
            INSERT INTO payments (
                payment_id,
                bill_id,
                patient_id,
                amount,
                payment_method,
                payment_status,
                transaction_id,
                created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    payment.getPaymentId()
            );

            statement.setInt(
                    2,
                    payment.getBillId()
            );

            statement.setInt(
                    3,
                    payment.getPatientId()
            );

            statement.setDouble(
                    4,
                    payment.getAmount()
            );

            if (payment.getPaymentMethod() != null) {

                statement.setString(
                        5,
                        payment.getPaymentMethod().name()
                );

            } else {

                statement.setNull(
                        5,
                        Types.VARCHAR
                );
            }

            if (payment.getStatus() != null) {

                statement.setString(
                        6,
                        payment.getStatus().name()
                );

            } else {

                statement.setString(
                        6,
                        PaymentStatus.PENDING.name()
                );
            }

            statement.setString(
                    7,
                    payment.getTransactionId()
            );

            if (payment.getCreatedAt() != null) {

                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                payment.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );
            }

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to add payment.",
                    e
            );
        }
    }


    // =========================================================
    // UPDATE PAYMENT
    // =========================================================

    public void update(Payment payment) {

        String sql = """
            UPDATE payments
            SET bill_id = ?,
                patient_id = ?,
                amount = ?,
                payment_method = ?,
                payment_status = ?,
                transaction_id = ?,
                created_at = ?
            WHERE payment_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    payment.getBillId()
            );

            statement.setInt(
                    2,
                    payment.getPatientId()
            );

            statement.setDouble(
                    3,
                    payment.getAmount()
            );

            if (payment.getPaymentMethod() != null) {

                statement.setString(
                        4,
                        payment.getPaymentMethod().name()
                );

            } else {

                statement.setNull(
                        4,
                        Types.VARCHAR
                );
            }

            if (payment.getStatus() != null) {

                statement.setString(
                        5,
                        payment.getStatus().name()
                );

            } else {

                statement.setString(
                        5,
                        PaymentStatus.PENDING.name()
                );
            }

            statement.setString(
                    6,
                    payment.getTransactionId()
            );

            if (payment.getCreatedAt() != null) {

                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                payment.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                LocalDateTime.now()
                        )
                );
            }

            statement.setInt(
                    8,
                    payment.getPaymentId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update payment with ID: "
                            + payment.getPaymentId(),
                    e
            );
        }
    }


    // =========================================================
    // DELETE PAYMENT
    // =========================================================

    public void delete(int paymentId) {

        String sql = """
            DELETE FROM payments
            WHERE payment_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete payment with ID: "
                            + paymentId,
                    e
            );
        }
    }


    // =========================================================
    // IDENTITY VERIFICATION
    // =========================================================

    public void updateVerification(
            int patientId,
            boolean verified,
            String method,
            String reference) {

        String sql = """
            UPDATE patients
            SET identity_verified = ?,
                verification_method = ?,
                verification_reference = ?,
                verified_at = ?
            WHERE patient_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setBoolean(1, verified);
            statement.setString(2, method);
            statement.setString(3, reference);

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

            statement.setInt(5, patientId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update patient identity verification.",
                    e
            );
        }
    }


    // =========================================================
    // MAP RESULT SET TO PAYMENT
    // =========================================================

    private Payment mapPayment(
            ResultSet resultSet)
            throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentId(
                resultSet.getInt("payment_id")
        );

        payment.setBillId(
                resultSet.getInt("bill_id")
        );

        payment.setPatientId(
                resultSet.getInt("patient_id")
        );

        payment.setAmount(
                resultSet.getDouble("amount")
        );

        String paymentMethod =
                resultSet.getString(
                        "payment_method"
                );

        if (paymentMethod != null
                && !paymentMethod.isBlank()) {

            payment.setPaymentMethod(
                    PaymentMethod.valueOf(
                            paymentMethod.toUpperCase()
                    )
            );
        }

        String paymentStatus =
                resultSet.getString(
                        "payment_status"
                );

        if (paymentStatus != null
                && !paymentStatus.isBlank()) {

            payment.setStatus(
                    PaymentStatus.valueOf(
                            paymentStatus.toUpperCase()
                    )
            );

        } else {

            payment.setStatus(
                    PaymentStatus.PENDING
            );
        }

        payment.setTransactionId(
                resultSet.getString(
                        "transaction_id"
                )
        );

        Timestamp timestamp =
                resultSet.getTimestamp(
                        "created_at"
                );

        if (timestamp != null) {

            payment.setCreatedAt(
                    timestamp.toLocalDateTime()
            );
        }

        return payment;
    }
}