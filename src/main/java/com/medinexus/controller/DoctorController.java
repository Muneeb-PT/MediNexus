package com.medinexus.controller;

import com.medinexus.model.Appointment;
import com.medinexus.model.Doctor;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.AppointmentService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class DoctorController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private Label appointmentCountLabel;

    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private TableColumn<Appointment, String> patientColumn;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> reasonColumn;

    @FXML
    private TableColumn<Appointment, String> urgencyColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;


    private final DoctorService doctorService =
            new DoctorService();

    private final AppointmentService appointmentService =
            new AppointmentService();

    private final ObservableList<Appointment> appointments =
            FXCollections.observableArrayList();


    @FXML
    public void initialize() {

        setupColumns();

        loadDoctorDashboard();
    }


    private void setupColumns() {

        patientColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getPatientName()
                        )
        );

        dateColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getAppointmentDate()
                                )
                        )
        );

        timeColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getAppointmentTime()
                                )
                        )
        );

        reasonColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getReason()
                        )
        );

        urgencyColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getUrgency()
                        )
        );

        statusColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getStatus()
                                        .name()
                        )
        );
    }


    private void loadDoctorDashboard() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            welcomeLabel.setText(
                    "No user is currently logged in."
            );

            return;
        }


        if (currentUser.getRole() != Role.DOCTOR) {

            welcomeLabel.setText(
                    "This dashboard is for doctors only."
            );

            return;
        }


        Doctor doctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );


        if (doctor == null) {

            welcomeLabel.setText(
                    "Doctor profile not found."
            );

            return;
        }


        welcomeLabel.setText(
                "Welcome, " + doctor.getName()
        );

        specializationLabel.setText(
                doctor.getSpecialization()
        );


        List<Appointment> doctorAppointments =
                appointmentService.getDoctorAppointments(
                        doctor.getDoctorId()
                );


        appointments.setAll(
                doctorAppointments
        );

        appointmentTable.setItems(
                appointments
        );


        appointmentCountLabel.setText(
                String.valueOf(
                        doctorAppointments.size()
                )
        );
    }
}