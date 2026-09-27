package com.medinexus.controller;

import com.medinexus.model.Patient;
import com.medinexus.model.Prescription;
import com.medinexus.model.Role;
import com.medinexus.service.PatientService;
import com.medinexus.service.PrescriptionService;
import com.medinexus.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientPrescriptionController {

    @FXML
    private TableView<Prescription> prescriptionTable;

    @FXML
    private TableColumn<Prescription, Number> prescriptionIdColumn;

    @FXML
    private TableColumn<Prescription, String> dateColumn;

    @FXML
    private TableColumn<Prescription, Number> medicalRecordColumn;

    @FXML
    private TableColumn<Prescription, Number> medicineColumn;

    @FXML
    private TableColumn<Prescription, Number> quantityColumn;

    @FXML
    private TableColumn<Prescription, String> dosageColumn;

    @FXML
    private TableColumn<Prescription, String> frequencyColumn;

    @FXML
    private TableColumn<Prescription, String> durationColumn;

    @FXML
    private TableColumn<Prescription, String> instructionsColumn;

    @FXML
    private TableColumn<Prescription, String> statusColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private Button refreshButton;

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final PatientService patientService =
            new PatientService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private void initialize() {

        prescriptionIdColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleIntegerProperty(
                        cell.getValue().getPrescriptionId()
                )
        );

        dateColumn.setCellValueFactory(cell -> {

            if (cell.getValue().getCreatedAt() == null) {
                return new SimpleStringProperty("-");
            }

            return new SimpleStringProperty(
                    cell.getValue()
                            .getCreatedAt()
                            .format(dateFormatter)
            );
        });

        medicalRecordColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleIntegerProperty(
                        cell.getValue().getMedicalRecordId()
                )
        );

        medicineColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleIntegerProperty(
                        cell.getValue().getMedicineId()
                )
        );

        quantityColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleIntegerProperty(
                        cell.getValue().getQuantity()
                )
        );

        dosageColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safe(cell.getValue().getDosage())
                )
        );

        frequencyColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safe(cell.getValue().getFrequency())
                )
        );

        durationColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safe(cell.getValue().getDuration())
                )
        );

        instructionsColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safe(cell.getValue().getInstructions())
                )
        );

        statusColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getStatus() == null
                                ? "-"
                                : cell.getValue().getStatus().name()
                )
        );

        loadPrescriptions();
    }

    @FXML
    private void handleRefresh() {
        loadPrescriptions();
    }

    @FXML
    private void handleClose() {
        if (prescriptionTable.getScene() != null &&
                prescriptionTable.getScene().getWindow() != null) {

            prescriptionTable.getScene()
                    .getWindow()
                    .hide();
        }
    }

    private void loadPrescriptions() {

        try {

            if (!SessionManager.isLoggedIn()) {
                showMessage("No user is currently logged in.");
                return;
            }

            if (SessionManager.getCurrentUser().getRole()
                    != Role.PATIENT) {

                showMessage(
                        "Access denied. Only patients can view prescriptions."
                );

                prescriptionTable.setItems(
                        FXCollections.observableArrayList()
                );

                return;
            }

            long userId =
                    SessionManager.getCurrentUser().getUserId();

            Patient patient =
                    patientService.getPatientByUserId((int) userId);

            if (patient == null) {
                showMessage("Patient profile was not found.");
                return;
            }

            List<Prescription> prescriptions =
                    prescriptionService.getPatientPrescriptions(
                            patient.getPatientId()
                    );

            ObservableList<Prescription> items =
                    FXCollections.observableArrayList(
                            prescriptions
                    );

            prescriptionTable.setItems(items);

            if (items.isEmpty()) {
                showMessage("No prescriptions found.");
            } else {
                showMessage(
                        items.size()
                                + " prescription(s) found."
                );
            }

        } catch (Exception e) {

            showMessage(
                    "Unable to load prescriptions: "
                            + safe(e.getMessage())
            );
        }
    }

    private void showMessage(String message) {

        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    private String safe(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "-";
        }

        return value;
    }
}