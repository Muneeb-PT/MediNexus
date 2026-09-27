package com.medinexus.repository;

import com.medinexus.model.EmergencyStatus;
import com.medinexus.model.EmergencyVehicle;
import com.medinexus.model.VehicleStatus;
import com.medinexus.service.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmergencyVehicleRepository {

    public List<EmergencyVehicle> findAll() {

        String sql = """
                SELECT
                    vehicle_id,
                    vehicle_number,
                    vehicle_type,
                    driver_name,
                    driver_phone,
                    status,
                    current_location
                FROM emergency_vehicles
                ORDER BY vehicle_id
                """;

        List<EmergencyVehicle> vehicles =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                vehicles.add(
                        mapVehicle(resultSet)
                );
            }

            return vehicles;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to load emergency vehicles.",
                    e
            );
        }
    }

    public EmergencyVehicle findById(
            long vehicleId) {

        String sql = """
                SELECT
                    vehicle_id,
                    vehicle_number,
                    vehicle_type,
                    driver_name,
                    driver_phone,
                    status,
                    current_location
                FROM emergency_vehicles
                WHERE vehicle_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    vehicleId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapVehicle(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to find emergency vehicle.",
                    e
            );
        }

        return null;
    }

    public List<EmergencyVehicle>
    findAvailableVehicles() {

        String sql = """
                SELECT
                    vehicle_id,
                    vehicle_number,
                    vehicle_type,
                    driver_name,
                    driver_phone,
                    status,
                    current_location
                FROM emergency_vehicles
                WHERE status = 'AVAILABLE'
                ORDER BY vehicle_id
                """;

        List<EmergencyVehicle> vehicles =
                new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                vehicles.add(
                        mapVehicle(resultSet)
                );
            }

            return vehicles;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to load available emergency vehicles.",
                    e
            );
        }
    }

    public void updateStatus(
            long vehicleId,
            VehicleStatus status,
            String currentLocation) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Vehicle status cannot be null."
            );
        }

        String sql = """
                UPDATE emergency_vehicles
                SET status = ?,
                    current_location = ?
                WHERE vehicle_id = ?
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
                    currentLocation
            );

            statement.setLong(
                    3,
                    vehicleId
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {

                throw new IllegalArgumentException(
                        "Emergency vehicle not found: "
                                + vehicleId
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to update emergency vehicle status.",
                    e
            );
        }
    }

    /**
     * Atomically dispatches an emergency vehicle.
     *
     * This transaction locks both the vehicle and the
     * emergency request before changing either record.
     *
     * This prevents two users from assigning the same
     * available vehicle to different emergencies at
     * the same time.
     */
    public void dispatchVehicleAtomically(
            long emergencyId,
            long vehicleId) {

        String lockVehicleSql = """
                SELECT status
                FROM emergency_vehicles
                WHERE vehicle_id = ?
                FOR UPDATE
                """;

        String lockEmergencySql = """
                SELECT
                    status,
                    assigned_vehicle_id
                FROM emergency_requests
                WHERE emergency_id = ?
                FOR UPDATE
                """;

        String updateVehicleSql = """
                UPDATE emergency_vehicles
                SET status = 'DISPATCHED'
                WHERE vehicle_id = ?
                """;

        String updateEmergencySql = """
                UPDATE emergency_requests
                SET assigned_vehicle_id = ?,
                    status = 'DISPATCHED',
                    dispatched_at = CURRENT_TIMESTAMP
                WHERE emergency_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                /*
                 * ------------------------------------------------
                 * STEP 1
                 * Lock the selected emergency vehicle.
                 * ------------------------------------------------
                 */

                VehicleStatus vehicleStatus;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     lockVehicleSql
                             )) {

                    statement.setLong(
                            1,
                            vehicleId
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException(
                                    "Emergency vehicle not found: "
                                            + vehicleId
                            );
                        }

                        vehicleStatus =
                                VehicleStatus.valueOf(
                                        resultSet
                                                .getString(
                                                        "status"
                                                )
                                                .toUpperCase()
                                );
                    }
                }

                /*
                 * Vehicle must still be available.
                 */

                if (vehicleStatus
                        != VehicleStatus.AVAILABLE) {

                    throw new IllegalStateException(
                            "Selected emergency vehicle is no longer available."
                    );
                }

                /*
                 * ------------------------------------------------
                 * STEP 2
                 * Lock the emergency request.
                 * ------------------------------------------------
                 */

                EmergencyStatus emergencyStatus;

                Long existingVehicleId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     lockEmergencySql
                             )) {

                    statement.setLong(
                            1,
                            emergencyId
                    );

                    try (ResultSet resultSet =
                                 statement.executeQuery()) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException(
                                    "Emergency request not found: "
                                            + emergencyId
                            );
                        }

                        emergencyStatus =
                                EmergencyStatus.valueOf(
                                        resultSet
                                                .getString(
                                                        "status"
                                                )
                                                .toUpperCase()
                                );

                        existingVehicleId =
                                resultSet.getObject(
                                        "assigned_vehicle_id",
                                        Long.class
                                );
                    }
                }

                /*
                 * Emergency must still be REQUESTED.
                 */

                if (emergencyStatus
                        != EmergencyStatus.REQUESTED) {

                    throw new IllegalStateException(
                            "Only REQUESTED emergencies can be dispatched."
                    );
                }

                /*
                 * Emergency must not already have
                 * another vehicle.
                 */

                if (existingVehicleId != null) {

                    throw new IllegalStateException(
                            "Emergency already has a vehicle assigned."
                    );
                }

                /*
                 * ------------------------------------------------
                 * STEP 3
                 * Reserve the vehicle.
                 * ------------------------------------------------
                 */

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updateVehicleSql
                             )) {

                    statement.setLong(
                            1,
                            vehicleId
                    );

                    int affectedRows =
                            statement.executeUpdate();

                    if (affectedRows != 1) {

                        throw new IllegalStateException(
                                "Unable to reserve emergency vehicle."
                        );
                    }
                }

                /*
                 * ------------------------------------------------
                 * STEP 4
                 * Assign the vehicle to the emergency.
                 * ------------------------------------------------
                 */

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updateEmergencySql
                             )) {

                    statement.setLong(
                            1,
                            vehicleId
                    );

                    statement.setLong(
                            2,
                            emergencyId
                    );

                    int affectedRows =
                            statement.executeUpdate();

                    if (affectedRows != 1) {

                        throw new IllegalStateException(
                                "Unable to assign emergency vehicle."
                        );
                    }
                }

                /*
                 * ------------------------------------------------
                 * STEP 5
                 * Commit both changes together.
                 * ------------------------------------------------
                 */

                connection.commit();

            } catch (Exception e) {

                /*
                 * If anything fails, undo BOTH database changes.
                 */

                try {
                    connection.rollback();

                } catch (SQLException rollbackException) {

                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to dispatch emergency vehicle atomically.",
                    e
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Emergency dispatch failed.",
                    e
            );
        }
    }

    private EmergencyVehicle mapVehicle(
            ResultSet resultSet)
            throws SQLException {

        VehicleStatus status =
                VehicleStatus.valueOf(
                        resultSet
                                .getString("status")
                                .toUpperCase()
                );

        return new EmergencyVehicle(
                resultSet.getLong("vehicle_id"),
                resultSet.getString("vehicle_number"),
                resultSet.getString("vehicle_type"),
                resultSet.getString("driver_name"),
                resultSet.getString("driver_phone"),
                status,
                resultSet.getString("current_location")
        );
    }
}