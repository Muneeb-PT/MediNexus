package com.medinexus;

import com.medinexus.model.Prescription;
import com.medinexus.repository.PrescriptionJdbcRepository;

import java.util.List;

public class PrescriptionDatabaseTest {

    public static void main(String[] args) {

        PrescriptionJdbcRepository repository =
                new PrescriptionJdbcRepository();

        System.out.println("========================================");
        System.out.println("   PRESCRIPTION JDBC DATABASE TEST");
        System.out.println("========================================");

        // Test 1: Get all prescriptions
        System.out.println("\n1. Fetching all prescriptions...");

        List<Prescription> prescriptions =
                repository.findAll();

        System.out.println(
                "Total prescriptions: "
                        + prescriptions.size()
        );

        for (Prescription prescription : prescriptions) {

            System.out.println(
                    "ID: " + prescription.getPrescriptionId()
                            + " | Patient: " + prescription.getPatientId()
                            + " | Doctor: " + prescription.getDoctorId()
                            + " | Medicine: " + prescription.getMedicineId()
                            + " | Quantity: " + prescription.getQuantity()
                            + " | Status: " + prescription.getStatus()
            );
        }

        // Test 2: Find prescription by ID
        System.out.println("\n2. Finding prescription ID 1...");

        Prescription prescription =
                repository.findById(1);

        if (prescription != null) {

            System.out.println(
                    "Found prescription:"
            );

            System.out.println(
                    "Prescription ID: "
                            + prescription.getPrescriptionId()
            );

            System.out.println(
                    "Medical Record ID: "
                            + prescription.getMedicalRecordId()
            );

            System.out.println(
                    "Patient ID: "
                            + prescription.getPatientId()
            );

            System.out.println(
                    "Doctor ID: "
                            + prescription.getDoctorId()
            );

            System.out.println(
                    "Medicine ID: "
                            + prescription.getMedicineId()
            );

            System.out.println(
                    "Quantity: "
                            + prescription.getQuantity()
            );

            System.out.println(
                    "Status: "
                            + prescription.getStatus()
            );

        } else {

            System.out.println(
                    "Prescription ID 1 not found."
            );
        }

        // Test 3: Find by patient
        System.out.println("\n3. Finding prescriptions for patient 1...");

        List<Prescription> patientPrescriptions =
                repository.findByPatientId(1);

        System.out.println(
                "Patient 1 prescriptions: "
                        + patientPrescriptions.size()
        );

        // Test 4: Find by doctor
        System.out.println("\n4. Finding prescriptions for doctor 101...");

        List<Prescription> doctorPrescriptions =
                repository.findByDoctorId(101);

        System.out.println(
                "Doctor 101 prescriptions: "
                        + doctorPrescriptions.size()
        );

        // Test 5: Find by medical record
        System.out.println("\n5. Finding prescriptions for medical record 1...");

        List<Prescription> recordPrescriptions =
                repository.findByMedicalRecordId(1);

        System.out.println(
                "Medical record 1 prescriptions: "
                        + recordPrescriptions.size()
        );

        System.out.println("\n========================================");
        System.out.println("   PRESCRIPTION JDBC TEST COMPLETED");
        System.out.println("========================================");
    }
}