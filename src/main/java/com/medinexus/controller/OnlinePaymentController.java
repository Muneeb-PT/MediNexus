package com.medinexus.controller;

import com.medinexus.model.Bill;
import com.medinexus.model.Payment;
import com.medinexus.model.PaymentMethod;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.BillService;
import com.medinexus.service.PatientService;
import com.medinexus.service.PaymentService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class OnlinePaymentController {

    @FXML
    private TableView<Bill> billTable;

    @FXML
    private TableColumn<Bill, Integer> billIdColumn;

    @FXML
    private TableColumn<Bill, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Bill, Double> amountColumn;

    @FXML
    private TableColumn<Bill, String> statusColumn;

    @FXML
    private TableColumn<Bill, LocalDateTime> dateColumn;

    @FXML
    private Label selectedBillLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;

    @FXML
    private Label statusLabel;

    private final BillService billService =
            new BillService();

    private final PaymentService paymentService =
            new PaymentService();

    private final PatientService patientService =
            new PatientService();

    /*
     * Used when BillingController opens this screen
     * with a specific bill.
     */
    private Bill preselectedBill;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        configureTable();

        paymentMethodComboBox.setItems(
                FXCollections.observableArrayList(
                        PaymentMethod.UPI,
                        PaymentMethod.CARD,
                        PaymentMethod.NET_BANKING
                )
        );

        paymentMethodComboBox.getSelectionModel()
                .select(PaymentMethod.UPI);

        billTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldBill, newBill) ->
                                showSelectedBill(newBill)
                );

        loadBills();
    }


    // =========================================================
    // SET BILL
    // =========================================================

    /**
     * Allows BillingController to open the payment
     * screen for a specific bill.
     */
    public void setBill(Bill bill) {

        if (bill == null) {
            return;
        }

        this.preselectedBill = bill;

        /*
         * initialize() normally runs before this method
         * when FXMLLoader creates the controller.
         *
         * Therefore the table should already be populated.
         */
        if (billTable != null) {

            for (Bill existingBill :
                    billTable.getItems()) {

                if (existingBill.getBillId()
                        == bill.getBillId()) {

                    billTable.getSelectionModel()
                            .select(existingBill);

                    billTable.scrollTo(existingBill);

                    showSelectedBill(existingBill);

                    return;
                }
            }
        }

        /*
         * If the bill wasn't found in the current list,
         * show it directly only if it belongs to the
         * currently logged-in patient.
         */
        showSelectedBill(bill);
    }


    // =========================================================
    // TABLE CONFIGURATION
    // =========================================================

    private void configureTable() {

        billIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("billId")
        );

        appointmentIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentId")
        );

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalAmount")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentStatus")
        );

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdAt")
        );
    }


    // =========================================================
    // LOAD BILLS
    // =========================================================

    private void loadBills() {

        try {

            User currentUser =
                    SessionManager.getCurrentUser();

            if (currentUser == null) {

                showStatus(
                        "No user is currently logged in."
                );

                return;
            }

            if (currentUser.getRole()
                    != Role.PATIENT) {

                showStatus(
                        "Online payment is currently "
                                + "available for patients."
                );

                billTable.setItems(
                        FXCollections.observableArrayList()
                );

                return;
            }

            var patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient == null) {

                showStatus(
                        "Patient profile was not found."
                );

                return;
            }

            List<Bill> bills =
                    billService.getPatientBills(
                            patient.getPatientId()
                    );

            /*
             * Only pending bills are displayed.
             *
             * PAID bills cannot be paid again.
             */
            List<Bill> pendingBills =
                    bills.stream()
                            .filter(bill ->
                                    bill != null
                                            && bill.getPaymentStatus()
                                            != null
                                            && bill.getPaymentStatus()
                                            .equalsIgnoreCase(
                                                    "PENDING"
                                            )
                            )
                            .toList();

            ObservableList<Bill> billList =
                    FXCollections.observableArrayList(
                            pendingBills
                    );

            billTable.setItems(billList);

            /*
             * If BillingController passed a bill,
             * automatically select it.
             */
            if (preselectedBill != null) {

                for (Bill bill : billList) {

                    if (bill.getBillId()
                            == preselectedBill.getBillId()) {

                        billTable.getSelectionModel()
                                .select(bill);

                        billTable.scrollTo(bill);

                        showSelectedBill(bill);

                        return;
                    }
                }
            }

            if (billList.isEmpty()) {

                showStatus(
                        "There are no pending bills."
                );

            } else {

                showStatus(
                        "Select a bill to continue "
                                + "with payment."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showStatus(
                    "Unable to load bills: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // SHOW SELECTED BILL
    // =========================================================

    private void showSelectedBill(Bill bill) {

        if (bill == null) {

            selectedBillLabel.setText(
                    "No bill selected."
            );

            amountLabel.setText(
                    "₹0.00"
            );

            return;
        }

        selectedBillLabel.setText(
                "Bill #"
                        + bill.getBillId()
                        + " | Appointment #"
                        + bill.getAppointmentId()
        );

        amountLabel.setText(
                String.format(
                        "₹%.2f",
                        bill.getTotalAmount()
                )
        );

        showStatus(
                "Ready for payment."
        );
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        preselectedBill = null;

        loadBills();

        selectedBillLabel.setText(
                "No bill selected."
        );

        amountLabel.setText(
                "₹0.00"
        );
    }


    // =========================================================
    // PAY ONLINE
    // =========================================================

    @FXML
    private void handlePayOnline() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Session Error",
                    "No user is currently logged in."
            );

            return;
        }

        if (currentUser.getRole()
                != Role.PATIENT) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only patients can make online payments."
            );

            return;
        }

        Bill selectedBill =
                billTable.getSelectionModel()
                        .getSelectedItem();

        /*
         * If a bill was passed from BillingController
         * but somehow wasn't selected in the table,
         * use the preselected bill.
         */
        if (selectedBill == null) {
            selectedBill = preselectedBill;
        }

        if (selectedBill == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Bill Selected",
                    "Please select a pending bill."
            );

            return;
        }

        if (selectedBill.getPaymentStatus() == null
                || !"PENDING".equalsIgnoreCase(
                selectedBill.getPaymentStatus()
        )) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid Bill",
                    "Only pending bills can be paid."
            );

            return;
        }

        PaymentMethod method =
                paymentMethodComboBox.getValue();

        if (method == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Payment Method",
                    "Please select a payment method."
            );

            return;
        }

        try {

            statusLabel.setText(
                    "Processing payment..."
            );

            Payment payment =
                    paymentService.processOnlinePayment(
                            selectedBill.getBillId(),
                            method
                    );

            if (payment == null) {

                throw new IllegalStateException(
                        "Payment processing returned no result."
                );
            }

            /*
             * Demo gateway may return FAILED.
             * Do not display FAILED as successful.
             */
            if (payment.getStatus() == null
                    || !"SUCCESS".equalsIgnoreCase(
                    payment.getStatus().name()
            )) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Payment Failed",
                        "The payment was not successful.\n\n"
                                + "Transaction ID: "
                                + payment.getTransactionId()
                );

                loadBills();

                return;
            }

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Payment Successful",
                    "Payment completed successfully.\n\n"
                            + "Bill ID: "
                            + selectedBill.getBillId()
                            + "\n"
                            + "Amount: ₹"
                            + String.format(
                            "%.2f",
                            payment.getAmount()
                    )
                            + "\n"
                            + "Method: "
                            + payment.getPaymentMethod()
                            + "\n"
                            + "Transaction ID: "
                            + payment.getTransactionId()
            );

            /*
             * Reload from database so that the UI reflects
             * the actual PAID/PENDING state.
             */
            preselectedBill = null;

            loadBills();

            selectedBillLabel.setText(
                    "Payment completed."
            );

            amountLabel.setText(
                    "₹0.00"
            );

            showStatus(
                    "Payment completed successfully."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Payment Failed",
                    e.getMessage()
                            == null
                            ? "Unable to process payment."
                            : e.getMessage()
            );

            showStatus(
                    "Payment failed."
            );
        }
    }


    // =========================================================
    // STATUS
    // =========================================================

    private void showStatus(String message) {

        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(
                message == null
                        ? ""
                        : message
        );
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

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