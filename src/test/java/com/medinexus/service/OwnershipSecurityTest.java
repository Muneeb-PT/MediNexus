package com.medinexus.service;

import com.medinexus.model.Permission;
import com.medinexus.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnershipSecurityTest {

    private final AuthorizationService authorizationService =
            new AuthorizationService();

    @Test
    void adminCanDeletePatients() {
        assertTrue(
                authorizationService.hasPermission(
                        Role.ADMIN,
                        Permission.DELETE_PATIENT
                )
        );
    }

    @Test
    void doctorCannotDeletePatients() {
        assertFalse(
                authorizationService.hasPermission(
                        Role.DOCTOR,
                        Permission.DELETE_PATIENT
                )
        );
    }

    @Test
    void receptionistCanAddPatients() {
        assertTrue(
                authorizationService.hasPermission(
                        Role.RECEPTIONIST,
                        Permission.ADD_PATIENT
                )
        );
    }

    @Test
    void patientCannotDeletePatients() {
        assertFalse(
                authorizationService.hasPermission(
                        Role.PATIENT,
                        Permission.DELETE_PATIENT
                )
        );
    }

    @Test
    void patientCannotAddPatients() {
        assertFalse(
                authorizationService.hasPermission(
                        Role.PATIENT,
                        Permission.ADD_PATIENT
                )
        );
    }

    @Test
    void receptionistCannotDeletePatients() {
        assertFalse(
                authorizationService.hasPermission(
                        Role.RECEPTIONIST,
                        Permission.DELETE_PATIENT
                )
        );
    }

    @Test
    void doctorCannotAddPatients() {
        assertFalse(
                authorizationService.hasPermission(
                        Role.DOCTOR,
                        Permission.ADD_PATIENT
                )
        );
    }

    @Test
    void adminCanAddPatients() {
        assertTrue(
                authorizationService.hasPermission(
                        Role.ADMIN,
                        Permission.ADD_PATIENT
                )
        );
    }
}