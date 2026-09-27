package com.medinexus.controller;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.model.Doctor;
import com.medinexus.model.Patient;
import com.medinexus.model.PriorityResult;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.AppointmentPriorityService;
import com.medinexus.service.AppointmentService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;

public class AppointmentController {

    @FXML
    private TableView<Appointment> appointmentTable;

    @FXML
    private Button medicalRecordButton;

    @FXML
    private Button triageDetailsButton;

    @FXML
    private TableColumn<Appointment, String> idColumn;

    @FXML
    private TableColumn<Appointment, String> patientColumn;

    @FXML
    private TableColumn<Appointment, String> doctorColumn;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> reasonColumn;

    @FXML
    private TableColumn<Appointment, String> urgencyColumn;

    @FXML
    private TableColumn<Appointment, String> priorityScoreColumn;

    @FXML
    private TableColumn<Appointment, String> priorityColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button completeButton;

    private final AppointmentService appointmentService =
            new AppointmentService();

    private final PatientService patientService =
            new PatientService();

    private final DoctorService doctorService =
            new DoctorService();

    private final AppointmentPriorityService appointmentPriorityService =
            new AppointmentPriorityService();

    private final ObservableList<Appointment> appointments =
            FXCollections.observableArrayList();


    // =========================================================
    // INITIALIZATION
    // =========================================================

    @FXML
    public void initialize() {

        configureColumns();

        appointmentTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateActionButtons()
                );

        loadAppointments();
    }


    // =========================================================
    // TABLE COLUMNS
    // =========================================================

    private void configureColumns() {

        idColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getAppointmentId()
                                )
                        )
        );

        patientColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safe(
                                        cellData.getValue()
                                                .getPatientName()
                                )
                        )
        );

        doctorColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safe(
                                        cellData.getValue()
                                                .getDoctorName()
                                )
                        )
        );

        dateColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getAppointmentDate() == null
                                        ? ""
                                        : cellData.getValue()
                                        .getAppointmentDate()
                                        .toString()
                        )
        );

        timeColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getAppointmentTime() == null
                                        ? ""
                                        : cellData.getValue()
                                        .getAppointmentTime()
                                        .toString()
                        )
        );

        reasonColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safe(
                                        cellData.getValue()
                                                .getReason()
                                )
                        )
        );

        urgencyColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                safe(
                                        cellData.getValue()
                                                .getUrgency()
                                )
                        )
        );


        // =========================================================
        // SMART TRIAGE SCORE
        // =========================================================

        priorityScoreColumn.setCellValueFactory(
                cellData -> {

                    int score =
                            appointmentPriorityService
                                    .calculateTriageScore(
                                            cellData.getValue()
                                    );

                    return new SimpleStringProperty(
                            String.valueOf(score)
                    );
                }
        );


        /*
         * Professional score badge.
         *
         * The score itself remains calculated by the
         * Smart Triage Engine.
         */
        priorityScoreColumn.setCellFactory(
                column ->
                        new TableCell<Appointment, String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty || item == null) {

                                    setText(null);
                                    setStyle("");

                                    return;
                                }

                                setText(item);

                                setAlignment(
                                        javafx.geometry.Pos.CENTER
                                );

                                setStyle(
                                        "-fx-font-weight: bold;"
                                                + "-fx-background-radius: 6;"
                                                + "-fx-padding: 4 8 4 8;"
                                );
                            }
                        }
        );


        // =========================================================
        // SMART TRIAGE CATEGORY
        // =========================================================

        priorityColumn.setCellValueFactory(
                cellData -> {

                    PriorityResult result =
                            appointmentPriorityService
                                    .calculatePriority(
                                            cellData.getValue()
                                    );

                    return new SimpleStringProperty(
                            result.getCategory()
                    );
                }
        );


        /*
         * Professional priority badges.
         *
         * IMPORTANT:
         * These styles are visual only.
         * The actual priority calculation remains
         * inside AppointmentPriorityService.
         */
        priorityColumn.setCellFactory(
                column ->
                        new TableCell<Appointment, String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty || item == null) {

                                    setText(null);
                                    setStyle("");

                                    return;
                                }

                                String priority =
                                        item.trim().toUpperCase();

                                setText(priority);

                                setAlignment(
                                        javafx.geometry.Pos.CENTER
                                );

                                switch (priority) {

                                    case "EMERGENCY":

                                        setStyle(
                                                "-fx-font-weight: bold;"
                                                        + "-fx-text-fill: #b91c1c;"
                                                        + "-fx-background-color: #fee2e2;"
                                                        + "-fx-background-radius: 6;"
                                                        + "-fx-padding: 4 8 4 8;"
                                        );

                                        break;


                                    case "CHRONIC":

                                        setStyle(
                                                "-fx-font-weight: bold;"
                                                        + "-fx-text-fill: #b45309;"
                                                        + "-fx-background-color: #fef3c7;"
                                                        + "-fx-background-radius: 6;"
                                                        + "-fx-padding: 4 8 4 8;"
                                        );

                                        break;


                                    case "ROUTINE":

                                        setStyle(
                                                "-fx-font-weight: bold;"
                                                        + "-fx-text-fill: #166534;"
                                                        + "-fx-background-color: #dcfce7;"
                                                        + "-fx-background-radius: 6;"
                                                        + "-fx-padding: 4 8 4 8;"
                                        );

                                        break;


                                    default:

                                        setStyle(
                                                "-fx-font-weight: bold;"
                                                        + "-fx-background-color: #f3f4f6;"
                                                        + "-fx-background-radius: 6;"
                                                        + "-fx-padding: 4 8 4 8;"
                                        );

                                        break;
                                }
                            }
                        }
        );


        // =========================================================
        // STATUS
        // =========================================================

        statusColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getStatus() == null
                                        ? ""
                                        : cellData.getValue()
                                        .getStatus()
                                        .name()
                        )
        );
    }


    // =========================================================
    // LOAD APPOINTMENTS
    // =========================================================

    private void loadAppointments() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            messageLabel.setText(
                    "No user is currently logged in."
            );

            appointmentTable
                    .getItems()
                    .clear();

            updateActionButtons();

            return;
        }

        Role role =
                currentUser.getRole();

        appointments.clear();

        try {

            /*
             * PATIENT
             *
             * Patients can see only their own appointments.
             *
             * Patients do not access the operational
             * Smart Triage Queue.
             */
            if (role == Role.PATIENT) {

                Patient patient =
                        patientService.getPatientByUserId(
                                currentUser.getUserId()
                        );

                if (patient == null) {

                    messageLabel.setText(
                            "Patient profile not found."
                    );

                    appointmentTable.setItems(
                            appointments
                    );

                    updateActionButtons();

                    return;
                }

                appointments.addAll(
                        appointmentService
                                .getPatientAppointments(
                                        patient.getPatientId()
                                )
                );

                messageLabel.setText(
                        "Showing your appointments."
                );

            }

            /*
             * ADMIN / RECEPTIONIST / DOCTOR
             *
             * Staff users receive the Smart Triage Queue.
             *
             * AppointmentService enforces:
             *
             * ADMIN        -> all appointments
             * RECEPTIONIST -> all appointments
             * DOCTOR       -> own appointments only
             */
            else {

                appointments.addAll(
                        appointmentService
                                .getSmartTriageQueue()
                );

                if (role == Role.DOCTOR) {

                    messageLabel.setText(
                            "Showing your appointments ordered by Smart Triage."
                    );

                } else {

                    messageLabel.setText(
                            "Showing Smart Triage Queue."
                    );
                }
            }

            appointmentTable.setItems(
                    appointments
            );

        } catch (SecurityException e) {

            appointments.clear();

            appointmentTable.setItems(
                    appointments
            );

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            appointments.clear();

            appointmentTable.setItems(
                    appointments
            );

            messageLabel.setText(
                    "Unable to load appointments."
            );
        }

        updateActionButtons();
    }


    // =========================================================
    // CONFIRM APPOINTMENT
    // =========================================================

    @FXML
    private void handleConfirm() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedAppointment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select an appointment first."
            );

            return;
        }

        try {

            boolean success =
                    appointmentService.confirmAppointment(
                            selectedAppointment.getAppointmentId()
                    );

            if (success) {

                messageLabel.setText(
                        "Appointment confirmed successfully."
                );

                loadAppointments();

            } else {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Cannot Confirm",
                        "Only SCHEDULED appointments can be confirmed."
                );
            }

        } catch (SecurityException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // CANCEL APPOINTMENT
    // =========================================================

    @FXML
    private void handleCancel() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedAppointment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select an appointment first."
            );

            return;
        }

        try {

            boolean success =
                    appointmentService.cancelAppointment(
                            selectedAppointment.getAppointmentId()
                    );

            if (success) {

                messageLabel.setText(
                        "Appointment cancelled successfully."
                );

                loadAppointments();

            } else {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Cannot Cancel",
                        "This appointment cannot be cancelled."
                );
            }

        } catch (SecurityException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // COMPLETE APPOINTMENT
    // =========================================================

    @FXML
    private void handleComplete() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedAppointment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select an appointment first."
            );

            return;
        }

        try {

            boolean success =
                    appointmentService.completeAppointment(
                            selectedAppointment.getAppointmentId()
                    );

            if (success) {

                messageLabel.setText(
                        "Appointment completed successfully."
                );

                loadAppointments();

            } else {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Cannot Complete",
                        "Only CONFIRMED appointments can be completed."
                );
            }

        } catch (SecurityException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // MEDICAL RECORD
    // =========================================================

    @FXML
    private void handleCreateMedicalRecord() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedAppointment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a completed appointment first."
            );

            return;
        }

        if (selectedAppointment.getStatus()
                != AppointmentStatus.COMPLETED) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid Appointment",
                    "A medical record can only be created for a completed appointment."
            );

            return;
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null ||
                currentUser.getRole() != Role.DOCTOR) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only doctors can create medical records."
            );

            return;
        }

        /*
         * Additional ownership protection.
         *
         * The logged-in doctor must own the appointment.
         */
        if (selectedAppointment.getDoctorId()
                != getCurrentDoctorId()) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Doctors can only create medical records for their own appointments."
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

            Parent root =
                    loader.load();

            AddMedicalRecordController controller =
                    loader.getController();

            controller.setAppointment(
                    selectedAppointment
            );

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Create Medical Record - MediNexus"
            );

            stage.setScene(
                    new Scene(
                            root,
                            850,
                            700
                    )
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Unable to open the medical record screen."
            );
        }
    }


    // =========================================================
    // SMART TRIAGE DETAILS
    // =========================================================

    @FXML
    private void handleTriageDetails() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedAppointment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select an appointment first."
            );

            return;
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST
                && currentUser.getRole() != Role.DOCTOR)) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Smart Triage details are available only to hospital staff."
            );

            return;
        }

        try {

            PriorityResult priorityResult =
                    appointmentPriorityService
                            .calculatePriority(
                                    selectedAppointment
                            );

            int finalScore =
                    appointmentPriorityService
                            .calculateTriageScore(
                                    selectedAppointment
                            );

            int waitingBonus =
                    appointmentPriorityService
                            .calculateWaitingTimeBonus(
                                    selectedAppointment
                            );

            String explanation =
                    appointmentPriorityService
                            .explainTriageScore(
                                    selectedAppointment
                            );

            String patientName =
                    safe(
                            selectedAppointment
                                    .getPatientName()
                    );

            String doctorName =
                    safe(
                            selectedAppointment
                                    .getDoctorName()
                    );

            String date =
                    selectedAppointment
                            .getAppointmentDate() == null
                            ? "Not specified"
                            : selectedAppointment
                            .getAppointmentDate()
                            .toString();

            String time =
                    selectedAppointment
                            .getAppointmentTime() == null
                            ? "Not specified"
                            : selectedAppointment
                            .getAppointmentTime()
                            .toString();

            String details =
                    "Patient: " + patientName
                            + "\n"
                            + "Doctor: " + doctorName
                            + "\n"
                            + "Appointment: " + date
                            + " " + time
                            + "\n\n"
                            + "Priority Category: "
                            + priorityResult.getCategory()
                            + "\n"
                            + "Base Severity Score: "
                            + priorityResult.getScore()
                            + "\n"
                            + "Waiting-Time Bonus: "
                            + waitingBonus
                            + "\n"
                            + "Final Triage Score: "
                            + finalScore
                            + "\n\n"
                            + "Assessment:\n"
                            + explanation
                            + "\n\n"
                            + "Note: Smart Triage is a rule-based "
                            + "operational prioritization mechanism. "
                            + "It does not replace clinical judgment.";

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Smart Triage Assessment",
                    details
            );

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Triage Error",
                    "Unable to calculate the Smart Triage assessment."
            );
        }
    }


    // =========================================================
    // ACTION BUTTON SECURITY
    // =========================================================

    private void updateActionButtons() {

        Appointment selectedAppointment =
                appointmentTable
                        .getSelectionModel()
                        .getSelectedItem();

        User currentUser =
                SessionManager.getCurrentUser();

        boolean staffUser =
                currentUser != null &&
                        (currentUser.getRole() == Role.ADMIN ||
                                currentUser.getRole() == Role.DOCTOR ||
                                currentUser.getRole() == Role.RECEPTIONIST);

        /*
         * No staff user or no appointment selected.
         */
        if (!staffUser || selectedAppointment == null) {

            disableButton(confirmButton);
            disableButton(cancelButton);
            disableButton(completeButton);
            disableButton(medicalRecordButton);
            disableButton(triageDetailsButton);

            return;
        }

        AppointmentStatus status =
                selectedAppointment.getStatus();


        // -----------------------------------------------------
        // CONFIRM
        // -----------------------------------------------------

        if (confirmButton != null) {

            boolean canConfirm =
                    status == AppointmentStatus.SCHEDULED;

            /*
             * Doctors can confirm only their own appointments.
             */
            if (currentUser.getRole() == Role.DOCTOR) {

                canConfirm =
                        canConfirm &&
                                selectedAppointment.getDoctorId()
                                        == getCurrentDoctorId();
            }

            confirmButton.setDisable(!canConfirm);
        }


        // -----------------------------------------------------
        // CANCEL
        // -----------------------------------------------------

        if (cancelButton != null) {

            boolean canCancel =
                    status != AppointmentStatus.COMPLETED &&
                            status != AppointmentStatus.CANCELLED &&
                            status != AppointmentStatus.MISSED;

            /*
             * Doctors can cancel only their own appointments.
             */
            if (currentUser.getRole() == Role.DOCTOR) {

                canCancel =
                        canCancel &&
                                selectedAppointment.getDoctorId()
                                        == getCurrentDoctorId();
            }

            cancelButton.setDisable(!canCancel);
        }


        // -----------------------------------------------------
        // COMPLETE
        // -----------------------------------------------------

        if (completeButton != null) {

            boolean canComplete =
                    status == AppointmentStatus.CONFIRMED;

            /*
             * Doctors can complete only their own appointments.
             */
            if (currentUser.getRole() == Role.DOCTOR) {

                canComplete =
                        canComplete &&
                                selectedAppointment.getDoctorId()
                                        == getCurrentDoctorId();
            }

            completeButton.setDisable(!canComplete);
        }


        // -----------------------------------------------------
        // MEDICAL RECORD
        // -----------------------------------------------------

        if (medicalRecordButton != null) {

            boolean allowed =
                    currentUser.getRole() == Role.DOCTOR
                            && selectedAppointment.getDoctorId()
                            == getCurrentDoctorId()
                            && status == AppointmentStatus.COMPLETED;

            medicalRecordButton.setDisable(!allowed);
        }


        // -----------------------------------------------------
        // SMART TRIAGE DETAILS
        // -----------------------------------------------------

        /*
         * Smart Triage details are available to:
         *
         * ADMIN
         * RECEPTIONIST
         * DOCTOR
         *
         * Patients do not receive the operational
         * triage assessment.
         */
        if (triageDetailsButton != null) {

            boolean allowed =
                    currentUser.getRole() == Role.ADMIN
                            || currentUser.getRole() == Role.RECEPTIONIST
                            || currentUser.getRole() == Role.DOCTOR;

            triageDetailsButton.setDisable(!allowed);
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadAppointments();
    }


    // =========================================================
    // CREATE APPOINTMENT
    // =========================================================

    @FXML
    private void handleCreateAppointment() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/CreateAppointmentView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Create Appointment - MediNexus"
            );

            stage.setScene(
                    new Scene(
                            root,
                            800,
                            700
                    )
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Unable to open appointment booking."
            );
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }


    private void disableButton(Button button) {

        if (button != null) {
            button.setDisable(true);
        }
    }


    private int getCurrentDoctorId() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return -1;
        }

        Doctor doctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (doctor == null) {
            return -1;
        }

        return doctor.getDoctorId();
    }
}