package com.medinexus.payment;

import java.util.UUID;

public class DemoPaymentGateway
        implements PaymentGateway {

    @Override
    public PaymentResult processPayment(
            double amount,
            String description) {

        if (amount <= 0) {

            return new PaymentResult(
                    false,
                    null,
                    "Invalid payment amount."
            );
        }

        String transactionId =
                "DEMO-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        return new PaymentResult(
                true,
                transactionId,
                "Demo online payment successful."
        );
    }
}