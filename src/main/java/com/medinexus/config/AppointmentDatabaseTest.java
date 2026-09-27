package com.medinexus.config;

import com.medinexus.model.Appointment;
import com.medinexus.repository.AppointmentJdbcRepository;

import java.util.List;

public class AppointmentDatabaseTest {

    public static void main(String[] args) {

        AppointmentJdbcRepository repository =
                new AppointmentJdbcRepository();

        System.out.println("=== APPOINTMENT DATABASE TEST ===");

        List<Appointment> appointments =
                repository.findAll();

        System.out.println(
                "Total appointments: "
                        + appointments.size()
        );

        for (Appointment appointment : appointments) {

            System.out.println(
                    appointment.getAppointmentId()
                            + " | Patient: "
                            + appointment.getPatientName()
                            + " | Doctor: "
                            + appointment.getDoctorName()
                            + " | Date: "
                            + appointment.getAppointmentDate()
                            + " | Time: "
                            + appointment.getAppointmentTime()
                            + " | Status: "
                            + appointment.getStatus()
            );
        }

        System.out.println();

        List<Appointment> patientAppointments =
                repository.findByPatientId(1);

        System.out.println(
                "Patient #1 appointments: "
                        + patientAppointments.size()
        );

        List<Appointment> doctorAppointments =
                repository.findByDoctorId(101);

        System.out.println(
                "Doctor #101 appointments: "
                        + doctorAppointments.size()
        );

        boolean available =
                repository.isDoctorAvailable(
                        101,
                        java.time.LocalDate.of(
                                2026,
                                9,
                                25
                        ),
                        java.time.LocalTime.of(
                                10,
                                0
                        )
                );

        System.out.println(
                "Doctor #101 available at "
                        + "2026-09-25 10:00: "
                        + available
        );
    }
}