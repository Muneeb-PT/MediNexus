package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PatientServiceAccessTest {

    private final PatientService patientService = new PatientService();

    @AfterEach
    void clearSession() {
        SessionManager.logout();
    }

    @Test
    void doctorCannotLookUpAnotherUsersPatientProfile() {
        SessionManager.login(
                new User(101, "doctor", "Dr. Ahmed", Role.DOCTOR)
        );

        assertThrows(
                SecurityException.class,
                () -> patientService.getPatientByUserId(202)
        );
    }

    @Test
    void patientCannotLookUpAnotherUsersPatientProfile() {
        SessionManager.login(
                new User(101, "patient", "Test Patient", Role.PATIENT)
        );

        assertThrows(
                SecurityException.class,
                () -> patientService.getPatientByUserId(202)
        );
    }
}
