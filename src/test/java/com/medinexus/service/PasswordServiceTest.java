package com.medinexus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {


    @Test
    void passwordCanBeHashedAndVerified() {

        String password = "MediNexus@123";

        String hash =
                PasswordService.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);

        assertTrue(
                PasswordService.verifyPassword(
                        password,
                        hash
                )
        );
    }

    @Test
    void incorrectPasswordIsRejected() {

        String hash =
                PasswordService.hashPassword(
                        "MediNexus@123"
                );

        assertFalse(
                PasswordService.verifyPassword(
                        "WrongPassword",
                        hash
                )
        );
    }

    @Test
    void samePasswordProducesDifferentHashes() {

        String password = "MediNexus@123";

        String hash1 =
                PasswordService.hashPassword(password);

        String hash2 =
                PasswordService.hashPassword(password);

        assertNotEquals(hash1, hash2);

        assertTrue(
                PasswordService.verifyPassword(
                        password,
                        hash1
                )
        );

        assertTrue(
                PasswordService.verifyPassword(
                        password,
                        hash2
                )
        );
    }

    @Test
    void emptyPasswordCannotBeHashed() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordService.hashPassword("")
        );
    }

    @Test
    void nullPasswordCannotBeHashed() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordService.hashPassword(null)
        );
    }

    @Test
    void invalidHashDoesNotAuthenticate() {

        assertFalse(
                PasswordService.verifyPassword(
                        "MediNexus@123",
                        "not-a-valid-bcrypt-hash"
                )
        );
    }


}
