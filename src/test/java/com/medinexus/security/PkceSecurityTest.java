package com.medinexus.security;

import com.medinexus.service.identity.PkceUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PkceSecurityTest {

    @Test
    void codeVerifierShouldBeGenerated() {

        String verifier =
                PkceUtil.generateCodeVerifier();

        assertNotNull(verifier);
        assertFalse(verifier.isBlank());
    }

    @Test
    void generatedCodeVerifierShouldProduceChallenge() {

        String verifier =
                PkceUtil.generateCodeVerifier();

        String challenge =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        assertNotNull(challenge);
        assertFalse(challenge.isBlank());
    }

    @Test
    void sameVerifierShouldProduceSameChallenge() {

        String verifier =
                "test-code-verifier-123";

        String first =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        String second =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        assertEquals(
                first,
                second
        );
    }

    @Test
    void differentVerifiersShouldProduceDifferentChallenges() {

        String firstVerifier =
                "first-code-verifier";

        String secondVerifier =
                "second-code-verifier";

        String firstChallenge =
                PkceUtil.generateCodeChallenge(
                        firstVerifier
                );

        String secondChallenge =
                PkceUtil.generateCodeChallenge(
                        secondVerifier
                );

        assertNotEquals(
                firstChallenge,
                secondChallenge
        );
    }

    @Test
    void nullVerifierShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PkceUtil.generateCodeChallenge(null)
        );
    }

    @Test
    void blankVerifierShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PkceUtil.generateCodeChallenge(" ")
        );
    }
}