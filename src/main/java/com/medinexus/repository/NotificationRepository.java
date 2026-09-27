package com.medinexus.repository;

import com.medinexus.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final List<Notification> notifications =
            new ArrayList<>();

    // -----------------------------------------
    // Find all notifications
    // -----------------------------------------

    public List<Notification> findAll() {

        return new ArrayList<>(notifications);
    }

    // -----------------------------------------
    // Find notifications by user ID
    // -----------------------------------------

    public List<Notification> findByUserId(
            long userId) {

        List<Notification> result =
                new ArrayList<>();

        for (Notification notification :
                notifications) {

            if (notification.getUserId()
                    == userId) {

                result.add(notification);
            }
        }

        return result;
    }

    // -----------------------------------------
    // Find notification by ID
    // -----------------------------------------

    public Notification findById(
            int notificationId) {

        for (Notification notification :
                notifications) {

            if (notification.getNotificationId()
                    == notificationId) {

                return notification;
            }
        }

        return null;
    }

    // -----------------------------------------
    // Add notification
    // -----------------------------------------

    public void add(
            Notification notification) {

        if (notification != null) {
            notifications.add(notification);
        }
    }

    // -----------------------------------------
    // Update notification
    // -----------------------------------------

    public void update(
            Notification updatedNotification) {

        if (updatedNotification == null) {
            return;
        }

        for (int i = 0;
             i < notifications.size();
             i++) {

            if (notifications.get(i)
                    .getNotificationId()
                    == updatedNotification
                    .getNotificationId()) {

                notifications.set(
                        i,
                        updatedNotification
                );

                return;
            }
        }
    }

    // -----------------------------------------
    // Delete notification
    // -----------------------------------------

    public void delete(
            int notificationId) {

        notifications.removeIf(
                notification ->
                        notification.getNotificationId()
                                == notificationId
        );
    }
}
