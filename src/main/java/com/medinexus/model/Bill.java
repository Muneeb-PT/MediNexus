package com.medinexus.model;

import java.time.LocalDateTime;

public class Bill {

    private int billId;
    private int patientId;
    private int appointmentId;

    private double consultationCharge;
    private double medicineCharge;
    private double otherCharges;

    private double totalAmount;

    private String paymentStatus;
    private LocalDateTime createdAt;

    public Bill() {
    }

    public Bill(int billId,
                int patientId,
                int appointmentId,
                double consultationCharge,
                double medicineCharge,
                double otherCharges,
                double totalAmount,
                String paymentStatus,
                LocalDateTime createdAt) {

        this.billId = billId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationCharge = consultationCharge;
        this.medicineCharge = medicineCharge;
        this.otherCharges = otherCharges;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getConsultationCharge() {
        return consultationCharge;
    }

    public void setConsultationCharge(double consultationCharge) {
        this.consultationCharge = consultationCharge;
    }

    public double getMedicineCharge() {
        return medicineCharge;
    }

    public void setMedicineCharge(double medicineCharge) {
        this.medicineCharge = medicineCharge;
    }

    public double getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(double otherCharges) {
        this.otherCharges = otherCharges;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}