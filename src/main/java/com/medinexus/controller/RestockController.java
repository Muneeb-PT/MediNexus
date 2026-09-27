package com.medinexus.controller;

import com.medinexus.model.Medicine;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.PharmacyInventoryService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;

import java.util.List;

public class RestockController {

    @FXML
    private ComboBox<Medicine> medicineComboBox;

    @FXML
    private Label currentStockLabel;

    @FXML
    private Label reorderLevelLabel;

    @FXML
    private Label stockStatusLabel;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Button restockButton;

    private final PharmacyInventoryService inventoryService =
            new PharmacyInventoryService();

    private final ObservableList<Medicine> medicines =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        verifyAccess();

        quantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        10000,
                        1
                )
        );

        medicineComboBox.setItems(medicines);

        medicineComboBox.setCellFactory(listView ->
                new javafx.scene.control.ListCell<>() {

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
                                            + " (Stock: "
                                            + medicine.getQuantity()
                                            + ")"
                            );
                        }
                    }
                }
        );

        medicineComboBox.setButtonCell(
                new javafx.scene.control.ListCell<>() {

                    @Override
                    protected void updateItem(
                            Medicine medicine,
                            boolean empty) {

                        super.updateItem(medicine, empty);

                        if (empty || medicine == null) {
                            setText("Select medicine");
                        } else {
                            setText(
                                    medicine.getName()
                                            + " (Stock: "
                                            + medicine.getQuantity()
                                            + ")"
                            );
                        }
                    }
                }
        );

        medicineComboBox.setOnAction(
                event -> updateMedicineInformation()
        );

        loadMedicines();
    }

    private void verifyAccess() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            disableForm();
            messageLabel.setText(
                    "Please log in to manage inventory."
            );
            return;
        }

        Role role = currentUser.getRole();

        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST) {

            disableForm();

            messageLabel.setText(
                    "Only Admin or Receptionist can restock medicines."
            );
        }
    }

    private void disableForm() {

        medicineComboBox.setDisable(true);
        quantitySpinner.setDisable(true);
        notesArea.setDisable(true);
        restockButton.setDisable(true);
    }

    private void loadMedicines() {

        try {

            List<Medicine> medicineList =
                    inventoryService.getMedicines();

            medicines.setAll(medicineList);

            if (medicineList.isEmpty()) {

                messageLabel.setText(
                        "No medicines are available."
                );

                return;
            }

            messageLabel.setText(
                    "Select a medicine and enter the quantity to add."
            );

        } catch (SecurityException e) {

            disableForm();

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Unable to load medicines."
            );

            e.printStackTrace();
        }
    }

    private void updateMedicineInformation() {

        Medicine selectedMedicine =
                medicineComboBox.getValue();

        if (selectedMedicine == null) {

            currentStockLabel.setText("-");
            reorderLevelLabel.setText("-");
            stockStatusLabel.setText("-");

            return;
        }

        currentStockLabel.setText(
                String.valueOf(
                        selectedMedicine.getQuantity()
                )
        );

        reorderLevelLabel.setText(
                String.valueOf(
                        selectedMedicine.getReorderLevel()
                )
        );

        if (selectedMedicine.getQuantity() == 0) {

            stockStatusLabel.setText(
                    "OUT OF STOCK"
            );

        } else if (
                selectedMedicine.getQuantity()
                        <= selectedMedicine.getReorderLevel()) {

            stockStatusLabel.setText(
                    "LOW STOCK"
            );

        } else {

            stockStatusLabel.setText(
                    "AVAILABLE"
            );
        }
    }

    @FXML
    private void handleRestock() {

        Medicine selectedMedicine =
                medicineComboBox.getValue();

        if (selectedMedicine == null) {

            messageLabel.setText(
                    "Please select a medicine."
            );

            return;
        }

        int quantity =
                quantitySpinner.getValue();

        String notes =
                notesArea.getText() == null
                        ? ""
                        : notesArea.getText().trim();

        if (quantity <= 0) {

            messageLabel.setText(
                    "Restock quantity must be greater than zero."
            );

            return;
        }

        if (quantity > 10000) {

            messageLabel.setText(
                    "Restock quantity cannot exceed 10,000."
            );

            return;
        }

        try {

            inventoryService.restockMedicine(
                    selectedMedicine.getMedicineId(),
                    quantity,
                    notes
            );

            messageLabel.setText(
                    "Successfully restocked "
                            + selectedMedicine.getName()
                            + " by "
                            + quantity
                            + " units."
            );

            reloadMedicineData(
                    selectedMedicine.getMedicineId()
            );

            quantitySpinner.getValueFactory()
                    .setValue(1);

            notesArea.clear();

        } catch (SecurityException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Restock failed. Please try again."
            );

            e.printStackTrace();
        }
    }

    private void reloadMedicineData(int selectedMedicineId) {

        try {

            List<Medicine> medicineList =
                    inventoryService.getMedicines();

            medicines.setAll(medicineList);

            for (Medicine medicine : medicineList) {

                if (medicine.getMedicineId()
                        == selectedMedicineId) {

                    medicineComboBox.setValue(medicine);

                    updateMedicineInformation();

                    break;
                }
            }

        } catch (RuntimeException e) {

            e.printStackTrace();

            loadMedicines();
        }
    }
}