package com.medinexus.config;

import com.medinexus.model.Patient;
import com.medinexus.repository.PatientJdbcRepository;

import java.util.List;

public class PatientDatabaseTest {

    public static void main(String[] args) {

        PatientJdbcRepository repository =
                new PatientJdbcRepository();

        List<Patient> patients =
                repository.findAll();

        System.out.println("Patients loaded from MySQL:");
        System.out.println("--------------------------------");

        for (Patient patient : patients) {

            System.out.println(
                    patient.getPatientId()
                            + " | "
                            + patient.getName()
                            + " | "
                            + patient.getAge()
                            + " | "
                            + patient.getGender()
                            + " | "
                            + patient.getPhone()
                            + " | "
                            + patient.getBloodGroup()
            );
        }

        System.out.println("--------------------------------");
        System.out.println(
                "Total patients: " + patients.size()
        );
    }
}