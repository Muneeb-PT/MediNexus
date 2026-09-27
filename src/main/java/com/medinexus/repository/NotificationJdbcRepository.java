package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationJdbcRepository {

    // -----------------------------------------
    // Find all notifications
    // -----------------------------------------

    public List<Notification> findAll() {

        List<Notification> notifications =
                new ArrayList<>();

        String sql = """
                SELECT notification_id,
                       user_id,
                       title,
                       message,
                       type,
                       is_read,
                       created_at
                FROM notifications
                ORDER BY created_at DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                notifications.add(
                        mapNotification(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch notifications.",
                    e
            );
        }

        return notifications;
    }

    // -----------------------------------------
    // Find notifications by user ID
    // -----------------------------------------

    public List<Notification> findByUserId(
            long userId) {

        List<Notification> notifications =
                new ArrayList<>();

        String sql = """
                SELECT notification_id,
                       user_id,
                       title,
                       message,
                       type,
                       is_read,
                       created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, userId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    notifications.add(
                            mapNotification(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch user notifications.",
                    e
            );
        }

        return notifications;
    }

    // -----------------------------------------
    // Find notification by ID
    // -----------------------------------------

    public Notification findById(
            int notificationId) {

        String sql = """
                SELECT notification_id,
                       user_id,
                       title,
                       message,
                       type,
                       is_read,
                       created_at
                FROM notifications
                WHERE notification_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    notificationId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    return mapNotification(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch notification.",
                    e
            );
        }

        return null;
    }

    // -----------------------------------------
    // Add notification
    // -----------------------------------------

    public void add(
            Notification notification) {

        if (notification == null) {

            throw new IllegalArgumentException(
                    "Notification cannot be null."
            );
        }

        String sql = """
                INSERT INTO notifications
                (
                    notification_id,
                    user_id,
                    title,
                    message,
                    type,
                    is_read,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    notification.getNotificationId()
            );

            statement.setLong(
                    2,
                    notification.getUserId()
            );

            statement.setString(
                    3,
                    notification.getTitle()
            );

            statement.setString(
                    4,
                    notification.getMessage()
            );

            statement.setString(
                    5,
                    notification.getType()
            );

            statement.setBoolean(
                    6,
                    notification.isRead()
            );

            if (notification.getCreatedAt() != null) {

                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                notification.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        7,
                        Timestamp.valueOf(
                                java.time.LocalDateTime.now()
                        )
                );
            }

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to add notification.",
                    e
            );
        }
    }

    // -----------------------------------------
    // Update notification
    // -----------------------------------------

    public void update(
            Notification notification) {

        if (notification == null) {

            throw new IllegalArgumentException(
                    "Notification cannot be null."
            );
        }

        String sql = """
                UPDATE notifications
                SET user_id = ?,
                    title = ?,
                    message = ?,
                    type = ?,
                    is_read = ?,
                    created_at = ?
                WHERE notification_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    notification.getUserId()
            );

            statement.setString(
                    2,
                    notification.getTitle()
            );

            statement.setString(
                    3,
                    notification.getMessage()
            );

            statement.setString(
                    4,
                    notification.getType()
            );

            statement.setBoolean(
                    5,
                    notification.isRead()
            );

            if (notification.getCreatedAt() != null) {

                statement.setTimestamp(
                        6,
                        Timestamp.valueOf(
                                notification.getCreatedAt()
                        )
                );

            } else {

                statement.setTimestamp(
                        6,
                        Timestamp.valueOf(
                                java.time.LocalDateTime.now()
                        )
                );
            }

            statement.setInt(
                    7,
                    notification.getNotificationId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update notification.",
                    e
            );
        }
    }

    // -----------------------------------------
    // Delete notification
    // -----------------------------------------

    public void delete(
            int notificationId) {

        String sql = """
                DELETE FROM notifications
                WHERE notification_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    notificationId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete notification.",
                    e
            );
        }
    }

    // -----------------------------------------
    // Map ResultSet → Notification
    // -----------------------------------------

    private Notification mapNotification(
            ResultSet resultSet)
            throws SQLException {

        Notification notification =
                new Notification();

        notification.setNotificationId(
                resultSet.getInt(
                        "notification_id"
                )
        );

        notification.setUserId(
                resultSet.getLong(
                        "user_id"
                )
        );

        notification.setTitle(
                resultSet.getString(
                        "title"
                )
        );

        notification.setMessage(
                resultSet.getString(
                        "message"
                )
        );

        notification.setType(
                resultSet.getString(
                        "type"
                )
        );

        notification.setRead(
                resultSet.getBoolean(
                        "is_read"
                )
        );

        Timestamp createdAt =
                resultSet.getTimestamp(
                        "created_at"
                );

        if (createdAt != null) {

            notification.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        return notification;
    }
}