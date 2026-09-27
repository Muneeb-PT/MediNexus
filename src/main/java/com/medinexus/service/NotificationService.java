package com.medinexus.service;

import com.medinexus.model.Notification;
import com.medinexus.repository.NotificationJdbcRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {

    private final NotificationJdbcRepository repository =
            new NotificationJdbcRepository();

    // -----------------------------------------
    // Get all notifications
    // -----------------------------------------

    public List<Notification> getAllNotifications() {

        return repository.findAll();
    }

    // -----------------------------------------
    // Get notifications for a user
    // -----------------------------------------

    public List<Notification> getUserNotifications(
            long userId) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        return repository.findByUserId(userId);
    }

    // -----------------------------------------
    // Get notification by ID
    // -----------------------------------------

    public Notification getNotificationById(
            int notificationId) {

        return repository.findById(notificationId);
    }

    // -----------------------------------------
    // Create notification
    // -----------------------------------------

    public Notification createNotification(
            long userId,
            String title,
            String message,
            String type) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        if (title == null || title.isBlank()) {

            throw new IllegalArgumentException(
                    "Notification title cannot be empty."
            );
        }

        if (message == null || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Notification message cannot be empty."
            );
        }

        Notification notification =
                new Notification(
                        generateNotificationId(),
                        (int) userId,
                        title,
                        message,
                        type,
                        LocalDateTime.now()
                );

        notification.setRead(false);

        repository.add(notification);

        return notification;
    }

    // -----------------------------------------
    // Mark notification as read
    // -----------------------------------------

    public void markAsRead(int notificationId) {

        Notification notification =
                repository.findById(notificationId);

        if (notification == null) {

            throw new IllegalArgumentException(
                    "Notification not found."
            );
        }

        notification.setRead(true);

        repository.update(notification);
    }

    // -----------------------------------------
    // Mark all notifications as read
    // -----------------------------------------

    public void markAllAsRead(long userId) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        List<Notification> notifications =
                repository.findByUserId(userId);

        for (Notification notification : notifications) {

            if (!notification.isRead()) {

                notification.setRead(true);

                repository.update(notification);
            }
        }
    }

    // -----------------------------------------
    // Get unread notification count
    // -----------------------------------------

    public int getUnreadCount(long userId) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        return (int) repository.findByUserId(userId)
                .stream()
                .filter(notification -> !notification.isRead())
                .count();
    }

    // -----------------------------------------
    // Generate notification ID
    // -----------------------------------------

    public int generateNotificationId() {

        return repository.findAll()
                .stream()
                .mapToInt(
                        Notification::getNotificationId
                )
                .max()
                .orElse(0) + 1;
    }

    // -----------------------------------------
    // Delete notification
    // -----------------------------------------

    public void deleteNotification(
            int notificationId) {

        repository.delete(notificationId);
    }
}
