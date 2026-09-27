package com.medinexus.controller;

import com.medinexus.model.Doctor;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.DoctorService;
import com.medinexus.service.MedicalRecordService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

public class MedicalRecordsController {

    @FXML
    private Label patientLabel;

    @FXML
    private TableView<MedicalRecord> recordTable;

    @FXML
    private TableColumn<MedicalRecord, String> recordIdColumn;

    @FXML
    private TableColumn<MedicalRecord, String> dateColumn;

    @FXML
    private TableColumn<MedicalRecord, String> diagnosisColumn;

    @FXML
    private TableColumn<MedicalRecord, String> symptomsColumn;

    @FXML
    private TableColumn<MedicalRecord, String> treatmentColumn;

    @FXML
    private TableColumn<MedicalRecord, String> notesColumn;

    private final MedicalRecordService medicalRecordService =
            new MedicalRecordService();

    private final DoctorService doctorService =
            new DoctorService();

    private final ObservableList<MedicalRecord> records =
            FXCollections.observableArrayList();

    private Patient selectedPatient;

    @FXML
    public void initialize() {

        setupColumns();

        recordTable.setItems(records);
    }

    private void setupColumns() {

        recordIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue().getRecordId()
                        )
                )
        );

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue().getCreatedAt()
                        )
                )
        );

        diagnosisColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDiagnosis()
                )
        );

        symptomsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getSymptoms()
                )
        );

        treatmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getTreatment()
                )
        );

        notesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getNotes()
                )
        );
    }

    public void setPatient(Patient patient) {

        this.selectedPatient = patient;

        if (patient == null) {

            patientLabel.setText("Patient: -");

            records.clear();

            return;
        }

        patientLabel.setText(
                "Patient: "
                        + patient.getName()
                        + " | ID: "
                        + patient.getPatientId()
        );

        loadRecords();
    }

    private void loadRecords() {

        if (selectedPatient == null) {
            return;
        }

        List<MedicalRecord> patientRecords =
                medicalRecordService.getPatientRecords(
                        selectedPatient.getPatientId()
                );

        records.setAll(patientRecords);
    }

    @FXML
    private void handleRefresh() {

        loadRecords();
    }

    @FXML
    private void handleAddRecord() {

        if (selectedPatient == null) {

            showAlert(
                    "No Patient Selected",
                    "Please select a patient first."
            );

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

        Doctor doctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (doctor == null) {

            showAlert(
                    "Doctor Not Found",
                    "Doctor profile could not be found."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/AddMedicalRecordView.fxml"
                            )
                    );

            Parent root = loader.load();

            AddMedicalRecordController controller =
                    loader.getController();

            controller.setPatient(selectedPatient);

            Stage stage = new Stage();

            stage.setTitle("Add Medical Record");

            stage.setScene(
                    new Scene(root, 850, 650)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Unable to open the medical record form."
            );
        }
    }

    @FXML
    private void handleCreatePrescription() {

        if (selectedPatient == null) {

            showAlert(
                    "No Patient Selected",
                    "Please select a patient first."
            );

            return;
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || currentUser.getRole() != Role.DOCTOR) {

            showAlert(
                    "Access Denied",
                    "Only doctors can create prescriptions."
            );

            return;
        }

        Doctor doctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (doctor == null) {

            showAlert(
                    "Doctor Not Found",
                    "Doctor profile could not be found."
            );

            return;
        }

        MedicalRecord selectedRecord =
                recordTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedRecord == null) {

            showAlert(
                    "No Medical Record Selected",
                    "Please select a medical record first."
            );

            return;
        }

        if (selectedRecord.getDoctorId()
                != doctor.getDoctorId()) {

            showAlert(
                    "Access Denied",
                    "You can only create prescriptions "
                            + "for your own medical records."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/AddPrescriptionView.fxml"
                            )
                    );

            Parent root = loader.load();

            AddPrescriptionController controller =
                    loader.getController();

            controller.setMedicalRecord(
                    selectedRecord
            );

            Stage stage = new Stage();

            stage.setTitle("Create E-Prescription");

            stage.setScene(
                    new Scene(root, 850, 650)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Unable to open the prescription form."
            );
        }
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
    @FXML
    private void handleViewPrescriptions() {

        if (selectedPatient == null) {

            showAlert(
                    "No Patient Selected",
                    "Please select a patient first."
            );

            return;
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    "Access Denied",
                    "No user is currently logged in."
            );

            return;
        }

        MedicalRecord selectedRecord =
                recordTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedRecord == null) {

            showAlert(
                    "No Medical Record Selected",
                    "Please select a medical record first."
            );

            return;
        }

        if (currentUser.getRole() == Role.DOCTOR) {

            Doctor doctor =
                    doctorService.getDoctorByUserId(
                            currentUser.getUserId()
                    );

            if (doctor == null
                    || selectedRecord.getDoctorId()
                    != doctor.getDoctorId()) {

                showAlert(
                        "Access Denied",
                        "You can only view prescriptions "
                                + "associated with your records."
                );

                return;
            }
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/PrescriptionView.fxml"
                            )
                    );

            Parent root = loader.load();

            PrescriptionController controller =
                    loader.getController();

            controller.setMedicalRecord(
                    selectedRecord
            );

            Stage stage = new Stage();

            stage.setTitle("E-Prescriptions");

            stage.setScene(
                    new Scene(root, 1100, 650)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Error",
                    "Unable to open prescriptions."
            );
        }
    }

}