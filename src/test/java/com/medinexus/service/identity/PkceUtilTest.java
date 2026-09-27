package com.medinexus.service.identity;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class PkceUtilTest {

    @Test
    void generatedVerifierIsNotEmpty() {

        String verifier =
                PkceUtil.generateCodeVerifier();

        assertNotNull(verifier);
        assertFalse(verifier.isBlank());
    }

    @Test
    void generatedVerifierUsesUrlSafeEncoding() {

        String verifier =
                PkceUtil.generateCodeVerifier();

        assertFalse(
                verifier.contains("+")
        );

        assertFalse(
                verifier.contains("/")
        );

        assertFalse(
                verifier.contains("=")
        );
    }

    @Test
    void generatedVerifiersShouldNotAllBeIdentical() {

        String first =
                PkceUtil.generateCodeVerifier();

        String second =
                PkceUtil.generateCodeVerifier();

        assertNotEquals(
                first,
                second
        );
    }

    @Test
    void emptyVerifierCannotGenerateChallenge() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PkceUtil.generateCodeChallenge("")
        );
    }

    @Test
    void nullVerifierCannotGenerateChallenge() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PkceUtil.generateCodeChallenge(null)
        );
    }

    @Test
    void codeChallengeUsesSha256AndBase64UrlEncoding()
            throws Exception {

        String verifier =
                "test-code-verifier-123";

        byte[] hash =
                MessageDigest
                        .getInstance("SHA-256")
                        .digest(
                                verifier.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String expected =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(hash);

        String actual =
                PkceUtil.generateCodeChallenge(
                        verifier
                );

        assertEquals(
                expected,
                actual
        );
    }
}