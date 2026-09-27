package com.medinexus.repository;

import com.medinexus.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    private final List<Patient> patients;

    public PatientRepository() {

        patients = new ArrayList<>();

        loadSamplePatients();
    }

    private void loadSamplePatients() {

        patients.add(
                new Patient(
                        1,
                        1,
                        "Mohammed Muneeb",
                        20,
                        "Male",
                        "9876543210",
                        "O+"
                )
        );

        patients.add(
                new Patient(
                        2,
                        2,
                        "Aisha Rahman",
                        25,
                        "Female",
                        "9876543211",
                        "A+"
                )
        );

        patients.add(
                new Patient(
                        3,
                        3,
                        "Ahmed Ali",
                        45,
                        "Male",
                        "9876543212",
                        "B+"
                )
        );
    }

    public Patient findByUserId(int userId) {

        for (Patient patient : patients) {

            if (patient.getUserId() == userId) {
                return patient;
            }
        }

        return null;
    }

    public Patient findByPatientId(int patientId) {

        for (Patient patient : patients) {

            if (patient.getPatientId() == patientId) {
                return patient;
            }
        }

        return null;
    }

    public List<Patient> findAll() {

        return new ArrayList<>(patients);
    }
}