package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentJdbcRepository {

    // -----------------------------------------
    // Find all appointments
    // -----------------------------------------

    public List<Appointment> findAll() {

        List<Appointment> appointments =
                new ArrayList<>();

        String sql = """
                SELECT appointment_id,
                       patient_id,
                       doctor_id,
                       patient_name,
                       doctor_name,
                       appointment_date,
                       appointment_time,
                       reason,
                       urgency,
                       status
                FROM appointments
                ORDER BY appointment_date, appointment_time
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                appointments.add(
                        mapAppointment(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch appointments",
                    e
            );
        }

        return appointments;
    }

    // -----------------------------------------
    // Find appointments by patient
    // -----------------------------------------

    public List<Appointment> findByPatientId(
            int patientId) {

        List<Appointment> appointments =
                new ArrayList<>();

        String sql = """
                SELECT appointment_id,
                       patient_id,
                       doctor_id,
                       patient_name,
                       doctor_name,
                       appointment_date,
                       appointment_time,
                       reason,
                       urgency,
                       status
                FROM appointments
                WHERE patient_id = ?
                ORDER BY appointment_date, appointment_time
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    appointments.add(
                            mapAppointment(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch appointments for patient "
                            + patientId,
                    e
            );
        }

        return appointments;
    }

    // -----------------------------------------
    // Find appointments by doctor
    // -----------------------------------------

    public List<Appointment> findByDoctorId(
            int doctorId) {

        List<Appointment> appointments =
                new ArrayList<>();

        String sql = """
                SELECT appointment_id,
                       patient_id,
                       doctor_id,
                       patient_name,
                       doctor_name,
                       appointment_date,
                       appointment_time,
                       reason,
                       urgency,
                       status
                FROM appointments
                WHERE doctor_id = ?
                ORDER BY appointment_date, appointment_time
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    appointments.add(
                            mapAppointment(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch appointments for doctor "
                            + doctorId,
                    e
            );
        }

        return appointments;
    }

    // -----------------------------------------
    // Find appointments by doctor AND patient
    // -----------------------------------------

    public List<Appointment> findByDoctorAndPatient(
            int doctorId,
            int patientId) {

        List<Appointment> appointments =
                new ArrayList<>();

        String sql = """
                SELECT appointment_id,
                       patient_id,
                       doctor_id,
                       patient_name,
                       doctor_name,
                       appointment_date,
                       appointment_time,
                       reason,
                       urgency,
                       status
                FROM appointments
                WHERE doctor_id = ?
                  AND patient_id = ?
                ORDER BY appointment_date, appointment_time
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

                    appointments.add(
                            mapAppointment(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch appointments for doctor "
                            + doctorId
                            + " and patient "
                            + patientId,
                    e
            );
        }

        return appointments;
    }

    // -----------------------------------------
    // Find appointment by ID
    // -----------------------------------------

    public Appointment findById(
            int appointmentId) {

        String sql = """
                SELECT appointment_id,
                       patient_id,
                       doctor_id,
                       patient_name,
                       doctor_name,
                       appointment_date,
                       appointment_time,
                       reason,
                       urgency,
                       status
                FROM appointments
                WHERE appointment_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, appointmentId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapAppointment(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find appointment "
                            + appointmentId,
                    e
            );
        }

        return null;
    }

    // -----------------------------------------
    // Add appointment
    // -----------------------------------------

    public int add(
            Appointment appointment) {

        String sql = """
                INSERT INTO appointments (
                    patient_id,
                    doctor_id,
                    patient_name,
                    doctor_name,
                    appointment_date,
                    appointment_time,
                    reason,
                    urgency,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                    appointment.getPatientId()
            );

            statement.setInt(
                    2,
                    appointment.getDoctorId()
            );

            statement.setString(
                    3,
                    appointment.getPatientName()
            );

            statement.setString(
                    4,
                    appointment.getDoctorName()
            );

            statement.setDate(
                    5,
                    Date.valueOf(
                            appointment.getAppointmentDate()
                    )
            );

            statement.setTime(
                    6,
                    Time.valueOf(
                            appointment.getAppointmentTime()
                    )
            );

            statement.setString(
                    7,
                    appointment.getReason()
            );

            statement.setString(
                    8,
                    appointment.getUrgency()
            );

            statement.setString(
                    9,
                    appointment.getStatus().name()
            );

            statement.executeUpdate();

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    int generatedId =
                            generatedKeys.getInt(1);

                    appointment.setAppointmentId(
                            generatedId
                    );

                    return generatedId;
                }
            }

            throw new RuntimeException(
                    "Appointment was created, but its generated ID "
                            + "could not be retrieved."
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to add appointment",
                    e
            );
        }
    }

    // -----------------------------------------
    // Update appointment
    // -----------------------------------------

    public void update(
            Appointment appointment) {

        String sql = """
                UPDATE appointments
                SET patient_id = ?,
                    doctor_id = ?,
                    patient_name = ?,
                    doctor_name = ?,
                    appointment_date = ?,
                    appointment_time = ?,
                    reason = ?,
                    urgency = ?,
                    status = ?
                WHERE appointment_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    appointment.getPatientId()
            );

            statement.setInt(
                    2,
                    appointment.getDoctorId()
            );

            statement.setString(
                    3,
                    appointment.getPatientName()
            );

            statement.setString(
                    4,
                    appointment.getDoctorName()
            );

            statement.setDate(
                    5,
                    Date.valueOf(
                            appointment.getAppointmentDate()
                    )
            );

            statement.setTime(
                    6,
                    Time.valueOf(
                            appointment.getAppointmentTime()
                    )
            );

            statement.setString(
                    7,
                    appointment.getReason()
            );

            statement.setString(
                    8,
                    appointment.getUrgency()
            );

            statement.setString(
                    9,
                    appointment.getStatus().name()
            );

            statement.setInt(
                    10,
                    appointment.getAppointmentId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update appointment "
                            + appointment.getAppointmentId(),
                    e
            );
        }
    }

    // -----------------------------------------
    // Check doctor availability
    // -----------------------------------------

    public boolean isDoctorAvailable(
            int doctorId,
            java.time.LocalDate date,
            java.time.LocalTime time
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM appointments
                WHERE doctor_id = ?
                  AND appointment_date = ?
                  AND appointment_time = ?
                  AND status IN ('SCHEDULED', 'CONFIRMED')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    doctorId
            );

            statement.setDate(
                    2,
                    Date.valueOf(date)
            );

            statement.setTime(
                    3,
                    Time.valueOf(time)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getInt(1) == 0;
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to check doctor availability",
                    e
            );
        }

        return false;
    }

    // -----------------------------------------
    // Map ResultSet → Appointment
    // -----------------------------------------

    private Appointment mapAppointment(
            ResultSet resultSet
    ) throws SQLException {

        return new Appointment(
                resultSet.getInt("appointment_id"),
                resultSet.getInt("patient_id"),
                resultSet.getInt("doctor_id"),
                resultSet.getString("patient_name"),
                resultSet.getString("doctor_name"),
                resultSet.getDate(
                        "appointment_date"
                ).toLocalDate(),
                resultSet.getTime(
                        "appointment_time"
                ).toLocalTime(),
                resultSet.getString("reason"),
                resultSet.getString("urgency"),
                AppointmentStatus.valueOf(
                        resultSet.getString("status")
                )
        );
    }
}