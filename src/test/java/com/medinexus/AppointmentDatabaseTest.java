package com.medinexus;

import com.medinexus.model.Appointment;
import com.medinexus.repository.AppointmentJdbcRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentDatabaseTest {

    public static void main(String[] args) {

        AppointmentJdbcRepository repository =
                new AppointmentJdbcRepository();

        System.out.println("======================================");
        System.out.println("   MEDINEXUS APPOINTMENT DB TEST");
        System.out.println("======================================");

        // -------------------------------------------------
        // 1. FIND ALL
        // -------------------------------------------------
        System.out.println("\n1. All appointments:");

        List<Appointment> appointments = repository.findAll();

        for (Appointment appointment : appointments) {
            printAppointment(appointment);
        }

        // -------------------------------------------------
        // 2. FIND BY ID
        // -------------------------------------------------
        System.out.println("\n2. Find appointment ID 1:");

        Appointment appointment = repository.findById(1);

        if (appointment != null) {
            printAppointment(appointment);
        } else {
            System.out.println("Appointment not found.");
        }

        // -------------------------------------------------
        // 3. FIND BY PATIENT
        // -------------------------------------------------
        System.out.println("\n3. Appointments for patient 1:");

        List<Appointment> patientAppointments =
                repository.findByPatientId(1);

        for (Appointment a : patientAppointments) {
            printAppointment(a);
        }

        // -------------------------------------------------
        // 4. FIND BY DOCTOR
        // -------------------------------------------------
        System.out.println("\n4. Appointments for doctor 101:");

        List<Appointment> doctorAppointments =
                repository.findByDoctorId(101);

        for (Appointment a : doctorAppointments) {
            printAppointment(a);
        }

        // -------------------------------------------------
        // 5. DOCTOR AVAILABILITY
        // -------------------------------------------------
        System.out.println("\n5. Doctor availability:");

        boolean available = repository.isDoctorAvailable(
                101,
                LocalDate.of(2026, 9, 25),
                LocalTime.of(10, 0)
        );

        System.out.println(
                "Doctor 101 available at 2026-09-25 10:00 = "
                        + available
        );

        System.out.println("\n======================================");
        System.out.println("        TEST COMPLETED");
        System.out.println("======================================");
    }

    private static void printAppointment(Appointment appointment) {

        System.out.println("--------------------------------------");
        System.out.println("Appointment ID : " + appointment.getAppointmentId());
        System.out.println("Patient        : " + appointment.getPatientName());
        System.out.println("Doctor         : " + appointment.getDoctorName());
        System.out.println("Date           : " + appointment.getAppointmentDate());
        System.out.println("Time           : " + appointment.getAppointmentTime());
        System.out.println("Reason         : " + appointment.getReason());
        System.out.println("Urgency        : " + appointment.getUrgency());
        System.out.println("Status         : " + appointment.getStatus());
    }
}