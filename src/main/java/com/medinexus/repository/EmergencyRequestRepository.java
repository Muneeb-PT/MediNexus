package com.medinexus.repository;

import com.medinexus.model.EmergencyRequest;
import com.medinexus.model.EmergencyStatus;
import com.medinexus.service.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EmergencyRequestRepository {

    public long create(
            EmergencyRequest request) {

        String sql = """
                INSERT INTO emergency_requests (
                    patient_id,
                    requested_by,
                    emergency_type,
                    description,
                    severity,
                    pickup_location,
                    destination,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             java.sql.Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(
                    1,
                    request.getPatientId()
            );

            statement.setInt(
                    2,
                    request.getRequestedBy()
            );

            statement.setString(
                    3,
                    request.getEmergencyType()
            );

            statement.setString(
                    4,
                    request.getDescription()
            );

            statement.setString(
                    5,
                    request.getSeverity()
            );

            statement.setString(
                    6,
                    request.getPickupLocation()
            );

            statement.setString(
                    7,
                    request.getDestination()
            );

            statement.setString(
                    8,
                    request.getStatus().name()
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "Emergency request was not created."
                );
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new IllegalStateException(
                    "Emergency request ID was not generated."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to create emergency request.",
                    e
            );
        }
    }

    public EmergencyRequest findById(
            long emergencyId) {

        String sql = """
                SELECT
                    emergency_id,
                    patient_id,
                    requested_by,
                    emergency_type,
                    description,
                    severity,
                    pickup_location,
                    destination,
                    status,
                    requested_at,
                    dispatched_at,
                    arrived_at,
                    completed_at,
                    assigned_vehicle_id
                FROM emergency_requests
                WHERE emergency_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    emergencyId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRequest(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to find emergency request.",
                    e
            );
        }

        return null;
    }

    public List<EmergencyRequest> findAll() {

        String sql = """
                SELECT
                    emergency_id,
                    patient_id,
                    requested_by,
                    emergency_type,
                    description,
                    severity,
                    pickup_location,
                    destination,
                    status,
                    requested_at,
                    dispatched_at,
                    arrived_at,
                    completed_at,
                    assigned_vehicle_id
                FROM emergency_requests
                ORDER BY requested_at DESC
                """;

        List<EmergencyRequest> requests =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                requests.add(
                        mapRequest(resultSet)
                );
            }

            return requests;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to load emergency requests.",
                    e
            );
        }
    }

    public List<EmergencyRequest> findByPatientId(
            int patientId) {

        String sql = """
                SELECT
                    emergency_id,
                    patient_id,
                    requested_by,
                    emergency_type,
                    description,
                    severity,
                    pickup_location,
                    destination,
                    status,
                    requested_at,
                    dispatched_at,
                    arrived_at,
                    completed_at,
                    assigned_vehicle_id
                FROM emergency_requests
                WHERE patient_id = ?
                ORDER BY requested_at DESC
                """;

        List<EmergencyRequest> requests =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    patientId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    requests.add(
                            mapRequest(resultSet)
                    );
                }
            }

            return requests;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to load patient emergency requests.",
                    e
            );
        }
    }

    public void updateStatus(
            long emergencyId,
            EmergencyStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Emergency status cannot be null."
            );
        }

        String sql = """
                UPDATE emergency_requests
                SET status = ?,
                    arrived_at =
                        CASE
                            WHEN ? = 'ON_SCENE'
                            THEN CURRENT_TIMESTAMP
                            ELSE arrived_at
                        END,
                    completed_at =
                        CASE
                            WHEN ? = 'COMPLETED'
                            THEN CURRENT_TIMESTAMP
                            ELSE completed_at
                        END
                WHERE emergency_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    status.name()
            );

            statement.setString(
                    2,
                    status.name()
            );

            statement.setString(
                    3,
                    status.name()
            );

            statement.setLong(
                    4,
                    emergencyId
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalArgumentException(
                        "Emergency request not found: "
                                + emergencyId
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to update emergency request status.",
                    e
            );
        }
    }

    private EmergencyRequest mapRequest(
            ResultSet resultSet)
            throws SQLException {

        Timestamp requestedAt =
                resultSet.getTimestamp(
                        "requested_at"
                );

        Timestamp dispatchedAt =
                resultSet.getTimestamp(
                        "dispatched_at"
                );

        Timestamp arrivedAt =
                resultSet.getTimestamp(
                        "arrived_at"
                );

        Timestamp completedAt =
                resultSet.getTimestamp(
                        "completed_at"
                );

        String statusText =
                resultSet.getString("status");

        EmergencyStatus status =
                EmergencyStatus.valueOf(
                        statusText.toUpperCase()
                );

        Long vehicleId =
                resultSet.getObject(
                        "assigned_vehicle_id",
                        Long.class
                );

        return new EmergencyRequest(
                resultSet.getLong("emergency_id"),
                resultSet.getInt("patient_id"),
                resultSet.getInt("requested_by"),
                resultSet.getString("emergency_type"),
                resultSet.getString("description"),
                resultSet.getString("severity"),
                resultSet.getString("pickup_location"),
                resultSet.getString("destination"),
                status,
                requestedAt != null
                        ? requestedAt.toLocalDateTime()
                        : null,
                dispatchedAt != null
                        ? dispatchedAt.toLocalDateTime()
                        : null,
                arrivedAt != null
                        ? arrivedAt.toLocalDateTime()
                        : null,
                completedAt != null
                        ? completedAt.toLocalDateTime()
                        : null,
                vehicleId
        );
    }
}