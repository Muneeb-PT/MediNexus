package com.medinexus.security;

import com.medinexus.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityOwnershipSecurityTest {

    @Test
    void patientRoleMustNotBeAdmin() {

        assertNotEquals(
                Role.ADMIN,
                Role.PATIENT
        );
    }

    @Test
    void doctorRoleMustNotBeAdmin() {

        assertNotEquals(
                Role.ADMIN,
                Role.DOCTOR
        );
    }

    @Test
    void receptionistRoleMustNotBeAdmin() {

        assertNotEquals(
                Role.ADMIN,
                Role.RECEPTIONIST
        );
    }

    @Test
    void onlyAdminShouldBeAdminRole() {

        assertEquals(
                Role.ADMIN,
                Role.valueOf("ADMIN")
        );
    }

    @Test
    void patientRoleShouldBePatientRole() {

        assertEquals(
                Role.PATIENT,
                Role.valueOf("PATIENT")
        );
    }
}