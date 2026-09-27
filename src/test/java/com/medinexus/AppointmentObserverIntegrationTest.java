package com.medinexus;

import com.medinexus.model.Appointment;
import com.medinexus.service.AppointmentService;
import com.medinexus.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentObserverIntegrationTest {

    public static void main(String[] args) {

        AppointmentService service =
                new AppointmentService();

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

        System.out.println(
                "================================"
        );

        System.out.println(
                "Creating appointment..."
        );

        System.out.println(
                "================================"
        );

        service.createAppointment(
                appointment
        );

        System.out.println(
                "Appointment creation completed."
        );
    }
}