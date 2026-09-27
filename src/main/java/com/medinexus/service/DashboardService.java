package com.medinexus.service;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Role;
import com.medinexus.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.medinexus.model.InventoryAlert;
import com.medinexus.service.SessionManager;
import com.medinexus.service.PharmacyInventoryService;

public class DashboardService {

    // =========================================================
    // BASIC COUNTS
    // =========================================================

    public int getPatientCount() {

        User user = requireLoggedInUser();

        return switch (user.getRole()) {

            case ADMIN, RECEPTIONIST ->
                    getCount(
                            "SELECT COUNT(*) FROM patients"
                    );

            case DOCTOR ->
                    getCountWithParameter(
                            """
                            SELECT COUNT(DISTINCT patient_id)
                            FROM appointments
                            WHERE doctor_id = ?
                            """,
                            user.getUserId()
                    );

            case PATIENT -> 1;
        };
    }


    public int getDoctorCount() {

        User user = requireLoggedInUser();

        return switch (user.getRole()) {

            case ADMIN, RECEPTIONIST ->
                    getCount(
                            "SELECT COUNT(*) FROM doctors"
                    );

            case DOCTOR -> 1;

            case PATIENT -> 0;
        };
    }


    public int getAppointmentCount() {

        User user = requireLoggedInUser();

        return switch (user.getRole()) {

            case ADMIN, RECEPTIONIST ->
                    getCount(
                            "SELECT COUNT(*) FROM appointments"
                    );

            case DOCTOR ->
                    getCountWithParameter(
                            """
                            SELECT COUNT(*)
                            FROM appointments
                            WHERE doctor_id = ?
                            """,
                            user.getUserId()
                    );

            case PATIENT -> {

                int patientId =
                        getPatientIdForUser(
                                user.getUserId()
                        );

                if (patientId <= 0) {
                    yield 0;
                }

                yield getCountWithParameter(
                        """
                        SELECT COUNT(*)
                        FROM appointments
                        WHERE patient_id = ?
                        """,
                        patientId
                );
            }
        };
    }


    public int getMedicineCount() {

        User user = requireLoggedInUser();

        return switch (user.getRole()) {

            case ADMIN, RECEPTIONIST ->
                    getCount(
                            "SELECT COUNT(*) FROM medicines"
                    );

            case DOCTOR, PATIENT -> 0;
        };
    }


    // =========================================================
    // MEDICINE STOCK
    // =========================================================

    public int getLowStockCount() {

        User user = requireLoggedInUser();

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            return 0;
        }

        return getCount(
                """
                SELECT COUNT(*)
                FROM medicines
                WHERE quantity > 0
                  AND quantity <= 10
                """
        );
    }


    public int getOutOfStockCount() {

        User user = requireLoggedInUser();

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            return 0;
        }

        return getCount(
                """
                SELECT COUNT(*)
                FROM medicines
                WHERE quantity <= 0
                """
        );
    }


    // =========================================================
    // RECENT APPOINTMENTS
    // =========================================================

    /*
     * The parameters are retained because DashboardController
     * already calls this method with role and user ID.
     *
     * Security does NOT trust those parameters.
     * The authenticated SessionManager user is used instead.
     */
    public List<DashboardAppointment> getRecentAppointments(
            Role ignoredRole,
            long ignoredUserId) {

        User currentUser =
                requireLoggedInUser();

        Role role =
                currentUser.getRole();

        long userId =
                currentUser.getUserId();

        List<DashboardAppointment> appointments =
                new ArrayList<>();

        String sql;

        if (role == Role.PATIENT) {

            int patientId =
                    getPatientIdForUser(
                            (int) userId
                    );

            if (patientId <= 0) {
                return appointments;
            }

            sql = """
                    SELECT appointment_id,
                           patient_id,
                           doctor_id,
                           appointment_date,
                           appointment_time,
                           urgency,
                           status
                    FROM appointments
                    WHERE patient_id = ?
                    ORDER BY appointment_date DESC,
                             appointment_time DESC
                    LIMIT 5
                    """;

        } else if (role == Role.DOCTOR) {

            sql = """
                    SELECT appointment_id,
                           patient_id,
                           doctor_id,
                           appointment_date,
                           appointment_time,
                           urgency,
                           status
                    FROM appointments
                    WHERE doctor_id = ?
                    ORDER BY appointment_date DESC,
                             appointment_time DESC
                    LIMIT 5
                    """;

        } else if (role == Role.ADMIN
                || role == Role.RECEPTIONIST) {

            sql = """
                    SELECT appointment_id,
                           patient_id,
                           doctor_id,
                           appointment_date,
                           appointment_time,
                           urgency,
                           status
                    FROM appointments
                    ORDER BY appointment_date DESC,
                             appointment_time DESC
                    LIMIT 5
                    """;

        } else {

            throw new SecurityException(
                    "Unsupported dashboard role."
            );
        }

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            if (role == Role.PATIENT) {

                int patientId =
                        getPatientIdForUser(
                                (int) userId
                        );

                statement.setInt(
                        1,
                        patientId
                );

            } else if (role == Role.DOCTOR) {

                statement.setLong(
                        1,
                        userId
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    int appointmentId =
                            resultSet.getInt(
                                    "appointment_id"
                            );

                    long patientId =
                            resultSet.getLong(
                                    "patient_id"
                            );

                    long doctorId =
                            resultSet.getLong(
                                    "doctor_id"
                            );

                    LocalDate appointmentDate =
                            resultSet.getDate(
                                    "appointment_date"
                            ).toLocalDate();

                    LocalTime appointmentTime =
                            resultSet.getTime(
                                    "appointment_time"
                            ).toLocalTime();

                    String urgency =
                            resultSet.getString(
                                    "urgency"
                            );

                    String status =
                            resultSet.getString(
                                    "status"
                            );

                    appointments.add(
                            new DashboardAppointment(
                                    appointmentId,
                                    patientId,
                                    doctorId,
                                    appointmentDate,
                                    appointmentTime,
                                    urgency,
                                    status
                            )
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to load dashboard appointments.",
                    e
            );
        }

        return appointments;
    }


    // =========================================================
    // FIND PATIENT ID FROM USER ID
    // =========================================================

    private int getPatientIdForUser(
            long userId) {

        String sql = """
                SELECT patient_id
                FROM patients
                WHERE user_id = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    userId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getInt(
                            "patient_id"
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to find patient for user.",
                    e
            );
        }

        return 0;
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User requireLoggedInUser() {

        if (!SessionManager.isLoggedIn()) {

            throw new SecurityException(
                    "No user is currently logged in."
            );
        }

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {

            throw new SecurityException(
                    "No authenticated user found."
            );
        }

        return user;
    }


    // =========================================================
    // GENERIC COUNT
    // =========================================================

    private int getCount(
            String sql) {

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                return resultSet.getInt(1);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to load dashboard count.",
                    e
            );
        }

        return 0;
    }


    // =========================================================
    // PARAMETERIZED COUNT
    // =========================================================

    private int getCountWithParameter(
            String sql,
            long parameter) {

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    parameter
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to load dashboard count.",
                    e
            );
        }

        return 0;
    }


    // =========================================================
    // DASHBOARD APPOINTMENT MODEL
    // =========================================================

    public static class DashboardAppointment {

        private final int appointmentId;
        private final long patientId;
        private final long doctorId;
        private final LocalDate appointmentDate;
        private final LocalTime appointmentTime;
        private final String urgency;
        private final String status;

        public DashboardAppointment(
                int appointmentId,
                long patientId,
                long doctorId,
                LocalDate appointmentDate,
                LocalTime appointmentTime,
                String urgency,
                String status) {

            this.appointmentId =
                    appointmentId;

            this.patientId =
                    patientId;

            this.doctorId =
                    doctorId;

            this.appointmentDate =
                    appointmentDate;

            this.appointmentTime =
                    appointmentTime;

            this.urgency =
                    urgency;

            this.status =
                    status;
        }

        public int getAppointmentId() {
            return appointmentId;
        }

        public long getPatientId() {
            return patientId;
        }

        public long getDoctorId() {
            return doctorId;
        }

        public LocalDate getAppointmentDate() {
            return appointmentDate;
        }

        public LocalTime getAppointmentTime() {
            return appointmentTime;
        }

        public String getUrgency() {
            return urgency;
        }

        public String getStatus() {
            return status;
        }
    }
    public int getInventoryAlertCount() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "User must be logged in."
            );
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            return 0;
        }

        PharmacyInventoryService pharmacyInventoryService =
                new PharmacyInventoryService();

        return pharmacyInventoryService
                .getInventoryAlerts()
                .size();
    }
    public int getOutOfStockAlertCount() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "User must be logged in."
            );
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            return 0;
        }

        PharmacyInventoryService pharmacyInventoryService =
                new PharmacyInventoryService();

        return (int) pharmacyInventoryService
                .getInventoryAlerts()
                .stream()
                .filter(InventoryAlert::isOutOfStock)
                .count();
    }
    public int getLowStockAlertCount() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "User must be logged in."
            );
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            return 0;
        }

        PharmacyInventoryService pharmacyInventoryService =
                new PharmacyInventoryService();

        return (int) pharmacyInventoryService
                .getInventoryAlerts()
                .stream()
                .filter(InventoryAlert::isLowStock)
                .count();
    }
}