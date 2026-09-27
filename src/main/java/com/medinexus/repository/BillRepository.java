package com.medinexus.repository;

import com.medinexus.model.Bill;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillRepository {

    private static final List<Bill> bills =
            new ArrayList<>();

    public BillRepository() {

        if (bills.isEmpty()) {
            loadSampleBills();
        }
    }

    private void loadSampleBills() {

        bills.add(
                new Bill(
                        1,
                        1,
                        1001,
                        300.00,
                        150.00,
                        0.00,
                        450.00,
                        "PAID",
                        LocalDateTime.now().minusDays(10)
                )
        );

        bills.add(
                new Bill(
                        2,
                        2,
                        1002,
                        300.00,
                        50.00,
                        25.00,
                        375.00,
                        "PENDING",
                        LocalDateTime.now().minusDays(5)
                )
        );
    }

    public List<Bill> findAll() {
        return new ArrayList<>(bills);
    }

    public Bill findById(int billId) {

        for (Bill bill : bills) {

            if (bill.getBillId() == billId) {
                return bill;
            }
        }

        return null;
    }

    public List<Bill> findByPatientId(int patientId) {

        List<Bill> result = new ArrayList<>();

        for (Bill bill : bills) {

            if (bill.getPatientId() == patientId) {
                result.add(bill);
            }
        }

        return result;
    }

    public Bill findByAppointmentId(int appointmentId) {

        for (Bill bill : bills) {

            if (bill.getAppointmentId()
                    == appointmentId) {

                return bill;
            }
        }

        return null;
    }

    public void add(Bill bill) {

        if (bill != null) {
            bills.add(bill);
        }
    }

    public void update(Bill updatedBill) {

        if (updatedBill == null) {
            return;
        }

        for (int i = 0; i < bills.size(); i++) {

            if (bills.get(i).getBillId()
                    == updatedBill.getBillId()) {

                bills.set(i, updatedBill);
                return;
            }
        }
    }
}