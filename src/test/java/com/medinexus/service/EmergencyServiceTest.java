package com.medinexus.service;

import com.medinexus.model.EmergencyRequest;
import com.medinexus.model.EmergencyStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.model.VehicleStatus;
import com.medinexus.repository.EmergencyRequestRepository;
import com.medinexus.repository.EmergencyVehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyServiceTest {

    private EmergencyService emergencyService;

    @BeforeEach
    void setUp() {
        emergencyService =
                new EmergencyService();

        SessionManager.logout();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    private void login(
            int userId,
            String username,
            String fullName,
            Role role) {

        SessionManager.login(
                new User(
                        userId,
                        username,
                        fullName,
                        role,
                        "test-password-hash"
                )
        );
    }

    @Test
    void unauthenticatedUserCannotViewEmergencies() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .getEmergencyRequests()
        );
    }

    @Test
    void unauthenticatedUserCannotDispatchVehicle() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .dispatchVehicle(
                                        999999L,
                                        999999L
                                )
        );
    }

    @Test
    void patientCanViewOwnEmergencyRequests() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        assertNotNull(requests);

        for (EmergencyRequest request : requests) {

            assertTrue(
                    request.getPatientId() > 0
            );
        }
    }

    @Test
    void doctorCanMonitorEmergencyRequests() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        assertNotNull(requests);
    }

    @Test
    void receptionistCanViewEmergencyRequests() {

        login(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        assertNotNull(requests);
    }

    @Test
    void adminCanViewEmergencyRequests() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        assertNotNull(requests);
    }

    @Test
    void patientCannotAccessAvailableVehicles() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .getAvailableVehicles()
        );
    }

    @Test
    void doctorCannotAccessAvailableVehicles() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .getAvailableVehicles()
        );
    }

    @Test
    void receptionistCanAccessAvailableVehicles() {

        login(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST
        );

        List<?> vehicles =
                emergencyService
                        .getAvailableVehicles();

        assertNotNull(vehicles);
    }

    @Test
    void adminCanAccessAvailableVehicles() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        List<?> vehicles =
                emergencyService
                        .getAvailableVehicles();

        assertNotNull(vehicles);
    }

    @Test
    void patientCannotDispatchVehicle() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .dispatchVehicle(
                                        999999L,
                                        999999L
                                )
        );
    }

    @Test
    void doctorCannotDispatchVehicle() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .dispatchVehicle(
                                        999999L,
                                        999999L
                                )
        );
    }

    @Test
    void patientCannotReturnVehicle() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .returnVehicle(
                                        999999L
                                )
        );
    }

    @Test
    void doctorCannotReturnVehicle() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        emergencyService
                                .returnVehicle(
                                        999999L
                                )
        );
    }

    @Test
    void invalidEmergencyIdIsRejected() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        emergencyService
                                .getEmergency(
                                        Long.MAX_VALUE
                                )
        );
    }

    @Test
    void invalidVehicleIdIsRejected() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        emergencyService
                                .dispatchVehicle(
                                        Long.MAX_VALUE,
                                        Long.MAX_VALUE
                                )
        );
    }

    @Test
    void returnVehicleRejectsNonReturningVehicle() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        List<
                com.medinexus.model.EmergencyVehicle
                > vehicles =
                emergencyService.getAllVehicles();

        assertFalse(
                vehicles.isEmpty()
        );

        var vehicle =
                vehicles.get(0);

        if (vehicle.getStatus()
                != VehicleStatus.RETURNING) {

            assertThrows(
                    IllegalStateException.class,
                    () ->
                            emergencyService
                                    .returnVehicle(
                                            vehicle.getVehicleId()
                                    )
            );
        }
    }

    @Test
    void adminCanReadExistingEmergency() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        if (!requests.isEmpty()) {

            EmergencyRequest request =
                    requests.get(0);

            EmergencyRequest loaded =
                    emergencyService.getEmergency(
                            request.getEmergencyId()
                    );

            assertNotNull(loaded);

            assertEquals(
                    request.getEmergencyId(),
                    loaded.getEmergencyId()
            );
        }
    }

    @Test
    void doctorCanReadExistingEmergency() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        List<EmergencyRequest> requests =
                emergencyService
                        .getEmergencyRequests();

        if (!requests.isEmpty()) {

            EmergencyRequest request =
                    requests.get(0);

            EmergencyRequest loaded =
                    emergencyService.getEmergency(
                            request.getEmergencyId()
                    );

            assertNotNull(loaded);
        }
    }
}