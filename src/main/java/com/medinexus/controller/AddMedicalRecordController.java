package com.medinexus.controller;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.model.Doctor;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.AppointmentService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.MedicalRecordService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.List;

public class AddMedicalRecordController {


    @FXML
    private Label patientLabel;

    @FXML
    private Label doctorLabel;

    @FXML
    private ComboBox<Appointment> appointmentComboBox;

    @FXML
    private TextArea symptomsField;

    @FXML
    private TextArea diagnosisField;

    @FXML
    private TextArea treatmentField;

    @FXML
    private TextArea notesField;

    private final MedicalRecordService medicalRecordService =
            new MedicalRecordService();

    private final AppointmentService appointmentService =
            new AppointmentService();

    private final DoctorService doctorService =
            new DoctorService();

    private final PatientService patientService =
            new PatientService();

    private Patient selectedPatient;

    private Doctor currentDoctor;

    @FXML
    public void initialize() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    "Access Denied",
                    "No user is currently logged in."
            );

            return;
        }

        if (currentUser.getRole() != Role.DOCTOR) {

            showAlert(
                    "Access Denied",
                    "Only doctors can create medical records."
            );

            return;
        }

        currentDoctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (currentDoctor == null) {

            showAlert(
                    "Doctor Not Found",
                    "Doctor profile could not be found."
            );

            return;
        }

        appointmentComboBox.setItems(
                FXCollections.observableArrayList()
        );

        appointmentComboBox.setConverter(
                new StringConverter<Appointment>() {

                    @Override
                    public String toString(
                            Appointment appointment) {

                        if (appointment == null) {
                            return "";
                        }

                        return appointment.getAppointmentDate()
                                + " "
                                + appointment.getAppointmentTime()
                                + " - "
                                + appointment.getReason()
                                + " ["
                                + appointment.getStatus()
                                + "]";
                    }

                    @Override
                    public Appointment fromString(
                            String string) {

                        return null;
                    }
                }
        );

        doctorLabel.setText(
                "Doctor: " + currentDoctor.getName()
        );

        appointmentComboBox.setPromptText(
                "Select a completed appointment"
        );
    }

    public void setPatient(Patient patient) {

        this.selectedPatient = patient;

        if (patient == null) {

            patientLabel.setText(
                    "Patient: -"
            );

            appointmentComboBox.getItems().clear();

            appointmentComboBox.setPromptText(
                    "No patient selected"
            );

            return;
        }

        patientLabel.setText(
                "Patient: "
                        + patient.getName()
                        + " | ID: "
                        + patient.getPatientId()
        );

        if (currentDoctor != null) {

            doctorLabel.setText(
                    "Doctor: "
                            + currentDoctor.getName()
                            + " | ID: "
                            + currentDoctor.getDoctorId()
            );
        }

        loadAppointments();
    }

    /**
     * Opens this screen for a specific appointment.
     *
     * The appointment must belong to the logged-in doctor
     * and must be completed.
     */
    public void setAppointment(Appointment appointment) {

        if (appointment == null) {
            return;
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || currentUser.getRole() != Role.DOCTOR) {

            showAlert(
                    "Access Denied",
                    "Only doctors can create medical records."
            );

            return;
        }

        if (currentDoctor == null) {
            return;
        }

        if (appointment.getDoctorId()
                != currentDoctor.getDoctorId()) {

            showAlert(
                    "Access Denied",
                    "This appointment does not belong to you."
            );

            return;
        }

        if (appointment.getStatus()
                != AppointmentStatus.COMPLETED) {

            showAlert(
                    "Invalid Appointment",
                    "A medical record can only be created for a completed appointment."
            );

            return;
        }

        Patient patient =
                patientService.getPatientById(
                        appointment.getPatientId()
                );

        if (patient == null) {

            showAlert(
                    "Patient Not Found",
                    "The patient profile could not be found."
            );

            return;
        }

        setPatient(patient);

        for (Appointment item :
                appointmentComboBox.getItems()) {

            if (item.getAppointmentId()
                    == appointment.getAppointmentId()) {

                appointmentComboBox.setValue(item);

                break;
            }
        }
    }

    /**
     * Loads completed appointments belonging to:
     *
     * 1. The currently logged-in doctor
     * 2. The selected patient
     *
     * Appointments that already have a medical record
     * are excluded.
     */
    private void loadAppointments() {

        appointmentComboBox.getItems().clear();

        if (selectedPatient == null) {

            appointmentComboBox.setPromptText(
                    "No patient selected"
            );

            return;
        }

        if (currentDoctor == null) {

            appointmentComboBox.setPromptText(
                    "Doctor profile not found"
            );

            return;
        }

        List<Appointment> doctorAppointments =
                appointmentService.getDoctorAppointments(
                        currentDoctor.getDoctorId()
                );

        for (Appointment appointment :
                doctorAppointments) {

            if (appointment.getPatientId()
                    != selectedPatient.getPatientId()) {

                continue;
            }

            if (appointment.getStatus()
                    != AppointmentStatus.COMPLETED) {

                continue;
            }

            if (hasMedicalRecord(
                    appointment.getAppointmentId())) {

                continue;
            }

            appointmentComboBox.getItems().add(
                    appointment
            );
        }

        if (appointmentComboBox.getItems().isEmpty()) {

            appointmentComboBox.setPromptText(
                    "No appointments found for this patient"
            );

        } else {

            appointmentComboBox.setPromptText(
                    "Select appointment"
            );
        }
    }

    private boolean hasMedicalRecord(
            int appointmentId) {

        List<MedicalRecord> records =
                medicalRecordService.getAllRecords();

        for (MedicalRecord record :
                records) {

            if (record.getAppointmentId()
                    == appointmentId) {

                return true;
            }
        }

        return false;
    }

    @FXML
    private void handleSave() {

        if (selectedPatient == null) {

            showAlert(
                    "Missing Patient",
                    "No patient has been selected."
            );

            return;
        }

        if (currentDoctor == null) {

            showAlert(
                    "Missing Doctor",
                    "Doctor profile could not be found."
            );

            return;
        }

        Appointment selectedAppointment =
                appointmentComboBox.getValue();

        if (selectedAppointment == null) {

            showAlert(
                    "Missing Appointment",
                    "Please select a completed appointment."
            );

            return;
        }

        if (selectedAppointment.getStatus()
                != AppointmentStatus.COMPLETED) {

            showAlert(
                    "Invalid Appointment",
                    "A medical record can only be created for a completed appointment."
            );

            return;
        }

        if (selectedAppointment.getPatientId()
                != selectedPatient.getPatientId()) {

            showAlert(
                    "Invalid Patient",
                    "The selected appointment does not belong to this patient."
            );

            return;
        }

        if (selectedAppointment.getDoctorId()
                != currentDoctor.getDoctorId()) {

            showAlert(
                    "Invalid Doctor",
                    "The selected appointment does not belong to the logged-in doctor."
            );

            return;
        }

        if (hasMedicalRecord(
                selectedAppointment.getAppointmentId())) {

            showAlert(
                    "Medical Record Exists",
                    "A medical record already exists for this appointment."
            );

            loadAppointments();

            return;
        }

        String symptoms =
                safeTrim(symptomsField.getText());

        String diagnosis =
                safeTrim(diagnosisField.getText());

        String treatment =
                safeTrim(treatmentField.getText());

        String notes =
                safeTrim(notesField.getText());

        if (symptoms.isEmpty()
                || diagnosis.isEmpty()
                || treatment.isEmpty()) {

            showAlert(
                    "Incomplete Information",
                    "Symptoms, diagnosis and treatment are required."
            );

            return;
        }

        try {

            /*
             * The record ID is now generated by MySQL
             * using AUTO_INCREMENT.
             *
             * We therefore pass 0 here. The repository
             * does not insert this value into the database.
             */
            MedicalRecord record =
                    new MedicalRecord(
                            0,
                            selectedPatient.getPatientId(),
                            currentDoctor.getDoctorId(),
                            selectedAppointment.getAppointmentId(),
                            diagnosis,
                            symptoms,
                            treatment,
                            notes,
                            LocalDateTime.now()
                    );

            MedicalRecord savedRecord =
                    medicalRecordService.createRecord(
                            record
                    );

            showAlert(
                    "Success",
                    "Medical record created successfully."
                            + "\n\n"
                            + "Medical Record #"
                            + savedRecord.getRecordId()
            );

            clearForm();

            loadAppointments();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Unable to create medical record.\n\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleClear() {

        clearForm();
    }

    private void clearForm() {

        appointmentComboBox.setValue(null);

        symptomsField.clear();

        diagnosisField.clear();

        treatmentField.clear();

        notesField.clear();
    }

    @FXML
    private void handleCancel() {

        clearForm();
    }

    private String safeTrim(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

}