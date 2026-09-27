package com.medinexus.repository;

import com.medinexus.model.Prescription;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionRepository {

    private static final List<Prescription> prescriptions =
            new ArrayList<>();

    // ---------------------------------------------------------
    // SAMPLE DATA
    // ---------------------------------------------------------

    static {

        prescriptions.add(
                new Prescription(
                        1,
                        1,
                        1,
                        101,
                        1,              // Amlodipine
                        30,             // Quantity
                        "5 mg",
                        "Once daily",
                        "30 days",
                        "Take after breakfast",
                        LocalDateTime.now().minusDays(10)
                )
        );

        prescriptions.add(
                new Prescription(
                        2,
                        2,
                        2,
                        102,
                        2,              // Paracetamol
                        6,              // Quantity
                        "500 mg",
                        "Twice daily",
                        "3 days",
                        "Take after food",
                        LocalDateTime.now().minusDays(5)
                )
        );
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    public List<Prescription> findAll() {
        return new ArrayList<>(prescriptions);
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    public Prescription findById(int prescriptionId) {

        for (Prescription prescription : prescriptions) {

            if (prescription.getPrescriptionId()
                    == prescriptionId) {

                return prescription;
            }
        }

        return null;
    }

    // ---------------------------------------------------------
    // FIND BY MEDICAL RECORD
    // ---------------------------------------------------------

    public List<Prescription> findByMedicalRecordId(
            int medicalRecordId) {

        List<Prescription> result =
                new ArrayList<>();

        for (Prescription prescription : prescriptions) {

            if (prescription.getMedicalRecordId()
                    == medicalRecordId) {

                result.add(prescription);
            }
        }

        return result;
    }

    // ---------------------------------------------------------
    // FIND BY PATIENT
    // ---------------------------------------------------------

    public List<Prescription> findByPatientId(
            int patientId) {

        List<Prescription> result =
                new ArrayList<>();

        for (Prescription prescription : prescriptions) {

            if (prescription.getPatientId()
                    == patientId) {

                result.add(prescription);
            }
        }

        return result;
    }

    // ---------------------------------------------------------
    // FIND BY DOCTOR
    // ---------------------------------------------------------

    public List<Prescription> findByDoctorId(
            int doctorId) {

        List<Prescription> result =
                new ArrayList<>();

        for (Prescription prescription : prescriptions) {

            if (prescription.getDoctorId()
                    == doctorId) {

                result.add(prescription);
            }
        }

        return result;
    }

    // ---------------------------------------------------------
    // ADD
    // ---------------------------------------------------------

    public void add(Prescription prescription) {

        if (prescription != null) {
            prescriptions.add(prescription);
        }
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    public void update(Prescription updatedPrescription) {

        if (updatedPrescription == null) {
            return;
        }

        for (int i = 0; i < prescriptions.size(); i++) {

            Prescription existingPrescription =
                    prescriptions.get(i);

            if (existingPrescription.getPrescriptionId()
                    == updatedPrescription.getPrescriptionId()) {

                prescriptions.set(
                        i,
                        updatedPrescription
                );

                return;
            }
        }
    }
}