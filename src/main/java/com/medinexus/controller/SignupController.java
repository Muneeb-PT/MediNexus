package com.medinexus.controller;

import com.medinexus.service.PatientSignupService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> bloodGroupComboBox;

    @FXML
    private Label messageLabel;

    private final PatientSignupService signupService =
            new PatientSignupService();

    @FXML
    public void initialize() {

        genderComboBox.getItems().addAll(
                "Male",
                "Female",
                "Other"
        );

        bloodGroupComboBox.getItems().addAll(
                "A+",
                "A-",
                "B+",
                "B-",
                "AB+",
                "AB-",
                "O+",
                "O-"
        );
    }

    @FXML
    private void handleSignup() {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText();

        String confirmPassword =
                confirmPasswordField.getText();

        String fullName =
                fullNameField.getText().trim();

        String ageText =
                ageField.getText().trim();

        String gender =
                genderComboBox.getValue();

        String phone =
                phoneField.getText().trim();

        String bloodGroup =
                bloodGroupComboBox.getValue();

        if (!password.equals(confirmPassword)) {

            showMessage(
                    "Passwords do not match."
            );

            return;
        }

        int age;

        try {

            age = Integer.parseInt(ageText);

        } catch (NumberFormatException e) {

            showMessage(
                    "Please enter a valid age."
            );

            return;
        }

        try {

            signupService.registerPatient(
                    username,
                    password,
                    fullName,
                    age,
                    gender,
                    phone,
                    bloodGroup
            );

            showMessage(
                    "Account created successfully. Please log in."
            );

            clearForm();

        } catch (IllegalArgumentException e) {

            showMessage(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            showMessage(
                    "Unable to create account. Please try again."
            );

            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/LoginView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(
                            root,
                            1200,
                            700
                    );

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

            showMessage(
                    "Unable to return to login."
            );
        }
    }

    private void clearForm() {

        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameField.clear();
        ageField.clear();
        phoneField.clear();

        genderComboBox.getSelectionModel()
                .clearSelection();

        bloodGroupComboBox.getSelectionModel()
                .clearSelection();
    }

    private void showMessage(String message) {

        messageLabel.setText(
                message == null
                        ? "Unable to complete request."
                        : message
        );
    }
}