package com.medinexus.service;

import com.medinexus.model.Appointment;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentOwnershipTest {

    private final AppointmentService appointmentService =
            new AppointmentService();

    @AfterEach
    void cleanupSession() {
        SessionManager.logout();
    }

    @Test
    void patientCannotAccessAnotherPatientsAppointments() {

        SessionManager.login(
                new User(
                        2,
                        "patient2",
                        "Aisha Rahman",
                        Role.PATIENT
                )
        );

        assertThrows(
                SecurityException.class,
                () -> appointmentService.getPatientAppointments(1)
        );
    }

    @Test
    void patientCanAccessOwnAppointments() {

        SessionManager.login(
                new User(
                        1,
                        "patient",
                        "Mohammed Muneeb",
                        Role.PATIENT
                )
        );

        assertDoesNotThrow(
                () -> appointmentService.getPatientAppointments(1)
        );
    }

    @Test
    void doctorCannotAccessAnotherDoctorsAppointments() {

        SessionManager.login(
                new User(
                        101,
                        "doctor",
                        "Dr. Ahmed",
                        Role.DOCTOR
                )
        );

        assertThrows(
                SecurityException.class,
                () -> appointmentService.getDoctorAppointments(102)
        );
    }

    @Test
    void doctorCanAccessOwnAppointments() {

        SessionManager.login(
                new User(
                        101,
                        "doctor",
                        "Dr. Ahmed",
                        Role.DOCTOR
                )
        );

        assertDoesNotThrow(
                () -> appointmentService.getDoctorAppointments(101)
        );
    }

    @Test
    void receptionistCanAccessPatientAppointments() {

        SessionManager.login(
                new User(
                        500,
                        "receptionist",
                        "MediNexus Receptionist",
                        Role.RECEPTIONIST
                )
        );

        assertDoesNotThrow(
                () -> appointmentService.getPatientAppointments(1)
        );
    }

    @Test
    void unauthenticatedUserCannotAccessAllAppointments() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () -> appointmentService.getAllAppointments()
        );
    }
}