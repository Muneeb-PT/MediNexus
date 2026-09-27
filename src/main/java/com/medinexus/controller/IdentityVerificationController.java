package com.medinexus.controller;

import com.medinexus.model.IdentityVerification;
import com.medinexus.model.Role;
import com.medinexus.model.VerificationStatus;
import com.medinexus.service.IdentityVerificationService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

public class IdentityVerificationController {

    @FXML
    private TableView<IdentityVerification> verificationTable;

    @FXML
    private TableColumn<IdentityVerification, Number> verificationIdColumn;

    @FXML
    private TableColumn<IdentityVerification, Number> patientIdColumn;

    @FXML
    private TableColumn<IdentityVerification, String> providerColumn;

    @FXML
    private TableColumn<IdentityVerification, String> statusColumn;

    @FXML
    private TableColumn<IdentityVerification, String> documentTypeColumn;

    @FXML
    private TableColumn<IdentityVerification, String> documentReferenceColumn;

    @FXML
    private TableColumn<IdentityVerification, String> verifiedAtColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button refreshButton;

    private final IdentityVerificationService
            verificationService =
            new IdentityVerificationService();

    private final ObservableList<IdentityVerification>
            verificationList =
            FXCollections.observableArrayList();

    private static final DateTimeFormatter
            DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy HH:mm"
            );

    @FXML
    private void initialize() {

        configureColumns();
        configureStatusColumn();
        configureRows();

        verificationTable.setItems(
                verificationList
        );

        updateButtonState();

        loadVerifications();
    }

    private void configureColumns() {

        verificationIdColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleIntegerProperty(
                                cell.getValue()
                                        .getVerificationId()
                        )
        );

        patientIdColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleIntegerProperty(
                                cell.getValue()
                                        .getPatientId()
                        )
        );

        providerColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getProvider()
                                )
                        )
        );

        statusColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleStringProperty(
                                cell.getValue()
                                        .getStatus()
                                        .name()
                        )
        );

        documentTypeColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getDocumentType()
                                )
                        )
        );

        documentReferenceColumn.setCellValueFactory(
                cell ->
                        new javafx.beans.property.SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getDocumentReference()
                                )
                        )
        );

        verifiedAtColumn.setCellValueFactory(
                cell -> {

                    IdentityVerification verification =
                            cell.getValue();

                    String value = "";

                    if (verification.getVerifiedAt()
                            != null) {

                        value =
                                verification
                                        .getVerifiedAt()
                                        .format(
                                                DATE_TIME_FORMATTER
                                        );
                    }

                    return new javafx.beans.property
                            .SimpleStringProperty(value);
                }
        );
    }

    private void configureStatusColumn() {

        statusColumn.setCellFactory(
                column ->
                        new TableCell<>() {

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

                                switch (status) {

                                    case "VERIFIED" ->
                                            setStyle(
                                                    "-fx-text-fill: #198754;"
                                                            + "-fx-font-weight: bold;"
                                            );

                                    case "FAILED" ->
                                            setStyle(
                                                    "-fx-text-fill: #dc3545;"
                                                            + "-fx-font-weight: bold;"
                                            );

                                    case "PENDING" ->
                                            setStyle(
                                                    "-fx-text-fill: #d97706;"
                                                            + "-fx-font-weight: bold;"
                                            );

                                    default ->
                                            setStyle("");
                                }
                            }
                        }
        );
    }

    private void configureRows() {

        verificationTable.setRowFactory(
                table -> {

                    TableRow<IdentityVerification> row =
                            new TableRow<>() {

                                @Override
                                protected void updateItem(
                                        IdentityVerification item,
                                        boolean empty) {

                                    super.updateItem(
                                            item,
                                            empty
                                    );

                                    if (empty || item == null) {

                                        setStyle("");

                                        return;
                                    }

                                    if (item.getStatus()
                                            == VerificationStatus.PENDING) {

                                        setStyle(
                                                "-fx-border-color: #f59e0b;"
                                                        + "-fx-border-width: 0 0 1 0;"
                                        );

                                    } else {

                                        setStyle("");
                                    }
                                }
                            };

                    row.setOnMouseClicked(event ->
                            updateButtonState()
                    );

                    return row;
                }
        );
    }

    @FXML
    private void handleApprove() {

        IdentityVerification selected =
                verificationTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    "No Selection",
                    "Please select a verification request.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (selected.getStatus()
                != VerificationStatus.PENDING) {

            showAlert(
                    "Invalid Request",
                    "Only pending verification requests can be approved.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        boolean confirmed =
                showConfirmation(
                        "Approve Verification",
                        "Approve identity verification request #"
                                + selected.getVerificationId()
                                + "?"
                );

        if (!confirmed) {
            return;
        }

        try {

            verificationService.approveVerification(
                    selected.getVerificationId()
            );

            showAlert(
                    "Verification Approved",
                    "Identity verification request #"
                            + selected.getVerificationId()
                            + " has been approved.",
                    Alert.AlertType.INFORMATION
            );

            loadVerifications();

        } catch (Exception e) {

            showAlert(
                    "Approval Failed",
                    safeExceptionMessage(e),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void handleReject() {

        IdentityVerification selected =
                verificationTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    "No Selection",
                    "Please select a verification request.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (selected.getStatus()
                != VerificationStatus.PENDING) {

            showAlert(
                    "Invalid Request",
                    "Only pending verification requests can be rejected.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        boolean confirmed =
                showConfirmation(
                        "Reject Verification",
                        "Reject identity verification request #"
                                + selected.getVerificationId()
                                + "?"
                );

        if (!confirmed) {
            return;
        }

        try {

            verificationService.rejectVerification(
                    selected.getVerificationId()
            );

            showAlert(
                    "Verification Rejected",
                    "Identity verification request #"
                            + selected.getVerificationId()
                            + " has been rejected.",
                    Alert.AlertType.INFORMATION
            );

            loadVerifications();

        } catch (Exception e) {

            showAlert(
                    "Rejection Failed",
                    safeExceptionMessage(e),
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void handleRefresh() {

        loadVerifications();
    }

    private void loadVerifications() {

        if (!SessionManager.isLoggedIn()) {

            verificationList.clear();

            messageLabel.setText(
                    "Please log in to access identity verification."
            );

            updateButtonState();

            return;
        }

        if (SessionManager.getCurrentUser()
                .getRole() != Role.ADMIN) {

            verificationList.clear();

            messageLabel.setText(
                    "Access denied. Only administrators can review identity verification."
            );

            updateButtonState();

            return;
        }

        try {

            verificationList.setAll(
                    verificationService
                            .getPendingVerifications()
            );

            if (verificationList.isEmpty()) {

                messageLabel.setText(
                        "No pending identity verification requests."
                );

            } else {

                messageLabel.setText(
                        verificationList.size()
                                + " pending verification request(s)."
                );
            }

        } catch (Exception e) {

            verificationList.clear();

            messageLabel.setText(
                    "Unable to load verification requests."
            );

            showAlert(
                    "Load Failed",
                    safeExceptionMessage(e),
                    Alert.AlertType.ERROR
            );
        }

        updateButtonState();
    }

    private void updateButtonState() {

        boolean admin =
                SessionManager.isLoggedIn()
                        && SessionManager.getCurrentUser()
                        .getRole() == Role.ADMIN;

        IdentityVerification selected =
                verificationTable == null
                        ? null
                        : verificationTable
                        .getSelectionModel()
                        .getSelectedItem();

        boolean pending =
                selected != null
                        && selected.getStatus()
                        == VerificationStatus.PENDING;

        if (approveButton != null) {
            approveButton.setDisable(
                    !admin || !pending
            );
        }

        if (rejectButton != null) {
            rejectButton.setDisable(
                    !admin || !pending
            );
        }
    }

    private boolean showConfirmation(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .orElse(javafx.scene.control.ButtonType.CANCEL)
                == javafx.scene.control.ButtonType.OK;
    }

    private void showAlert(
            String title,
            String message,
            Alert.AlertType type) {

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

    private String safeExceptionMessage(
            Exception exception) {

        if (exception.getMessage() == null
                || exception.getMessage().isBlank()) {

            return "An unexpected error occurred.";
        }

        return exception.getMessage();
    }
}