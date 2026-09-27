package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityVerificationSecurityTest {

    @AfterEach
    void cleanup() {
        SessionManager.logout();
    }

    @Test
    void patientCannotApproveVerification() {

        User patient =
                new User(
                        1,
                        "patient",
                        "Patient User",
                        Role.PATIENT,
                        null
                );

        SessionManager.login(patient);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                SecurityException.class,
                () -> service.approveVerification(1)
        );
    }

    @Test
    void doctorCannotApproveVerification() {

        User doctor =
                new User(
                        2,
                        "doctor",
                        "Doctor User",
                        Role.DOCTOR,
                        null
                );

        SessionManager.login(doctor);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                SecurityException.class,
                () -> service.approveVerification(1)
        );
    }

    @Test
    void receptionistCannotApproveVerification() {

        User receptionist =
                new User(
                        3,
                        "receptionist",
                        "Receptionist User",
                        Role.RECEPTIONIST,
                        null
                );

        SessionManager.login(receptionist);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                SecurityException.class,
                () -> service.approveVerification(1)
        );
    }

    @Test
    void patientCannotRejectVerification() {

        User patient =
                new User(
                        1,
                        "patient",
                        "Patient User",
                        Role.PATIENT,
                        null
                );

        SessionManager.login(patient);

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                SecurityException.class,
                () -> service.rejectVerification(1)
        );
    }

    @Test
    void unauthenticatedUserCannotAccessVerificationQueue() {

        SessionManager.logout();

        IdentityVerificationService service =
                new IdentityVerificationService(
                        new DemoVerificationProvider()
                );

        assertThrows(
                SecurityException.class,
                service::getPendingVerifications
        );
    }
}