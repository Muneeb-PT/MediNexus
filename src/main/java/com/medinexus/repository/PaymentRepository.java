package com.medinexus.repository;

import com.medinexus.model.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

    private static final List<Payment> payments =
            new ArrayList<>();

    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    public Payment findById(int paymentId) {

        for (Payment payment : payments) {

            if (payment.getPaymentId() == paymentId) {
                return payment;
            }
        }

        return null;
    }

    public List<Payment> findByBillId(int billId) {

        List<Payment> result = new ArrayList<>();

        for (Payment payment : payments) {

            if (payment.getBillId() == billId) {
                result.add(payment);
            }
        }

        return result;
    }

    public void add(Payment payment) {

        if (payment != null) {
            payments.add(payment);
        }
    }

    public void update(Payment updatedPayment) {

        if (updatedPayment == null) {
            return;
        }

        for (int i = 0; i < payments.size(); i++) {

            if (payments.get(i).getPaymentId()
                    == updatedPayment.getPaymentId()) {

                payments.set(i, updatedPayment);
                return;
            }
        }
    }
}