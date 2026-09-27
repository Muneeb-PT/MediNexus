package com.medinexus;

import com.medinexus.model.Bill;
import com.medinexus.repository.BillJdbcRepository;

import java.util.List;

public class BillDatabaseTest {

    public static void main(String[] args) {

        BillJdbcRepository repository =
                new BillJdbcRepository();

        System.out.println("========================================");
        System.out.println("       BILL JDBC DATABASE TEST");
        System.out.println("========================================");

        // -----------------------------------------------------
        // TEST 1: GET ALL BILLS
        // -----------------------------------------------------

        System.out.println("\n1. Fetching all bills...");

        List<Bill> bills =
                repository.findAll();

        System.out.println(
                "Total bills: " + bills.size()
        );

        for (Bill bill : bills) {

            System.out.println(
                    "ID: " + bill.getBillId()
                            + " | Patient: " + bill.getPatientId()
                            + " | Appointment: " + bill.getAppointmentId()
                            + " | Total: " + bill.getTotalAmount()
                            + " | Status: " + bill.getPaymentStatus()
            );
        }

        // -----------------------------------------------------
        // TEST 2: FIND BILL BY ID
        // -----------------------------------------------------

        System.out.println("\n2. Finding bill ID 1...");

        Bill bill =
                repository.findById(1);

        if (bill != null) {

            System.out.println(
                    "Bill found successfully."
            );

            System.out.println(
                    "Bill ID: "
                            + bill.getBillId()
            );

            System.out.println(
                    "Patient ID: "
                            + bill.getPatientId()
            );

            System.out.println(
                    "Appointment ID: "
                            + bill.getAppointmentId()
            );

            System.out.println(
                    "Consultation Charge: "
                            + bill.getConsultationCharge()
            );

            System.out.println(
                    "Medicine Charge: "
                            + bill.getMedicineCharge()
            );

            System.out.println(
                    "Other Charges: "
                            + bill.getOtherCharges()
            );

            System.out.println(
                    "Total Amount: "
                            + bill.getTotalAmount()
            );

            System.out.println(
                    "Payment Status: "
                            + bill.getPaymentStatus()
            );

            System.out.println(
                    "Created At: "
                            + bill.getCreatedAt()
            );

        } else {

            System.out.println(
                    "Bill ID 1 not found."
            );
        }

        // -----------------------------------------------------
        // TEST 3: FIND PATIENT BILLS
        // -----------------------------------------------------

        System.out.println(
                "\n3. Finding bills for patient 1..."
        );

        List<Bill> patientBills =
                repository.findByPatientId(1);

        System.out.println(
                "Patient 1 bills: "
                        + patientBills.size()
        );

        // -----------------------------------------------------
        // TEST 4: FIND BILL BY APPOINTMENT
        // -----------------------------------------------------

        System.out.println(
                "\n4. Finding bill for appointment 1..."
        );

        Bill appointmentBill =
                repository.findByAppointmentId(1);

        if (appointmentBill != null) {

            System.out.println(
                    "Appointment bill found."
            );

            System.out.println(
                    "Bill ID: "
                            + appointmentBill.getBillId()
            );

            System.out.println(
                    "Total: "
                            + appointmentBill.getTotalAmount()
            );

            System.out.println(
                    "Status: "
                            + appointmentBill.getPaymentStatus()
            );

        } else {

            System.out.println(
                    "No bill found for appointment 1."
            );
        }

        System.out.println("\n========================================");
        System.out.println("       BILL JDBC TEST COMPLETED");
        System.out.println("========================================");
    }
}