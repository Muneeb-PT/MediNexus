package com.medinexus.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AIAssistantController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {

        chatArea.setEditable(false);

        chatArea.appendText(
                "MediNexus AI Assistant\n" +
                        "────────────────────────────\n" +
                        "Hello! I'm the MediNexus Assistant.\n\n" +
                        "I can currently help you understand:\n" +
                        "• Appointments\n" +
                        "• Medical records\n" +
                        "• Prescriptions\n" +
                        "• Billing and payments\n" +
                        "• Pharmacy services\n" +
                        "• Identity verification\n" +
                        "• Emergency services\n" +
                        "• MediNexus system features\n\n"
        );

        statusLabel.setText("Assistant ready");
    }

    @FXML
    private void handleSend() {

        String message = messageField.getText();

        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String question = message.trim();

        chatArea.appendText(
                "\nYou:\n" +
                        question +
                        "\n\n"
        );

        String response = generateResponse(question);

        chatArea.appendText(
                "MediNexus AI:\n" +
                        response +
                        "\n\n"
        );

        messageField.clear();
        statusLabel.setText("Response generated");
    }

    private String generateResponse(String question) {

        String text = question.toLowerCase();

        if (text.contains("appointment")
                || text.contains("book")
                || text.contains("schedule")) {

            return "You can use the Appointments module from your dashboard. " +
                    "Available appointment actions depend on your MediNexus role.";
        }

        if (text.contains("prescription")
                || text.contains("medicine")) {

            return "Prescriptions are created through the clinical workflow. " +
                    "Doctors can create prescriptions for eligible patients, while authorized pharmacy staff can process prescriptions.";
        }

        if (text.contains("bill")
                || text.contains("payment")
                || text.contains("pay")) {

            return "Open Billing to view available bills. Patients can use the supported online payment workflow, while authorized staff can process permitted payment operations.";
        }

        if (text.contains("medical record")
                || text.contains("medical history")
                || text.contains("diagnosis")) {

            return "Medical records contain clinical information and are protected by MediNexus role and ownership rules.";
        }

        if (text.contains("emergency")
                || text.contains("ambulance")) {

            return "For a real emergency, contact the appropriate local emergency service immediately. MediNexus can provide an emergency-request workflow for the hospital system.";
        }

        if (text.contains("identity")
                || text.contains("digilocker")
                || text.contains("verification")) {

            return "Identity verification is intended to connect a patient with a trusted digital identity provider. A verification result should only be marked verified after successful provider confirmation.";
        }

        if (text.contains("help")) {

            return "You can open the Help Center for information about appointments, medical records, prescriptions, billing, pharmacy, identity verification, emergency services, and account security.";
        }

        return "I can help with MediNexus features such as appointments, medical records, prescriptions, billing, pharmacy, identity verification, emergency services, and account support. Try asking about one of these areas.";
    }

    @FXML
    private void handleBack() {

        DashboardController.navigateToDashboard();
    }
}