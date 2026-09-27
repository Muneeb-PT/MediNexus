package com.medinexus.service.identity;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Clock;

public class OAuthStateStore {

    private static final long STATE_EXPIRY_SECONDS = 600;

    private final Map<String, OAuthState> states =
            new ConcurrentHashMap<>();

    private final Clock clock;

    public void save(
            String state,
            int patientId,
            String codeVerifier) {

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "OAuth state cannot be empty."
            );
        }

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new IllegalArgumentException(
                    "PKCE code verifier cannot be empty."
            );
        }

        states.put(
                state,
                new OAuthState(
                        patientId,
                        codeVerifier,
                        Instant.now(clock)
                )
        );
    }

    public OAuthState consume(String state) {

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException(
                    "OAuth state cannot be empty."
            );
        }

        OAuthState stored = states.remove(state);

        if (stored == null) {
            throw new IllegalStateException(
                    "Invalid or already-used OAuth state."
            );
        }

        if (stored.createdAt()
                .plusSeconds(STATE_EXPIRY_SECONDS)
                .isBefore(Instant.now(clock))) {

            throw new IllegalStateException(
                    "OAuth state has expired."
            );
        }

        return stored;
    }

    public record OAuthState(
            int patientId,
            String codeVerifier,
            Instant createdAt
    ) {
    }
    public OAuthStateStore() {
        this(Clock.systemUTC());
    }

    OAuthStateStore(Clock clock) {

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null."
            );
        }

        this.clock = clock;
    }
}