package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartTriageQueueTest {

    @AfterEach
    void clearSession() {
        SessionManager.logout();
    }

    @Test
    void unauthenticatedUserCannotAccessTriageQueue() {

        SessionManager.logout();

        AppointmentService service =
                new AppointmentService();

        assertThrows(
                SecurityException.class,
                service::getSmartTriageQueue
        );
    }

    @Test
    void patientCannotAccessOperationalTriageQueue() {

        User patient =
                new User(
                        1,
                        "patient",
                        "Mohammed Muneeb",
                        Role.PATIENT,
                        "hash"
                );

        SessionManager.login(patient);

        AppointmentService service =
                new AppointmentService();

        assertThrows(
                SecurityException.class,
                service::getSmartTriageQueue
        );
    }

    @Test
    void doctorCanAccessOwnTriageQueue() {

        User doctor =
                new User(
                        101,
                        "doctor",
                        "Dr. Ahmed",
                        Role.DOCTOR,
                        "hash"
                );

        SessionManager.login(doctor);

        AppointmentService service =
                new AppointmentService();

        assertDoesNotThrow(
                service::getSmartTriageQueue
        );
    }

    @Test
    void receptionistCanAccessTriageQueue() {

        User receptionist =
                new User(
                        500,
                        "receptionist",
                        "MediNexus Receptionist",
                        Role.RECEPTIONIST,
                        "hash"
                );

        SessionManager.login(receptionist);

        AppointmentService service =
                new AppointmentService();

        assertDoesNotThrow(
                service::getSmartTriageQueue
        );
    }

    @Test
    void adminCanAccessTriageQueue() {

        User admin =
                new User(
                        900,
                        "admin",
                        "MediNexus Administrator",
                        Role.ADMIN,
                        "hash"
                );

        SessionManager.login(admin);

        AppointmentService service =
                new AppointmentService();

        assertDoesNotThrow(
                service::getSmartTriageQueue
        );
    }
}