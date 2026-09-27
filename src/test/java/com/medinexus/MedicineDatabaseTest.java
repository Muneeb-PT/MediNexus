package com.medinexus;

import com.medinexus.model.Medicine;
import com.medinexus.repository.MedicineJdbcRepository;

import java.util.List;

public class MedicineDatabaseTest {

    public static void main(String[] args) {

        MedicineJdbcRepository repository =
                new MedicineJdbcRepository();

        System.out.println("======================================");
        System.out.println("     MEDINEXUS MEDICINE DB TEST");
        System.out.println("======================================");

        // -----------------------------------------
        // 1. FIND ALL
        // -----------------------------------------

        System.out.println("\n1. All medicines:");

        List<Medicine> medicines =
                repository.findAll();

        for (Medicine medicine : medicines) {
            printMedicine(medicine);
        }

        // -----------------------------------------
        // 2. FIND BY ID
        // -----------------------------------------

        System.out.println("\n2. Find medicine ID 1:");

        Medicine medicine =
                repository.findById(1);

        if (medicine != null) {
            printMedicine(medicine);
        } else {
            System.out.println(
                    "Medicine not found."
            );
        }

        // -----------------------------------------
        // 3. FIND BY NAME
        // -----------------------------------------

        System.out.println(
                "\n3. Find medicine by name:"
        );

        Medicine byName =
                repository.findByName(
                        "Paracetamol 500mg"
                );

        if (byName != null) {
            printMedicine(byName);
        } else {
            System.out.println(
                    "Medicine not found."
            );
        }

        System.out.println("\n======================================");
        System.out.println("          TEST COMPLETED");
        System.out.println("======================================");
    }

    private static void printMedicine(
            Medicine medicine) {

        System.out.println("--------------------------------------");
        System.out.println(
                "Medicine ID  : "
                        + medicine.getMedicineId()
        );
        System.out.println(
                "Name         : "
                        + medicine.getName()
        );
        System.out.println(
                "Category     : "
                        + medicine.getCategory()
        );
        System.out.println(
                "Manufacturer : "
                        + medicine.getManufacturer()
        );
        System.out.println(
                "Unit Price   : "
                        + medicine.getUnitPrice()
        );
        System.out.println(
                "Quantity     : "
                        + medicine.getQuantity()
        );
        System.out.println(
                "Reorder Level: "
                        + medicine.getReorderLevel()
        );
        System.out.println(
                "Available    : "
                        + medicine.isAvailable()
        );
    }
}