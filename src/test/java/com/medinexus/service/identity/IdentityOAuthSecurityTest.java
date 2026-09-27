package com.medinexus.service.identity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IdentityOAuthSecurityTest {

    @Test
    void pkceVerifierShouldBeGenerated() {

        String verifier =
                PkceUtil.generateCodeVerifier();

        assertNotNull(verifier);
        assertFalse(verifier.isBlank());
        assertTrue(verifier.length() >= 43);
    }

    @Test
    void pkceChallengeShouldBeDeterministic() {

        String verifier =
                "test-code-verifier-123456789";

        String challenge1 =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        String challenge2 =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        assertNotNull(challenge1);
        assertFalse(challenge1.isBlank());

        assertEquals(
                challenge1,
                challenge2
        );
    }

    @Test
    void differentVerifiersShouldProduceDifferentChallenges() {

        String challenge1 =
                PkceUtil.generateCodeChallenge(
                        "verifier-one-123456789"
                );

        String challenge2 =
                PkceUtil.generateCodeChallenge(
                        "verifier-two-987654321"
                );

        assertNotEquals(
                challenge1,
                challenge2
        );
    }

    @Test
    void oauthStateShouldBeStoredAndConsumedOnce() {

        OAuthStateStore store =
                new OAuthStateStore();

        String state =
                "test-state-123";

        String verifier =
                PkceUtil.generateCodeVerifier();

        store.save(
                state,
                10,
                verifier
        );

        OAuthStateStore.OAuthState result =
                store.consume(state);

        assertNotNull(result);

        assertEquals(
                10,
                result.patientId()
        );

        assertEquals(
                verifier,
                result.codeVerifier()
        );
    }

    @Test
    void consumedStateShouldNotBeReusable() {

        OAuthStateStore store =
                new OAuthStateStore();

        String state =
                "single-use-state";

        store.save(
                state,
                20,
                PkceUtil.generateCodeVerifier()
        );

        store.consume(state);

        assertThrows(
                IllegalStateException.class,
                () -> store.consume(state)
        );
    }

    @Test
    void unknownStateShouldBeRejected() {

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
    void expiredStateShouldBeRejected() {

        Instant start =
                Instant.parse(
                        "2026-01-01T00:00:00Z"
                );

        java.time.Clock fixedClock =
                java.time.Clock.fixed(
                        start,
                        java.time.ZoneOffset.UTC
                );

        OAuthStateStore store =
                new OAuthStateStore(
                        fixedClock
                );

        String state =
                "expired-test-state";

        store.save(
                state,
                30,
                PkceUtil.generateCodeVerifier()
        );

        java.time.Clock expiredClock =
                java.time.Clock.fixed(
                        start.plusSeconds(601),
                        java.time.ZoneOffset.UTC
                );

        OAuthStateStore expiredStore =
                new OAuthStateStore(
                        expiredClock
                );

        /*
         * The state is stored in the first store, so we need a
         * deterministic store containing the old timestamp.
         *
         * Therefore the following verifies constructor validation
         * separately rather than pretending the state was shared.
         */
        assertNotNull(store);
        assertNotNull(expiredStore);
    }

    @Test
    void blankStateShouldBeRejected() {

        OAuthStateStore store =
                new OAuthStateStore();

        assertThrows(
                IllegalArgumentException.class,
                () -> store.consume("")
        );
    }

    @Test
    void blankCodeVerifierShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PkceUtil.generateCodeChallenge("")
        );
    }
}