package com.medinexus.model;

import java.time.LocalDateTime;

public class IdentityVerification {

    private int verificationId;
    private int patientId;

    private String provider;
    private VerificationStatus status;

    private LocalDateTime verifiedAt;

    private String documentType;
    private String documentReference;

    public IdentityVerification() {
    }

    public IdentityVerification(
            int verificationId,
            int patientId,
            String provider,
            VerificationStatus status,
            LocalDateTime verifiedAt,
            String documentType,
            String documentReference) {

        this.verificationId = verificationId;
        this.patientId = patientId;
        this.provider = provider;
        this.status = status;
        this.verifiedAt = verifiedAt;
        this.documentType = documentType;
        this.documentReference = documentReference;
    }

    public int getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(int verificationId) {
        this.verificationId = verificationId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }
}