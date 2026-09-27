package com.medinexus.repository;

import com.medinexus.model.SupportTicket;
import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Role;
import com.medinexus.model.User;



import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupportTicketRepository {

    public SupportTicket add(SupportTicket ticket) {

        String sql = """
                INSERT INTO support_tickets
                (
                    user_id,
                    type,
                    category,
                    subject,
                    description,
                    priority,
                    status,
                    assigned_to
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setLong(1, ticket.getUserId());
            statement.setString(2, ticket.getType());
            statement.setString(3, ticket.getCategory());
            statement.setString(4, ticket.getSubject());
            statement.setString(5, ticket.getDescription());
            statement.setString(6, ticket.getPriority());
            statement.setString(7, ticket.getStatus());

            if (ticket.getAssignedTo() == null) {
                statement.setNull(8, Types.BIGINT);
            } else {
                statement.setLong(8, ticket.getAssignedTo());
            }

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    ticket.setTicketId(keys.getLong(1));
                } else {
                    throw new SQLException(
                            "Failed to retrieve generated support ticket ID."
                    );
                }
            }

            return ticket;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to create support ticket.",
                    e
            );
        }
    }

    public List<SupportTicket> findByUserId(long userId) {

        String sql = """
                SELECT
                    ticket_id,
                    user_id,
                    type,
                    category,
                    subject,
                    description,
                    priority,
                    status,
                    assigned_to,
                    created_at,
                    updated_at,
                    resolved_at
                FROM support_tickets
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        List<SupportTicket> tickets = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    tickets.add(mapRow(resultSet));
                }
            }

            return tickets;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve support tickets.",
                    e
            );
        }
    }

    public List<SupportTicket> findAll() {

        String sql = """
                SELECT
                    ticket_id,
                    user_id,
                    type,
                    category,
                    subject,
                    description,
                    priority,
                    status,
                    assigned_to,
                    created_at,
                    updated_at,
                    resolved_at
                FROM support_tickets
                ORDER BY created_at DESC
                """;

        List<SupportTicket> tickets = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                tickets.add(mapRow(resultSet));
            }

            return tickets;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve support tickets.",
                    e
            );
        }
    }

    public SupportTicket findById(long ticketId) {

        String sql = """
                SELECT
                    ticket_id,
                    user_id,
                    type,
                    category,
                    subject,
                    description,
                    priority,
                    status,
                    assigned_to,
                    created_at,
                    updated_at,
                    resolved_at
                FROM support_tickets
                WHERE ticket_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to retrieve support ticket.",
                    e
            );
        }
    }

    public void updateStatus(
            long ticketId,
            String status) {

        String sql;

        if ("RESOLVED".equals(status)) {

            sql = """
                    UPDATE support_tickets
                    SET status = ?,
                        resolved_at = CURRENT_TIMESTAMP
                    WHERE ticket_id = ?
                    """;

        } else {

            sql = """
                    UPDATE support_tickets
                    SET status = ?,
                        resolved_at = NULL
                    WHERE ticket_id = ?
                    """;
        }

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, status);
            statement.setLong(2, ticketId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update support ticket status.",
                    e
            );
        }
    }

    public void assignTicket(
            long ticketId,
            long assignedTo) {

        String sql = """
                UPDATE support_tickets
                SET assigned_to = ?
                WHERE ticket_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, assignedTo);
            statement.setLong(2, ticketId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to assign support ticket.",
                    e
            );
        }
    }

    private SupportTicket mapRow(ResultSet resultSet)
            throws SQLException {

        long assignedTo = resultSet.getLong("assigned_to");

        Long assignedUserId =
                resultSet.wasNull()
                        ? null
                        : assignedTo;

        Timestamp createdTimestamp =
                resultSet.getTimestamp("created_at");

        Timestamp updatedTimestamp =
                resultSet.getTimestamp("updated_at");

        Timestamp resolvedTimestamp =
                resultSet.getTimestamp("resolved_at");

        return new SupportTicket(
                resultSet.getLong("ticket_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("type"),
                resultSet.getString("category"),
                resultSet.getString("subject"),
                resultSet.getString("description"),
                resultSet.getString("priority"),
                resultSet.getString("status"),
                assignedUserId,
                createdTimestamp == null
                        ? null
                        : createdTimestamp.toLocalDateTime(),
                updatedTimestamp == null
                        ? null
                        : updatedTimestamp.toLocalDateTime(),
                resolvedTimestamp == null
                        ? null
                        : resolvedTimestamp.toLocalDateTime()
        );
    }

    public List<User> findSupportStaff() {

        List<User> staff = new ArrayList<>();

        String sql = """
            SELECT user_id, username, full_name, role
            FROM users
            WHERE role IN ('ADMIN', 'RECEPTIONIST')
            ORDER BY role, full_name
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Role role =
                        Role.valueOf(
                                resultSet.getString("role")
                        );

                User user = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("full_name"),
                        role
                );

                staff.add(user);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to load support staff.",
                    e
            );
        }

        return staff;
    }

}