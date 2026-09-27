package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityVerificationTransitionTest {

    @AfterEach
    void cleanup() {
        SessionManager.logout();
    }

    @Test
    void invalidVerificationIdCannotBeApproved() {

        User admin =
                new User(
                        100,
                        "admin",
                        "Admin User",
                        Role.ADMIN,
                        null
                );

        SessionManager.login(admin);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveVerification(0)
        );
    }

    @Test
    void invalidVerificationIdCannotBeRejected() {

        User admin =
                new User(
                        100,
                        "admin",
                        "Admin User",
                        Role.ADMIN,
                        null
                );

        SessionManager.login(admin);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectVerification(0)
        );
    }

    @Test
    void invalidVerificationIdCannotBeLoaded() {

        User admin =
                new User(
                        100,
                        "admin",
                        "Admin User",
                        Role.ADMIN,
                        null
                );

        SessionManager.login(admin);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getVerificationById(0)
        );
    }
}