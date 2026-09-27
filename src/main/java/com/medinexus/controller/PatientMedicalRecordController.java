package com.medinexus.controller;

import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Patient;
import com.medinexus.model.User;
import com.medinexus.service.MedicalRecordService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientMedicalRecordController {

    @FXML
    private Label patientLabel;

    @FXML
    private TableView<MedicalRecord> medicalRecordTable;

    @FXML
    private TableColumn<MedicalRecord, Integer> recordIdColumn;

    @FXML
    private TableColumn<MedicalRecord, Integer> doctorIdColumn;

    @FXML
    private TableColumn<MedicalRecord, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<MedicalRecord, String> diagnosisColumn;

    @FXML
    private TableColumn<MedicalRecord, String> symptomsColumn;

    @FXML
    private TableColumn<MedicalRecord, String> treatmentColumn;

    @FXML
    private TableColumn<MedicalRecord, String> notesColumn;

    @FXML
    private TableColumn<MedicalRecord, String> dateColumn;

    private final PatientService patientService =
            new PatientService();

    private final MedicalRecordService medicalRecordService =
            new MedicalRecordService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {

        recordIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("recordId"));

        doctorIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("doctorId"));

        appointmentIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentId"));

        diagnosisColumn.setCellValueFactory(
                new PropertyValueFactory<>("diagnosis"));

        symptomsColumn.setCellValueFactory(
                new PropertyValueFactory<>("symptoms"));

        treatmentColumn.setCellValueFactory(
                new PropertyValueFactory<>("treatment"));

        notesColumn.setCellValueFactory(
                new PropertyValueFactory<>("notes"));

        dateColumn.setCellValueFactory(
                cellData -> {

                    MedicalRecord record = cellData.getValue();

                    LocalDateTime date =
                            record.getCreatedAt();

                    String formatted =
                            date == null
                                    ? "-"
                                    : date.format(dateFormatter);

                    return new javafx.beans.property.SimpleStringProperty(
                            formatted
                    );
                }
        );

        loadPatientRecords();
    }

    private void loadPatientRecords() {

        try {

            User currentUser =
                    SessionManager.getCurrentUser();

            if (currentUser == null) {
                showError("No user is currently logged in.");
                return;
            }

            Patient patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient == null) {
                showError("Patient profile not found.");
                return;
            }

            patientLabel.setText(
                    "Patient: " + patient.getName()
                            + " | Patient ID: "
                            + patient.getPatientId()
            );

            List<MedicalRecord> records =
                    medicalRecordService.getPatientRecords(
                            patient.getPatientId()
                    );

            medicalRecordTable.setItems(
                    FXCollections.observableArrayList(records)
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to load medical records:\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleRefresh() {
        loadPatientRecords();
    }

    @FXML
    private void handleClose() {

        if (medicalRecordTable.getScene() == null) {
            return;
        }

        medicalRecordTable
                .getScene()
                .getWindow()
                .hide();
    }

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("MediNexus");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}