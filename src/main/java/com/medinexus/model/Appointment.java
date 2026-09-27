package com.medinexus.model;

import java.time.LocalDate;
import java.time.LocalTime;
import com.medinexus.model.AppointmentStatus;

public class Appointment {

    private int appointmentId;
    private int patientId;
    private int doctorId;

    private String patientName;
    private String doctorName;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String reason;
    private String urgency;

    private AppointmentStatus status;

    public Appointment() {
    }

    public Appointment(
            int appointmentId,
            int patientId,
            int doctorId,
            String patientName,
            String doctorName,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason,
            String urgency,
            AppointmentStatus status) {

        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.urgency = urgency;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(
            LocalDate appointmentDate) {

        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(
            LocalTime appointmentTime) {

        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(
            AppointmentStatus status) {

        this.status = status;
    }
}