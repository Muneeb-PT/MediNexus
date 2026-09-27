package com.medinexus.config;

import com.medinexus.model.Patient;
import com.medinexus.service.PatientService;

import java.util.List;

public class PatientServiceDatabaseTest {

    public static void main(String[] args) {

        PatientService service = new PatientService();

        System.out.println("=== PATIENT DATABASE TEST ===");

        List<Patient> patients =
                service.getAllPatients();

        System.out.println(
                "Total patients: " + patients.size()
        );

        for (Patient patient : patients) {

            System.out.println(
                    patient.getPatientId()
                            + " | "
                            + patient.getName()
                            + " | User ID: "
                            + patient.getUserId()
            );
        }

        System.out.println();

        Patient patient =
                service.getPatientById(1);

        if (patient != null) {

            System.out.println(
                    "Patient ID 1: "
                            + patient.getName()
            );
        } else {

            System.out.println(
                    "Patient ID 1 not found."
            );
        }

        Patient userPatient =
                service.getPatientByUserId(1);

        if (userPatient != null) {

            System.out.println(
                    "User ID 1 belongs to: "
                            + userPatient.getName()
            );
        } else {

            System.out.println(
                    "No patient found for User ID 1."
            );
        }
    }
}