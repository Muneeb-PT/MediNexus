package com.medinexus.service;

import com.medinexus.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides hospital statistics and reporting data.
 *
 * Reports are intentionally calculated from the database rather than
 * maintaining separate counters, so the values remain synchronized
 * with the actual hospital records.
 */
public class ReportService {

    /**
     * Returns total number of registered patients.
     */
    public int getTotalPatients() {
        String sql = "SELECT COUNT(*) FROM patients";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch total patients.", e);
        }

        return 0;
    }

    /**
     * Returns total number of doctors.
     */
    public int getTotalDoctors() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'DOCTOR'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch total doctors.", e);
        }

        return 0;
    }

    /**
     * Returns total number of appointments.
     */
    public int getTotalAppointments() {
        String sql = "SELECT COUNT(*) FROM appointments";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch total appointments.", e);
        }

        return 0;
    }

    /**
     * Returns number of completed appointments.
     */
    public int getCompletedAppointments() {
        return getAppointmentCountByStatus("COMPLETED");
    }

    /**
     * Returns number of scheduled appointments.
     */
    public int getScheduledAppointments() {
        return getAppointmentCountByStatus("SCHEDULED");
    }

    /**
     * Returns number of cancelled appointments.
     */
    public int getCancelledAppointments() {
        return getAppointmentCountByStatus("CANCELLED");
    }

    /**
     * Returns number of missed appointments.
     */
    public int getMissedAppointments() {
        return getAppointmentCountByStatus("MISSED");
    }

    private int getAppointmentCountByStatus(String status) {

        String sql = """
                SELECT COUNT(*)
                FROM appointments
                WHERE status = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch appointment statistics.", e
            );
        }

        return 0;
    }

    /**
     * Returns total billed revenue.
     *
     * This includes all bills regardless of payment status.
     */
    public double getTotalBilledAmount() {

        String sql = """
                SELECT COALESCE(SUM(total_amount), 0)
                FROM bills
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch total billed amount.", e
            );
        }

        return 0.0;
    }

    /**
     * Returns total amount actually paid.
     */
    public double getTotalPaidAmount() {

        String sql = """
                SELECT COALESCE(SUM(total_amount), 0)
                FROM bills
                WHERE payment_status = 'PAID'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch total paid amount.", e
            );
        }

        return 0.0;
    }

    /**
     * Returns total amount still pending.
     */
    public double getTotalPendingAmount() {

        String sql = """
                SELECT COALESCE(SUM(total_amount), 0)
                FROM bills
                WHERE payment_status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch pending amount.", e
            );
        }

        return 0.0;
    }

    /**
     * Returns total number of medicines.
     */
    public int getTotalMedicines() {

        String sql = "SELECT COUNT(*) FROM medicines";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medicine count.", e
            );
        }

        return 0;
    }

    /**
     * Returns number of medicines currently out of stock.
     */
    public int getOutOfStockMedicines() {

        String sql = """
                SELECT COUNT(*)
                FROM medicines
                WHERE quantity <= 0
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch out-of-stock medicines.", e
            );
        }

        return 0;
    }

    /**
     * Returns number of medicines with available stock.
     */
    public int getAvailableMedicines() {

        String sql = """
                SELECT COUNT(*)
                FROM medicines
                WHERE quantity > 0
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch available medicine count.", e
            );
        }

        return 0;
    }

    /**
     * Returns total prescriptions.
     */
    public int getTotalPrescriptions() {

        String sql = "SELECT COUNT(*) FROM prescriptions";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescription count.", e
            );
        }

        return 0;
    }

    /**
     * Returns prescriptions grouped by status.
     */
    public int getPrescriptionCountByStatus(String status) {

        String sql = """
                SELECT COUNT(*)
                FROM prescriptions
                WHERE status = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch prescription statistics.", e
            );
        }

        return 0;
    }
}