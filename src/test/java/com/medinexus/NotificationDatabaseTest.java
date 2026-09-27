package com.medinexus.test;

import com.medinexus.model.Notification;
import com.medinexus.repository.NotificationJdbcRepository;

import java.util.List;

public class NotificationDatabaseTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("  MediNexus Notification Database Test");
        System.out.println("======================================");

        NotificationJdbcRepository repository =
                new NotificationJdbcRepository();

        // -----------------------------------------
        // Test 1: Get all notifications
        // -----------------------------------------

        System.out.println("\n--- All Notifications ---");

        List<Notification> notifications =
                repository.findAll();

        System.out.println(
                "Total notifications: "
                        + notifications.size()
        );

        for (Notification notification : notifications) {

            System.out.println(
                    "ID: " + notification.getNotificationId()
                            + " | User: " + notification.getUserId()
                            + " | Title: " + notification.getTitle()
                            + " | Type: " + notification.getType()
                            + " | Read: " + notification.isRead()
            );
        }

        // -----------------------------------------
        // Test 2: Find notification by ID
        // -----------------------------------------

        System.out.println("\n--- Notification ID 1 ---");

        Notification notification =
                repository.findById(1);

        if (notification != null) {

            System.out.println(
                    "Notification found successfully."
            );

            System.out.println(
                    "ID      : "
                            + notification.getNotificationId()
            );

            System.out.println(
                    "User ID : "
                            + notification.getUserId()
            );

            System.out.println(
                    "Title   : "
                            + notification.getTitle()
            );

            System.out.println(
                    "Message : "
                            + notification.getMessage()
            );

            System.out.println(
                    "Type    : "
                            + notification.getType()
            );

            System.out.println(
                    "Read    : "
                            + notification.isRead()
            );

            System.out.println(
                    "Created : "
                            + notification.getCreatedAt()
            );

        } else {

            System.out.println(
                    "Notification ID 1 not found."
            );
        }

        // -----------------------------------------
        // Test 3: Find notifications by user
        // -----------------------------------------

        System.out.println(
                "\n--- Notifications for User ID 1 ---"
        );

        List<Notification> userNotifications =
                repository.findByUserId(1);

        System.out.println(
                "Notification count: "
                        + userNotifications.size()
        );

        for (Notification n : userNotifications) {

            System.out.println(
                    "ID: " + n.getNotificationId()
                            + " | "
                            + n.getTitle()
                            + " | Read: "
                            + n.isRead()
            );
        }

        System.out.println("\n======================================");
        System.out.println(" Notification Database Test Completed");
        System.out.println("======================================");
    }
}