package com.medinexus.service;

import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Prescription;
import com.medinexus.model.PrescriptionStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.repository.PrescriptionJdbcRepository;

import java.util.List;

public class PrescriptionService {

    private final PrescriptionJdbcRepository repository =
            new PrescriptionJdbcRepository();

    // ---------------------------------------------------------
    // GET ALL PRESCRIPTIONS
    // ---------------------------------------------------------

    /**
     * ADMIN:
     * Can view all prescriptions.
     *
     * RECEPTIONIST:
     * Can access prescription data required for
     * pharmacy/billing operations.
     *
     * DOCTOR:
     * Must use getDoctorPrescriptions() so that
     * clinical records remain ownership-filtered.
     */
    public List<Prescription> getAllPrescriptions() {

        User user = getCurrentUser();

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to view all prescriptions."
            );
        }

        return repository.findAll();
    }

    // ---------------------------------------------------------
    // GET PRESCRIPTION BY ID
    // ---------------------------------------------------------

    public Prescription getPrescriptionById(
            int prescriptionId) {

        if (prescriptionId <= 0) {
            return null;
        }

        Prescription prescription =
                repository.findById(prescriptionId);

        if (prescription == null) {
            return null;
        }

        ensurePrescriptionAccess(
                prescription
        );

        return prescription;
    }

    // ---------------------------------------------------------
    // GET PRESCRIPTIONS FOR MEDICAL RECORD
    // ---------------------------------------------------------

    public List<Prescription>
    getMedicalRecordPrescriptions(
            int medicalRecordId) {

        if (medicalRecordId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid medical record ID."
            );
        }

        User user = getCurrentUser();

        // ADMIN can access any medical record.
        if (user.getRole() == Role.ADMIN) {

            return repository.findByMedicalRecordId(
                    medicalRecordId
            );
        }

        // Receptionist access is retained for
        // pharmacy/billing workflow.
        if (user.getRole() == Role.RECEPTIONIST) {

            return repository.findByMedicalRecordId(
                    medicalRecordId
            );
        }

        // DOCTOR → medical record must belong
        // to the current doctor.
        if (user.getRole() == Role.DOCTOR) {

            MedicalRecordService
                    medicalRecordService =
                    new MedicalRecordService();

            MedicalRecord record =
                    medicalRecordService.getRecordById(
                            medicalRecordId
                    );

            if (record == null) {
                return List.of();
            }

            if (record.getDoctorId()
                    != user.getUserId()) {

                throw new SecurityException(
                        "Doctors can only access prescriptions "
                                + "for their own medical records."
                );
            }

            return repository.findByMedicalRecordId(
                    medicalRecordId
            );
        }

        // PATIENT → medical record must belong
        // to the logged-in patient.
        if (user.getRole() == Role.PATIENT) {

            MedicalRecordService
                    medicalRecordService =
                    new MedicalRecordService();

            MedicalRecord record =
                    medicalRecordService.getRecordById(
                            medicalRecordId
                    );

            if (record == null) {
                return List.of();
            }

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || record.getPatientId()
                    != patient.getPatientId()) {

                throw new SecurityException(
                        "Patients can only access prescriptions "
                                + "for their own medical records."
                );
            }

            return repository.findByMedicalRecordId(
                    medicalRecordId
            );
        }

        throw new SecurityException(
                "You are not authorized to access prescriptions."
        );
    }

    // ---------------------------------------------------------
    // GET PATIENT PRESCRIPTIONS
    // ---------------------------------------------------------

    public List<Prescription>
    getPatientPrescriptions(
            int patientId) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        User user = getCurrentUser();

        // ADMIN → all prescriptions for requested patient.
        if (user.getRole() == Role.ADMIN) {

            return repository.findByPatientId(
                    patientId
            );
        }

        // Receptionist → required operational access.
        if (user.getRole() == Role.RECEPTIONIST) {

            return repository.findByPatientId(
                    patientId
            );
        }

        // PATIENT → own prescriptions only.
        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId()
                    != patientId) {

                throw new SecurityException(
                        "Patients can only access their own prescriptions."
                );
            }

            return repository.findByPatientId(
                    patientId
            );
        }

        // DOCTOR → only prescriptions where BOTH
        // doctor and patient match.
        if (user.getRole() == Role.DOCTOR) {

            return repository.findByDoctorAndPatient(
                    user.getUserId(),
                    patientId
            );
        }

        throw new SecurityException(
                "You are not authorized to access patient prescriptions."
        );
    }

    // ---------------------------------------------------------
    // GET DOCTOR PRESCRIPTIONS
    // ---------------------------------------------------------

    public List<Prescription>
    getDoctorPrescriptions(
            int doctorId) {

        if (doctorId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        User user = getCurrentUser();

        // ADMIN can inspect any doctor's prescriptions.
        if (user.getRole() == Role.ADMIN) {

            return repository.findByDoctorId(
                    doctorId
            );
        }

        // Receptionist access is operational.
        if (user.getRole() == Role.RECEPTIONIST) {

            return repository.findByDoctorId(
                    doctorId
            );
        }

        // Doctor can access only their own prescriptions.
        if (user.getRole() == Role.DOCTOR) {

            if (user.getUserId() != doctorId) {

                throw new SecurityException(
                        "Doctors can only access their own prescriptions."
                );
            }

            return repository.findByDoctorId(
                    doctorId
            );
        }

        throw new SecurityException(
                "You are not authorized to access doctor prescriptions."
        );
    }

    // ---------------------------------------------------------
    // CREATE PRESCRIPTION
    // ---------------------------------------------------------

    public Prescription createPrescription(
            Prescription prescription) {

        if (prescription == null) {

            throw new IllegalArgumentException(
                    "Prescription cannot be null."
            );
        }

        User user = getCurrentUser();

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.DOCTOR) {

            throw new SecurityException(
                    "Only doctors and administrators can "
                            + "create prescriptions."
            );
        }

        if (prescription.getMedicalRecordId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid medical record ID."
            );
        }

        if (prescription.getPatientId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (prescription.getDoctorId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid doctor ID."
            );
        }

        if (prescription.getMedicineId() <= 0) {

            throw new IllegalArgumentException(
                    "Please select a valid medicine."
            );
        }

        if (prescription.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Medicine quantity must be greater than zero."
            );
        }

        if (prescription.getDosage() == null
                || prescription.getDosage().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Dosage is required."
            );
        }

        if (prescription.getFrequency() == null
                || prescription.getFrequency().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Frequency is required."
            );
        }

        if (prescription.getDuration() == null
                || prescription.getDuration().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Duration is required."
            );
        }

        // -----------------------------------------------------
        // CLINICAL OWNERSHIP VALIDATION
        // -----------------------------------------------------

        MedicalRecordService
                medicalRecordService =
                new MedicalRecordService();

        MedicalRecord record =
                medicalRecordService.getRecordById(
                        prescription.getMedicalRecordId()
                );

        if (record == null) {

            throw new IllegalArgumentException(
                    "The selected medical record does not exist."
            );
        }

        /*
         * The prescription must match the medical record.
         *
         * This prevents a doctor/admin from creating a
         * prescription where:
         *
         * medical_record_id = A
         * patient_id        = B
         * doctor_id         = C
         *
         * when A actually belongs to different patient/doctor.
         */

        if (record.getPatientId()
                != prescription.getPatientId()) {

            throw new SecurityException(
                    "Prescription patient does not match "
                            + "the medical record patient."
            );
        }

        if (record.getDoctorId()
                != prescription.getDoctorId()) {

            throw new SecurityException(
                    "Prescription doctor does not match "
                            + "the medical record doctor."
            );
        }

        // -----------------------------------------------------
        // DOCTOR OWNERSHIP
        // -----------------------------------------------------

        if (user.getRole() == Role.DOCTOR
                && prescription.getDoctorId()
                != user.getUserId()) {

            throw new SecurityException(
                    "Doctors can only create prescriptions "
                            + "under their own account."
            );
        }

        // -----------------------------------------------------
        // DUPLICATE MEDICINE CHECK
        // -----------------------------------------------------

        boolean duplicate =
                repository.findByMedicalRecordId(
                                prescription
                                        .getMedicalRecordId()
                        )
                        .stream()
                        .anyMatch(
                                existing ->
                                        existing.getMedicineId()
                                                == prescription
                                                .getMedicineId()
                        );

        if (duplicate) {

            throw new IllegalArgumentException(
                    "A prescription for this medicine "
                            + "already exists for this "
                            + "medical record."
            );
        }

        // -----------------------------------------------------
        // DEFAULT STATUS
        // -----------------------------------------------------

        prescription.setStatus(
                PrescriptionStatus.PENDING
        );

        return repository.add(
                prescription
        );
    }

    // ---------------------------------------------------------
    // UPDATE PRESCRIPTION STATUS
    // ---------------------------------------------------------

    public void updateStatus(
            int prescriptionId,
            PrescriptionStatus status) {

        if (prescriptionId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }

        Prescription prescription =
                repository.findById(
                        prescriptionId
                );

        if (prescription == null) {

            throw new IllegalArgumentException(
                    "Prescription not found."
            );
        }

        if (status == null) {

            throw new IllegalArgumentException(
                    "Prescription status cannot be null."
            );
        }

        User user = getCurrentUser();

        // Patients cannot modify status.
        if (user.getRole() == Role.PATIENT) {

            throw new SecurityException(
                    "Patients cannot directly change "
                            + "prescription status."
            );
        }

        // Doctors can modify only their own prescriptions.
        if (user.getRole() == Role.DOCTOR
                && prescription.getDoctorId()
                != user.getUserId()) {

            throw new SecurityException(
                    "Doctors can only change status of "
                            + "their own prescriptions."
            );
        }

        // Only ADMIN, DOCTOR and RECEPTIONIST may perform
        // operational status transitions.
        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.DOCTOR
                && user.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to change "
                            + "prescription status."
            );
        }

        PrescriptionStatus currentStatus =
                prescription.getStatus();

        if (!isValidTransition(
                currentStatus,
                status)) {

            throw new IllegalStateException(
                    "Invalid prescription status transition: "
                            + currentStatus
                            + " → "
                            + status
            );
        }

        prescription.setStatus(status);

        repository.update(
                prescription
        );
    }

    // ---------------------------------------------------------
    // VALID STATUS TRANSITION
    // ---------------------------------------------------------

    private boolean isValidTransition(
            PrescriptionStatus currentStatus,
            PrescriptionStatus newStatus) {

        if (currentStatus == null
                || newStatus == null) {

            return false;
        }

        if (currentStatus == newStatus) {
            return true;
        }

        switch (currentStatus) {

            case PENDING:
                return newStatus ==
                        PrescriptionStatus.DISPENSED;

            case DISPENSED:
                return newStatus ==
                        PrescriptionStatus.BILLED;

            case BILLED:
                return newStatus ==
                        PrescriptionStatus.PAID;

            case PAID:
                return false;

            default:
                return false;
        }
    }

    // ---------------------------------------------------------
    // DISPENSE
    // ---------------------------------------------------------

    public void markAsDispensed(
            int prescriptionId) {

        updateStatus(
                prescriptionId,
                PrescriptionStatus.DISPENSED
        );
    }

    // ---------------------------------------------------------
    // MARK AS BILLED
    // ---------------------------------------------------------

    public void markAsBilled(
            int prescriptionId) {

        updateStatus(
                prescriptionId,
                PrescriptionStatus.BILLED
        );
    }

    // ---------------------------------------------------------
    // MARK AS PAID
    // ---------------------------------------------------------

    public void markAsPaid(
            int prescriptionId) {

        updateStatus(
                prescriptionId,
                PrescriptionStatus.PAID
        );
    }

    // ---------------------------------------------------------
    // SECURITY HELPERS
    // ---------------------------------------------------------

    private User getCurrentUser() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {

            throw new SecurityException(
                    "You must be logged in to access "
                            + "prescriptions."
            );
        }

        return user;
    }

    private void ensurePrescriptionAccess(
            Prescription prescription) {

        User user = getCurrentUser();

        // ADMIN → full access.
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // Receptionist → operational access.
        if (user.getRole() == Role.RECEPTIONIST) {
            return;
        }

        // DOCTOR → own prescriptions only.
        if (user.getRole() == Role.DOCTOR) {

            if (prescription.getDoctorId()
                    != user.getUserId()) {

                throw new SecurityException(
                        "Doctors can only access "
                                + "their own prescriptions."
                );
            }

            return;
        }

        // PATIENT → own prescriptions only.
        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || prescription.getPatientId()
                    != patient.getPatientId()) {

                throw new SecurityException(
                        "Patients can only access "
                                + "their own prescriptions."
                );
            }

            return;
        }

        throw new SecurityException(
                "You are not authorized to access "
                        + "this prescription."
        );
    }
}