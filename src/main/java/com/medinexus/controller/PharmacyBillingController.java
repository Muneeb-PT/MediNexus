package com.medinexus.controller;

import com.medinexus.model.Bill;
import com.medinexus.model.Medicine;
import com.medinexus.model.Patient;
import com.medinexus.model.Prescription;
import com.medinexus.model.PrescriptionStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.BillService;
import com.medinexus.service.MedicineService;
import com.medinexus.service.PatientService;
import com.medinexus.service.PaymentService;
import com.medinexus.service.PrescriptionBillingService;
import com.medinexus.service.PrescriptionService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class PharmacyBillingController {

    @FXML
    private TableView<Prescription> prescriptionTable;

    @FXML
    private TableColumn<Prescription, String> prescriptionIdColumn;

    @FXML
    private TableColumn<Prescription, String> patientColumn;

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
    private TableColumn<Prescription, String> statusColumn;

    @FXML
    private TableColumn<Prescription, String> stockColumn;

    @FXML
    private Label selectedMedicineLabel;

    @FXML
    private Label medicineChargeLabel;

    @FXML
    private TextField consultationChargeField;

    @FXML
    private TextField otherChargesField;

    @FXML
    private Label totalLabel;

    @FXML
    private Label statusLabel;

    // =========================================================
    // SERVICES
    // =========================================================

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final MedicineService medicineService =
            new MedicineService();

    private final BillService billService =
            new BillService();

    private final PrescriptionBillingService prescriptionBillingService =
            new PrescriptionBillingService();

    private final PaymentService paymentService =
            new PaymentService();

    private final PatientService patientService =
            new PatientService();

    // =========================================================
    // TABLE DATA
    // =========================================================

    private final ObservableList<Prescription> prescriptions =
            FXCollections.observableArrayList();

    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        setupColumns();

        prescriptionTable.setItems(prescriptions);

        setupSelectionListener();

        loadPrescriptions();

        calculateTotal();
    }

    // =========================================================
    // TABLE COLUMNS
    // =========================================================

    private void setupColumns() {

        prescriptionIdColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue()
                                        .getPrescriptionId()
                        )
                )
        );

        patientColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        getPatientName(
                                cellData.getValue()
                                        .getPatientId()
                        )
                )
        );

        medicineColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        getMedicineName(
                                cellData.getValue()
                                        .getMedicineId()
                        )
                )
        );

        quantityColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue()
                                        .getQuantity()
                        )
                )
        );

        dosageColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue()
                                .getDosage()
                )
        );

        frequencyColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue()
                                .getFrequency()
                )
        );

        durationColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue()
                                .getDuration()
                )
        );

        statusColumn.setCellValueFactory(
                cellData -> {

                    if (cellData.getValue().getStatus() == null) {
                        return new SimpleStringProperty("");
                    }

                    return new SimpleStringProperty(
                            cellData.getValue()
                                    .getStatus()
                                    .name()
                    );
                }
        );

        if (stockColumn != null) {
            stockColumn.setCellValueFactory(
                    cellData -> new SimpleStringProperty(
                            getStockText(
                                    cellData.getValue()
                                            .getMedicineId()
                            )
                    )
            );
        }
    }

    // =========================================================
    // SELECTION LISTENER
    // =========================================================

    private void setupSelectionListener() {

        prescriptionTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (newValue == null) {

                                clearSelectedPrescription();

                                return;
                            }

                            showPrescriptionDetails(newValue);
                        }
                );
    }

    // =========================================================
    // LOAD PRESCRIPTIONS
    // =========================================================

    private void loadPrescriptions() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (!hasBillingAccess(currentUser)) {

            prescriptions.clear();

            statusLabel.setText(
                    "Access denied. Admin or Receptionist access required."
            );

            return;
        }

        List<Prescription> allPrescriptions =
                prescriptionService.getAllPrescriptions();

        prescriptions.setAll(allPrescriptions);

        statusLabel.setText(
                prescriptions.size()
                        + " prescription(s) available."
        );
    }

    // =========================================================
    // SELECTED PRESCRIPTION
    // =========================================================

    private void showPrescriptionDetails(
            Prescription prescription) {

        Medicine medicine =
                medicineService.getMedicineById(
                        prescription.getMedicineId()
                );

        if (medicine == null) {

            selectedMedicineLabel.setText(
                    "Unknown Medicine"
            );

            medicineChargeLabel.setText(
                    "₹0.00"
            );

            calculateTotal();

            return;
        }

        selectedMedicineLabel.setText(
                medicine.getName()
                        + " | Stock: "
                        + medicine.getQuantity()
        );

        double medicineCharge =
                medicineService.calculateMedicineCharge(
                        medicine.getMedicineId(),
                        prescription.getQuantity()
                );

        medicineChargeLabel.setText(
                formatCurrency(medicineCharge)
        );

        calculateTotal();
    }

    // =========================================================
    // CLEAR SELECTION
    // =========================================================

    private void clearSelectedPrescription() {

        selectedMedicineLabel.setText("-");

        medicineChargeLabel.setText(
                "₹0.00"
        );

        calculateTotal();
    }

    // =========================================================
    // CALCULATE TOTAL
    // =========================================================

    @FXML
    private void handleCalculate() {

        calculateTotal();
    }

    private void calculateTotal() {

        double consultationCharge =
                parseCharge(
                        consultationChargeField.getText()
                );

        double otherCharges =
                parseCharge(
                        otherChargesField.getText()
                );

        double medicineCharge = 0.0;

        Prescription selectedPrescription =
                prescriptionTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPrescription != null) {

            Medicine medicine =
                    medicineService.getMedicineById(
                            selectedPrescription.getMedicineId()
                    );

            if (medicine != null) {

                medicineCharge =
                        medicine.getUnitPrice()
                                * selectedPrescription.getQuantity();
            }
        }

        double total =
                consultationCharge
                        + medicineCharge
                        + otherCharges;

        totalLabel.setText(
                formatCurrency(total)
        );
    }

    // =========================================================
    // DISPENSE MEDICINE
    // =========================================================

    @FXML
    private void handleDispense() {

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

        if (selectedPrescription.getStatus()
                != PrescriptionStatus.PENDING) {

            showAlert(
                    "Invalid Status",
                    "Only PENDING prescriptions can be dispensed."
            );

            return;
        }

        try {

            prescriptionBillingService.dispensePrescription(
                    selectedPrescription.getPrescriptionId()
            );

            showAlert(
                    "Medicine Dispensed",
                    "Medicine dispensed successfully."
            );

            loadPrescriptions();

        } catch (Exception e) {

            showAlert(
                    "Dispensing Failed",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // GENERATE BILL
    // =========================================================

    @FXML
    private void handleGenerateBill() {

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

        if (selectedPrescription.getStatus()
                != PrescriptionStatus.DISPENSED) {

            showAlert(
                    "Invalid Status",
                    "A bill can only be generated after the medicine has been dispensed."
            );

            return;
        }

        try {

            double consultationCharge =
                    consultationChargeField.getText()
                            .trim()
                            .isEmpty()
                            ? 0
                            : Double.parseDouble(
                            consultationChargeField
                                    .getText()
                                    .trim()
                    );

            double otherCharges =
                    otherChargesField.getText()
                            .trim()
                            .isEmpty()
                            ? 0
                            : Double.parseDouble(
                            otherChargesField
                                    .getText()
                                    .trim()
                    );

            if (consultationCharge < 0
                    || otherCharges < 0) {

                throw new IllegalArgumentException(
                        "Charges cannot be negative."
                );
            }

            prescriptionBillingService.createBillFromPrescription(
                    selectedPrescription.getPrescriptionId(),
                    consultationCharge,
                    otherCharges
            );

            showAlert(
                    "Bill Generated",
                    "Bill generated successfully."
            );

            loadPrescriptions();

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Amount",
                    "Please enter valid numeric charges."
            );

        } catch (Exception e) {

            showAlert(
                    "Bill Generation Failed",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // RECORD CASH PAYMENT FOR PRESCRIPTION
    // =========================================================

    @FXML
    private void handleMarkPaid() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (!hasBillingAccess(currentUser)) {

            showAlert(
                    "Access Denied",
                    "Only Admin or Receptionist can record cash payments."
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

        if (selectedPrescription.getStatus()
                != PrescriptionStatus.BILLED) {

            showAlert(
                    "Invalid Status",
                    "Only BILLED prescriptions can be paid."
            );

            return;
        }

        try {

            paymentService.recordCashPaymentForPrescription(
                    selectedPrescription.getPrescriptionId()
            );

            showAlert(
                    "Payment Successful",
                    "Cash payment recorded successfully."
            );

            loadPrescriptions();

        } catch (Exception e) {

            showAlert(
                    "Payment Failed",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadPrescriptions();

        prescriptionTable
                .getSelectionModel()
                .clearSelection();

        clearSelectedPrescription();

        calculateTotal();
    }

    // =========================================================
    // ACCESS CONTROL
    // =========================================================

    private boolean hasBillingAccess(User user) {

        if (user == null) {
            return false;
        }

        return user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST;
    }

    // =========================================================
    // PATIENT NAME
    // =========================================================

    private String getPatientName(int patientId) {

        try {

            Patient patient =
                    patientService.getPatientById(patientId);

            if (patient == null) {
                return "Unknown Patient";
            }

            return patient.getName();

        } catch (Exception e) {

            return "Unknown Patient";
        }
    }

    // =========================================================
    // MEDICINE NAME
    // =========================================================

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

    // =========================================================
    // STOCK
    // =========================================================

    private String getStockText(int medicineId) {

        Medicine medicine =
                medicineService.getMedicineById(
                        medicineId
                );

        if (medicine == null) {
            return "N/A";
        }

        return String.valueOf(
                medicine.getQuantity()
        );
    }

    // =========================================================
    // PARSE CHARGE
    // =========================================================

    private double parseCharge(String value) {

        if (value == null
                || value.isBlank()) {

            return 0.0;
        }

        try {

            double charge =
                    Double.parseDouble(
                            value.trim()
                    );

            if (charge < 0) {
                return 0.0;
            }

            return charge;

        } catch (NumberFormatException e) {

            return 0.0;
        }
    }

    // =========================================================
    // REQUIRED CHARGE
    // =========================================================

    private double parseRequiredCharge(
            String value,
            String fieldName) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        try {

            double charge =
                    Double.parseDouble(
                            value.trim()
                    );

            if (charge < 0) {

                throw new IllegalArgumentException(
                        fieldName
                                + " cannot be negative."
                );
            }

            return charge;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be a valid number."
            );
        }
    }

    // =========================================================
    // CURRENCY
    // =========================================================

    private String formatCurrency(double amount) {

        return String.format(
                "₹%.2f",
                amount
        );
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(
                message == null
                        ? "Unknown error."
                        : message
        );

        alert.showAndWait();
    }
}
