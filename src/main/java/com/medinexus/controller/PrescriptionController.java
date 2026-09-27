package com.medinexus.controller;

import com.medinexus.model.Bill;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Medicine;
import com.medinexus.model.Prescription;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.MedicineService;
import com.medinexus.service.PrescriptionBillingService;
import com.medinexus.service.PrescriptionService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrescriptionController {

    @FXML
    private Label patientLabel;

    @FXML
    private Label recordLabel;

    @FXML
    private TableView<Prescription> prescriptionTable;

    @FXML
    private TableColumn<Prescription, String> prescriptionIdColumn;

    @FXML
    private TableColumn<Prescription, String> dateColumn;

    @FXML
    private TableColumn<Prescription, String> medicineColumn;

    @FXML
    private TableColumn<Prescription, String> quantityColumn;

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

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final MedicineService medicineService =
            new MedicineService();

    private final PrescriptionBillingService
            prescriptionBillingService =
            new PrescriptionBillingService();

    private final ObservableList<Prescription> prescriptions =
            FXCollections.observableArrayList();

    private MedicalRecord selectedRecord;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {

        setupColumns();

        prescriptionTable.setItems(prescriptions);
    }

    private void setupColumns() {

        prescriptionIdColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getPrescriptionId()
                                )
                        )
        );

        dateColumn.setCellValueFactory(
                cellData -> {

                    if (cellData.getValue().getCreatedAt()
                            == null) {

                        return new SimpleStringProperty("-");
                    }

                    return new SimpleStringProperty(
                            cellData.getValue()
                                    .getCreatedAt()
                                    .format(dateFormatter)
                    );
                }
        );

        medicineColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                getMedicineName(
                                        cellData.getValue()
                                                .getMedicineId()
                                )
                        )
        );

        quantityColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getQuantity()
                                )
                        )
        );

        dosageColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getDosage()
                        )
        );

        frequencyColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getFrequency()
                        )
        );

        durationColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getDuration()
                        )
        );

        instructionsColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getInstructions()
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

    private String getMedicineName(int medicineId) {

        Medicine medicine =
                medicineService.getMedicineById(
                        medicineId
                );

        if (medicine == null) {
            return "Unknown Medicine";
        }

        return medicine.getName();
    }

    public void setMedicalRecord(
            MedicalRecord medicalRecord) {

        this.selectedRecord =
                medicalRecord;

        if (medicalRecord == null) {

            patientLabel.setText(
                    "Patient: -"
            );

            recordLabel.setText(
                    "Medical Record: -"
            );

            prescriptions.clear();

            return;
        }

        recordLabel.setText(
                "Medical Record: #"
                        + medicalRecord.getRecordId()
        );

        patientLabel.setText(
                "Patient ID: "
                        + medicalRecord.getPatientId()
        );

        loadPrescriptions();
    }

    private void loadPrescriptions() {

        if (selectedRecord == null) {
            return;
        }

        List<Prescription> recordPrescriptions =
                prescriptionService
                        .getMedicalRecordPrescriptions(
                                selectedRecord.getRecordId()
                        );

        prescriptions.setAll(
                recordPrescriptions
        );
    }

    @FXML
    private void handleRefresh() {

        loadPrescriptions();
    }

    @FXML
    private void handleDispense() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    "Access Denied",
                    "No user is currently logged in."
            );

            return;
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            showAlert(
                    "Access Denied",
                    "Only Admin or Receptionist can dispense medicines."
            );

            return;
        }

        Prescription selectedPrescription =
                prescriptionTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPrescription == null) {

            showAlert(
                    "No Prescription Selected",
                    "Please select a prescription first."
            );

            return;
        }

        try {

            prescriptionBillingService
                    .dispensePrescription(
                            selectedPrescription
                                    .getPrescriptionId()
                    );

            showAlert(
                    "Prescription Dispensed",
                    "Prescription #"
                            + selectedPrescription
                            .getPrescriptionId()
                            + " has been dispensed successfully."
            );

            loadPrescriptions();

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            showAlert(
                    "Unable to Dispense",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleGenerateBill() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    "Access Denied",
                    "No user is currently logged in."
            );

            return;
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            showAlert(
                    "Access Denied",
                    "Only Admin or Receptionist can generate bills."
            );

            return;
        }

        Prescription selectedPrescription =
                prescriptionTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPrescription == null) {

            showAlert(
                    "No Prescription Selected",
                    "Please select a prescription first."
            );

            return;
        }

        try {

            Bill bill =
                    prescriptionBillingService
                            .createBillFromPrescription(
                                    selectedPrescription
                                            .getPrescriptionId(),

                                    300.00,

                                    0.00
                            );

            showAlert(
                    "Bill Created",
                    "Bill #" + bill.getBillId()
                            + " created successfully.\n\n"
                            + "Medicine Charge: ₹"
                            + String.format(
                            "%.2f",
                            bill.getMedicineCharge()
                    )
                            + "\nConsultation: ₹"
                            + String.format(
                            "%.2f",
                            bill.getConsultationCharge()
                    )
                            + "\nTotal: ₹"
                            + String.format(
                            "%.2f",
                            bill.getTotalAmount()
                    )
            );

            loadPrescriptions();

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            showAlert(
                    "Unable to Generate Bill",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleClose() {

        Stage stage =
                (Stage) prescriptionTable
                        .getScene()
                        .getWindow();

        stage.close();
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