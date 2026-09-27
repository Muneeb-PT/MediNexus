package com.medinexus.config;

import com.medinexus.model.Doctor;
import com.medinexus.repository.DoctorJdbcRepository;

import java.util.List;

public class DoctorDatabaseTest {

    public static void main(String[] args) {

        DoctorJdbcRepository repository =
                new DoctorJdbcRepository();

        System.out.println("=== DOCTOR DATABASE TEST ===");

        List<Doctor> doctors =
                repository.findAll();

        System.out.println(
                "Total doctors: " + doctors.size()
        );

        for (Doctor doctor : doctors) {

            System.out.println(
                    doctor.getDoctorId()
                            + " | "
                            + doctor.getName()
                            + " | "
                            + doctor.getSpecialization()
                            + " | Available: "
                            + doctor.isAvailable()
            );
        }

        System.out.println();

        Doctor doctor =
                repository.findById(101);

        if (doctor != null) {

            System.out.println(
                    "Doctor ID 101: "
                            + doctor.getName()
                            + " - "
                            + doctor.getSpecialization()
            );

        } else {

            System.out.println(
                    "Doctor ID 101 not found."
            );
        }
    }
}