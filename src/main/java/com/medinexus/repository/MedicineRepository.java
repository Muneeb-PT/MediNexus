package com.medinexus.repository;

import com.medinexus.model.Medicine;

import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {

    private static final List<Medicine> medicines =
            new ArrayList<>();

    public MedicineRepository() {

        if (medicines.isEmpty()) {
            loadSampleMedicines();
        }
    }

    private void loadSampleMedicines() {

        medicines.add(
                new Medicine(
                        1,
                        "Amlodipine",
                        "Blood Pressure",
                        "ABC Pharma",
                        4.50,
                        100,
                        20,
                        true
                )
        );

        medicines.add(
                new Medicine(
                        2,
                        "Paracetamol",
                        "Pain Relief",
                        "MediCare Pharma",
                        2.00,
                        250,
                        50,
                        true
                )
        );

        medicines.add(
                new Medicine(
                        3,
                        "Azithromycin",
                        "Antibiotic",
                        "HealthPlus",
                        12.50,
                        8,
                        20,
                        true
                )
        );

        medicines.add(
                new Medicine(
                        4,
                        "Omeprazole",
                        "Gastric",
                        "Wellness Pharma",
                        3.75,
                        0,
                        15,
                        false
                )
        );
    }

    public List<Medicine> findAll() {
        return new ArrayList<>(medicines);
    }

    public Medicine findById(int medicineId) {

        for (Medicine medicine : medicines) {

            if (medicine.getMedicineId() == medicineId) {
                return medicine;
            }
        }

        return null;
    }

    public Medicine findByName(String name) {

        if (name == null) {
            return null;
        }

        for (Medicine medicine : medicines) {

            if (medicine.getName()
                    .equalsIgnoreCase(name.trim())) {

                return medicine;
            }
        }

        return null;
    }

    public void add(Medicine medicine) {

        if (medicine != null) {
            medicines.add(medicine);
        }
    }

    public void update(Medicine updatedMedicine) {

        if (updatedMedicine == null) {
            return;
        }

        for (int i = 0; i < medicines.size(); i++) {

            if (medicines.get(i).getMedicineId()
                    == updatedMedicine.getMedicineId()) {

                medicines.set(i, updatedMedicine);
                return;
            }
        }
    }

    public void delete(int medicineId) {

        medicines.removeIf(
                medicine ->
                        medicine.getMedicineId()
                                == medicineId
        );
    }
}