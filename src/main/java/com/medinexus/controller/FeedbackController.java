package com.medinexus.controller;

import com.medinexus.model.User;
import com.medinexus.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.medinexus.model.SupportTicket;
import com.medinexus.service.SupportTicketService;

public class FeedbackController {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea messageArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {

        typeComboBox.getItems().addAll(
                "Feedback",
                "Complaint",
                "Support Request",
                "Suggestion"
        );

        categoryComboBox.getItems().addAll(
                "Appointments",
                "Medical Records",
                "Prescriptions",
                "Billing & Payments",
                "Pharmacy",
                "Identity Verification",
                "Emergency Services",
                "Account & Security",
                "Other"
        );

        typeComboBox.getSelectionModel().selectFirst();
        categoryComboBox.getSelectionModel().selectFirst();

        statusLabel.setText("Ready to receive your feedback.");
    }

    @FXML
    private void handleSubmit() {

        try {

            SupportTicket ticket =
                    supportTicketService.createTicket(
                            typeComboBox.getValue(),
                            categoryComboBox.getValue(),
                            subjectField.getText(),
                            messageArea.getText()
                    );

            statusLabel.setText(
                    "Ticket #" +
                            ticket.getTicketId() +
                            " submitted successfully. " +
                            "Priority: " +
                            ticket.getPriority()
            );

            subjectField.clear();
            messageArea.clear();

        } catch (SecurityException e) {

            statusLabel.setText(
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            statusLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            statusLabel.setText(
                    "Unable to submit the ticket. Please try again."
            );
        }
    }

    @FXML
    private void handleClear() {

        subjectField.clear();
        messageArea.clear();

        typeComboBox.getSelectionModel().selectFirst();
        categoryComboBox.getSelectionModel().selectFirst();

        statusLabel.setText("Form cleared.");
    }

    @FXML
    private void handleBack() {

        DashboardController.navigateToDashboard();
    }

    private final SupportTicketService supportTicketService =
            new SupportTicketService();


}