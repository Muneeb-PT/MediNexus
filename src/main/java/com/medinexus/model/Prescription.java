package com.medinexus.model;

import java.time.LocalDateTime;

public class Prescription {

    private int prescriptionId;
    private int medicalRecordId;
    private int patientId;
    private int doctorId;
    private int medicineId;
    private int quantity;

    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;

    private PrescriptionStatus status;

    private LocalDateTime createdAt;


    // ---------------------------------------------------------
    // DEFAULT CONSTRUCTOR
    // ---------------------------------------------------------

    public Prescription() {

        this.status = PrescriptionStatus.PENDING;
    }


    // ---------------------------------------------------------
    // FULL CONSTRUCTOR
    // ---------------------------------------------------------

    public Prescription(
            int prescriptionId,
            int medicalRecordId,
            int patientId,
            int doctorId,
            int medicineId,
            int quantity,
            String dosage,
            String frequency,
            String duration,
            String instructions,
            LocalDateTime createdAt) {

        this.prescriptionId = prescriptionId;
        this.medicalRecordId = medicalRecordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
        this.createdAt = createdAt;

        // New prescriptions start as PENDING
        this.status = PrescriptionStatus.PENDING;
    }


    // ---------------------------------------------------------
    // GETTERS
    // ---------------------------------------------------------

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public int getMedicalRecordId() {
        return medicalRecordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // ---------------------------------------------------------
    // SETTERS
    // ---------------------------------------------------------

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public void setMedicalRecordId(int medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}