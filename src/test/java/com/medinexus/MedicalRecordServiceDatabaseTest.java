package com.medinexus;

import com.medinexus.model.MedicalRecord;
import com.medinexus.service.MedicalRecordService;

import java.time.LocalDateTime;
import java.util.List;

public class MedicalRecordServiceDatabaseTest {

    public static void main(String[] args) {

        MedicalRecordService service =
                new MedicalRecordService();

        System.out.println(
                "======================================"
        );

        System.out.println(
                " MEDINEXUS MEDICAL RECORD SERVICE TEST"
        );

        System.out.println(
                "======================================"
        );

        // -----------------------------------------
        // 1. READ ALL
        // -----------------------------------------

        System.out.println(
                "\n1. Reading medical records..."
        );

        List<MedicalRecord> records =
                service.getAllRecords();

        System.out.println(
                "Total records: " + records.size()
        );

        for (MedicalRecord record : records) {

            System.out.println(
                    "Record ID: "
                            + record.getRecordId()
                            + " | Patient: "
                            + record.getPatientId()
                            + " | Doctor: "
                            + record.getDoctorId()
            );
        }

        // -----------------------------------------
        // 2. READ PATIENT RECORDS
        // -----------------------------------------

        System.out.println(
                "\n2. Patient 1 records:"
        );

        List<MedicalRecord> patientRecords =
                service.getPatientRecords(1);

        for (MedicalRecord record :
                patientRecords) {

            System.out.println(
                    "Record ID: "
                            + record.getRecordId()
            );
        }

        // -----------------------------------------
        // 3. READ DOCTOR RECORDS
        // -----------------------------------------

        System.out.println(
                "\n3. Doctor 101 records:"
        );

        List<MedicalRecord> doctorRecords =
                service.getDoctorRecords(101);

        for (MedicalRecord record :
                doctorRecords) {

            System.out.println(
                    "Record ID: "
                            + record.getRecordId()
            );
        }

        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "        SERVICE TEST COMPLETED"
        );

        System.out.println(
                "======================================"
        );
    }
}