package com.medinexus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatientSignupServiceTest {

    private final PatientSignupService signupService =
            new PatientSignupService();

    @Test
    void rejectsEmptyUsername() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "",
                        "StrongPass123",
                        "Test Patient",
                        21,
                        "Male",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsShortUsername() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "abc",
                        "StrongPass123",
                        "Test Patient",
                        21,
                        "Male",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsShortPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "1234",
                        "Test Patient",
                        21,
                        "Male",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsInvalidAge() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "StrongPass123",
                        "Test Patient",
                        0,
                        "Male",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsMissingFullName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "StrongPass123",
                        "",
                        21,
                        "Male",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsMissingGender() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "StrongPass123",
                        "Test Patient",
                        21,
                        "",
                        "9999999999",
                        "O+"
                )
        );
    }

    @Test
    void rejectsMissingPhone() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "StrongPass123",
                        "Test Patient",
                        21,
                        "Male",
                        "",
                        "O+"
                )
        );
    }

    @Test
    void rejectsMissingBloodGroup() {

        assertThrows(
                IllegalArgumentException.class,
                () -> signupService.registerPatient(
                        "testpatient",
                        "StrongPass123",
                        "Test Patient",
                        21,
                        "Male",
                        "9999999999",
                        ""
                )
        );
    }
}