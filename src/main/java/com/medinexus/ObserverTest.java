package com.medinexus;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.observer.AppointmentEventManager;
import com.medinexus.observer.EmailNotificationObserver;
import com.medinexus.observer.SMSNotificationObserver;

import java.time.LocalDate;
import java.time.LocalTime;

public class ObserverTest {

    public static void main(String[] args) {

        Appointment appointment =
                new Appointment(
                        2001,
                        1,
                        101,
                        "Mohammed Muneeb",
                        "Dr. Ahmed",
                        LocalDate.now(),
                        LocalTime.of(14, 30),
                        "Regular check-up",
                        "Routine",
                        AppointmentStatus.SCHEDULED
                );

        AppointmentEventManager eventManager =
                new AppointmentEventManager();

        EmailNotificationObserver emailObserver =
                new EmailNotificationObserver();

        SMSNotificationObserver smsObserver =
                new SMSNotificationObserver();

        eventManager.addObserver(
                emailObserver
        );

        eventManager.addObserver(
                smsObserver
        );

        System.out.println(
                "================================"
        );

        System.out.println(
                "APPOINTMENT CREATED"
        );

        System.out.println(
                "================================"
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "CREATED"
        );

        System.out.println();

        // Change appointment status
        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        System.out.println(
                "================================"
        );

        System.out.println(
                "APPOINTMENT CANCELLED"
        );

        System.out.println(
                "================================"
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "CANCELLED"
        );
    }
}