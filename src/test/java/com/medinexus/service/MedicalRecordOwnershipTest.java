package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicalRecordOwnershipTest {

    private final MedicalRecordService medicalRecordService =
            new MedicalRecordService();

    @AfterEach
    void cleanupSession() {
        SessionManager.logout();
    }

    @Test
    void patientCanAccessOwnMedicalRecords() {

        SessionManager.login(
                new User(
                        1,
                        "patient",
                        "Mohammed Muneeb",
                        Role.PATIENT
                )
        );

        assertDoesNotThrow(
                () -> medicalRecordService.getPatientRecords(1)
        );
    }

    @Test
    void patientCannotAccessAnotherPatientsMedicalRecords() {

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
                () -> medicalRecordService.getPatientRecords(1)
        );
    }

    @Test
    void doctorCanAccessOwnMedicalRecords() {

        SessionManager.login(
                new User(
                        101,
                        "doctor",
                        "Dr. Ahmed",
                        Role.DOCTOR
                )
        );

        assertDoesNotThrow(
                () -> medicalRecordService.getDoctorRecords(101)
        );
    }

    @Test
    void doctorCannotAccessAnotherDoctorsRecords() {

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
                () -> medicalRecordService.getDoctorRecords(102)
        );
    }

    @Test
    void receptionistCannotAccessMedicalRecords() {

        SessionManager.login(
                new User(
                        500,
                        "receptionist",
                        "MediNexus Receptionist",
                        Role.RECEPTIONIST
                )
        );

        assertThrows(
                SecurityException.class,
                () -> medicalRecordService.getPatientRecords(1)
        );
    }

    @Test
    void unauthenticatedUserCannotAccessMedicalRecords() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () -> medicalRecordService.getPatientRecords(1)
        );
    }

    @Test
    void doctorCannotViewAllMedicalRecords() {

        User doctor = new User(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        SessionManager.login(doctor);

        assertThrows(
                SecurityException.class,
                () -> medicalRecordService.getAllRecords()
        );
    }
}