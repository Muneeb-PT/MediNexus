package com.medinexus.service;

import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.repository.MedicalRecordJdbcRepository;

import java.util.List;

public class MedicalRecordService {

    private final MedicalRecordJdbcRepository repository;

    public MedicalRecordService() {
        repository = new MedicalRecordJdbcRepository();
    }

    // ---------------------------------------------------------
    // GET ALL MEDICAL RECORDS
    // ---------------------------------------------------------

    public List<MedicalRecord> getAllRecords() {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {

            throw new SecurityException(
                    "Only administrators can view all medical records."
            );
        }

        return repository.findAll();
    }

    // ---------------------------------------------------------
    // GET MEDICAL RECORD BY ID
    // ---------------------------------------------------------

    public MedicalRecord getRecordById(
            int recordId) {

        if (recordId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medical record ID."
            );
        }

        MedicalRecord record =
                repository.findById(recordId);

        if (record == null) {
            return null;
        }

        ensureRecordAccess(record);

        return record;
    }

    // ---------------------------------------------------------
    // GET PATIENT MEDICAL RECORDS
    // ---------------------------------------------------------

    public List<MedicalRecord> getPatientRecords(
            int patientId) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        User currentUser = getCurrentUser();

        Role role = currentUser.getRole();

        // ADMIN → all patient records.
        if (role == Role.ADMIN) {

            return repository.findForPatient(
                    patientId
            );
        }

        // PATIENT → own records only.
        if (role == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId() != patientId) {

                throw new SecurityException(
                        "Access denied. You can access only your own medical records."
                );
            }

            return repository.findForPatient(
                    patientId
            );
        }

        // DOCTOR → only records where BOTH
        // doctor_id and patient_id match.
        if (role == Role.DOCTOR) {

            return repository.findByDoctorAndPatient(
                    currentUser.getUserId(),
                    patientId
            );
        }

        // Receptionists have no clinical-record access.
        throw new SecurityException(
                "Receptionists cannot access medical records."
        );
    }

    // ---------------------------------------------------------
    // GET DOCTOR MEDICAL RECORDS
    // ---------------------------------------------------------

    public List<MedicalRecord> getDoctorRecords(
            int doctorId) {

        if (doctorId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        User currentUser = getCurrentUser();

        Role role = currentUser.getRole();

        // ADMIN → can inspect a doctor's records.
        if (role == Role.ADMIN) {

            return repository.findForDoctor(
                    doctorId
            );
        }

        // DOCTOR → own records only.
        if (role == Role.DOCTOR) {

            if (currentUser.getUserId() != doctorId) {

                throw new SecurityException(
                        "Doctors can access only their own clinical records."
                );
            }

            return repository.findForDoctor(
                    doctorId
            );
        }

        throw new SecurityException(
                "You do not have permission to view doctor medical records."
        );
    }

    // ---------------------------------------------------------
    // CREATE MEDICAL RECORD
    // ---------------------------------------------------------

    public MedicalRecord createRecord(
            MedicalRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Medical record cannot be null."
            );
        }

        User currentUser = getCurrentUser();

        Role role = currentUser.getRole();

        if (role != Role.ADMIN
                && role != Role.DOCTOR) {

            throw new SecurityException(
                    "Only doctors or administrators can create medical records."
            );
        }

        if (record.getPatientId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (record.getDoctorId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        if (record.getAppointmentId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid appointment ID."
            );
        }

        // Doctors can create records only under
        // their own doctor ID.
        if (role == Role.DOCTOR
                && record.getDoctorId()
                != currentUser.getUserId()) {

            throw new SecurityException(
                    "Doctors can create records only for themselves."
            );
        }

        return repository.add(record);
    }

    // ---------------------------------------------------------
    // SECURITY
    // ---------------------------------------------------------

    private void ensureRecordAccess(
            MedicalRecord record) {

        User currentUser = getCurrentUser();

        Role role = currentUser.getRole();

        // ADMIN → full access.
        if (role == Role.ADMIN) {
            return;
        }

        // PATIENT → own records only.
        if (role == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId()
                    != record.getPatientId()) {

                throw new SecurityException(
                        "Access denied. You can access only your own medical records."
                );
            }

            return;
        }

        // DOCTOR → records created by that doctor only.
        if (role == Role.DOCTOR) {

            if (record.getDoctorId()
                    != currentUser.getUserId()) {

                throw new SecurityException(
                        "Access denied. Doctors can access only their own clinical records."
                );
            }

            return;
        }

        throw new SecurityException(
                "You do not have permission to access medical records."
        );
    }

    // ---------------------------------------------------------
    // CURRENT USER
    // ---------------------------------------------------------

    private User getCurrentUser() {

        if (!SessionManager.isLoggedIn()) {

            throw new SecurityException(
                    "No user is currently logged in."
            );
        }

        return SessionManager.getCurrentUser();
    }
}