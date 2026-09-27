package com.medinexus.test;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.observer.InAppNotificationObserver;
import com.medinexus.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class NotificationObserverTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("   MediNexus Observer Integration Test");
        System.out.println("======================================");

        // -----------------------------------------
        // Create observer
        // -----------------------------------------

        InAppNotificationObserver observer =
                new InAppNotificationObserver();

        // -----------------------------------------
        // Create test appointment object
        // -----------------------------------------

        Appointment appointment =
                new Appointment(
                        9999,
                        1,
                        101,
                        "Mohammed Muneeb",
                        "Dr. Ahmed",
                        LocalDate.now().plusDays(7),
                        LocalTime.of(10, 30),
                        "Observer integration test",
                        "Routine",
                        AppointmentStatus.SCHEDULED
                );

        // -----------------------------------------
        // Count notifications before event
        // -----------------------------------------

        NotificationService notificationService =
                new NotificationService();

        List<?> before =
                notificationService.getUserNotifications(1);

        int beforeCount = before.size();

        System.out.println(
                "\nNotifications before event: "
                        + beforeCount
        );

        // -----------------------------------------
        // Trigger Observer
        // -----------------------------------------

        System.out.println(
                "\nTriggering CREATED appointment event..."
        );

        observer.onAppointmentChanged(
                appointment,
                "CREATED"
        );

        // -----------------------------------------
        // Count notifications after event
        // -----------------------------------------

        List<?> after =
                notificationService.getUserNotifications(1);

        int afterCount = after.size();

        System.out.println(
                "Notifications after event: "
                        + afterCount
        );

        // -----------------------------------------
        // Verify
        // -----------------------------------------

        if (afterCount == beforeCount + 1) {

            System.out.println(
                    "\nSUCCESS: Observer created "
                            + "a notification in MySQL."
            );

        } else {

            System.out.println(
                    "\nFAILED: Notification was not "
                            + "created correctly."
            );
        }

        // -----------------------------------------
        // Display latest notification
        // -----------------------------------------

        if (!after.isEmpty()) {

            var latest =
                    notificationService
                            .getUserNotifications(1)
                            .get(0);

            System.out.println(
                    "\nLatest notification:"
            );

            System.out.println(
                    "ID      : "
                            + latest.getNotificationId()
            );

            System.out.println(
                    "Title   : "
                            + latest.getTitle()
            );

            System.out.println(
                    "Message : "
                            + latest.getMessage()
            );

            System.out.println(
                    "Type    : "
                            + latest.getType()
            );

            System.out.println(
                    "Read    : "
                            + latest.isRead()
            );
        }

        System.out.println("\n======================================");
        System.out.println(" Observer Integration Test Completed");
        System.out.println("======================================");
    }
}