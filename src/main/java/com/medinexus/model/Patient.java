package com.medinexus.model;

import java.time.LocalDateTime;

public class Patient {

    private int patientId;
    private int userId;

    private String name;
    private int age;
    private String gender;
    private String phone;
    private String bloodGroup;
    private boolean identityVerified;
    private String verificationMethod;
    private String verificationReference;
    private LocalDateTime verifiedAt;



    public Patient() {
    }


    /*
     * Existing constructor.
     *
     * Kept so our current code does not break.
     */
    public Patient(
            int patientId,
            String name,
            int age,
            String gender,
            String phone,
            String bloodGroup) {

        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
    }


    /*
     * Constructor with user ownership.
     */
    public Patient(
            int patientId,
            int userId,
            String name,
            int age,
            String gender,
            String phone,
            String bloodGroup) {

        this.patientId = patientId;
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
    }


    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public boolean isIdentityVerified() {
        return identityVerified;
    }

    public void setIdentityVerified(boolean identityVerified) {
        this.identityVerified = identityVerified;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationReference() {
        return verificationReference;
    }

    public void setVerificationReference(String verificationReference) {
        this.verificationReference = verificationReference;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

}