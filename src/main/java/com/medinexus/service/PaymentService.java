package com.medinexus.service;

import com.medinexus.model.Bill;
import com.medinexus.model.Payment;
import com.medinexus.model.PaymentMethod;
import com.medinexus.model.PaymentStatus;
import com.medinexus.model.Prescription;
import com.medinexus.model.PrescriptionStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.payment.DemoPaymentGateway;
import com.medinexus.payment.PaymentGateway;
import com.medinexus.payment.PaymentResult;
import com.medinexus.repository.PaymentJdbcRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentService {

    private final PaymentJdbcRepository repository =
            new PaymentJdbcRepository();

    private final BillService billService =
            new BillService();

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final PaymentGateway paymentGateway =
            new DemoPaymentGateway();


    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    public List<Payment> getAllPayments() {

        return repository.findAll();
    }


    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    public Payment getPaymentById(int paymentId) {

        if (paymentId <= 0) {
            return null;
        }

        return repository.findById(paymentId);
    }


    // =========================================================
    // GET PAYMENT BY BILL ID
    // =========================================================

    public Payment getPaymentByBillId(int billId) {

        if (billId <= 0) {
            return null;
        }

        List<Payment> payments =
                repository.findByBillId(billId);

        if (payments == null
                || payments.isEmpty()) {

            return null;
        }

        /*
         * Always prefer the successful payment when multiple
         * payment attempts exist for the same bill.
         */
        for (Payment payment : payments) {

            if (payment != null
                    && payment.getStatus()
                    == PaymentStatus.SUCCESS) {

                return payment;
            }
        }

        return payments.get(0);
    }


    // =========================================================
    // GET PAYMENTS BY PATIENT
    // =========================================================

    public List<Payment> getPatientPayments(
            int patientId) {

        if (patientId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        return repository.findByPatientId(
                patientId
        );
    }


    // =========================================================
    // PROCESS ONLINE PAYMENT
    // =========================================================

    public Payment processOnlinePayment(
            int billId,
            PaymentMethod paymentMethod) {

        // -----------------------------------------------------
        // SESSION CHECK
        // -----------------------------------------------------

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            throw new IllegalStateException(
                    "No user is currently logged in."
            );
        }


        // -----------------------------------------------------
        // ROLE CHECK
        // -----------------------------------------------------

        if (currentUser.getRole() != Role.PATIENT) {

            throw new SecurityException(
                    "Only patients can make online payments."
            );
        }


        // -----------------------------------------------------
        // PAYMENT METHOD VALIDATION
        // -----------------------------------------------------

        if (paymentMethod == null) {

            throw new IllegalArgumentException(
                    "Please select a payment method."
            );
        }

        if (paymentMethod == PaymentMethod.CASH) {

            throw new IllegalArgumentException(
                    "Cash payments cannot be processed online."
            );
        }


        // -----------------------------------------------------
        // BILL VALIDATION
        // -----------------------------------------------------

        Bill bill =
                billService.getBillById(
                        billId
                );

        if (bill == null) {

            throw new IllegalArgumentException(
                    "Bill not found."
            );
        }


        // -----------------------------------------------------
        // PATIENT OWNERSHIP
        // -----------------------------------------------------

        PatientService patientService =
                new PatientService();

        com.medinexus.model.Patient patient =
                patientService.getPatientByUserId(
                        currentUser.getUserId()
                );

        if (patient == null) {

            throw new IllegalStateException(
                    "Patient profile not found."
            );
        }

        if (bill.getPatientId()
                != patient.getPatientId()) {

            throw new SecurityException(
                    "You are not authorized to pay this bill."
            );
        }


        // -----------------------------------------------------
        // BILL STATUS
        // -----------------------------------------------------

        if ("PAID".equalsIgnoreCase(
                bill.getPaymentStatus())) {

            throw new IllegalStateException(
                    "This bill has already been paid."
            );
        }


        // -----------------------------------------------------
        // EXISTING SUCCESSFUL PAYMENT
        // -----------------------------------------------------

        Payment existingPayment =
                getPaymentByBillId(
                        billId
                );

        if (existingPayment != null
                && existingPayment.getStatus()
                == PaymentStatus.SUCCESS) {

            throw new IllegalStateException(
                    "A successful payment already exists for this bill."
            );
        }


        // -----------------------------------------------------
        // AMOUNT VALIDATION
        // -----------------------------------------------------

        if (bill.getTotalAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Bill amount must be greater than zero."
            );
        }


        // -----------------------------------------------------
        // PAYMENT GATEWAY
        // -----------------------------------------------------

        PaymentResult result =
                paymentGateway.processPayment(
                        bill.getTotalAmount(),
                        "MediNexus Bill #" + billId
                );

        if (result == null) {

            throw new IllegalStateException(
                    "Payment gateway returned no result."
            );
        }


        // -----------------------------------------------------
        // TRANSACTION ID
        // -----------------------------------------------------

        String transactionId =
                result.getTransactionId();

        /*
         * A successful gateway transaction must have a
         * transaction ID.
         *
         * Without a transaction ID, the payment cannot be
         * safely made idempotent.
         */
        if (result.isSuccessful()
                && (transactionId == null
                || transactionId.isBlank())) {

            throw new IllegalStateException(
                    "Successful payment did not contain a transaction ID."
            );
        }


        // -----------------------------------------------------
        // FAILED PAYMENT
        // -----------------------------------------------------

        if (!result.isSuccessful()) {

            Payment failedPayment =
                    new Payment(
                            generatePaymentId(),
                            bill.getBillId(),
                            bill.getPatientId(),
                            bill.getTotalAmount(),
                            paymentMethod,
                            PaymentStatus.FAILED,
                            transactionId,
                            LocalDateTime.now()
                    );

            repository.add(
                    failedPayment
            );

            return failedPayment;
        }


        // -----------------------------------------------------
        // SUCCESSFUL PAYMENT
        // -----------------------------------------------------

        /*
         * The repository performs the complete operation
         * inside ONE database transaction:
         *
         * 1. Lock the bill
         * 2. Verify patient ownership
         * 3. Check transaction-id idempotency
         * 4. Mark bill as PAID
         * 5. Mark related BILLED prescriptions as PAID
         * 6. Insert SUCCESS payment
         * 7. Commit
         *
         * If the same gateway transaction is retried,
         * PaymentJdbcRepository can safely return the existing
         * successful payment instead of creating another one.
         */

        return repository.completeSuccessfulPayment(
                bill.getBillId(),
                bill.getPatientId(),
                bill.getTotalAmount(),
                paymentMethod,
                transactionId
        );
    }


    // =========================================================
    // RECORD CASH PAYMENT
    // =========================================================

    public Payment recordCashPayment(
            int billId) {

        // -----------------------------------------------------
        // SESSION CHECK
        // -----------------------------------------------------

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            throw new IllegalStateException(
                    "No user is currently logged in."
            );
        }


        // -----------------------------------------------------
        // ROLE CHECK
        // -----------------------------------------------------

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole()
                != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can record cash payments."
            );
        }


        // -----------------------------------------------------
        // BILL VALIDATION
        // -----------------------------------------------------

        Bill bill =
                billService.getBillById(
                        billId
                );

        if (bill == null) {

            throw new IllegalArgumentException(
                    "Bill not found."
            );
        }


        // -----------------------------------------------------
        // BILL STATUS
        // -----------------------------------------------------

        if ("PAID".equalsIgnoreCase(
                bill.getPaymentStatus())) {

            throw new IllegalStateException(
                    "This bill has already been paid."
            );
        }


        // -----------------------------------------------------
        // EXISTING SUCCESSFUL PAYMENT
        // -----------------------------------------------------

        Payment existingPayment =
                getPaymentByBillId(
                        billId
                );

        if (existingPayment != null
                && existingPayment.getStatus()
                == PaymentStatus.SUCCESS) {

            throw new IllegalStateException(
                    "A successful payment already exists for this bill."
            );
        }


        // -----------------------------------------------------
        // AMOUNT VALIDATION
        // -----------------------------------------------------

        if (bill.getTotalAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Bill amount must be greater than zero."
            );
        }


        // -----------------------------------------------------
        // CASH TRANSACTION ID
        // -----------------------------------------------------

        /*
         * UUID is used instead of System.currentTimeMillis()
         * because payment transaction IDs should remain unique
         * even when multiple payments are recorded within the
         * same millisecond.
         */
        String transactionId =
                "CASH-" + UUID.randomUUID();


        // -----------------------------------------------------
        // ATOMIC CASH PAYMENT
        // -----------------------------------------------------

        /*
         * Cash payment uses the SAME atomic database transaction
         * as online payment.
         *
         * Bill       -> PAID
         * Prescription(s) -> PAID
         * Payment    -> SUCCESS
         *
         * All operations commit together.
         */

        return repository.completeSuccessfulPayment(
                bill.getBillId(),
                bill.getPatientId(),
                bill.getTotalAmount(),
                PaymentMethod.CASH,
                transactionId
        );
    }


    // =========================================================
    // RECORD CASH PAYMENT FOR PRESCRIPTION
    // =========================================================

    public Payment recordCashPaymentForPrescription(
            int prescriptionId) {

        // -----------------------------------------------------
        // SESSION CHECK
        // -----------------------------------------------------

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            throw new IllegalStateException(
                    "No user is currently logged in."
            );
        }


        // -----------------------------------------------------
        // ROLE CHECK
        // -----------------------------------------------------

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole()
                != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can record payments."
            );
        }


        // -----------------------------------------------------
        // PRESCRIPTION ID
        // -----------------------------------------------------

        if (prescriptionId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }


        // -----------------------------------------------------
        // LOAD PRESCRIPTION
        // -----------------------------------------------------

        Prescription prescription =
                prescriptionService.getPrescriptionById(
                        prescriptionId
                );

        if (prescription == null) {

            throw new IllegalArgumentException(
                    "Prescription not found."
            );
        }


        // -----------------------------------------------------
        // PRESCRIPTION STATUS
        // -----------------------------------------------------

        if (prescription.getStatus()
                != PrescriptionStatus.BILLED) {

            throw new IllegalStateException(
                    "Prescription must be BILLED before payment."
            );
        }


        // -----------------------------------------------------
        // RESOLVE BILL
        // -----------------------------------------------------

        /*
         * Resolve the bill directly using the real database
         * relationship instead of asking MedicalRecordService
         * for a record.
         *
         * Receptionist is deliberately restricted from
         * viewing medical records.
         */

        int billId =
                repository.findBillIdForPrescription(
                        prescriptionId
                );

        if (billId <= 0) {

            throw new IllegalArgumentException(
                    "No bill exists for this prescription."
            );
        }


        // -----------------------------------------------------
        // LOAD BILL
        // -----------------------------------------------------

        Bill bill =
                billService.getBillById(
                        billId
                );

        if (bill == null) {

            throw new IllegalArgumentException(
                    "Bill not found."
            );
        }


        // -----------------------------------------------------
        // PATIENT CONSISTENCY CHECK
        // -----------------------------------------------------

        if (bill.getPatientId()
                != prescription.getPatientId()) {

            throw new SecurityException(
                    "Prescription and bill patient IDs do not match."
            );
        }


        // -----------------------------------------------------
        // PROCESS CASH PAYMENT
        // -----------------------------------------------------

        return recordCashPayment(
                bill.getBillId()
        );
    }


    // =========================================================
    // UPDATE PAYMENT
    // =========================================================

    public void updatePayment(
            Payment payment) {

        if (payment == null) {

            throw new IllegalArgumentException(
                    "Payment cannot be null."
            );
        }

        repository.update(
                payment
        );
    }


    // =========================================================
    // DELETE PAYMENT
    // =========================================================

    public void deletePayment(
            int paymentId) {

        if (paymentId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid payment ID."
            );
        }

        repository.delete(
                paymentId
        );
    }


    // =========================================================
    // GENERATE PAYMENT ID
    // =========================================================

    /*
     * Kept for compatibility with the existing application
     * and tests.
     *
     * IMPORTANT:
     * Successful database payments use MySQL AUTO_INCREMENT.
     * Therefore this method is NOT used for the atomic
     * successful-payment path.
     */
    public int generatePaymentId() {

        return repository.findAll()
                .stream()
                .mapToInt(
                        Payment::getPaymentId
                )
                .max()
                .orElse(0) + 1;
    }
}