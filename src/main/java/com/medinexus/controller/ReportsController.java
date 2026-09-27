package com.medinexus.controller;

import com.medinexus.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReportsController {

    @FXML
    private Label totalPatientsLabel;

    @FXML
    private Label totalDoctorsLabel;

    @FXML
    private Label availableMedicinesLabel;

    @FXML
    private Label totalAppointmentsLabel;

    @FXML
    private Label completedAppointmentsLabel;

    @FXML
    private Label scheduledAppointmentsLabel;

    @FXML
    private Label cancelledAppointmentsLabel;

    @FXML
    private Label totalMedicinesLabel;

    @FXML
    private Label outOfStockMedicinesLabel;

    @FXML
    private Label totalPrescriptionsLabel;

    @FXML
    private Label pendingPrescriptionsLabel;

    @FXML
    private Label dispensedPrescriptionsLabel;

    @FXML
    private Label billedPrescriptionsLabel;

    @FXML
    private Label paidPrescriptionsLabel;

    @FXML
    private Label totalBilledLabel;

    @FXML
    private Label totalPaidLabel;

    @FXML
    private Label totalPendingLabel;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadReportData();
    }

    private void loadReportData() {

        try {
            totalPatientsLabel.setText(
                    String.valueOf(reportService.getTotalPatients())
            );

            totalDoctorsLabel.setText(
                    String.valueOf(reportService.getTotalDoctors())
            );

            totalAppointmentsLabel.setText(
                    String.valueOf(reportService.getTotalAppointments())
            );

            completedAppointmentsLabel.setText(
                    String.valueOf(reportService.getCompletedAppointments())
            );

            scheduledAppointmentsLabel.setText(
                    String.valueOf(reportService.getScheduledAppointments())
            );

            cancelledAppointmentsLabel.setText(
                    String.valueOf(reportService.getCancelledAppointments())
            );

            totalMedicinesLabel.setText(
                    String.valueOf(reportService.getTotalMedicines())
            );

            outOfStockMedicinesLabel.setText(
                    String.valueOf(reportService.getOutOfStockMedicines())
            );

            totalPrescriptionsLabel.setText(
                    String.valueOf(reportService.getTotalPrescriptions())
            );

            pendingPrescriptionsLabel.setText(
                    String.valueOf(
                            reportService.getPrescriptionCountByStatus("PENDING")
                    )
            );

            availableMedicinesLabel.setText(
                    String.valueOf(reportService.getAvailableMedicines())
            );

            dispensedPrescriptionsLabel.setText(
                    String.valueOf(
                            reportService.getPrescriptionCountByStatus("DISPENSED")
                    )
            );

            billedPrescriptionsLabel.setText(
                    String.valueOf(
                            reportService.getPrescriptionCountByStatus("BILLED")
                    )
            );

            paidPrescriptionsLabel.setText(
                    String.valueOf(
                            reportService.getPrescriptionCountByStatus("PAID")
                    )
            );

            totalBilledLabel.setText(
                    formatCurrency(reportService.getTotalBilledAmount())
            );

            totalPaidLabel.setText(
                    formatCurrency(reportService.getTotalPaidAmount())
            );

            totalPendingLabel.setText(
                    formatCurrency(reportService.getTotalPendingAmount())
            );

        } catch (Exception e) {
            e.printStackTrace();

            setErrorValues();
        }
    }

    private String formatCurrency(double amount) {
        return String.format("₹ %.2f", amount);
    }

    private void setErrorValues() {

        totalPatientsLabel.setText("-");
        totalDoctorsLabel.setText("-");
        totalAppointmentsLabel.setText("-");
        completedAppointmentsLabel.setText("-");
        scheduledAppointmentsLabel.setText("-");
        cancelledAppointmentsLabel.setText("-");
        totalMedicinesLabel.setText("-");
        outOfStockMedicinesLabel.setText("-");
        totalPrescriptionsLabel.setText("-");
        pendingPrescriptionsLabel.setText("-");
        dispensedPrescriptionsLabel.setText("-");
        billedPrescriptionsLabel.setText("-");
        paidPrescriptionsLabel.setText("-");
        totalBilledLabel.setText("-");
        totalPaidLabel.setText("-");
        totalPendingLabel.setText("-");
        availableMedicinesLabel.setText("-");
    }

    @FXML
    private void handleRefresh() {
        loadReportData();
    }
}