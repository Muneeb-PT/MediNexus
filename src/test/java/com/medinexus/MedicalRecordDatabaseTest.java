package com.medinexus;

import com.medinexus.model.MedicalRecord;
import com.medinexus.repository.MedicalRecordJdbcRepository;

import java.util.List;

public class MedicalRecordDatabaseTest {

    public static void main(String[] args) {

        MedicalRecordJdbcRepository repository =
                new MedicalRecordJdbcRepository();

        System.out.println("======================================");
        System.out.println("   MEDINEXUS MEDICAL RECORD DB TEST");
        System.out.println("======================================");

        // -----------------------------------------
        // 1. FIND ALL
        // -----------------------------------------

        System.out.println("\n1. All medical records:");

        List<MedicalRecord> records =
                repository.findAll();

        for (MedicalRecord record : records) {
            printRecord(record);
        }

        // -----------------------------------------
        // 2. FIND BY ID
        // -----------------------------------------

        System.out.println("\n2. Find record ID 1:");

        MedicalRecord record =
                repository.findById(1);

        if (record != null) {
            printRecord(record);
        } else {
            System.out.println(
                    "Medical record not found."
            );
        }

        // -----------------------------------------
        // 3. FIND BY PATIENT
        // -----------------------------------------

        System.out.println(
                "\n3. Records for patient 1:"
        );

        List<MedicalRecord> patientRecords =
                repository.findByPatientId(1);

        for (MedicalRecord r : patientRecords) {
            printRecord(r);
        }

        // -----------------------------------------
        // 4. FIND BY DOCTOR
        // -----------------------------------------

        System.out.println(
                "\n4. Records for doctor 101:"
        );

        List<MedicalRecord> doctorRecords =
                repository.findByDoctorId(101);

        for (MedicalRecord r : doctorRecords) {
            printRecord(r);
        }

        System.out.println("\n======================================");
        System.out.println("        TEST COMPLETED");
        System.out.println("======================================");

        // -----------------------------------------
// 5. CREATE NEW MEDICAL RECORD
// -----------------------------------------

        System.out.println(
                "\n5. Creating a new medical record..."
        );

        MedicalRecord newRecord =
                new MedicalRecord(
                        9999,
                        1,
                        101,
                        1,
                        "Test Diagnosis",
                        "Test symptoms",
                        "Test treatment",
                        "JDBC integration test record.",
                        java.time.LocalDateTime.now()
                );

        repository.add(newRecord);

        System.out.println(
                "Medical record 9999 created successfully."
        );

// -----------------------------------------
// 6. VERIFY CREATED RECORD
// -----------------------------------------

        System.out.println(
                "\n6. Reading newly created record..."
        );

        MedicalRecord createdRecord =
                repository.findById(9999);

        if (createdRecord != null) {

            printRecord(createdRecord);

        } else {

            System.out.println(
                    "ERROR: Created record was not found."
            );
        }
    }

    private static void printRecord(
            MedicalRecord record) {

        System.out.println("--------------------------------------");
        System.out.println(
                "Record ID      : "
                        + record.getRecordId()
        );
        System.out.println(
                "Patient ID     : "
                        + record.getPatientId()
        );
        System.out.println(
                "Doctor ID      : "
                        + record.getDoctorId()
        );
        System.out.println(
                "Appointment ID : "
                        + record.getAppointmentId()
        );
        System.out.println(
                "Diagnosis      : "
                        + record.getDiagnosis()
        );
        System.out.println(
                "Symptoms       : "
                        + record.getSymptoms()
        );
        System.out.println(
                "Treatment      : "
                        + record.getTreatment()
        );
        System.out.println(
                "Notes          : "
                        + record.getNotes()
        );
        System.out.println(
                "Created At     : "
                        + record.getCreatedAt()
        );
    }
}