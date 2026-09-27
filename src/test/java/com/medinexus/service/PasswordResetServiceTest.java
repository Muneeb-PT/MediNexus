package com.medinexus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetServiceTest {

    private final PasswordResetService resetService =
            new PasswordResetService();

    @Test
    void rejectsNullResetToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        null,
                        "StrongPass123!"
                )
        );
    }

    @Test
    void rejectsEmptyResetToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "",
                        "StrongPass123!"
                )
        );
    }

    @Test
    void rejectsBlankResetToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "   ",
                        "StrongPass123!"
                )
        );
    }

    @Test
    void rejectsShortNewPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "demo-token",
                        "1234"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutUppercase() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "demo-token",
                        "strongpass123!"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutLowercase() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "demo-token",
                        "STRONGPASS123!"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutDigit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "demo-token",
                        "StrongPassword!"
                )
        );
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {

        assertThrows(
                IllegalArgumentException.class,
                () -> resetService.resetPassword(
                        "demo-token",
                        "StrongPass123"
                )
        );
    }
}