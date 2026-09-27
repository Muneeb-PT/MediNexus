package com.medinexus.controller;

import com.medinexus.model.Patient;
import com.medinexus.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddPatientController {

    @FXML
    private TextField patientIdField;

    @FXML
    private TextField nameField;

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

    private PatientDataListener patientDataListener;

    private final PatientService patientService =
            new PatientService();

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

    public void setPatientDataListener(
            PatientDataListener patientDataListener) {

        this.patientDataListener = patientDataListener;
    }

    @FXML
    private void handleSave() {

        String patientIdText =
                patientIdField.getText().trim();

        String name =
                nameField.getText().trim();

        String ageText =
                ageField.getText().trim();

        String gender =
                genderComboBox.getValue();

        String phone =
                phoneField.getText().trim();

        String bloodGroup =
                bloodGroupComboBox.getValue();

        if (patientIdText.isEmpty()
                || name.isEmpty()
                || ageText.isEmpty()
                || gender == null
                || phone.isEmpty()
                || bloodGroup == null) {

            messageLabel.setText(
                    "Please fill all fields."
            );

            return;
        }

        try {

            int patientId =
                    Integer.parseInt(patientIdText);

            int age =
                    Integer.parseInt(ageText);

            if (patientId <= 0) {

                messageLabel.setText(
                        "Patient ID must be positive."
                );

                return;
            }

            if (age <= 0 || age > 150) {

                messageLabel.setText(
                        "Please enter a valid age."
                );

                return;
            }

            Patient patient =
                    new Patient(
                            patientId,
                            0,
                            name,
                            age,
                            gender,
                            phone,
                            bloodGroup
                    );

            patientService.addPatient(patient);

            if (patientDataListener != null) {

                patientDataListener.onPatientAdded(
                        patient
                );
            }

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Patient Added",
                    "Patient has been added successfully."
            );

            messageLabel.setText(
                    "Patient added successfully."
            );

        } catch (NumberFormatException e) {

            messageLabel.setText(
                    "Patient ID and Age must be numbers."
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    e.getMessage()
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "Unable to Add Patient",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleClear() {

        patientIdField.clear();
        nameField.clear();
        ageField.clear();
        phoneField.clear();

        genderComboBox.setValue(null);
        bloodGroupComboBox.setValue(null);

        messageLabel.setText("");
    }

    @FXML
    private void handleCancel() {

        if (patientDataListener != null
                && patientDataListener instanceof PatientController) {

            ((PatientController) patientDataListener)
                    .showPatientList();
        }
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}