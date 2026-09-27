package com.medinexus.controller;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;

import com.medinexus.service.IdentityVerificationService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;
import com.medinexus.service.identity.IdentityBrowserService;
import com.medinexus.config.IdentityVerificationConfig;
import com.medinexus.service.identity.IdentityOAuthManager;
import com.medinexus.service.identity.DigiLockerOAuthService;
import com.medinexus.model.VerificationStatus;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

public class PatientProfileController {

    @FXML
    private Label patientIdLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label bloodGroupLabel;

    @FXML
    private Label verificationStatusLabel;

    @FXML
    private Button verifyIdentityButton;

    @FXML
    private Label messageLabel;
    @FXML
    private Label identityStatusLabel;

    private final PatientService patientService =
            new PatientService();


    private final IdentityOAuthManager identityOAuthManager =
            new IdentityOAuthManager();

    private final IdentityBrowserService identityBrowserService =
            new IdentityBrowserService();
    private Patient currentPatient;

    @FXML
    public void initialize() {
        loadPatientProfile();
        updateIdentityVerificationStatus();
    }

    private void loadPatientProfile() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            messageLabel.setText(
                    "No user is currently logged in."
            );

            verifyIdentityButton.setDisable(true);

            return;
        }

        if (currentUser.getRole() != Role.PATIENT) {

            messageLabel.setText(
                    "This profile is available for patients."
            );

            verifyIdentityButton.setDisable(true);

            return;
        }

        Patient patient =
                patientService.getPatientByUserId(
                        currentUser.getUserId()
                );

        if (patient == null) {

            messageLabel.setText(
                    "Patient profile not found."
            );

            verifyIdentityButton.setDisable(true);

            return;
        }

        currentPatient = patient;
        updateIdentityVerificationStatus();

        patientIdLabel.setText(
                String.valueOf(
                        patient.getPatientId()
                )
        );

        nameLabel.setText(
                patient.getName()
        );

        ageLabel.setText(
                String.valueOf(
                        patient.getAge()
                )
        );

        genderLabel.setText(
                patient.getGender()
        );

        phoneLabel.setText(
                patient.getPhone()
        );

        bloodGroupLabel.setText(
                patient.getBloodGroup()
        );
    }

    @FXML
    private void handleVerifyIdentity() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    "Authentication Required",
                    "Please log in before starting identity verification.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (currentUser.getRole() != Role.PATIENT) {

            showAlert(
                    "Access Denied",
                    "Only patients can verify their identity.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (currentPatient == null) {

            showAlert(
                    "Patient Profile",
                    "Patient profile could not be loaded.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        try {

            IdentityVerification verification =
                    identityVerificationService.verify(
                            currentPatient.getPatientId(),
                            currentPatient.getName()
                    );

            verificationStatusLabel.setText(
                    "Identity Status: "
                            + verification.getStatus()
            );

            updateIdentityVerificationStatus();

            verifyIdentityButton.setDisable(true);

            showAlert(
                    "Verification Submitted",
                    "Your identity verification request has been submitted.\n\n"
                            + "Status: "
                            + verification.getStatus(),
                    Alert.AlertType.INFORMATION
            );

        } catch (IllegalStateException e) {

            /*
             * Expected business-rule errors such as:
             *
             * "A verification request is already under review."
             *
             * should be shown to the user instead of reaching
             * the JavaFX event thread as an uncaught exception.
             */
            showAlert(
                    "Verification Request",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );

            updateIdentityVerificationStatus();

        } catch (SecurityException e) {

            showAlert(
                    "Access Denied",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );

        } catch (IllegalArgumentException e) {

            showAlert(
                    "Invalid Verification Request",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );

        } catch (Exception e) {

            showAlert(
                    "Verification Error",
                    "Unable to complete identity verification.\n\n"
                            + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void handleDigiLockerVerification() {

        if (!SessionManager.isLoggedIn()
                || SessionManager.getCurrentUser() == null) {

            showAlert(
                    "Authentication Required",
                    "Please log in before starting identity verification.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (currentPatient == null) {

            showAlert(
                    "Patient Profile",
                    "Patient profile could not be loaded.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        try {

            int patientId =
                    currentPatient.getPatientId();

            if (IdentityVerificationConfig.isDigiLocker()) {

                identityOAuthManager.setSuccessHandler(
                        result -> {

                            try {

                                IdentityVerification verification =
                                        identityVerificationService
                                                .processDigiLockerResult(
                                                        result.patientId(),
                                                        currentPatient.getName(),
                                                        result.digiLockerId(),
                                                        result.name()
                                                );
                                updateIdentityVerificationStatus();

                                showAlert(
                                        "Identity Verification",
                                        "DigiLocker identity processing completed.\n\n"
                                                + "Status: "
                                                + verification.getStatus()
                                                + "\n\n"
                                                + "Identity Reference: "
                                                + verification.getDocumentReference(),
                                        verification.getStatus()
                                                == VerificationStatus.VERIFIED
                                                ? Alert.AlertType.INFORMATION
                                                : Alert.AlertType.WARNING
                                );

                            } catch (Exception e) {

                                showAlert(
                                        "Verification Error",
                                        "Unable to process the DigiLocker identity.\n\n"
                                                + e.getMessage(),
                                        Alert.AlertType.ERROR
                                );
                            }
                        }
                );

                identityOAuthManager.setErrorHandler(
                        error -> {

                            showAlert(
                                    "DigiLocker Verification",
                                    "Unable to complete DigiLocker authorization.\n\n"
                                            + error.getMessage(),
                                    Alert.AlertType.ERROR
                            );
                        }
                );

                DigiLockerOAuthService.AuthorizationRequest request =
                        identityOAuthManager
                                .createAuthorizationRequest(
                                        patientId
                                );

                identityBrowserService.openAuthorizationPage(
                        request.authorizationUrl()
                );

                showAlert(
                        "DigiLocker Verification",
                        "DigiLocker authorization has been opened in your browser.\n\n"
                                + "Complete the authorization and return to MediNexus.",
                        Alert.AlertType.INFORMATION
                );

            } else {

                IdentityVerification verification =
                        identityVerificationService.submitVerification(
                                patientId,
                                currentPatient.getName()
                        );

                updateIdentityVerificationStatus();

                showAlert(
                        "Verification Submitted",
                        "Your identity verification request has been submitted.\n\n"
                                + "Status: "
                                + verification.getStatus(),
                        Alert.AlertType.INFORMATION
                );
            }

        } catch (IllegalStateException e) {

            showAlert(
                    "Verification Configuration",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );

        } catch (Exception e) {

            showAlert(
                    "Verification Error",
                    "Unable to start identity verification.\n\n"
                            + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }
    private void showAlert(
            String title,
            String message,
            Alert.AlertType type) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private final IdentityVerificationService identityVerificationService =
            new IdentityVerificationService();
    private void updateIdentityVerificationStatus() {

        if (identityStatusLabel == null) {
            return;
        }

        if (currentPatient == null) {

            identityStatusLabel.setText(
                    "Identity verification status: Not available"
            );

            return;
        }

        try {

            IdentityVerification verification =
                    identityVerificationService
                            .getOwnVerificationStatus();

            if (verification == null) {

                identityStatusLabel.setText(
                        "Identity verification status: Not verified"
                );

                identityStatusLabel.setStyle(
                        "-fx-text-fill: #666666;"
                );

                return;
            }

            switch (verification.getStatus()) {

                case PENDING -> {

                    identityStatusLabel.setText(
                            "Identity verification status: Under review"
                    );

                    identityStatusLabel.setStyle(
                            "-fx-text-fill: #d97706;"
                                    + "-fx-font-weight: bold;"
                    );
                }

                case VERIFIED -> {

                    identityStatusLabel.setText(
                            "Identity verification status: Verified"
                    );

                    identityStatusLabel.setStyle(
                            "-fx-text-fill: #198754;"
                                    + "-fx-font-weight: bold;"
                    );
                }

                case FAILED -> {

                    identityStatusLabel.setText(
                            "Identity verification status: Verification failed"
                    );

                    identityStatusLabel.setStyle(
                            "-fx-text-fill: #dc3545;"
                                    + "-fx-font-weight: bold;"
                    );
                }

                default -> {

                    identityStatusLabel.setText(
                            "Identity verification status: Unknown"
                    );

                    identityStatusLabel.setStyle("");
                }
            }

        } catch (Exception e) {

            identityStatusLabel.setText(
                    "Identity verification status: Unable to load"
            );

            identityStatusLabel.setStyle(
                    "-fx-text-fill: #dc3545;"
            );
        }
    }

}