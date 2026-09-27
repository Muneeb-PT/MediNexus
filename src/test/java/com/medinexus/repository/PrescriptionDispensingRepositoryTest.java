package com.medinexus.repository;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionDispensingRepositoryTest {

    private final PrescriptionDispensingRepository repository =
            new PrescriptionDispensingRepository();

    @AfterEach
    void cleanup() {
        SessionManager.logout();
    }

    // =========================================================
    // UNAUTHENTICATED ACCESS
    // =========================================================

    @Test
    void unauthenticatedUserCannotDispense() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () -> repository.dispensePrescription(999999999)
        );
    }

    // =========================================================
    // PATIENT ACCESS
    // =========================================================

    @Test
    void patientCannotDispenseMedicine() {

        SessionManager.login(
                new User(
                        1,
                        "test-patient",
                        "Test Patient",
                        Role.PATIENT,
                        "test-password-hash"
                )
        );

        assertThrows(
                SecurityException.class,
                () -> repository.dispensePrescription(999999999)
        );
    }

    // =========================================================
    // DOCTOR ACCESS
    // =========================================================

    @Test
    void doctorCannotDispenseMedicine() {

        SessionManager.login(
                new User(
                        101,
                        "test-doctor",
                        "Test Doctor",
                        Role.DOCTOR,
                        "test-password-hash"
                )
        );

        assertThrows(
                SecurityException.class,
                () -> repository.dispensePrescription(999999999)
        );
    }

    // =========================================================
    // ADMIN ACCESS
    // =========================================================

    @Test
    void adminCanReachDispensingRepository() {

        SessionManager.login(
                new User(
                        900,
                        "test-admin",
                        "Test Administrator",
                        Role.ADMIN,
                        "test-password-hash"
                )
        );

        boolean result =
                repository.dispensePrescription(
                        999999999
                );

        assertFalse(result);
    }

    // =========================================================
    // RECEPTIONIST ACCESS
    // =========================================================

    @Test
    void receptionistCanReachDispensingRepository() {

        SessionManager.login(
                new User(
                        500,
                        "test-receptionist",
                        "Test Receptionist",
                        Role.RECEPTIONIST,
                        "test-password-hash"
                )
        );

        boolean result =
                repository.dispensePrescription(
                        999999999
                );

        assertFalse(result);
    }

    // =========================================================
    // INVALID PRESCRIPTION ID
    // =========================================================

    @Test
    void invalidPrescriptionIdReturnsFalse() {

        SessionManager.login(
                new User(
                        900,
                        "test-admin",
                        "Test Administrator",
                        Role.ADMIN,
                        "test-password-hash"
                )
        );

        assertFalse(
                repository.dispensePrescription(0)
        );

        assertFalse(
                repository.dispensePrescription(-1)
        );
    }

    // =========================================================
    // NON-EXISTENT PRESCRIPTION
    // =========================================================

    @Test
    void nonExistentPrescriptionReturnsFalse() {

        SessionManager.login(
                new User(
                        900,
                        "test-admin",
                        "Test Administrator",
                        Role.ADMIN,
                        "test-password-hash"
                )
        );

        assertFalse(
                repository.dispensePrescription(
                        999999999
                )
        );
    }
}