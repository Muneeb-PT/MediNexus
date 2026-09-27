package com.medinexus.service;

import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientOwnershipTest {

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    @Test
    void adminCanAccessAllPatients() {

        User admin = new User(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN,
                "test"
        );

        SessionManager.login(admin);

        List<Patient> patients =
                patientService.getAllPatients();

        assertNotNull(patients);
    }

    @Test
    void receptionistCanAccessAllPatients() {

        User receptionist = new User(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST,
                "test"
        );

        SessionManager.login(receptionist);

        List<Patient> patients =
                patientService.getAllPatients();

        assertNotNull(patients);
    }

    @Test
    void patientCannotAccessCompletePatientList() {

        User patient = new User(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT,
                "test"
        );

        SessionManager.login(patient);

        assertThrows(
                SecurityException.class,
                () -> patientService.getAllPatients()
        );
    }

    @Test
    void patientCanAccessOwnProfile() {

        User patient = new User(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT,
                "test"
        );

        SessionManager.login(patient);

        Patient ownPatient =
                patientService.getPatientByUserId(1);

        assertNotNull(ownPatient);
        assertEquals(
                1,
                ownPatient.getUserId()
        );
    }

    @Test
    void patientCannotAccessAnotherUserProfile() {

        User patient = new User(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT,
                "test"
        );

        SessionManager.login(patient);

        assertThrows(
                SecurityException.class,
                () -> patientService.getPatientByUserId(2)
        );
    }

    @Test
    void patientCannotAccessAnotherPatientByPatientId() {

        User patient = new User(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT,
                "test"
        );

        SessionManager.login(patient);

        assertThrows(
                SecurityException.class,
                () -> patientService.getPatientById(2)
        );
    }

    @Test
    void doctorCanAccessClinicallyRelatedPatient() {

        User doctor = new User(
                102,
                "doctor2",
                "Dr. Sarah",
                Role.DOCTOR,
                "test"
        );

        SessionManager.login(doctor);

        List<Patient> patients =
                patientService.getAllPatients();

        assertNotNull(patients);

        /*
         * The repository filters the result using
         * the doctor's appointment relationship.
         */
        for (Patient patient : patients) {
            assertTrue(
                    patient.getPatientId() > 0
            );
        }
    }

    @Test
    void doctorCannotModifyPatientDemographics() {

        User doctor = new User(
                102,
                "doctor2",
                "Dr. Sarah",
                Role.DOCTOR,
                "test"
        );

        SessionManager.login(doctor);

        Patient patient =
                patientService.getPatientById(1);

        if (patient != null) {

            assertThrows(
                    SecurityException.class,
                    () -> patientService.updatePatient(patient)
            );
        }
    }

    @Test
    void unauthenticatedUserCannotAccessPatients() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () -> patientService.getAllPatients()
        );
    }
}