package com.medinexus.config;

import com.medinexus.model.Doctor;
import com.medinexus.service.DoctorService;

import java.util.List;

public class DoctorServiceDatabaseTest {

    public static void main(String[] args) {

        DoctorService service =
                new DoctorService();

        System.out.println("=== DOCTOR SERVICE DATABASE TEST ===");

        List<Doctor> doctors =
                service.getAllDoctors();

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
            );
        }

        System.out.println();

        Doctor doctor =
                service.getDoctorById(101);

        if (doctor != null) {

            System.out.println(
                    "Doctor #101: "
                            + doctor.getName()
            );
        }

        Doctor userDoctor =
                service.getDoctorByUserId(101);

        if (userDoctor != null) {

            System.out.println(
                    "User #101 belongs to: "
                            + userDoctor.getName()
            );
        }
    }
}