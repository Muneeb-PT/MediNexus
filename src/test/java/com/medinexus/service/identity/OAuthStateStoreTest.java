package com.medinexus.service.identity;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class OAuthStateStoreTest {

    @Test
    void validStateCanBeSavedAndConsumed() {

        Instant now =
                Instant.parse("2026-09-27T12:00:00Z");

        Clock clock =
                Clock.fixed(
                        now,
                        ZoneOffset.UTC
                );

        OAuthStateStore store =
                new OAuthStateStore(clock);

        store.save(
                "state-123",
                25,
                "verifier-123"
        );

        OAuthStateStore.OAuthState result =
                store.consume("state-123");

        assertEquals(
                25,
                result.patientId()
        );

        assertEquals(
                "verifier-123",
                result.codeVerifier()
        );

        assertEquals(
                now,
                result.createdAt()
        );
    }

    @Test
    void stateCannotBeConsumedTwice() {

        Clock clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-09-27T12:00:00Z"
                        ),
                        ZoneOffset.UTC
                );

        OAuthStateStore store =
                new OAuthStateStore(clock);

        store.save(
                "state-duplicate",
                25,
                "verifier"
        );

        store.consume("state-duplicate");

        assertThrows(
                IllegalStateException.class,
                () -> store.consume("state-duplicate")
        );
    }

    @Test
    void expiredStateCannotBeConsumed() {

        Instant createdAt =
                Instant.parse(
                        "2026-09-27T12:00:00Z"
                );

        java.util.concurrent.atomic.AtomicReference<Instant> currentTime =
                new java.util.concurrent.atomic.AtomicReference<>(
                        createdAt
                );

        Clock clock =
                Clock.fixed(
                        createdAt,
                        ZoneOffset.UTC
                );

        OAuthStateStore store =
                new OAuthStateStore(
                        new Clock() {

                            @Override
                            public ZoneOffset getZone() {
                                return ZoneOffset.UTC;
                            }

                            @Override
                            public Clock withZone(
                                    java.time.ZoneId zone) {
                                return this;
                            }

                            @Override
                            public Instant instant() {
                                return currentTime.get();
                            }
                        }
                );

        store.save(
                "expired-state",
                25,
                "verifier"
        );

        currentTime.set(
                createdAt.plusSeconds(601)
        );

        assertThrows(
                IllegalStateException.class,
                () -> store.consume("expired-state")
        );
    }

    @Test
    void emptyStateCannotBeSaved() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        "",
                        25,
                        "verifier"
                )
        );
    }

    @Test
    void invalidPatientCannotBeSaved() {

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
    void emptyCodeVerifierCannotBeSaved() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.save(
                        "state",
                        25,
                        ""
                )
        );
    }

    @Test
    void emptyStateCannotBeConsumed() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.consume("")
        );
    }

    @Test
    void unknownStateCannotBeConsumed() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalStateException.class,
                () -> store.consume(
                        "unknown-state"
                )
        );
    }

    @Test
    void nullClockCannotBeUsed() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new OAuthStateStore(null)
        );
    }
}