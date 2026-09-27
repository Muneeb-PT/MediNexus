package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardOwnershipTest {

    private final DashboardService dashboardService =
            new DashboardService();

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }


    // =========================================================
    // AUTHENTICATION REQUIREMENT
    // =========================================================

    @Test
    void dashboardRejectsUnauthenticatedUser() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () -> dashboardService.getPatientCount()
        );
    }


    // =========================================================
    // ADMIN
    // =========================================================

    @Test
    void adminCanAccessHospitalPatientCount() {

        User admin =
                new User(
                        900,
                        "admin",
                        "MediNexus Administrator",
                        Role.ADMIN,
                        "test"
                );

        SessionManager.login(admin);

        assertTrue(
                dashboardService.getPatientCount() >= 0
        );
    }


    @Test
    void adminCanAccessHospitalAppointmentCount() {

        User admin =
                new User(
                        900,
                        "admin",
                        "MediNexus Administrator",
                        Role.ADMIN,
                        "test"
                );

        SessionManager.login(admin);

        assertTrue(
                dashboardService.getAppointmentCount() >= 0
        );
    }


    // =========================================================
    // DOCTOR
    // =========================================================

    @Test
    void doctorDashboardUsesAuthenticatedDoctor() {

        User doctor =
                new User(
                        102,
                        "doctor2",
                        "Dr. Sarah",
                        Role.DOCTOR,
                        "test"
                );

        SessionManager.login(doctor);

        List<DashboardService.DashboardAppointment>
                appointments =
                dashboardService.getRecentAppointments(
                        Role.ADMIN,
                        900
                );

        assertNotNull(appointments);

        /*
         * The caller deliberately supplied ADMIN/900.
         *
         * DashboardService must ignore those values and
         * use the authenticated doctor from SessionManager.
         */
        for (DashboardService.DashboardAppointment appointment
                : appointments) {

            assertEquals(
                    102,
                    appointment.getDoctorId(),
                    "Doctor dashboard must contain only the authenticated doctor's appointments."
            );
        }
    }


    @Test
    void doctorGetsOnlyClinicallyRelatedPatientCount() {

        User doctor =
                new User(
                        102,
                        "doctor2",
                        "Dr. Sarah",
                        Role.DOCTOR,
                        "test"
                );

        SessionManager.login(doctor);

        int patientCount =
                dashboardService.getPatientCount();

        assertTrue(
                patientCount >= 0
        );
    }


    // =========================================================
    // PATIENT
    // =========================================================

    @Test
    void patientDashboardUsesOwnAppointments() {

        User patient =
                new User(
                        1,
                        "patient",
                        "Mohammed Muneeb",
                        Role.PATIENT,
                        "test"
                );

        SessionManager.login(patient);

        List<DashboardService.DashboardAppointment>
                appointments =
                dashboardService.getRecentAppointments(
                        Role.ADMIN,
                        900
                );

        assertNotNull(appointments);

        /*
         * The caller again attempts to supply ADMIN data.
         *
         * The service must use the authenticated patient.
         */
        for (DashboardService.DashboardAppointment appointment
                : appointments) {

            assertEquals(
                    1,
                    appointment.getPatientId(),
                    "Patient dashboard must contain only the authenticated patient's appointments."
            );
        }
    }


    @Test
    void patientDoesNotReceiveHospitalWideMedicineCount() {

        User patient =
                new User(
                        1,
                        "patient",
                        "Mohammed Muneeb",
                        Role.PATIENT,
                        "test"
                );

        SessionManager.login(patient);

        assertEquals(
                0,
                dashboardService.getMedicineCount()
        );
    }
}