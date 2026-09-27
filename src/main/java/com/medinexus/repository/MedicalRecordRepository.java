package com.medinexus.repository;

import com.medinexus.model.MedicalRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordRepository {

    private static final List<MedicalRecord> records =
            new ArrayList<>();

    public MedicalRecordRepository() {

        if (records.isEmpty()) {
            loadSampleRecords();
        }
    }

    private void loadSampleRecords() {

        records.add(
                new MedicalRecord(
                        1,
                        1,
                        101,
                        1001,
                        "Hypertension",
                        "Headache and dizziness",
                        "Blood pressure monitoring",
                        "Patient advised to reduce salt intake.",
                        LocalDateTime.now().minusDays(10)
                )
        );

        records.add(
                new MedicalRecord(
                        2,
                        2,
                        102,
                        1002,
                        "Fever",
                        "Fever and body pain",
                        "Rest and medication",
                        "Follow-up recommended after 3 days.",
                        LocalDateTime.now().minusDays(5)
                )
        );
    }

    public List<MedicalRecord> findAll() {

        return new ArrayList<>(records);
    }

    public MedicalRecord findById(int recordId) {

        for (MedicalRecord record : records) {

            if (record.getRecordId() == recordId) {
                return record;
            }
        }

        return null;
    }

    public List<MedicalRecord> findByPatientId(
            int patientId) {

        List<MedicalRecord> result =
                new ArrayList<>();

        for (MedicalRecord record : records) {

            if (record.getPatientId() == patientId) {
                result.add(record);
            }
        }

        return result;
    }

    public List<MedicalRecord> findByDoctorId(
            int doctorId) {

        List<MedicalRecord> result =
                new ArrayList<>();

        for (MedicalRecord record : records) {

            if (record.getDoctorId() == doctorId) {
                result.add(record);
            }
        }

        return result;
    }

    public void add(MedicalRecord record) {

        records.add(record);
    }
}