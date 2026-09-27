package com.medinexus.controller;

import com.medinexus.model.Medicine;
import com.medinexus.repository.MedicineInventoryTransactionRepository;
import com.medinexus.service.MedicineService;
import com.medinexus.service.PharmacyInventoryService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MedicineController {

    // =========================================================
    // MEDICINE INVENTORY
    // =========================================================

    @FXML
    private Label summaryLabel;

    @FXML
    private TableView<Medicine> medicineTable;

    @FXML
    private TableColumn<Medicine, String> idColumn;

    @FXML
    private TableColumn<Medicine, String> nameColumn;

    @FXML
    private TableColumn<Medicine, String> categoryColumn;

    @FXML
    private TableColumn<Medicine, String> manufacturerColumn;

    @FXML
    private TableColumn<Medicine, String> priceColumn;

    @FXML
    private TableColumn<Medicine, String> quantityColumn;

    @FXML
    private TableColumn<Medicine, String> stockColumn;

    // =========================================================
    // INVENTORY HISTORY
    // =========================================================

    @FXML
    private TableView<
            MedicineInventoryTransactionRepository.InventoryTransaction>
            historyTable;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> transactionIdColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> historyMedicineColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> typeColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> historyQuantityColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> stockBeforeColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> stockAfterColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> referenceColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> performedByColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> notesColumn;

    @FXML
    private TableColumn<
            MedicineInventoryTransactionRepository.InventoryTransaction,
            String> createdAtColumn;

    // =========================================================
    // SERVICES
    // =========================================================

    private final MedicineService medicineService =
            new MedicineService();

    private final PharmacyInventoryService inventoryService =
            new PharmacyInventoryService();

    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<Medicine> medicines =
            FXCollections.observableArrayList();

    private final ObservableList<
            MedicineInventoryTransactionRepository.InventoryTransaction>
            inventoryTransactions =
            FXCollections.observableArrayList();

    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        setupMedicineColumns();

        setupHistoryColumns();

        medicineTable.setItems(
                medicines
        );

        historyTable.setItems(
                inventoryTransactions
        );

        loadMedicines();

        loadInventoryHistory();
    }

    // =========================================================
    // MEDICINE COLUMNS
    // =========================================================

    private void setupMedicineColumns() {

        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue()
                                        .getMedicineId()
                        )
                )
        );

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue()
                                .getName()
                )
        );

        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue()
                                .getCategory()
                )
        );

        manufacturerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue()
                                .getManufacturer()
                )
        );

        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.format(
                                "₹%.2f",
                                cellData.getValue()
                                        .getUnitPrice()
                        )
                )
        );

        quantityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(
                                cellData.getValue()
                                        .getQuantity()
                        )
                )
        );

        stockColumn.setCellValueFactory(cellData -> {

            Medicine medicine =
                    cellData.getValue();

            String status;

            if (medicine.getQuantity() == 0) {

                status = "OUT OF STOCK";

            } else if (
                    medicineService.isLowStock(
                            medicine)) {

                status = "LOW STOCK";

            } else {

                status = "AVAILABLE";
            }

            return new SimpleStringProperty(
                    status
            );
        });
    }

    // =========================================================
    // INVENTORY HISTORY COLUMNS
    // =========================================================

    private void setupHistoryColumns() {

        transactionIdColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getTransactionId()
                                )
                        )
        );

        historyMedicineColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getMedicineId()
                                )
                        )
        );

        typeColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getTransactionType()
                        )
        );

        historyQuantityColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getQuantity()
                                )
                        )
        );

        stockBeforeColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getStockBefore()
                                )
                        )
        );

        stockAfterColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getStockAfter()
                                )
                        )
        );

        referenceColumn.setCellValueFactory(
                cellData -> {

                    MedicineInventoryTransactionRepository
                            .InventoryTransaction transaction =
                            cellData.getValue();

                    String referenceType =
                            transaction.getReferenceType();

                    Long referenceId =
                            transaction.getReferenceId();

                    if (referenceType == null
                            || referenceType.isBlank()) {

                        return new SimpleStringProperty(
                                "-"
                        );
                    }

                    if (referenceId == null) {

                        return new SimpleStringProperty(
                                referenceType
                        );
                    }

                    return new SimpleStringProperty(
                            referenceType
                                    + " #"
                                    + referenceId
                    );
                }
        );

        performedByColumn.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        cellData.getValue()
                                                .getPerformedBy()
                                )
                        )
        );

        notesColumn.setCellValueFactory(
                cellData -> {

                    String notes =
                            cellData.getValue()
                                    .getNotes();

                    return new SimpleStringProperty(
                            notes == null
                                    || notes.isBlank()
                                    ? "-"
                                    : notes
                    );
                }
        );

        createdAtColumn.setCellValueFactory(
                cellData -> {

                    if (cellData.getValue()
                            .getCreatedAt() == null) {

                        return new SimpleStringProperty(
                                "-"
                        );
                    }

                    return new SimpleStringProperty(
                            cellData.getValue()
                                    .getCreatedAt()
                                    .toString()
                    );
                }
        );
    }

    // =========================================================
    // LOAD MEDICINES
    // =========================================================

    private void loadMedicines() {

        try {

            List<Medicine> result =
                    medicineService.getAllMedicines();

            medicines.setAll(
                    result
            );

            updateSummary();

        } catch (Exception e) {

            showAlert(
                    "Inventory Error",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // LOAD INVENTORY HISTORY
    // =========================================================

    private void loadInventoryHistory() {

        try {

            List<
                    MedicineInventoryTransactionRepository
                            .InventoryTransaction>
                    result =
                    inventoryService.getInventoryHistory();

            inventoryTransactions.setAll(
                    result
            );

        } catch (SecurityException e) {

            inventoryTransactions.clear();

        } catch (Exception e) {

            showAlert(
                    "Inventory History Error",
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        int total =
                medicines.size();

        int outOfStock = 0;

        int lowStock = 0;

        for (Medicine medicine :
                medicines) {

            if (medicine.getQuantity() == 0) {

                outOfStock++;

            } else if (
                    medicineService.isLowStock(
                            medicine)) {

                lowStock++;
            }
        }

        summaryLabel.setText(
                "Medicines: "
                        + total
                        + " | Low Stock: "
                        + lowStock
                        + " | Out of Stock: "
                        + outOfStock
                        + " | Transactions: "
                        + inventoryTransactions.size()
        );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadMedicines();

        loadInventoryHistory();

        updateSummary();
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
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
    @FXML
    private void handleRestock() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/RestockView.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setTitle(
                    "MediNexus - Medicine Restock"
            );

            stage.setScene(
                    new Scene(root, 850, 700)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    // =========================================================
// CLEAR FILTERS
// =========================================================

    @FXML
    private void handleClearFilters() {

        loadMedicines();

        loadInventoryHistory();
    }
}