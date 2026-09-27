package com.medinexus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityMatchServiceTest {

    private final IdentityMatchService service =
            new IdentityMatchService();

    @Test
    void exactNamesShouldMatch() {

        assertTrue(
                service.matchesName(
                        "Mohammed Muneeb",
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void namesShouldMatchIgnoringCase() {

        assertTrue(
                service.matchesName(
                        "Mohammed Muneeb",
                        "MOHAMMED MUNEEB"
                )
        );
    }

    @Test
    void namesShouldMatchIgnoringExtraSpaces() {

        assertTrue(
                service.matchesName(
                        "Mohammed   Muneeb",
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void differentNamesShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        "Mohammed Muneeb",
                        "Mohammed Abrar"
                )
        );
    }

    @Test
    void blankPatientNameShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        "",
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void blankVerifiedNameShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        "Mohammed Muneeb",
                        ""
                )
        );
    }

    @Test
    void nullPatientNameShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        null,
                        "Mohammed Muneeb"
                )
        );
    }

    @Test
    void nullVerifiedNameShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        "Mohammed Muneeb",
                        null
                )
        );
    }

    @Test
    void partialNameShouldNotAutomaticallyMatch() {

        assertFalse(
                service.matchesName(
                        "Mohammed Muneeb",
                        "Mohammed Muneeb PT"
                )
        );
    }

    @Test
    void whitespaceOnlyNameShouldNotMatch() {

        assertFalse(
                service.matchesName(
                        "   ",
                        "Mohammed Muneeb"
                )
        );
    }
}