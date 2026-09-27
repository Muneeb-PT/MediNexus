package com.medinexus.controller;

import com.medinexus.model.Payment;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.service.PatientService;
import com.medinexus.service.PaymentService;
import com.medinexus.service.SessionManager;
import com.medinexus.model.PaymentStatus;
import com.medinexus.service.ReceiptService;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class PatientPaymentHistoryController {

    @FXML
    private TableView<Payment> paymentTable;

    @FXML
    private TableColumn<Payment, String> paymentIdColumn;

    @FXML
    private TableColumn<Payment, String> billIdColumn;

    @FXML
    private TableColumn<Payment, String> amountColumn;

    @FXML
    private TableColumn<Payment, String> methodColumn;

    @FXML
    private TableColumn<Payment, String> statusColumn;

    @FXML
    private TableColumn<Payment, String> transactionIdColumn;

    @FXML
    private TableColumn<Payment, String> dateColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    private final PaymentService paymentService =
            new PaymentService();

    private final ReceiptService receiptService =
            new ReceiptService();

    private final PatientService patientService =
            new PatientService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        configureColumns();

        configureStatusStyling();

        loadPayments();
    }


    // =========================================================
    // CONFIGURE TABLE COLUMNS
    // =========================================================

    private void configureColumns() {

        paymentIdColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        String.valueOf(
                                cell.getValue().getPaymentId()
                        )
                )
        );


        billIdColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        String.valueOf(
                                cell.getValue().getBillId()
                        )
                )
        );


        amountColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        String.format(
                                "₹%.2f",
                                cell.getValue().getAmount()
                        )
                )
        );


        methodColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getPaymentMethod() == null
                                ? "-"
                                : cell.getValue()
                                .getPaymentMethod()
                                .name()
                )
        );


        statusColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getStatus() == null
                                ? "-"
                                : cell.getValue()
                                .getStatus()
                                .name()
                )
        );


        transactionIdColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safe(
                                cell.getValue()
                                        .getTransactionId()
                        )
                )
        );


        dateColumn.setCellValueFactory(cell -> {

            Payment payment =
                    cell.getValue();

            if (payment.getCreatedAt() == null) {

                return new SimpleStringProperty("-");
            }

            return new SimpleStringProperty(
                    payment.getCreatedAt()
                            .format(dateFormatter)
            );
        });
    }


    // =========================================================
    // STATUS STYLING
    // =========================================================

    private void configureStatusStyling() {

        statusColumn.setCellFactory(
                column -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String status,
                            boolean empty) {

                        super.updateItem(
                                status,
                                empty
                        );

                        if (empty || status == null) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        setText(status);

                        switch (status.toUpperCase()) {

                            case "SUCCESS":

                                setStyle(
                                        "-fx-font-weight: bold;"
                                                + "-fx-text-fill: green;"
                                );

                                break;


                            case "FAILED":

                                setStyle(
                                        "-fx-font-weight: bold;"
                                                + "-fx-text-fill: red;"
                                );

                                break;


                            case "PENDING":

                                setStyle(
                                        "-fx-font-weight: bold;"
                                                + "-fx-text-fill: orange;"
                                );

                                break;


                            default:

                                setStyle(
                                        "-fx-font-weight: bold;"
                                );
                        }
                    }
                }
        );


        /*
         * Add a subtle row-level indication as well.
         */
        paymentTable.setRowFactory(
                tableView -> new TableRow<>() {

                    @Override
                    protected void updateItem(
                            Payment payment,
                            boolean empty) {

                        super.updateItem(
                                payment,
                                empty
                        );

                        if (empty || payment == null) {

                            setStyle("");

                            return;
                        }

                        if (payment.getStatus() == null) {

                            setStyle("");

                            return;
                        }

                        switch (
                                payment.getStatus()
                                        .name()
                                        .toUpperCase()
                        ) {

                            case "SUCCESS":

                                setStyle(
                                        "-fx-border-color: transparent;"
                                );

                                break;


                            case "FAILED":

                                setStyle(
                                        "-fx-border-color: red;"
                                                + "-fx-border-width: 0 0 1 0;"
                                );

                                break;


                            case "PENDING":

                                setStyle(
                                        "-fx-border-color: orange;"
                                                + "-fx-border-width: 0 0 1 0;"
                                );

                                break;


                            default:

                                setStyle("");
                        }
                    }
                }
        );
    }


    // =========================================================
    // LOAD PAYMENTS
    // =========================================================

    private void loadPayments() {

        try {

            // -------------------------------------------------
            // LOGIN CHECK
            // -------------------------------------------------

            if (!SessionManager.isLoggedIn()) {

                paymentTable.getItems().clear();

                showMessage(
                        "Please login first."
                );

                return;
            }


            // -------------------------------------------------
            // ROLE CHECK
            // -------------------------------------------------

            if (SessionManager
                    .getCurrentUser()
                    .getRole()
                    != Role.PATIENT) {

                paymentTable.getItems().clear();

                showMessage(
                        "Only patients can view payment history."
                );

                return;
            }


            // -------------------------------------------------
            // FIND CURRENT PATIENT
            // -------------------------------------------------

            Patient patient =
                    patientService.getPatientByUserId(
                            SessionManager
                                    .getCurrentUser()
                                    .getUserId()
                    );

            if (patient == null) {

                paymentTable.getItems().clear();

                showMessage(
                        "Patient profile not found."
                );

                return;
            }


            // -------------------------------------------------
            // LOAD ONLY CURRENT PATIENT PAYMENTS
            // -------------------------------------------------

            List<Payment> payments =
                    paymentService.getPatientPayments(
                            patient.getPatientId()
                    );


            paymentTable.getItems().setAll(
                    payments
            );


            // -------------------------------------------------
            // RESULT MESSAGE
            // -------------------------------------------------

            if (payments.isEmpty()) {

                showMessage(
                        "No payment history found."
                );

            } else {

                showMessage(
                        payments.size()
                                + " payment record(s) found."
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            paymentTable.getItems().clear();

            showMessage(
                    "Unable to load payment history: "
                            + safe(e.getMessage())
            );
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadPayments();
    }


    // =========================================================
    // CLOSE
    // =========================================================

    @FXML
    private void handleClose() {

        if (paymentTable.getScene() == null) {

            return;
        }

        Stage stage =
                (Stage) paymentTable
                        .getScene()
                        .getWindow();

        stage.close();
    }


    // =========================================================
    // MESSAGE
    // =========================================================

    private void showMessage(
            String message) {

        if (messageLabel != null) {

            messageLabel.setText(
                    message
            );
        }
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value) {

        return value == null
                || value.trim().isEmpty()
                ? "-"
                : value;
    }

    @FXML
    private void handleGenerateReceipt() {

        try {

            if (!SessionManager.isLoggedIn()
                    || SessionManager.getCurrentUser().getRole()
                    != com.medinexus.model.Role.PATIENT) {

                showAlert(
                        "Access Denied",
                        "Only patients can generate payment receipts.",
                        Alert.AlertType.WARNING
                );

                return;
            }

            Payment selectedPayment =
                    paymentTable.getSelectionModel()
                            .getSelectedItem();

            if (selectedPayment == null) {

                showAlert(
                        "No Payment Selected",
                        "Please select a successful payment first.",
                        Alert.AlertType.WARNING
                );

                return;
            }

            if (selectedPayment.getStatus()
                    != PaymentStatus.SUCCESS) {

                showAlert(
                        "Receipt Unavailable",
                        "A receipt can only be generated for a successful payment.",
                        Alert.AlertType.WARNING
                );

                return;
            }

            FileChooser fileChooser =
                    new FileChooser();

            fileChooser.setTitle(
                    "Save MediNexus Payment Receipt"
            );

            fileChooser.setInitialFileName(
                    "MediNexus_Payment_Receipt_"
                            + selectedPayment.getPaymentId()
                            + ".pdf"
            );

            FileChooser.ExtensionFilter pdfFilter =
                    new FileChooser.ExtensionFilter(
                            "PDF Files (*.pdf)",
                            "*.pdf"
                    );

            fileChooser.getExtensionFilters()
                    .add(pdfFilter);

            Window window =
                    paymentTable.getScene() == null
                            ? null
                            : paymentTable.getScene().getWindow();

            File selectedFile =
                    fileChooser.showSaveDialog(window);

            if (selectedFile == null) {
                return;
            }

            String filePath =
                    selectedFile.getAbsolutePath();

            if (!filePath.toLowerCase()
                    .endsWith(".pdf")) {

                selectedFile =
                        new File(filePath + ".pdf");
            }

            receiptService.generateReceipt(
                    selectedPayment,
                    selectedFile.toPath()
            );

            showAlert(
                    "Receipt Generated",
                    "Payment receipt was successfully generated.\n\n"
                            + selectedFile.getAbsolutePath(),
                    Alert.AlertType.INFORMATION
            );

        } catch (SecurityException e) {

            showAlert(
                    "Access Denied",
                    e.getMessage(),
                    Alert.AlertType.ERROR
            );

        } catch (IllegalStateException e) {

            showAlert(
                    "Receipt Unavailable",
                    e.getMessage(),
                    Alert.AlertType.WARNING
            );

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Receipt Error",
                    "Unable to generate the payment receipt.\n\n"
                            + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}