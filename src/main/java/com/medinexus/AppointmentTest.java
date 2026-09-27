package com.medinexus;

import com.medinexus.model.Appointment;
import com.medinexus.service.AppointmentService;
import com.medinexus.model.AppointmentStatus;

import java.util.List;

public class AppointmentTest {

    public static void main(String[] args) {

        AppointmentService service =
                new AppointmentService();

        System.out.println(
                "================================"
        );

        System.out.println(
                "MediNexus Appointment Test"
        );

        System.out.println(
                "================================"
        );

        List<Appointment> appointments =
                service.getAllAppointments();

        for (Appointment appointment :
                appointments) {

            System.out.println(
                    "ID: "
                            + appointment.getAppointmentId()
            );

            System.out.println(
                    "Patient: "
                            + appointment.getPatientName()
            );

            System.out.println(
                    "Doctor: "
                            + appointment.getDoctorName()
            );

            System.out.println(
                    "Date: "
                            + appointment.getAppointmentDate()
            );

            System.out.println(
                    "Time: "
                            + appointment.getAppointmentTime()
            );

            System.out.println(
                    "Urgency: "
                            + appointment.getUrgency()
            );

            System.out.println(
                    "Status: "
                            + appointment.getStatus()
            );

            System.out.println(
                    "Scheduled: "
                            + AppointmentStatus.SCHEDULED
            );

            System.out.println(
                    "--------------------------------"
            );
        }
    }
}