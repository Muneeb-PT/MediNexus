package com.medinexus.controller;

import com.medinexus.model.Bill;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.BillService;
import com.medinexus.service.PatientService;
import com.medinexus.service.PaymentService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class BillingController {

    @FXML
    private TableView<Bill> billingTable;

    @FXML
    private TableColumn<Bill, Integer> billIdColumn;

    @FXML
    private TableColumn<Bill, Integer> patientIdColumn;

    @FXML
    private TableColumn<Bill, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Bill, Double> consultationColumn;

    @FXML
    private TableColumn<Bill, Double> medicineColumn;

    @FXML
    private TableColumn<Bill, Double> otherChargesColumn;

    @FXML
    private TableColumn<Bill, Double> totalColumn;

    @FXML
    private TableColumn<Bill, String> paymentStatusColumn;

    private final BillService billService =
            new BillService();

    private final PatientService patientService =
            new PatientService();

    private final PaymentService paymentService =
            new PaymentService();


    @FXML
    public void initialize() {

        billIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("billId")
        );

        patientIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientId")
        );

        appointmentIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentId")
        );

        consultationColumn.setCellValueFactory(
                new PropertyValueFactory<>("consultationCharge")
        );

        medicineColumn.setCellValueFactory(
                new PropertyValueFactory<>("medicineCharge")
        );

        otherChargesColumn.setCellValueFactory(
                new PropertyValueFactory<>("otherCharges")
        );

        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalAmount")
        );

        paymentStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentStatus")
        );

        loadBills();
    }


    // =========================================================
    // LOAD BILLS
    // =========================================================

    private void loadBills() {

        try {

            User currentUser =
                    SessionManager.getCurrentUser();

            if (currentUser == null) {
                showError("No user is currently logged in.");
                return;
            }

            List<Bill> bills;

            // -------------------------------------------------
            // PATIENT
            // -------------------------------------------------

            if (currentUser.getRole() == Role.PATIENT) {

                var patient =
                        patientService.getPatientByUserId(
                                currentUser.getUserId()
                        );

                if (patient == null) {
                    showError("Patient profile not found.");
                    return;
                }

                bills =
                        billService.getPatientBills(
                                patient.getPatientId()
                        );

            }

            // -------------------------------------------------
            // ADMIN / RECEPTIONIST
            // -------------------------------------------------

            else if (
                    currentUser.getRole() == Role.ADMIN
                            || currentUser.getRole()
                            == Role.RECEPTIONIST) {

                bills = billService.getAllBills();

            }

            else {

                showError(
                        "You are not authorized to view billing."
                );

                return;
            }

            billingTable.setItems(
                    FXCollections.observableArrayList(
                            bills
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to load bills:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {
        loadBills();
    }


    // =========================================================
    // ONLINE PAYMENT
    // =========================================================

    @FXML
    private void handlePayOnline() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || currentUser.getRole() != Role.PATIENT) {

            showError(
                    "Only patients can make online payments."
            );

            return;
        }

        Bill selectedBill =
                billingTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedBill == null) {

            showError(
                    "Please select a bill first."
            );

            return;
        }

        if ("PAID".equalsIgnoreCase(
                selectedBill.getPaymentStatus())) {

            showError(
                    "This bill has already been paid."
            );

            return;
        }

        try {

            /*
             * Open the dedicated online payment screen.
             *
             * The bill ID is passed to the controller after
             * the FXML is loaded.
             */

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass().getResource(
                                    "/fxml/OnlinePaymentView.fxml"
                            )
                    );

            javafx.scene.Parent root =
                    loader.load();

            OnlinePaymentController controller =
                    loader.getController();

            controller.setBill(selectedBill);

            billingTable.getScene()
                    .getWindow()
                    .hide();

            javafx.stage.Stage stage =
                    new javafx.stage.Stage();

            stage.setTitle(
                    "MediNexus - Online Payment"
            );

            stage.setScene(
                    new javafx.scene.Scene(root)
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to open online payment:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CASH PAYMENT
    // =========================================================

    @FXML
    private void handleMarkPaid() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            showError(
                    "Only Admin or Receptionist can record cash payments."
            );

            return;
        }

        Bill selectedBill =
                billingTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedBill == null) {

            showError(
                    "Please select a bill first."
            );

            return;
        }

        try {

            paymentService.recordCashPayment(
                    selectedBill.getBillId()
            );

            showInfo(
                    "Payment recorded successfully."
            );

            loadBills();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to record payment:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CLOSE
    // =========================================================

    @FXML
    private void handleClose() {

        if (billingTable == null
                || billingTable.getScene() == null) {

            return;
        }

        billingTable
                .getScene()
                .getWindow()
                .hide();
    }


    // =========================================================
    // ALERTS
    // =========================================================

    private void showInfo(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("MediNexus");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("MediNexus");
        alert.setHeaderText("Billing Error");
        alert.setContentText(message);

        alert.showAndWait();
    }
}