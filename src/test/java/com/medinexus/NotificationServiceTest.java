package com.medinexus.test;

import com.medinexus.model.Notification;
import com.medinexus.service.NotificationService;

import java.util.List;

public class NotificationServiceTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("   MediNexus Notification Service Test");
        System.out.println("======================================");

        NotificationService service =
                new NotificationService();

        // -----------------------------------------
        // Test 1: Get all notifications
        // -----------------------------------------

        System.out.println("\n--- All Notifications ---");

        List<Notification> notifications =
                service.getAllNotifications();

        System.out.println(
                "Total notifications: "
                        + notifications.size()
        );

        for (Notification notification : notifications) {

            System.out.println(
                    "ID: " + notification.getNotificationId()
                            + " | User: " + notification.getUserId()
                            + " | Title: " + notification.getTitle()
                            + " | Read: " + notification.isRead()
            );
        }

        // -----------------------------------------
        // Test 2: Get user notifications
        // -----------------------------------------

        System.out.println(
                "\n--- User 1 Notifications ---"
        );

        List<Notification> userNotifications =
                service.getUserNotifications(1);

        System.out.println(
                "Count: " + userNotifications.size()
        );

        // -----------------------------------------
        // Test 3: Unread count
        // -----------------------------------------

        System.out.println(
                "\n--- Unread Count ---"
        );

        long unreadCount =
                service.getUnreadCount(1);

        System.out.println(
                "User 1 unread notifications: "
                        + unreadCount
        );

        // -----------------------------------------
        // Test 4: Read existing notification
        // -----------------------------------------
        // We deliberately DO NOT modify the database
        // in this test.

        Notification firstNotification =
                service.getNotificationById(1);

        if (firstNotification != null) {

            System.out.println(
                    "\nNotification ID 1 successfully loaded."
            );

            System.out.println(
                    "Title: "
                            + firstNotification.getTitle()
            );

            System.out.println(
                    "Current read status: "
                            + firstNotification.isRead()
            );
        }

        System.out.println("\n======================================");
        System.out.println(" Notification Service Test Completed");
        System.out.println("======================================");
    }
}