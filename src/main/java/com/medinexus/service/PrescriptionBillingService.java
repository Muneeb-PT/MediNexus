package com.medinexus.service;

import com.medinexus.model.Bill;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Medicine;
import com.medinexus.model.Prescription;
import com.medinexus.model.PrescriptionStatus;
import com.medinexus.repository.PrescriptionBillingRepository;
import com.medinexus.repository.PrescriptionDispensingRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles the business workflow connecting prescriptions,
 * medicine inventory and billing.
 *
 * Prescription lifecycle:
 *
 * PENDING
 *    ↓
 * DISPENSED
 *    ↓
 * BILLED
 *    ↓
 * PAID
 *
 * Billing model:
 *
 * Multiple prescriptions belonging to the same medical record
 * and appointment are combined into ONE bill.
 */
public class PrescriptionBillingService {

    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;
    private final MedicineService medicineService;
    private final BillService billService;

    private final PrescriptionDispensingRepository dispensingRepository;
    private final PrescriptionBillingRepository billingRepository;

    public PrescriptionBillingService() {

        prescriptionService =
                new PrescriptionService();

        medicalRecordService =
                new MedicalRecordService();

        medicineService =
                new MedicineService();

        billService =
                new BillService();

        dispensingRepository =
                new PrescriptionDispensingRepository();

        billingRepository =
                new PrescriptionBillingRepository();
    }

    // =========================================================
    // DISPENSE PRESCRIPTION
    // =========================================================

    /**
     * Dispenses one prescription atomically.
     *
     * PENDING → DISPENSED
     */
    public void dispensePrescription(
            int prescriptionId) {

        if (prescriptionId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }

        Prescription prescription =
                prescriptionService.getPrescriptionById(
                        prescriptionId
                );

        if (prescription == null) {

            throw new IllegalArgumentException(
                    "Prescription not found."
            );
        }

        if (prescription.getStatus()
                != PrescriptionStatus.PENDING) {

            throw new IllegalStateException(
                    "Only PENDING prescriptions can be dispensed."
            );
        }

        boolean success =
                dispensingRepository.dispensePrescription(
                        prescriptionId
                );

        if (!success) {

            throw new IllegalStateException(
                    "Prescription could not be dispensed. "
                            + "Please check the prescription status "
                            + "and medicine stock."
            );
        }
    }

    // =========================================================
    // CREATE BILL FROM PRESCRIPTIONS
    // =========================================================

    /**
     * Creates ONE combined bill for all DISPENSED prescriptions
     * belonging to the same medical record / appointment.
     *
     * Example:
     *
     * Prescription 1 → Medicine A
     * Prescription 2 → Medicine B
     * Prescription 3 → Medicine C
     *
     *              ↓
     *
     *          ONE BILL
     *
     * After successful billing:
     *
     * DISPENSED → BILLED
     * for every prescription included in the bill.
     *
     * Bill creation and prescription status changes are performed
     * inside one database transaction.
     */
    public Bill createBillFromPrescription(
            int prescriptionId,
            double consultationCharge,
            double otherCharges) {

        if (prescriptionId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }

        if (consultationCharge < 0
                || otherCharges < 0) {

            throw new IllegalArgumentException(
                    "Charges cannot be negative."
            );
        }

        // -----------------------------------------------------
        // 1. Find selected prescription
        // -----------------------------------------------------

        Prescription selectedPrescription =
                prescriptionService.getPrescriptionById(
                        prescriptionId
                );

        if (selectedPrescription == null) {

            throw new IllegalArgumentException(
                    "Prescription not found."
            );
        }

        if (selectedPrescription.getStatus()
                != PrescriptionStatus.DISPENSED) {

            throw new IllegalStateException(
                    "Prescription must be DISPENSED before billing."
            );
        }

        // -----------------------------------------------------
        // 2. Find medical record
        // -----------------------------------------------------

        MedicalRecord medicalRecord =
                medicalRecordService.getRecordById(
                        selectedPrescription.getMedicalRecordId()
                );

        if (medicalRecord == null) {

            throw new IllegalStateException(
                    "Medical record not found."
            );
        }

        if (medicalRecord.getAppointmentId() <= 0) {

            throw new IllegalStateException(
                    "Medical record is not linked to an appointment."
            );
        }

        int appointmentId =
                medicalRecord.getAppointmentId();

        // -----------------------------------------------------
        // 3. Check whether bill already exists
        // -----------------------------------------------------

        Bill existingBill =
                billService.getBillByAppointment(
                        appointmentId
                );

        if (existingBill != null) {

            throw new IllegalStateException(
                    "A bill already exists for this appointment."
            );
        }

        // -----------------------------------------------------
        // 4. Get all prescriptions for this medical record
        // -----------------------------------------------------

        List<Prescription> prescriptions =
                prescriptionService
                        .getMedicalRecordPrescriptions(
                                selectedPrescription.getMedicalRecordId()
                        );

        if (prescriptions == null
                || prescriptions.isEmpty()) {

            throw new IllegalStateException(
                    "No prescriptions found for this medical record."
            );
        }

        // -----------------------------------------------------
        // 5. Keep only DISPENSED prescriptions
        // -----------------------------------------------------

        List<Prescription> dispensedPrescriptions =
                prescriptions.stream()
                        .filter(p ->
                                p != null
                                        && p.getStatus()
                                        == PrescriptionStatus.DISPENSED)
                        .toList();

        if (dispensedPrescriptions.isEmpty()) {

            throw new IllegalStateException(
                    "No DISPENSED prescriptions are available for billing."
            );
        }

        // -----------------------------------------------------
        // 6. Calculate combined medicine charge
        // -----------------------------------------------------

        double medicineCharge = 0.0;

        for (Prescription prescription :
                dispensedPrescriptions) {

            Medicine medicine =
                    medicineService.getMedicineById(
                            prescription.getMedicineId()
                    );

            if (medicine == null) {

                throw new IllegalStateException(
                        "Medicine not found for prescription ID: "
                                + prescription.getPrescriptionId()
                );
            }

            double charge =
                    medicineService.calculateMedicineCharge(
                            prescription.getMedicineId(),
                            prescription.getQuantity()
                    );

            medicineCharge += charge;
        }

        // -----------------------------------------------------
        // 7. Calculate total
        // -----------------------------------------------------

        double totalAmount =
                consultationCharge
                        + medicineCharge
                        + otherCharges;

        // -----------------------------------------------------
        // 8. Create bill
        // -----------------------------------------------------
        //
        // bill_id = 0 intentionally.
        //
        // MySQL AUTO_INCREMENT will generate the real ID.
        // PrescriptionBillingRepository performs the INSERT
        // and prescription status updates atomically.
        // -----------------------------------------------------

        Bill bill = new Bill(
                0,
                selectedPrescription.getPatientId(),
                appointmentId,
                consultationCharge,
                medicineCharge,
                otherCharges,
                totalAmount,
                "PENDING",
                LocalDateTime.now()
        );

        // -----------------------------------------------------
        // 9. Atomically:
        //
        //    INSERT BILL
        //        +
        //    DISPENSED → BILLED
        // -----------------------------------------------------

        billingRepository
                .createBillAndMarkPrescriptionsBilled(
                        bill,
                        dispensedPrescriptions
                );

        // -----------------------------------------------------
        // 10. Return bill with database-generated ID
        // -----------------------------------------------------

        return bill;
    }

    // =========================================================
    // MARK PRESCRIPTION AS PAID
    // =========================================================

    /**
     * Marks the bill and all prescriptions belonging to the
     * same appointment as PAID.
     *
     * BILLED → PAID
     */
    public void markPrescriptionAsPaid(
            int prescriptionId) {

        if (prescriptionId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid prescription ID."
            );
        }

        Prescription prescription =
                prescriptionService.getPrescriptionById(
                        prescriptionId
                );

        if (prescription == null) {

            throw new IllegalArgumentException(
                    "Prescription not found."
            );
        }

        if (prescription.getStatus()
                != PrescriptionStatus.BILLED) {

            throw new IllegalStateException(
                    "Prescription must be BILLED before payment."
            );
        }

        MedicalRecord medicalRecord =
                medicalRecordService.getRecordById(
                        prescription.getMedicalRecordId()
                );

        if (medicalRecord == null) {

            throw new IllegalStateException(
                    "Medical record not found."
            );
        }

        if (medicalRecord.getAppointmentId() <= 0) {

            throw new IllegalStateException(
                    "Medical record is not linked to an appointment."
            );
        }

        int appointmentId =
                medicalRecord.getAppointmentId();

        Bill bill =
                billService.getBillByAppointment(
                        appointmentId
                );

        if (bill == null) {

            throw new IllegalStateException(
                    "Bill not found for this appointment."
            );
        }

        if (!"PENDING".equalsIgnoreCase(
                bill.getPaymentStatus())) {

            throw new IllegalStateException(
                    "Bill is not in PENDING payment state."
            );
        }

        // -----------------------------------------------------
        // Mark bill as paid
        // -----------------------------------------------------

        billService.markAsPaid(
                bill.getBillId()
        );

        // -----------------------------------------------------
        // Mark all related BILLED prescriptions as PAID
        // -----------------------------------------------------

        List<Prescription> prescriptions =
                prescriptionService
                        .getMedicalRecordPrescriptions(
                                prescription.getMedicalRecordId()
                        );

        for (Prescription relatedPrescription :
                prescriptions) {

            if (relatedPrescription != null
                    && relatedPrescription.getStatus()
                    == PrescriptionStatus.BILLED) {

                prescriptionService.updateStatus(
                        relatedPrescription.getPrescriptionId(),
                        PrescriptionStatus.PAID
                );
            }
        }
    }
}