package com.medinexus.repository;

import com.medinexus.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    private static final List<Doctor> doctors =
            new ArrayList<>();

    public DoctorRepository() {

        if (doctors.isEmpty()) {
            loadSampleDoctors();
        }
    }

    private void loadSampleDoctors() {

        doctors.add(
                new Doctor(
                        101,
                        101,
                        "Dr. Ahmed",
                        "Cardiology",
                        "9876500001",
                        "ahmed@medinexus.com",
                        true
                )
        );

        doctors.add(
                new Doctor(
                        102,
                        102,
                        "Dr. Sarah",
                        "General Medicine",
                        "9876500002",
                        "sarah@medinexus.com",
                        true
                )
        );

        doctors.add(
                new Doctor(
                        103,
                        103,
                        "Dr. Mohammed",
                        "Neurology",
                        "9876500003",
                        "mohammed@medinexus.com",
                        true
                )
        );
    }

    public List<Doctor> findAll() {

        return new ArrayList<>(doctors);
    }

    public Doctor findByDoctorId(int doctorId) {

        for (Doctor doctor : doctors) {

            if (doctor.getDoctorId() == doctorId) {
                return doctor;
            }
        }

        return null;
    }

    public Doctor findByUserId(int userId) {

        for (Doctor doctor : doctors) {

            if (doctor.getUserId() == userId) {
                return doctor;
            }
        }

        return null;
    }
}