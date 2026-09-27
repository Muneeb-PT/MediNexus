package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyInventoryServiceTest {

    private PharmacyInventoryService inventoryService;

    @BeforeEach
    void setUp() {

        inventoryService =
                new PharmacyInventoryService();

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

    // ---------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUserCannotViewMedicines() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService.getMedicines()
        );
    }

    @Test
    void unauthenticatedUserCannotRestock() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService.restockMedicine(
                                1,
                                10,
                                "Test restock"
                        )
        );
    }

    @Test
    void unauthenticatedUserCannotViewHistory() {

        SessionManager.logout();

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getInventoryHistory()
        );
    }

    // ---------------------------------------------------------
    // PATIENT SECURITY
    // ---------------------------------------------------------

    @Test
    void patientCannotViewMedicineInventory() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService.getMedicines()
        );
    }

    @Test
    void patientCannotRestockMedicine() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService.restockMedicine(
                                1,
                                10,
                                "Unauthorized restock"
                        )
        );
    }

    @Test
    void patientCannotViewInventoryHistory() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getInventoryHistory()
        );
    }

    @Test
    void patientCannotCheckStock() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getStockQuantity(1)
        );
    }

    // ---------------------------------------------------------
    // DOCTOR SECURITY
    // ---------------------------------------------------------

    @Test
    void doctorCanViewMedicineInventory() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertDoesNotThrow(
                () ->
                        inventoryService.getMedicines()
        );
    }

    @Test
    void doctorCanCheckMedicineStock() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getStockQuantity(1)
        );
    }

    @Test
    void doctorCannotRestockMedicine() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService.restockMedicine(
                                1,
                                10,
                                "Doctor restock"
                        )
        );
    }

    @Test
    void doctorCannotViewInventoryHistory() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getInventoryHistory()
        );
    }

    // ---------------------------------------------------------
    // RECEPTIONIST SECURITY
    // ---------------------------------------------------------

    @Test
    void receptionistCanViewMedicineInventory() {

        login(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST
        );

        assertDoesNotThrow(
                () ->
                        inventoryService.getMedicines()
        );
    }

    @Test
    void receptionistCanViewInventoryHistory() {

        login(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getInventoryHistory()
        );
    }

    @Test
    void adminCanViewMedicineInventory() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertDoesNotThrow(
                () ->
                        inventoryService.getMedicines()
        );
    }

    @Test
    void adminCanViewInventoryHistory() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getInventoryHistory()
        );
    }

    // ---------------------------------------------------------
    // INPUT VALIDATION
    // ---------------------------------------------------------

    @Test
    void restockRejectsInvalidMedicineId() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.restockMedicine(
                                0,
                                10,
                                "Invalid medicine"
                        )
        );
    }

    @Test
    void restockRejectsNegativeMedicineId() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.restockMedicine(
                                -1,
                                10,
                                "Invalid medicine"
                        )
        );
    }

    @Test
    void restockRejectsZeroQuantity() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.restockMedicine(
                                1,
                                0,
                                "Invalid quantity"
                        )
        );
    }

    @Test
    void restockRejectsNegativeQuantity() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.restockMedicine(
                                1,
                                -5,
                                "Invalid quantity"
                        )
        );
    }

    @Test
    void restockRejectsUnknownMedicine() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.restockMedicine(
                                999999999,
                                10,
                                "Unknown medicine"
                        )
        );
    }

    @Test
    void medicineLookupRejectsInvalidId() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.getMedicine(0)
        );
    }

    @Test
    void medicineLookupRejectsUnknownMedicine() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService.getMedicine(
                                999999999
                        )
        );
    }

    @Test
    void medicineNameLookupRejectsBlankName() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService
                                .getMedicineByName(
                                        "   "
                                )
        );
    }

    @Test
    void historyRejectsInvalidMedicineId() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        inventoryService
                                .getInventoryHistory(0)
        );
    }

    // ---------------------------------------------------------
    // REORDER / STOCK CHECKS
    // ---------------------------------------------------------

    @Test
    void lowStockQueryIsAvailableToAdmin() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getLowStockMedicines()
        );
    }

    @Test
    void lowStockQueryIsAvailableToReceptionist() {

        login(
                500,
                "receptionist",
                "MediNexus Receptionist",
                Role.RECEPTIONIST
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getLowStockMedicines()
        );
    }

    @Test
    void patientCannotAccessLowStockInformation() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getLowStockMedicines()
        );
    }

    @Test
    void doctorCannotAccessLowStockInformation() {

        login(
                101,
                "doctor",
                "Dr. Ahmed",
                Role.DOCTOR
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getLowStockMedicines()
        );
    }

    @Test
    void outOfStockQueryIsAvailableToAdmin() {

        login(
                900,
                "admin",
                "MediNexus Administrator",
                Role.ADMIN
        );

        assertDoesNotThrow(
                () ->
                        inventoryService
                                .getOutOfStockMedicines()
        );
    }

    @Test
    void patientCannotAccessOutOfStockInformation() {

        login(
                1,
                "patient",
                "Mohammed Muneeb",
                Role.PATIENT
        );

        assertThrows(
                SecurityException.class,
                () ->
                        inventoryService
                                .getOutOfStockMedicines()
        );
    }
}