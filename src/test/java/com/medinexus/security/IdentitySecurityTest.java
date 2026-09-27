package com.medinexus.security;

import com.medinexus.model.Role;
import com.medinexus.service.AuthorizationService;
import com.medinexus.service.IdentityMatchService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentitySecurityTest {

    private final AuthorizationService authorizationService =
            new AuthorizationService();

    private final IdentityMatchService identityMatchService =
            new IdentityMatchService();

    @Test
    void adminRoleShouldBeRecognized() {

        assertEquals(
                Role.ADMIN,
                Role.valueOf("ADMIN")
        );
    }

    @Test
    void patientRoleShouldBeRecognized() {

        assertEquals(
                Role.PATIENT,
                Role.valueOf("PATIENT")
        );
    }

    @Test
    void identityNameMatchShouldBeCaseInsensitive() {

        assertTrue(
                identityMatchService.matchesName(
                        "Mohammed Muneeb",
                        "MOHAMMED MUNEEB"
                )
        );
    }

    @Test
    void identityNameMatchShouldIgnoreExtraSpaces() {

        assertTrue(
                identityMatchService.matchesName(
                        "Mohammed   Muneeb",
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void differentIdentityNamesShouldNotMatch() {

        assertFalse(
                identityMatchService.matchesName(
                        "Mohammed Muneeb",
                        "Another Person"
                )
        );
    }

    @Test
    void nullPatientNameShouldNotMatch() {

        assertFalse(
                identityMatchService.matchesName(
                        null,
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void nullVerifiedNameShouldNotMatch() {

        assertFalse(
                identityMatchService.matchesName(
                        "Mohammed Muneeb",
                        null
                )
        );
    }

    @Test
    void blankPatientNameShouldNotMatch() {

        assertFalse(
                identityMatchService.matchesName(
                        "   ",
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void blankVerifiedNameShouldNotMatch() {

        assertFalse(
                identityMatchService.matchesName(
                        "Mohammed Muneeb",
                        "   "
                )
        );
    }
}