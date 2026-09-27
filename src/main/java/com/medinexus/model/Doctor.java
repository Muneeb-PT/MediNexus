package com.medinexus.model;

public class Doctor {

    private int doctorId;
    private int userId;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private boolean available;

    public Doctor() {
    }

    public Doctor(
            int doctorId,
            int userId,
            String name,
            String specialization,
            String phone,
            String email,
            boolean available) {

        this.doctorId = doctorId;
        this.userId = userId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.available = available;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(
            String specialization) {

        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}