package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillJdbcRepository {

    // ---------------------------------------------------------
    // GET ALL BILLS
    // ---------------------------------------------------------

    public List<Bill> findAll() {

        List<Bill> bills = new ArrayList<>();

        String sql =
                "SELECT bill_id, patient_id, appointment_id, " +
                        "consultation_charge, medicine_charge, other_charges, " +
                        "total_amount, payment_status, created_at " +
                        "FROM bills " +
                        "ORDER BY bill_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                bills.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bills.",
                    e
            );
        }

        return bills;
    }

    // ---------------------------------------------------------
    // GET BILL BY ID
    // ---------------------------------------------------------

    public Bill findById(int billId) {

        String sql =
                "SELECT bill_id, patient_id, appointment_id, " +
                        "consultation_charge, medicine_charge, other_charges, " +
                        "total_amount, payment_status, created_at " +
                        "FROM bills " +
                        "WHERE bill_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, billId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bill.",
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // GET BILLS BY PATIENT
    // ---------------------------------------------------------

    public List<Bill> findByPatientId(int patientId) {

        List<Bill> bills = new ArrayList<>();

        String sql =
                "SELECT bill_id, patient_id, appointment_id, " +
                        "consultation_charge, medicine_charge, other_charges, " +
                        "total_amount, payment_status, created_at " +
                        "FROM bills " +
                        "WHERE patient_id = ? " +
                        "ORDER BY bill_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    bills.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve patient bills.",
                    e
            );
        }

        return bills;
    }

    // ---------------------------------------------------------
    // GET BILL BY APPOINTMENT
    // ---------------------------------------------------------

    public Bill findByAppointmentId(int appointmentId) {

        String sql =
                "SELECT bill_id, patient_id, appointment_id, " +
                        "consultation_charge, medicine_charge, other_charges, " +
                        "total_amount, payment_status, created_at " +
                        "FROM bills " +
                        "WHERE appointment_id = ? " +
                        "LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve bill for appointment.",
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // ADD BILL
    // ---------------------------------------------------------
    // bill_id is intentionally NOT included.
    // MySQL AUTO_INCREMENT generates it.
    // ---------------------------------------------------------

    public Bill add(Bill bill) {

        String sql =
                "INSERT INTO bills (" +
                        "patient_id, " +
                        "appointment_id, " +
                        "consultation_charge, " +
                        "medicine_charge, " +
                        "other_charges, " +
                        "total_amount, " +
                        "payment_status, " +
                        "created_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(
                    1,
                    bill.getPatientId()
            );

            if (bill.getAppointmentId() > 0) {
                statement.setInt(
                        2,
                        bill.getAppointmentId()
                );
            } else {
                statement.setNull(
                        2,
                        Types.INTEGER
                );
            }

            statement.setDouble(
                    3,
                    bill.getConsultationCharge()
            );

            statement.setDouble(
                    4,
                    bill.getMedicineCharge()
            );

            statement.setDouble(
                    5,
                    bill.getOtherCharges()
            );

            statement.setDouble(
                    6,
                    bill.getTotalAmount()
            );

            statement.setString(
                    7,
                    bill.getPaymentStatus()
            );

            if (bill.getCreatedAt() != null) {
                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                bill.getCreatedAt()
                        )
                );
            } else {
                statement.setTimestamp(
                        8,
                        Timestamp.valueOf(
                                java.time.LocalDateTime.now()
                        )
                );
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Creating bill failed. No rows were inserted."
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Creating bill failed. " +
                                    "No generated bill ID was returned."
                    );
                }

                int generatedBillId =
                        generatedKeys.getInt(1);

                bill.setBillId(generatedBillId);

                return bill;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to create bill.",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // UPDATE BILL
    // ---------------------------------------------------------

    public void update(Bill bill) {

        String sql =
                "UPDATE bills SET " +
                        "patient_id = ?, " +
                        "appointment_id = ?, " +
                        "consultation_charge = ?, " +
                        "medicine_charge = ?, " +
                        "other_charges = ?, " +
                        "total_amount = ?, " +
                        "payment_status = ? " +
                        "WHERE bill_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    bill.getPatientId()
            );

            if (bill.getAppointmentId() > 0) {
                statement.setInt(
                        2,
                        bill.getAppointmentId()
                );
            } else {
                statement.setNull(
                        2,
                        Types.INTEGER
                );
            }

            statement.setDouble(
                    3,
                    bill.getConsultationCharge()
            );

            statement.setDouble(
                    4,
                    bill.getMedicineCharge()
            );

            statement.setDouble(
                    5,
                    bill.getOtherCharges()
            );

            statement.setDouble(
                    6,
                    bill.getTotalAmount()
            );

            statement.setString(
                    7,
                    bill.getPaymentStatus()
            );

            statement.setInt(
                    8,
                    bill.getBillId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update bill.",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // MAP RESULT SET → BILL
    // ---------------------------------------------------------

    private Bill mapRow(ResultSet resultSet)
            throws SQLException {

        Bill bill = new Bill();

        bill.setBillId(
                resultSet.getInt("bill_id")
        );

        bill.setPatientId(
                resultSet.getInt("patient_id")
        );

        int appointmentId =
                resultSet.getInt("appointment_id");

        if (resultSet.wasNull()) {
            bill.setAppointmentId(0);
        } else {
            bill.setAppointmentId(appointmentId);
        }

        bill.setConsultationCharge(
                resultSet.getDouble("consultation_charge")
        );

        bill.setMedicineCharge(
                resultSet.getDouble("medicine_charge")
        );

        bill.setOtherCharges(
                resultSet.getDouble("other_charges")
        );

        bill.setTotalAmount(
                resultSet.getDouble("total_amount")
        );

        bill.setPaymentStatus(
                resultSet.getString("payment_status")
        );

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            bill.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        return bill;
    }
}