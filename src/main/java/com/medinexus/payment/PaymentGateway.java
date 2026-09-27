package com.medinexus.payment;

public interface PaymentGateway {

    PaymentResult processPayment(
            double amount,
            String description
    );
}