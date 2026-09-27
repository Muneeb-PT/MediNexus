package com.medinexus.repository;

import com.medinexus.model.VerificationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityVerificationAuditTest {

    private final IdentityVerificationJdbcRepository repository =
            new IdentityVerificationJdbcRepository();

    @Test
    void invalidVerificationIdCannotUpdateWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateStatusWithAudit(
                        0,
                        VerificationStatus.PENDING,
                        VerificationStatus.VERIFIED,
                        null,
                        1,
                        "VERIFIED",
                        "Test"
                )
        );
    }

    @Test
    void nullExpectedStatusCannotUpdateWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateStatusWithAudit(
                        1,
                        null,
                        VerificationStatus.VERIFIED,
                        null,
                        1,
                        "VERIFIED",
                        "Test"
                )
        );
    }

    @Test
    void nullNewStatusCannotUpdateWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateStatusWithAudit(
                        1,
                        VerificationStatus.PENDING,
                        null,
                        null,
                        1,
                        "VERIFIED",
                        "Test"
                )
        );
    }

    @Test
    void invalidPerformingUserCannotUpdateWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateStatusWithAudit(
                        1,
                        VerificationStatus.PENDING,
                        VerificationStatus.VERIFIED,
                        null,
                        0,
                        "VERIFIED",
                        "Test"
                )
        );
    }

    @Test
    void emptyAuditActionCannotUpdateWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.updateStatusWithAudit(
                        1,
                        VerificationStatus.PENDING,
                        VerificationStatus.VERIFIED,
                        null,
                        1,
                        "",
                        "Test"
                )
        );
    }

    @Test
    void nullVerificationCannotBeAddedWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.addWithAudit(
                        null,
                        null,
                        "SUBMITTED",
                        "Test"
                )
        );
    }

    @Test
    void emptyAuditActionCannotBeAddedWithAudit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.addWithAudit(
                        null,
                        null,
                        "",
                        "Test"
                )
        );
    }
}