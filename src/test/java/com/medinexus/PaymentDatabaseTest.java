package com.medinexus.test;

import com.medinexus.model.Payment;
import com.medinexus.repository.PaymentJdbcRepository;

import java.util.List;

public class PaymentDatabaseTest {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("   MediNexus Payment Database Test");
        System.out.println("======================================");

        PaymentJdbcRepository repository =
                new PaymentJdbcRepository();

        // -----------------------------------------
        // Test 1: Find all payments
        // -----------------------------------------

        System.out.println("\n--- All Payments ---");

        List<Payment> payments =
                repository.findAll();

        for (Payment payment : payments) {

            System.out.println(
                    "Payment ID      : " + payment.getPaymentId()
            );

            System.out.println(
                    "Bill ID         : " + payment.getBillId()
            );

            System.out.println(
                    "Patient ID      : " + payment.getPatientId()
            );

            System.out.println(
                    "Amount          : " + payment.getAmount()
            );

            System.out.println(
                    "Payment Method  : " + payment.getPaymentMethod()
            );

            System.out.println(
                    "Status          : " + payment.getStatus()
            );

            System.out.println(
                    "Transaction ID  : " + payment.getTransactionId()
            );

            System.out.println(
                    "Created At      : " + payment.getCreatedAt()
            );

            System.out.println("--------------------------------------");
        }

        // -----------------------------------------
        // Test 2: Find Payment ID 1
        // -----------------------------------------

        System.out.println("\n--- Payment ID 1 ---");

        Payment payment =
                repository.findById(1);

        if (payment != null) {

            System.out.println(
                    "Payment found successfully."
            );

            System.out.println(
                    "Payment ID     : " + payment.getPaymentId()
            );

            System.out.println(
                    "Bill ID        : " + payment.getBillId()
            );

            System.out.println(
                    "Patient ID     : " + payment.getPatientId()
            );

            System.out.println(
                    "Amount         : " + payment.getAmount()
            );

            System.out.println(
                    "Method         : " + payment.getPaymentMethod()
            );

            System.out.println(
                    "Status         : " + payment.getStatus()
            );

            System.out.println(
                    "Transaction ID : " + payment.getTransactionId()
            );

        } else {

            System.out.println(
                    "Payment ID 1 not found."
            );
        }

        // -----------------------------------------
        // Test 3: Find payments by Bill ID
        // -----------------------------------------

        System.out.println("\n--- Payments for Bill ID 1 ---");

        List<Payment> billPayments =
                repository.findByBillId(1);

        System.out.println(
                "Payment count: " + billPayments.size()
        );

        for (Payment p : billPayments) {

            System.out.println(
                    "Payment ID: " + p.getPaymentId()
                            + " | Amount: " + p.getAmount()
                            + " | Method: " + p.getPaymentMethod()
                            + " | Status: " + p.getStatus()
            );
        }

        // -----------------------------------------
        // Test 4: Find payments by Patient ID
        // -----------------------------------------

        System.out.println("\n--- Payments for Patient ID 1 ---");

        List<Payment> patientPayments =
                repository.findByPatientId(1);

        System.out.println(
                "Payment count: " + patientPayments.size()
        );

        for (Payment p : patientPayments) {

            System.out.println(
                    "Payment ID: " + p.getPaymentId()
                            + " | Bill ID: " + p.getBillId()
                            + " | Amount: " + p.getAmount()
                            + " | Status: " + p.getStatus()
            );
        }

        System.out.println("\n======================================");
        System.out.println("   Payment Database Test Completed");
        System.out.println("======================================");
    }
}