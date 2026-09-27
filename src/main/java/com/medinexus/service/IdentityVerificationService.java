package com.medinexus.service;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.VerificationStatus;
import com.medinexus.repository.IdentityVerificationJdbcRepository;

import java.time.LocalDateTime;
import java.util.List;

public class IdentityVerificationService {

    private final IdentityVerificationProvider provider;
    private final IdentityVerificationJdbcRepository repository;
    private final IdentityMatchService identityMatchService;

    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public IdentityVerificationService() {
        this(
                IdentityVerificationProviderFactory.create()
        );
    }

    // =========================================================
    // PROVIDER CONSTRUCTOR
    // =========================================================

    public IdentityVerificationService(
            IdentityVerificationProvider provider) {

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Identity verification provider cannot be null."
            );
        }

        this.provider = provider;

        this.repository =
                new IdentityVerificationJdbcRepository();

        this.identityMatchService =
                new IdentityMatchService();
    }

    // =========================================================
    // GET LATEST VERIFICATION
    // =========================================================

    public IdentityVerification getLatestVerification(
            int patientId) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        return repository.findLatestByPatientId(
                patientId
        );
    }

    // =========================================================
    // GET OWN VERIFICATION STATUS
    // =========================================================

    public IdentityVerification getOwnVerificationStatus() {

        if (!SessionManager.isLoggedIn()) {
            throw new SecurityException(
                    "You must be logged in."
            );
        }

        if (SessionManager.getCurrentUser().getRole()
                != Role.PATIENT) {

            throw new SecurityException(
                    "Only patients can access their own "
                            + "identity verification status."
            );
        }

        PatientService patientService =
                new PatientService();

        Patient patient =
                patientService.getPatientByUserId(
                        SessionManager.getCurrentUser().getUserId()
                );

        if (patient == null) {
            throw new SecurityException(
                    "Patient profile not found."
            );
        }

        return repository.findLatestByPatientId(
                patient.getPatientId()
        );
    }

    // =========================================================
    // SUBMIT VERIFICATION
    // =========================================================

    public IdentityVerification submitVerification(
            int patientId,
            String patientName) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (patientName == null
                || patientName.isBlank()) {

            throw new IllegalArgumentException(
                    "Patient name cannot be empty."
            );
        }

        IdentityVerification existing =
                repository.findLatestByPatientId(
                        patientId
                );

        if (existing != null
                && existing.getStatus()
                == VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "A verification request is already under review."
            );
        }

        IdentityVerification providerResult =
                provider.verifyIdentity(
                        patientId,
                        patientName
                );

        if (providerResult == null) {
            throw new IllegalStateException(
                    "Verification provider returned no result."
            );
        }

        IdentityVerification verification =
                new IdentityVerification(
                        0,
                        patientId,
                        providerResult.getProvider(),
                        VerificationStatus.PENDING,
                        null,
                        providerResult.getDocumentType(),
                        providerResult.getDocumentReference()
                );

        Integer performedBy =
                getCurrentUserIdOrNull();

        return repository.addWithAudit(
                verification,
                performedBy,
                "SUBMITTED",
                "Identity verification request submitted."
        );
    }

    // =========================================================
    // GET PENDING VERIFICATIONS
    // =========================================================

    public List<IdentityVerification>
    getPendingVerifications() {

        requireAdmin();

        return repository.findByStatus(
                VerificationStatus.PENDING
        );
    }

    // =========================================================
    // APPROVE VERIFICATION
    // =========================================================

    public void approveVerification(
            int verificationId) {

        requireAdmin();

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        int adminUserId =
                SessionManager.getCurrentUser()
                        .getUserId();

        repository.updateStatusWithAudit(
                verificationId,
                VerificationStatus.PENDING,
                VerificationStatus.VERIFIED,
                LocalDateTime.now(),
                adminUserId,
                "VERIFIED",
                "Identity verification approved by administrator."
        );
    }

    // =========================================================
    // REJECT VERIFICATION
    // =========================================================

    public void rejectVerification(
            int verificationId) {

        requireAdmin();

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        int adminUserId =
                SessionManager.getCurrentUser()
                        .getUserId();

        repository.updateStatusWithAudit(
                verificationId,
                VerificationStatus.PENDING,
                VerificationStatus.FAILED,
                null,
                adminUserId,
                "FAILED",
                "Identity verification rejected by administrator."
        );
    }

    // =========================================================
    // BACKWARD-COMPATIBLE VERIFY METHOD
    // =========================================================

    public IdentityVerification verify(
            int patientId,
            String patientName) {

        return submitVerification(
                patientId,
                patientName
        );
    }

    // =========================================================
    // PROCESS DIGILOCKER RESULT
    // =========================================================

    public IdentityVerification processDigiLockerResult(
            int patientId,
            String patientName,
            String digiLockerId,
            String digiLockerName) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        if (patientName == null
                || patientName.isBlank()) {

            throw new IllegalArgumentException(
                    "Patient name cannot be empty."
            );
        }

        if (digiLockerId == null
                || digiLockerId.isBlank()) {

            throw new IllegalArgumentException(
                    "DigiLocker identity reference cannot be empty."
            );
        }

        if (digiLockerName == null
                || digiLockerName.isBlank()) {

            throw new IllegalArgumentException(
                    "Verified identity name cannot be empty."
            );
        }

        boolean nameMatches =
                identityMatchService.matchesName(
                        patientName,
                        digiLockerName
                );

        VerificationStatus status =
                nameMatches
                        ? VerificationStatus.VERIFIED
                        : VerificationStatus.FAILED;

        IdentityVerification verification =
                new IdentityVerification(
                        0,
                        patientId,
                        "DigiLocker",
                        status,
                        nameMatches
                                ? LocalDateTime.now()
                                : null,
                        "DigiLocker Identity",
                        digiLockerId
                );

        Integer performedBy =
                getCurrentUserIdOrNull();

        String reason;

        if (nameMatches) {
            reason =
                    "DigiLocker identity verified successfully.";
        } else {
            reason =
                    "DigiLocker identity name did not match "
                            + "the MediNexus patient name.";
        }

        return repository.addWithAudit(
                verification,
                performedBy,
                nameMatches
                        ? "VERIFIED"
                        : "FAILED",
                reason
        );
    }

    // =========================================================
    // GET VERIFICATION BY ID
    // =========================================================

    public IdentityVerification getVerificationById(
            int verificationId) {

        requireAdmin();

        if (verificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid verification ID."
            );
        }

        return repository.findById(
                verificationId
        );
    }

    // =========================================================
    // CURRENT USER ID
    // =========================================================

    private Integer getCurrentUserIdOrNull() {

        if (!SessionManager.isLoggedIn()) {
            return null;
        }

        return SessionManager.getCurrentUser()
                .getUserId();
    }

    // =========================================================
    // ADMIN AUTHORIZATION
    // =========================================================

    private void requireAdmin() {

        if (!SessionManager.isLoggedIn()) {
            throw new SecurityException(
                    "You must be logged in."
            );
        }

        if (SessionManager.getCurrentUser().getRole()
                != Role.ADMIN) {

            throw new SecurityException(
                    "Only administrators can review "
                            + "identity verification requests."
            );
        }
    }
}