package com.medinexus.controller;

import com.medinexus.model.Patient;
import com.medinexus.service.PatientService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EditPatientController {

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

    private Patient patient;

    private PatientController patientController;

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

    public void setPatient(Patient patient) {

        this.patient = patient;

        patientIdField.setText(
                String.valueOf(patient.getPatientId())
        );

        nameField.setText(
                patient.getName()
        );

        ageField.setText(
                String.valueOf(patient.getAge())
        );

        genderComboBox.setValue(
                patient.getGender()
        );

        phoneField.setText(
                patient.getPhone()
        );

        bloodGroupComboBox.setValue(
                patient.getBloodGroup()
        );
    }

    public void setPatientController(
            PatientController patientController) {

        this.patientController = patientController;
    }

    @FXML
    private void handleUpdate() {

        if (patient == null) {

            messageLabel.setText(
                    "No patient selected."
            );

            return;
        }

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

        if (name.isEmpty()
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

            int age =
                    Integer.parseInt(ageText);

            if (age <= 0 || age > 150) {

                messageLabel.setText(
                        "Please enter a valid age."
                );

                return;
            }

            patient.setName(name);
            patient.setAge(age);
            patient.setGender(gender);
            patient.setPhone(phone);
            patient.setBloodGroup(bloodGroup);

            patientService.updatePatient(patient);

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Patient Updated",
                    "Patient details have been updated successfully."
            );

            if (patientController != null) {

                patientController.refreshPatientTable();

                patientController.showPatientList();
            }

        } catch (NumberFormatException e) {

            messageLabel.setText(
                    "Age must be a number."
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    e.getMessage()
            );

            showAlert(
                    Alert.AlertType.ERROR,
                    "Unable to Update Patient",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleCancel() {

        if (patientController != null) {

            patientController.showPatientList();
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