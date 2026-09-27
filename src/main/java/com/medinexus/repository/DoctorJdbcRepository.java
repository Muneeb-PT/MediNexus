package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorJdbcRepository {

    // =========================================================
    // FIND ALL DOCTORS
    // =========================================================

    public List<Doctor> findAll() {

        List<Doctor> doctors = new ArrayList<>();

        String sql = """
                SELECT
                    doctor_id,
                    user_id,
                    name,
                    specialization,
                    phone,
                    email,
                    available
                FROM doctors
                ORDER BY doctor_id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                doctors.add(mapDoctor(resultSet));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load doctors from MySQL",
                    e
            );
        }

        return doctors;
    }


    // =========================================================
    // FIND DOCTOR BY ID
    // =========================================================

    public Doctor findById(int doctorId) {

        String sql = """
                SELECT
                    doctor_id,
                    user_id,
                    name,
                    specialization,
                    phone,
                    email,
                    available
                FROM doctors
                WHERE doctor_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, doctorId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapDoctor(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find doctor with ID " + doctorId,
                    e
            );
        }

        return null;
    }


    // =========================================================
    // FIND DOCTOR BY USER ID
    // =========================================================

    public Doctor findByUserId(int userId) {

        String sql = """
                SELECT
                    doctor_id,
                    user_id,
                    name,
                    specialization,
                    phone,
                    email,
                    available
                FROM doctors
                WHERE user_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapDoctor(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to find doctor for user ID " + userId,
                    e
            );
        }

        return null;
    }


    // =========================================================
    // ADD DOCTOR
    // =========================================================

    public void add(Doctor doctor) {

        String checkDoctorSql = """
                SELECT COUNT(*)
                FROM doctors
                WHERE doctor_id = ?
                """;

        String nextUserIdSql = """
                SELECT COALESCE(MAX(user_id), 0) + 1
                FROM users
                """;

        String insertUserSql = """
                INSERT INTO users
                    (user_id, username, full_name, role)
                VALUES
                    (?, ?, ?, 'DOCTOR')
                """;

        String insertDoctorSql = """
                INSERT INTO doctors
                    (
                        doctor_id,
                        user_id,
                        name,
                        specialization,
                        phone,
                        email,
                        available
                    )
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            // -------------------------------------------------
            // Check doctor ID
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(checkDoctorSql)) {

                statement.setInt(1, doctor.getDoctorId());

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (resultSet.next()
                            && resultSet.getInt(1) > 0) {

                        throw new RuntimeException(
                                "Doctor ID "
                                        + doctor.getDoctorId()
                                        + " already exists."
                        );
                    }
                }
            }


            // -------------------------------------------------
            // Generate user ID
            // -------------------------------------------------

            int userId;

            try (PreparedStatement statement =
                         connection.prepareStatement(nextUserIdSql);
                 ResultSet resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                userId = resultSet.getInt(1);
            }


            // -------------------------------------------------
            // Create doctor user account
            // -------------------------------------------------

            String username =
                    "doctor" + doctor.getDoctorId();

            try (PreparedStatement statement =
                         connection.prepareStatement(insertUserSql)) {

                statement.setInt(1, userId);
                statement.setString(2, username);
                statement.setString(3, doctor.getName());

                statement.executeUpdate();
            }


            // -------------------------------------------------
            // Create doctor record
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(insertDoctorSql)) {

                statement.setInt(1, doctor.getDoctorId());
                statement.setInt(2, userId);
                statement.setString(3, doctor.getName());
                statement.setString(
                        4,
                        doctor.getSpecialization()
                );
                statement.setString(5, doctor.getPhone());
                statement.setString(6, doctor.getEmail());
                statement.setBoolean(
                        7,
                        doctor.isAvailable()
                );

                statement.executeUpdate();
            }

            connection.commit();

            // Store generated user ID in object
            doctor.setUserId(userId);

        } catch (SQLException e) {

            rollback(connection);

            throw new RuntimeException(
                    "Failed to add doctor",
                    e
            );

        } catch (RuntimeException e) {

            rollback(connection);

            throw e;

        } finally {

            closeConnection(connection);
        }
    }


    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public void update(Doctor doctor) {

        String doctorSql = """
                UPDATE doctors
                SET
                    name = ?,
                    specialization = ?,
                    phone = ?,
                    email = ?,
                    available = ?
                WHERE doctor_id = ?
                """;

        String userSql = """
                UPDATE users
                SET
                    full_name = ?
                WHERE user_id = ?
                """;

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            // -------------------------------------------------
            // Update doctor table
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(doctorSql)) {

                statement.setString(1, doctor.getName());
                statement.setString(
                        2,
                        doctor.getSpecialization()
                );
                statement.setString(3, doctor.getPhone());
                statement.setString(4, doctor.getEmail());
                statement.setBoolean(
                        5,
                        doctor.isAvailable()
                );
                statement.setInt(
                        6,
                        doctor.getDoctorId()
                );

                int affectedRows =
                        statement.executeUpdate();

                if (affectedRows == 0) {

                    throw new RuntimeException(
                            "Doctor ID "
                                    + doctor.getDoctorId()
                                    + " was not found."
                    );
                }
            }


            // -------------------------------------------------
            // Update linked user
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(userSql)) {

                statement.setString(
                        1,
                        doctor.getName()
                );

                statement.setInt(
                        2,
                        doctor.getUserId()
                );

                statement.executeUpdate();
            }

            connection.commit();

        } catch (SQLException e) {

            rollback(connection);

            throw new RuntimeException(
                    "Failed to update doctor",
                    e
            );

        } catch (RuntimeException e) {

            rollback(connection);

            throw e;

        } finally {

            closeConnection(connection);
        }
    }


    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    public void delete(int doctorId) {

        String findUserSql = """
                SELECT user_id
                FROM doctors
                WHERE doctor_id = ?
                """;

        String deleteDoctorSql = """
                DELETE FROM doctors
                WHERE doctor_id = ?
                """;

        String deleteUserSql = """
                DELETE FROM users
                WHERE user_id = ?
                """;

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();

            connection.setAutoCommit(false);

            int userId = -1;


            // -------------------------------------------------
            // Find linked user
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(findUserSql)) {

                statement.setInt(1, doctorId);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (resultSet.next()) {

                        userId =
                                resultSet.getInt("user_id");

                    } else {

                        throw new RuntimeException(
                                "Doctor ID "
                                        + doctorId
                                        + " was not found."
                        );
                    }
                }
            }


            // -------------------------------------------------
            // Delete doctor
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 deleteDoctorSql
                         )) {

                statement.setInt(1, doctorId);

                statement.executeUpdate();
            }


            // -------------------------------------------------
            // Delete linked user
            // -------------------------------------------------

            if (userId != -1) {

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     deleteUserSql
                             )) {

                    statement.setInt(1, userId);

                    statement.executeUpdate();
                }
            }

            connection.commit();

        } catch (SQLException e) {

            rollback(connection);

            throw new RuntimeException(
                    "Failed to delete doctor. "
                            + "The doctor may be referenced by "
                            + "appointments or other records.",
                    e
            );

        } catch (RuntimeException e) {

            rollback(connection);

            throw e;

        } finally {

            closeConnection(connection);
        }
    }


    // =========================================================
    // TOGGLE AVAILABILITY
    // =========================================================

    public void setAvailability(
            int doctorId,
            boolean available) {

        String sql = """
                UPDATE doctors
                SET available = ?
                WHERE doctor_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(1, available);
            statement.setInt(2, doctorId);

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {

                throw new RuntimeException(
                        "Doctor ID "
                                + doctorId
                                + " was not found."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update doctor availability",
                    e
            );
        }
    }


    // =========================================================
    // MAP RESULT SET
    // =========================================================

    private Doctor mapDoctor(
            ResultSet resultSet)
            throws SQLException {

        return new Doctor(
                resultSet.getInt("doctor_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("name"),
                resultSet.getString("specialization"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getBoolean("available")
        );
    }


    // =========================================================
    // ROLLBACK
    // =========================================================

    private void rollback(
            Connection connection) {

        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }


    // =========================================================
    // CLOSE CONNECTION
    // =========================================================

    private void closeConnection(
            Connection connection) {

        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}