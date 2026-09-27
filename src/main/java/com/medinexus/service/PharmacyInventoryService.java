package com.medinexus.service;

import com.medinexus.model.Medicine;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.repository.MedicineInventoryTransactionRepository;
import com.medinexus.repository.MedicineJdbcRepository;
import com.medinexus.model.InventoryAlert;
import com.medinexus.service.SessionManager;

import java.util.List;
import java.util.ArrayList;

public class PharmacyInventoryService {

    private final MedicineJdbcRepository medicineRepository;
    private final MedicineInventoryTransactionRepository
            transactionRepository;

    public PharmacyInventoryService() {

        medicineRepository =
                new MedicineJdbcRepository();

        transactionRepository =
                new MedicineInventoryTransactionRepository();
    }

    /*
     * Restock medicine.
     *
     * Only ADMIN and RECEPTIONIST can perform
     * inventory restocking.
     */
    public void restockMedicine(
            int medicineId,
            int quantity,
            String notes) {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        if (medicineId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medicine ID."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Restock quantity must be greater than zero."
            );
        }

        Medicine medicine =
                medicineRepository.findById(
                        medicineId
                );

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine not found: "
                            + medicineId
            );
        }

        transactionRepository.restock(
                medicineId,
                quantity,
                user.getUserId(),
                "RESTOCK",
                null,
                normalizeNotes(notes)
        );
    }

    /*
     * Restock with an external purchase/order reference.
     */
    public void restockMedicine(
            int medicineId,
            int quantity,
            String referenceType,
            Long referenceId,
            String notes) {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        if (medicineId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medicine ID."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Restock quantity must be greater than zero."
            );
        }

        if (referenceId != null
                && referenceId <= 0) {

            throw new IllegalArgumentException(
                    "Reference ID must be positive."
            );
        }

        Medicine medicine =
                medicineRepository.findById(
                        medicineId
                );

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine not found: "
                            + medicineId
            );
        }

        transactionRepository.restock(
                medicineId,
                quantity,
                user.getUserId(),
                normalizeReferenceType(
                        referenceType
                ),
                referenceId,
                normalizeNotes(notes)
        );
    }

    /*
     * Medicine lookup.
     *
     * ADMIN, RECEPTIONIST and DOCTOR can use this.
     * Patients cannot access pharmacy inventory.
     */
    public List<Medicine> getMedicines() {

        User user = requireLoggedInUser();

        Role role = user.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "Patients are not authorized to access medicine inventory."
            );
        }

        return medicineRepository.findAll();
    }

    /*
     * Find one medicine.
     */
    public Medicine getMedicine(
            int medicineId) {

        User user = requireLoggedInUser();

        Role role = user.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "You are not authorized to access medicine information."
            );
        }

        if (medicineId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medicine ID."
            );
        }

        Medicine medicine =
                medicineRepository.findById(
                        medicineId
                );

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine not found: "
                            + medicineId
            );
        }

        return medicine;
    }

    /*
     * Search medicine by exact name.
     */
    public Medicine getMedicineByName(
            String name) {

        User user = requireLoggedInUser();

        Role role = user.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "You are not authorized to search medicines."
            );
        }

        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Medicine name is required."
            );
        }

        return medicineRepository.findByName(
                name.trim()
        );
    }

    /*
     * Inventory history.
     *
     * Only ADMIN and RECEPTIONIST can view
     * operational inventory transactions.
     */
    public List<
            MedicineInventoryTransactionRepository.InventoryTransaction>
    getInventoryHistory() {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        return transactionRepository.findAll();
    }

    /*
     * Inventory history for a specific medicine.
     */
    public List<
            MedicineInventoryTransactionRepository.InventoryTransaction>
    getInventoryHistory(
            int medicineId) {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        if (medicineId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medicine ID."
            );
        }

        return transactionRepository
                .findByMedicineId(
                        medicineId
                );
    }

    /*
     * Returns medicines that have reached
     * or fallen below their reorder level.
     */
    public List<Medicine> getLowStockMedicines() {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        return medicineRepository.findAll()
                .stream()
                .filter(medicine ->
                        medicine.getQuantity()
                                <= medicine.getReorderLevel())
                .toList();
    }

    /*
     * Returns medicines with zero stock.
     */
    public List<Medicine> getOutOfStockMedicines() {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        return medicineRepository.findAll()
                .stream()
                .filter(medicine ->
                        medicine.getQuantity() <= 0)
                .toList();
    }

    /*
     * Checks whether a medicine needs restocking.
     */
    public boolean needsRestocking(
            int medicineId) {

        User user = requireLoggedInUser();

        requireInventoryManagementAccess(user);

        Medicine medicine =
                getMedicine(
                        medicineId
                );

        return medicine.getQuantity()
                <= medicine.getReorderLevel();
    }

    /*
     * Checks whether medicine is currently
     * available in stock.
     */
    public boolean isInStock(
            int medicineId) {

        User user = requireLoggedInUser();

        Role role = user.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "You are not authorized to check medicine stock."
            );
        }

        Medicine medicine =
                getMedicine(
                        medicineId
                );

        return medicine.getQuantity() > 0;
    }

    /*
     * Returns current stock quantity.
     */
    public int getStockQuantity(
            int medicineId) {

        User user = requireLoggedInUser();

        Role role = user.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "You are not authorized to view medicine stock."
            );
        }

        Medicine medicine =
                getMedicine(
                        medicineId
                );

        return medicine.getQuantity();
    }

    /*
     * Only ADMIN and RECEPTIONIST may modify
     * inventory.
     */
    private void requireInventoryManagementAccess(
            User user) {

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can manage pharmacy inventory."
            );
        }
    }

    private User requireLoggedInUser() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {
            throw new SecurityException(
                    "You must be logged in."
            );
        }

        return user;
    }

    private String normalizeNotes(
            String notes) {

        if (notes == null) {
            return null;
        }

        String value =
                notes.trim();

        return value.isEmpty()
                ? null
                : value;
    }

    private String normalizeReferenceType(
            String referenceType) {

        if (referenceType == null) {
            return null;
        }

        String value =
                referenceType.trim();

        return value.isEmpty()
                ? null
                : value;
    }

    // =========================================================
// INVENTORY ALERTS
// =========================================================

    public List<InventoryAlert> getInventoryAlerts() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "User must be logged in."
            );
        }

        Role role =
                currentUser.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can view inventory alerts."
            );
        }

        MedicineService medicineService =
                new MedicineService();

        List<Medicine> medicines =
                medicineService.getAllMedicines();

        List<InventoryAlert> alerts =
                new ArrayList<>();

        for (Medicine medicine : medicines) {

            if (medicine == null) {
                continue;
            }

            int quantity =
                    medicine.getQuantity();

            int reorderLevel =
                    medicine.getReorderLevel();

            if (quantity <= 0) {

                alerts.add(
                        new InventoryAlert(
                                medicine.getMedicineId(),
                                medicine.getName(),
                                quantity,
                                reorderLevel,
                                InventoryAlert.AlertType.OUT_OF_STOCK
                        )
                );

            } else if (quantity <= reorderLevel) {

                alerts.add(
                        new InventoryAlert(
                                medicine.getMedicineId(),
                                medicine.getName(),
                                quantity,
                                reorderLevel,
                                InventoryAlert.AlertType.LOW_STOCK
                        )
                );
            }
        }

        return alerts;
    }
}