package com.medinexus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void acceptsStrongPassword() {

        assertTrue(
                PasswordPolicy.isValid(
                        "MediNexus@2026"
                )
        );
    }

    @Test
    void rejectsShortPassword() {

        assertFalse(
                PasswordPolicy.isValid(
                        "Ab@123"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutUppercase() {

        assertFalse(
                PasswordPolicy.isValid(
                        "medinexus@123"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutLowercase() {

        assertFalse(
                PasswordPolicy.isValid(
                        "MEDINEXUS@123"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutDigit() {

        assertFalse(
                PasswordPolicy.isValid(
                        "MediNexus@"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {

        assertFalse(
                PasswordPolicy.isValid(
                        "MediNexus123"
                )
        );
    }

    @Test
    void rejectsPasswordContainingWhitespace() {

        assertFalse(
                PasswordPolicy.isValid(
                        "Medi Nexus@123"
                )
        );
    }

    @Test
    void rejectsPasswordLongerThan72Characters() {

        String password =
                "A" +
                        "a".repeat(70) +
                        "1@";

        assertFalse(
                PasswordPolicy.isValid(password)
        );
    }

    @Test
    void rejectsNullPassword() {

        assertFalse(
                PasswordPolicy.isValid(null)
        );
    }

    @Test
    void rejectsEmptyPassword() {

        assertFalse(
                PasswordPolicy.isValid("")
        );
    }
}