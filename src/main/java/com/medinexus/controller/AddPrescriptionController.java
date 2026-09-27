package com.medinexus.controller;

import com.medinexus.model.Doctor;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Medicine;
import com.medinexus.model.Patient;
import com.medinexus.model.Prescription;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.DoctorService;
import com.medinexus.service.MedicineService;
import com.medinexus.service.PatientService;
import com.medinexus.service.PrescriptionService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AddPrescriptionController {

    @FXML
    private Label patientLabel;

    @FXML
    private Label doctorLabel;

    @FXML
    private Label recordLabel;

    @FXML
    private ComboBox<Medicine> medicineComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField dosageField;

    @FXML
    private TextField frequencyField;

    @FXML
    private TextField durationField;

    @FXML
    private TextArea instructionsField;

    @FXML
    private Label stockLabel;

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final DoctorService doctorService =
            new DoctorService();

    private final PatientService patientService =
            new PatientService();

    private final MedicineService medicineService =
            new MedicineService();

    private MedicalRecord selectedRecord;

    private Doctor currentDoctor;

    @FXML
    public void initialize() {

        loadMedicines();

        medicineComboBox.setOnAction(event ->
                handleMedicineSelection()
        );
    }

    private void loadMedicines() {

        medicineComboBox.setItems(
                FXCollections.observableArrayList(
                        medicineService.getAllMedicines()
                )
        );

        medicineComboBox.setCellFactory(listView ->
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Medicine medicine,
                            boolean empty) {

                        super.updateItem(medicine, empty);

                        if (empty || medicine == null) {

                            setText(null);

                        } else {

                            setText(
                                    medicine.getName()
                                            + " | Stock: "
                                            + medicine.getQuantity()
                                            + " | ₹"
                                            + String.format(
                                            "%.2f",
                                            medicine.getUnitPrice()
                                    )
                            );
                        }
                    }
                }
        );

        medicineComboBox.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            Medicine medicine,
                            boolean empty) {

                        super.updateItem(medicine, empty);

                        if (empty || medicine == null) {

                            setText(null);

                        } else {

                            setText(
                                    medicine.getName()
                                            + " | ₹"
                                            + String.format(
                                            "%.2f",
                                            medicine.getUnitPrice()
                                    )
                            );
                        }
                    }
                }
        );
    }

    public void setMedicalRecord(
            MedicalRecord medicalRecord) {

        this.selectedRecord = medicalRecord;
        this.currentDoctor = null;

        if (medicalRecord == null) {

            patientLabel.setText("Patient: -");
            doctorLabel.setText("Doctor: -");
            recordLabel.setText("Medical Record: -");

            return;
        }

        recordLabel.setText(
                "Medical Record: #"
                        + medicalRecord.getRecordId()
        );

        Patient patient =
                patientService.getPatientById(
                        medicalRecord.getPatientId()
                );

        if (patient != null) {

            patientLabel.setText(
                    "Patient: "
                            + patient.getName()
                            + " | ID: "
                            + patient.getPatientId()
            );

        } else {

            patientLabel.setText(
                    "Patient ID: "
                            + medicalRecord.getPatientId()
            );
        }

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || currentUser.getRole() != Role.DOCTOR) {

            doctorLabel.setText(
                    "Doctor: Access denied"
            );

            return;
        }

        currentDoctor =
                doctorService.getDoctorByUserId(
                        currentUser.getUserId()
                );

        if (currentDoctor == null) {

            doctorLabel.setText(
                    "Doctor: Profile not found"
            );

            return;
        }

        doctorLabel.setText(
                "Doctor: "
                        + currentDoctor.getName()
                        + " | "
                        + currentDoctor.getSpecialization()
        );
    }

    private void handleMedicineSelection() {

        Medicine medicine =
                medicineComboBox.getValue();

        if (medicine == null) {

            stockLabel.setText(
                    "Select a medicine to view stock."
            );

            return;
        }

        if (medicine.getQuantity() <= 0) {

            stockLabel.setText(
                    "OUT OF STOCK: "
                            + medicine.getName()
            );

            return;
        }

        if (medicineService.isLowStock(medicine)) {

            stockLabel.setText(
                    "LOW STOCK: "
                            + medicine.getName()
                            + " | Available: "
                            + medicine.getQuantity()
            );

        } else {

            stockLabel.setText(
                    "Available: "
                            + medicine.getName()
                            + " | Stock: "
                            + medicine.getQuantity()
                            + " | Unit Price: ₹"
                            + String.format(
                            "%.2f",
                            medicine.getUnitPrice()
                    )
            );
        }
    }

    @FXML
    private void handleSave() {

        if (selectedRecord == null) {

            showAlert(
                    "Invalid Record",
                    "No medical record is selected."
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

        if (currentDoctor == null) {

            currentDoctor =
                    doctorService.getDoctorByUserId(
                            currentUser.getUserId()
                    );
        }

        if (currentDoctor == null) {

            showAlert(
                    "Doctor Not Found",
                    "The current doctor profile could not be found."
            );

            return;
        }

        /*
         * SECURITY CHECK
         *
         * A doctor can only create prescriptions
         * for medical records created by that doctor.
         */
        if (selectedRecord.getDoctorId()
                != currentDoctor.getDoctorId()) {

            showAlert(
                    "Access Denied",
                    "You can only prescribe medicines for your own medical records."
            );

            return;
        }

        Medicine selectedMedicine =
                medicineComboBox.getValue();

        if (selectedMedicine == null) {

            showAlert(
                    "Medicine Required",
                    "Please select a medicine."
            );

            return;
        }

        /*
         * Prevent duplicate prescriptions for the
         * same medical record and medicine.
         */
        if (hasExistingPrescription(
                selectedRecord.getRecordId(),
                selectedMedicine.getMedicineId())) {

            showAlert(
                    "Duplicate Prescription",
                    "A prescription for this medicine already exists for this medical record."
            );

            return;
        }

        if (selectedMedicine.getQuantity() <= 0) {

            showAlert(
                    "Out of Stock",
                    "The selected medicine is currently out of stock."
            );

            return;
        }

        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            quantityField
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            showAlert(
                    "Invalid Quantity",
                    "Please enter a valid whole number."
            );

            return;
        }

        if (quantity <= 0) {

            showAlert(
                    "Invalid Quantity",
                    "Quantity must be greater than zero."
            );

            return;
        }

        if (quantity > selectedMedicine.getQuantity()) {

            showAlert(
                    "Insufficient Stock",
                    "Available stock: "
                            + selectedMedicine.getQuantity()
            );

            return;
        }

        String dosage =
                dosageField
                        .getText()
                        .trim();

        String frequency =
                frequencyField
                        .getText()
                        .trim();

        String duration =
                durationField
                        .getText()
                        .trim();

        String instructions =
                instructionsField
                        .getText()
                        .trim();

        if (dosage.isEmpty()
                || frequency.isEmpty()
                || duration.isEmpty()) {

            showAlert(
                    "Missing Information",
                    "Please enter dosage, frequency and duration."
            );

            return;
        }

        /*
         * prescriptionId = 0 intentionally.
         *
         * MySQL generates the real ID using AUTO_INCREMENT.
         */
        Prescription prescription =
                new Prescription(
                        0,
                        selectedRecord.getRecordId(),
                        selectedRecord.getPatientId(),
                        currentDoctor.getDoctorId(),
                        selectedMedicine.getMedicineId(),
                        quantity,
                        dosage,
                        frequency,
                        duration,
                        instructions,
                        LocalDateTime.now()
                );

        try {

            Prescription savedPrescription =
                    prescriptionService.createPrescription(
                            prescription
                    );

            showAlert(
                    "Success",
                    "E-Prescription created successfully."
                            + "\n\n"
                            + "Prescription #"
                            + savedPrescription.getPrescriptionId()
                            + "\nMedicine: "
                            + selectedMedicine.getName()
                            + "\nStatus: "
                            + savedPrescription.getStatus()
            );

            closeWindow();

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            showAlert(
                    "Unable to Create Prescription",
                    e.getMessage()
            );
        }
    }

    private boolean hasExistingPrescription(
            int medicalRecordId,
            int medicineId) {

        if (medicineId <= 0) {
            return false;
        }

        return prescriptionService
                .getMedicalRecordPrescriptions(
                        medicalRecordId
                )
                .stream()
                .anyMatch(
                        prescription ->
                                prescription.getMedicineId()
                                        == medicineId
                );
    }

    @FXML
    private void handleClear() {

        medicineComboBox.setValue(null);

        quantityField.clear();

        dosageField.clear();

        frequencyField.clear();

        durationField.clear();

        instructionsField.clear();

        stockLabel.setText(
                "Select a medicine to view stock."
        );
    }

    @FXML
    private void handleCancel() {

        closeWindow();
    }

    private void closeWindow() {

        if (patientLabel == null
                || patientLabel.getScene() == null) {

            return;
        }

        Stage stage =
                (Stage) patientLabel
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