package com.medinexus.payment;

public class PaymentResult {

    private final boolean successful;
    private final String transactionId;
    private final String message;

    public PaymentResult(
            boolean successful,
            String transactionId,
            String message) {

        this.successful = successful;
        this.transactionId = transactionId;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMessage() {
        return message;
    }
}