package com.medinexus.service;

import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.repository.PatientJdbcRepository;

import java.util.List;

public class PatientService {

    private final PatientJdbcRepository repository;

    public PatientService() {
        this.repository = new PatientJdbcRepository();
    }

    // -----------------------------------------
    // Get patients visible to current user
    // -----------------------------------------

    public List<Patient> getAllPatients() {

        User currentUser =
                getCurrentUser();

        Role role =
                currentUser.getRole();

        /*
         * Admin and Receptionist can access
         * the complete operational patient list.
         */
        if (role == Role.ADMIN
                || role == Role.RECEPTIONIST) {

            return repository.findAll();
        }

        /*
         * Doctors can only see patients who have
         * an appointment relationship with them.
         */
        if (role == Role.DOCTOR) {

            return repository.findByDoctorId(
                    currentUser.getUserId()
            );
        }

        throw new SecurityException(
                "Patients cannot access the complete patient list."
        );
    }

    // -----------------------------------------
    // Get patient by ID
    // -----------------------------------------

    public Patient getPatientById(
            int patientId) {

        if (patientId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        Patient patient =
                repository.findById(
                        patientId
                );

        if (patient == null) {
            return null;
        }

        ensurePatientAccess(
                patient
        );

        return patient;
    }

    // -----------------------------------------
    // Get patient by user ID
    // -----------------------------------------

    public Patient getPatientByUserId(
            int userId) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        User currentUser =
                getCurrentUser();

        /*
         * Patients can only retrieve their own
         * patient account.
         */
        if ((currentUser.getRole() == Role.PATIENT
                || currentUser.getRole() == Role.DOCTOR)
                && currentUser.getUserId() != userId) {

            throw new SecurityException(
                    "Users can access only their own account through this method."
            );
        }

        /*
         * This method is for resolving a patient profile by
         * its linked user ID. Operational roles may look up
         * patient profiles; patients and doctors cannot use
         * it to enumerate other users' profiles.
         */
        return repository.findByUserId(
                userId
        );
    }

    // -----------------------------------------
    // Add patient
    // -----------------------------------------

    public void addPatient(
            Patient patient) {

        requirePermissionForPatientCreation();

        validatePatient(
                patient
        );

        repository.add(
                patient
        );
    }

    // -----------------------------------------
    // Update patient
    // -----------------------------------------

    public void updatePatient(
            Patient patient) {

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Patient cannot be null."
            );
        }

        ensurePatientAccess(
                patient
        );

        /*
         * Doctors are allowed to view clinically
         * related patients, but should not be able
         * to modify demographic/profile information.
         */
        User currentUser =
                getCurrentUser();

        if (currentUser.getRole() == Role.DOCTOR) {

            throw new SecurityException(
                    "Doctors cannot modify patient profile information."
            );
        }

        validatePatient(
                patient
        );

        repository.update(
                patient
        );
    }

    // -----------------------------------------
    // Delete patient
    // -----------------------------------------

    public void deletePatient(
            int patientId) {

        if (patientId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        User currentUser =
                getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new SecurityException(
                    "Only administrators can delete patients."
            );
        }

        repository.delete(
                patientId
        );
    }

    // -----------------------------------------
    // Update identity verification
    // -----------------------------------------

    public void updateIdentityVerification(
            int patientId,
            boolean verified,
            String method,
            String reference) {

        if (patientId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        Patient patient =
                repository.findById(
                        patientId
                );

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Patient not found."
            );
        }

        ensurePatientAccess(
                patient
        );

        if (verified) {

            if (method == null
                    || method.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Verification method is required."
                );
            }

            if (reference == null
                    || reference.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Verification reference is required."
                );
            }
        }

        repository.updateVerification(
                patientId,
                verified,
                method,
                reference
        );
    }

    // -----------------------------------------
    // Security helpers
    // -----------------------------------------

    private User getCurrentUser() {

        if (!SessionManager.isLoggedIn()) {

            throw new SecurityException(
                    "No user is currently logged in."
            );
        }

        return SessionManager.getCurrentUser();
    }

    private void ensurePatientAccess(
            Patient patient) {

        User currentUser =
                getCurrentUser();

        Role role =
                currentUser.getRole();

        // -----------------------------------------
        // Admin
        // -----------------------------------------

        if (role == Role.ADMIN) {
            return;
        }

        // -----------------------------------------
        // Receptionist
        // -----------------------------------------

        if (role == Role.RECEPTIONIST) {
            return;
        }

        // -----------------------------------------
        // Patient
        // -----------------------------------------

        if (role == Role.PATIENT) {

            if (patient.getUserId()
                    != currentUser.getUserId()) {

                throw new SecurityException(
                        "Access denied. You can access only your own patient profile."
                );
            }

            return;
        }

        // -----------------------------------------
        // Doctor
        // -----------------------------------------

        if (role == Role.DOCTOR) {

            boolean hasAccess =
                    repository.doctorHasPatientAccess(
                            currentUser.getUserId(),
                            patient.getPatientId()
                    );

            if (!hasAccess) {

                throw new SecurityException(
                        "Access denied. Doctors can access only clinically related patients."
                );
            }

            return;
        }

        throw new SecurityException(
                "Access denied."
        );
    }

    private void requirePermissionForPatientCreation() {

        User currentUser =
                getCurrentUser();

        Role role =
                currentUser.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You do not have permission to register patients."
            );
        }
    }

    // -----------------------------------------
    // Validation
    // -----------------------------------------

    private void validatePatient(
            Patient patient) {

        if (patient == null) {

            throw new IllegalArgumentException(
                    "Patient cannot be null."
            );
        }

        if (patient.getPatientId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (patient.getName() == null
                || patient.getName()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Patient name is required."
            );
        }

        if (patient.getAge() <= 0
                || patient.getAge() > 150) {

            throw new IllegalArgumentException(
                    "Patient age must be between 1 and 150."
            );
        }

        if (patient.getGender() == null
                || patient.getGender()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Patient gender is required."
            );
        }

        if (patient.getPhone() == null
                || patient.getPhone()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Patient phone number is required."
            );
        }

        if (patient.getBloodGroup() == null
                || patient.getBloodGroup()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Patient blood group is required."
            );
        }
    }
}