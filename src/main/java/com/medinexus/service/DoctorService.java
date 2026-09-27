package com.medinexus.service;

import com.medinexus.model.Doctor;
import com.medinexus.repository.DoctorJdbcRepository;

import java.util.List;

public class DoctorService {

    private final DoctorJdbcRepository repository;

    public DoctorService() {
        this.repository = new DoctorJdbcRepository();
    }


    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    public List<Doctor> getAllDoctors() {

        return repository.findAll();
    }


    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    public Doctor getDoctorById(
            int doctorId) {

        return repository.findById(doctorId);
    }


    // =========================================================
    // GET DOCTOR BY USER ID
    // =========================================================

    public Doctor getDoctorByUserId(
            int userId) {

        return repository.findByUserId(userId);
    }


    // =========================================================
    // ADD DOCTOR
    // =========================================================

    public void addDoctor(
            Doctor doctor) {

        validateDoctor(doctor);

        repository.add(doctor);
    }


    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public void updateDoctor(
            Doctor doctor) {

        validateDoctor(doctor);

        repository.update(doctor);
    }


    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    public void deleteDoctor(
            int doctorId) {

        if (doctorId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        repository.delete(doctorId);
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE DOCTOR
    // =========================================================

    public void setDoctorAvailability(
            int doctorId,
            boolean available) {

        if (doctorId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        repository.setAvailability(
                doctorId,
                available
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateDoctor(
            Doctor doctor) {

        if (doctor == null) {

            throw new IllegalArgumentException(
                    "Doctor cannot be null."
            );
        }

        if (doctor.getDoctorId() <= 0) {

            throw new IllegalArgumentException(
                    "Doctor ID must be greater than zero."
            );
        }

        if (isBlank(doctor.getName())) {

            throw new IllegalArgumentException(
                    "Doctor name is required."
            );
        }

        if (isBlank(
                doctor.getSpecialization())) {

            throw new IllegalArgumentException(
                    "Specialization is required."
            );
        }

        if (isBlank(doctor.getPhone())) {

            throw new IllegalArgumentException(
                    "Phone number is required."
            );
        }

        if (isBlank(doctor.getEmail())) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }
    }


    private boolean isBlank(
            String value) {

        return value == null
                || value.trim().isEmpty();
    }
}