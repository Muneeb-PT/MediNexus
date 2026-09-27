package com.medinexus.controller;

import com.medinexus.model.Appointment;
import com.medinexus.model.Doctor;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.navigation.NavigationHandler;
import com.medinexus.service.AppointmentService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoctorPatientsController {

    @FXML
    private Label doctorLabel;

    @FXML
    private TableView<Patient> patientTable;

    @FXML
    private TableColumn<Patient, String> patientIdColumn;

    @FXML
    private TableColumn<Patient, String> nameColumn;

    @FXML
    private TableColumn<Patient, String> ageColumn;

    @FXML
    private TableColumn<Patient, String> genderColumn;

    @FXML
    private TableColumn<Patient, String> phoneColumn;

    @FXML
    private TableColumn<Patient, String> bloodGroupColumn;

    private final DoctorService doctorService =
            new DoctorService();

    private final PatientService patientService =
            new PatientService();

    private final AppointmentService appointmentService =
            new AppointmentService();

    private final ObservableList<Patient> patients =
            FXCollections.observableArrayList();

    private NavigationHandler navigationHandler;


    @FXML
    public void initialize() {

        setupColumns();

        patientTable.setItems(patients);

        loadPatients();
    }


    public void setNavigationHandler(
            NavigationHandler navigationHandler) {

        this.navigationHandler =
                navigationHandler;
    }


    private void setupColumns() {

        patientIdColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getPatientId()
                                )
                        )
        );

        nameColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getName()
                        )
        );

        ageColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getAge()
                                )
                        )
        );

        genderColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getGender()
                        )
        );

        phoneColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getPhone()
                        )
        );

        bloodGroupColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getBloodGroup()
                        )
        );
    }


    private void loadPatients() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            doctorLabel.setText(
                    "No doctor is currently logged in."
            );

            patients.clear();

            return;
        }

        if (currentUser.getRole() != Role.DOCTOR) {

            doctorLabel.setText(
                    "Doctor access required."
            );

            patients.clear();

            return;
        }

        Doctor doctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (doctor == null) {

            doctorLabel.setText(
                    "Doctor profile not found."
            );

            patients.clear();

            return;
        }

        doctorLabel.setText(
                "Doctor: " + doctor.getName()
        );

        List<Appointment> appointments =
                appointmentService.getDoctorAppointments(
                        doctor.getDoctorId()
                );

        Set<Integer> patientIds =
                new HashSet<>();

        for (Appointment appointment :
                appointments) {

            patientIds.add(
                    appointment.getPatientId()
            );
        }

        patients.clear();

        for (Integer patientId :
                patientIds) {

            Patient patient =
                    patientService.getPatientById(
                            patientId
                    );

            if (patient != null) {
                patients.add(patient);
            }
        }
    }


    @FXML
    private void handleRefresh() {

        loadPatients();
    }


    @FXML
    private void handleViewRecords() {

        Patient selectedPatient =
                patientTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPatient == null) {

            showAlert(
                    "No Patient Selected",
                    "Please select a patient first."
            );

            return;
        }

        if (navigationHandler == null) {

            showAlert(
                    "Navigation Error",
                    "Medical Records navigation is not configured."
            );

            return;
        }

        navigationHandler.showMedicalRecords(
                selectedPatient
        );
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