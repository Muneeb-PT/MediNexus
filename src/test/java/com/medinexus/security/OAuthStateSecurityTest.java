package com.medinexus.security;

import com.medinexus.service.identity.OAuthStateStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OAuthStateSecurityTest {

    @Test
    void validStateShouldBeConsumedSuccessfully() {

        OAuthStateStore store =
                new OAuthStateStore();

        store.save(
                "state-123",
                10,
                "secure-code-verifier"
        );

        OAuthStateStore.OAuthState result =
                store.consume("state-123");

        assertEquals(
                10,
                result.patientId()
        );

        assertEquals(
                "secure-code-verifier",
                result.codeVerifier()
        );
    }

    @Test
    void consumedStateCannotBeReused() {

        OAuthStateStore store =
                new OAuthStateStore();

        store.save(
                "state-replay",
                10,
                "verifier"
        );

        store.consume("state-replay");

        assertThrows(
                IllegalStateException.class,
                () -> store.consume("state-replay")
        );
    }

    @Test
    void unknownStateShouldBeRejected() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalStateException.class,
                () -> store.consume("unknown-state")
        );
    }

    @Test
    void blankStateCannotBeSaved() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        " ",
                        10,
                        "verifier"
                )
        );
    }

    @Test
    void invalidPatientIdCannotBeSaved() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        "state",
                        0,
                        "verifier"
                )
        );
    }

    @Test
    void blankCodeVerifierCannotBeSaved() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        "state",
                        10,
                        " "
                )
        );
    }
}