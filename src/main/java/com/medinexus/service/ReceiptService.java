package com.medinexus.service;

import com.medinexus.model.Bill;
import com.medinexus.model.Patient;
import com.medinexus.model.Payment;
import com.medinexus.model.PaymentStatus;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class ReceiptService {

    private final PaymentService paymentService = new PaymentService();
    private final BillService billService = new BillService();
    private final PatientService patientService = new PatientService();

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Generates a PDF receipt for the currently logged-in patient.
     *
     * Security rules:
     * 1. User must be logged in.
     * 2. User must have PATIENT role.
     * 3. Payment must belong to the logged-in patient.
     * 4. Payment must be SUCCESS.
     * 5. Bill must belong to the same patient.
     * 6. Bill must be PAID.
     */
    public Path generateReceipt(Payment payment, Path outputPath) {

        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null.");
        }

        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null.");
        }

        if (!SessionManager.isLoggedIn()) {
            throw new SecurityException("Please log in before generating a receipt.");
        }

        if (SessionManager.getCurrentUser().getRole() != com.medinexus.model.Role.PATIENT) {
            throw new SecurityException(
                    "Only patients can generate their payment receipts."
            );
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Receipt can only be generated for a successful payment."
            );
        }

        if (payment.getBillId() <= 0) {
            throw new IllegalStateException("Invalid bill associated with payment.");
        }

        if (payment.getPatientId() <= 0) {
            throw new IllegalStateException("Invalid patient associated with payment.");
        }

        /*
         * Resolve the currently logged-in patient.
         * We deliberately do not trust the patient ID supplied by the UI.
         */
        Patient currentPatient =
                patientService.getPatientByUserId(
                        SessionManager.getCurrentUser().getUserId()
                );

        if (currentPatient == null) {
            throw new SecurityException(
                    "Patient profile could not be found."
            );
        }

        /*
         * Ownership check.
         */
        if (payment.getPatientId() != currentPatient.getPatientId()) {
            throw new SecurityException(
                    "You are not authorized to generate this receipt."
            );
        }

        Bill bill = billService.getBillById(payment.getBillId());

        if (bill == null) {
            throw new IllegalStateException(
                    "Bill associated with the payment was not found."
            );
        }

        /*
         * Verify that the bill also belongs to the current patient.
         */
        if (bill.getPatientId() != currentPatient.getPatientId()) {
            throw new SecurityException(
                    "This bill does not belong to the logged-in patient."
            );
        }

        /*
         * Verify payment-to-bill amount consistency.
         */
        if (Math.abs(payment.getAmount() - bill.getTotalAmount()) > 0.01) {
            throw new IllegalStateException(
                    "Payment amount does not match the bill amount."
            );
        }

        if (!"PAID".equalsIgnoreCase(bill.getPaymentStatus())) {
            throw new IllegalStateException(
                    "Receipt can only be generated for a paid bill."
            );
        }

        try {
            createPdf(payment, bill, currentPatient, outputPath);
            return outputPath;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to generate payment receipt: " + e.getMessage(),
                    e
            );
        }
    }


    private void createPdf(
            Payment payment,
            Bill bill,
            Patient patient,
            Path outputPath) throws IOException {

        Path parent = outputPath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float left = 55;
                float y = 790;

                /*
                 * Fonts
                 */
                PDType1Font bold =
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                PDType1Font regular =
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                /*
                 * Header
                 */
                writeText(
                        content,
                        "MEDINEXUS",
                        left,
                        y,
                        22,
                        bold
                );

                y -= 25;

                writeText(
                        content,
                        "Smart Hospital Management System",
                        left,
                        y,
                        11,
                        regular
                );

                y -= 35;

                /*
                 * Receipt title
                 */
                writeText(
                        content,
                        "PAYMENT RECEIPT",
                        left,
                        y,
                        17,
                        bold
                );

                y -= 30;

                /*
                 * Horizontal line
                 */
                drawLine(content, left, y, 540, y);

                y -= 25;

                /*
                 * Receipt number
                 */
                writeLabelValue(
                        content,
                        "Receipt No.",
                        "MNX-REC-" + payment.getPaymentId(),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Payment ID",
                        String.valueOf(payment.getPaymentId()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Bill ID",
                        String.valueOf(payment.getBillId()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 35;

                /*
                 * Patient information
                 */
                writeText(
                        content,
                        "PATIENT INFORMATION",
                        left,
                        y,
                        13,
                        bold
                );

                y -= 22;

                writeLabelValue(
                        content,
                        "Patient ID",
                        String.valueOf(patient.getPatientId()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Patient Name",
                        safe(patient.getName()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Phone",
                        safe(patient.getPhone()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 35;

                /*
                 * Bill information
                 */
                writeText(
                        content,
                        "BILL DETAILS",
                        left,
                        y,
                        13,
                        bold
                );

                y -= 25;

                writeLabelValue(
                        content,
                        "Appointment ID",
                        String.valueOf(bill.getAppointmentId()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 25;

                /*
                 * Charge breakdown
                 */
                writeCharge(
                        content,
                        "Consultation Charge",
                        bill.getConsultationCharge(),
                        left,
                        y,
                        regular
                );

                y -= 22;

                writeCharge(
                        content,
                        "Medicine Charge",
                        bill.getMedicineCharge(),
                        left,
                        y,
                        regular
                );

                y -= 22;

                writeCharge(
                        content,
                        "Other Charges",
                        bill.getOtherCharges(),
                        left,
                        y,
                        regular
                );

                y -= 25;

                drawLine(content, left, y, 540, y);

                y -= 25;

                writeCharge(
                        content,
                        "TOTAL AMOUNT",
                        bill.getTotalAmount(),
                        left,
                        y,
                        bold
                );

                y -= 35;

                /*
                 * Payment details
                 */
                writeText(
                        content,
                        "PAYMENT DETAILS",
                        left,
                        y,
                        13,
                        bold
                );

                y -= 25;

                writeLabelValue(
                        content,
                        "Payment Method",
                        payment.getPaymentMethod() == null
                                ? "N/A"
                                : payment.getPaymentMethod().toString(),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Payment Status",
                        "SUCCESS",
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Transaction ID",
                        safe(payment.getTransactionId()),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 20;

                writeLabelValue(
                        content,
                        "Payment Date",
                        payment.getCreatedAt() == null
                                ? "N/A"
                                : payment.getCreatedAt().format(DATE_FORMAT),
                        left,
                        y,
                        bold,
                        regular
                );

                y -= 50;

                /*
                 * Footer
                 */
                drawLine(content, left, y, 540, y);

                y -= 25;

                writeText(
                        content,
                        "Thank you for using MediNexus.",
                        left,
                        y,
                        11,
                        bold
                );

                y -= 18;

                writeText(
                        content,
                        "This is a computer-generated payment receipt.",
                        left,
                        y,
                        9,
                        regular
                );

                y -= 15;

                writeText(
                        content,
                        "Please retain this receipt for your records.",
                        left,
                        y,
                        9,
                        regular
                );
            }

            document.save(outputPath.toFile());
        }
    }


    private void writeText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float fontSize,
            PDType1Font font) throws IOException {

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(safe(text));
        content.endText();
    }


    private void writeLabelValue(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y,
            PDType1Font labelFont,
            PDType1Font valueFont) throws IOException {

        content.beginText();

        content.setFont(labelFont, 10);
        content.newLineAtOffset(x, y);
        content.showText(safe(label) + ": ");

        content.setFont(valueFont, 10);
        content.showText(safe(value));

        content.endText();
    }


    private void writeCharge(
            PDPageContentStream content,
            String label,
            double amount,
            float x,
            float y,
            PDType1Font font) throws IOException {

        content.beginText();

        content.setFont(font, 10);
        content.newLineAtOffset(x, y);
        content.showText(safe(label));

        content.newLineAtOffset(370, 0);
        content.showText(String.format("Rs. %.2f", amount));

        content.endText();
    }


    private void drawLine(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2) throws IOException {

        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }


    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }

        /*
         * PDFBox standard fonts do not support every Unicode character.
         * Replace unsupported common characters to keep the PDF reliable.
         */
        return value
                .replace("₹", "Rs.")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'");
    }
}