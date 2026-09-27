package com.medinexus.service;

import com.medinexus.model.Medicine;
import com.medinexus.repository.MedicineJdbcRepository;

import java.util.List;

public class MedicineService {

    private final MedicineJdbcRepository repository =
            new MedicineJdbcRepository();

    // -----------------------------------------
    // Get all medicines
    // -----------------------------------------

    public List<Medicine> getAllMedicines() {

        return repository.findAll();
    }

    // -----------------------------------------
    // Get medicine by ID
    // -----------------------------------------

    public Medicine getMedicineById(int medicineId) {

        return repository.findById(medicineId);
    }

    // -----------------------------------------
    // Get medicine by name
    // -----------------------------------------

    public Medicine getMedicineByName(String name) {

        return repository.findByName(name);
    }

    // -----------------------------------------
    // Add medicine
    // -----------------------------------------

    public void addMedicine(Medicine medicine) {

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine cannot be null."
            );
        }

        if (medicine.getName() == null
                || medicine.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Medicine name is required."
            );
        }

        if (medicine.getQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Medicine quantity cannot be negative."
            );
        }

        updateAvailability(medicine);

        repository.add(medicine);
    }

    // -----------------------------------------
    // Update medicine
    // -----------------------------------------

    public void updateMedicine(Medicine medicine) {

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine cannot be null."
            );
        }

        if (medicine.getQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Medicine quantity cannot be negative."
            );
        }

        updateAvailability(medicine);

        repository.update(medicine);
    }

    // -----------------------------------------
    // Delete medicine
    // -----------------------------------------

    public void deleteMedicine(int medicineId) {

        repository.delete(medicineId);
    }

    // -----------------------------------------
    // Check availability by ID
    // -----------------------------------------

    public boolean isAvailable(
            int medicineId,
            int requiredQuantity) {

        if (requiredQuantity <= 0) {
            return false;
        }

        Medicine medicine =
                getMedicineById(medicineId);

        if (medicine == null) {
            return false;
        }

        return medicine.getQuantity()
                >= requiredQuantity;
    }

    // -----------------------------------------
    // Check availability by name
    // -----------------------------------------

    public boolean isAvailable(
            String medicineName,
            int requiredQuantity) {

        if (requiredQuantity <= 0) {
            return false;
        }

        Medicine medicine =
                getMedicineByName(medicineName);

        if (medicine == null) {
            return false;
        }

        return medicine.getQuantity()
                >= requiredQuantity;
    }

    // -----------------------------------------
    // Reduce stock by ID
    // -----------------------------------------

    public boolean reduceStock(
            int medicineId,
            int quantity) {

        if (quantity <= 0) {
            return false;
        }

        Medicine medicine =
                getMedicineById(medicineId);

        if (medicine == null) {
            return false;
        }

        return repository.reduceStock(
                medicineId,
                quantity
        );
    }

    // -----------------------------------------
    // Reduce stock by name
    // -----------------------------------------

    public boolean reduceStock(
            String medicineName,
            int quantity) {

        Medicine medicine =
                getMedicineByName(medicineName);

        if (medicine == null) {
            return false;
        }

        return reduceStock(
                medicine.getMedicineId(),
                quantity
        );
    }

    // -----------------------------------------
    // Check low stock
    // -----------------------------------------

    public boolean isLowStock(Medicine medicine) {

        if (medicine == null) {
            return false;
        }

        return medicine.getQuantity() > 0
                && medicine.getQuantity()
                <= medicine.getReorderLevel();
    }

    // -----------------------------------------
    // Calculate medicine charge
    // -----------------------------------------

    public double calculateMedicineCharge(
            int medicineId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        Medicine medicine =
                getMedicineById(medicineId);

        if (medicine == null) {
            throw new IllegalArgumentException(
                    "Medicine not found."
            );
        }

        return medicine.getUnitPrice()
                * quantity;
    }

    // -----------------------------------------
    // Update availability
    // -----------------------------------------

    private void updateAvailability(
            Medicine medicine) {

        medicine.setAvailable(
                medicine.getQuantity() > 0
        );
    }
}