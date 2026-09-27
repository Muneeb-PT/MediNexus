package com.medinexus.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelpController {

    @FXML
    private Accordion helpAccordion;

    @FXML
    private Label roleLabel;

    @FXML
    private Button backButton;

    @FXML
    private VBox helpContent;

    @FXML
    public void initialize() {

        roleLabel.setText("MediNexus Help Center");

        if (helpAccordion != null) {
            helpAccordion.setExpandedPane(null);
        }
    }

    @FXML
    private void handleBack() {

        DashboardController.navigateToDashboard();
    }
}