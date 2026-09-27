package com.medinexus.test;

import com.medinexus.model.Appointment;
import com.medinexus.model.Bill;
import com.medinexus.model.Doctor;
import com.medinexus.model.MedicalRecord;
import com.medinexus.model.Medicine;
import com.medinexus.model.Notification;
import com.medinexus.model.Patient;
import com.medinexus.model.Payment;
import com.medinexus.model.Prescription;

import com.medinexus.service.AppointmentService;
import com.medinexus.service.BillService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.MedicalRecordService;
import com.medinexus.service.MedicineService;
import com.medinexus.service.NotificationService;
import com.medinexus.service.PatientService;
import com.medinexus.service.PaymentService;
import com.medinexus.service.PrescriptionService;

import java.util.List;

public class SystemIntegrationTest {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("       MediNexus System Integration Test");
        System.out.println("==============================================");

        boolean allPassed = true;

        // =========================================
        // 1. PATIENT
        // =========================================

        System.out.println("\n[1] Patient Module");

        PatientService patientService =
                new PatientService();

        Patient patient =
                patientService.getPatientById(1);

        if (patient != null) {

            System.out.println(
                    "PASS - Patient found: "
                            + patient.getName()
            );

        } else {

            System.out.println(
                    "FAIL - Patient not found"
            );

            allPassed = false;
        }

        // =========================================
        // 2. DOCTOR
        // =========================================

        System.out.println("\n[2] Doctor Module");

        DoctorService doctorService =
                new DoctorService();

        Doctor doctor =
                doctorService.getDoctorById(101);

        if (doctor != null) {

            System.out.println(
                    "PASS - Doctor found: "
                            + doctor.getName()
            );

        } else {

            System.out.println(
                    "FAIL - Doctor not found"
            );

            allPassed = false;
        }

        // =========================================
        // 3. APPOINTMENT
        // =========================================

        System.out.println("\n[3] Appointment Module");

        AppointmentService appointmentService =
                new AppointmentService();

        List<Appointment> appointments =
                appointmentService.getPatientAppointments(1);

        if (appointments != null
                && !appointments.isEmpty()) {

            Appointment appointment =
                    appointments.get(0);

            System.out.println(
                    "PASS - Appointment found: "
                            + appointment.getAppointmentId()
            );

            System.out.println(
                    "      Doctor: "
                            + appointment.getDoctorName()
            );

            System.out.println(
                    "      Status: "
                            + appointment.getStatus()
            );

        } else {

            System.out.println(
                    "FAIL - No appointment found"
            );

            allPassed = false;
        }

        // =========================================
        // 4. MEDICAL RECORD
        // =========================================

        System.out.println("\n[4] Medical Record Module");

        MedicalRecordService medicalRecordService =
                new MedicalRecordService();

        List<MedicalRecord> records =
                medicalRecordService.getPatientRecords(1);

        if (records != null
                && !records.isEmpty()) {

            MedicalRecord record =
                    records.get(0);

            System.out.println(
                    "PASS - Medical record found: "
                            + record.getRecordId()
            );

            System.out.println(
                    "      Diagnosis: "
                            + record.getDiagnosis()
            );

        } else {

            System.out.println(
                    "FAIL - Medical record not found"
            );

            allPassed = false;
        }

        // =========================================
        // 5. PRESCRIPTION
        // =========================================

        System.out.println("\n[5] Prescription Module");

        PrescriptionService prescriptionService =
                new PrescriptionService();

        List<Prescription> prescriptions =
                prescriptionService
                        .getPatientPrescriptions(1);

        if (prescriptions != null
                && !prescriptions.isEmpty()) {

            Prescription prescription =
                    prescriptions.get(0);

            System.out.println(
                    "PASS - Prescription found: "
                            + prescription.getPrescriptionId()
            );

            System.out.println(
                    "      Status: "
                            + prescription.getStatus()
            );

        } else {

            System.out.println(
                    "FAIL - Prescription not found"
            );

            allPassed = false;
        }

        // =========================================
        // 6. MEDICINE
        // =========================================

        System.out.println("\n[6] Medicine Module");

        MedicineService medicineService =
                new MedicineService();

        Medicine medicine =
                medicineService.getMedicineById(1);

        if (medicine != null) {

            System.out.println(
                    "PASS - Medicine found: "
                            + medicine.getName()
            );

            System.out.println(
                    "      Stock: "
                            + medicine.getQuantity()
            );

        } else {

            System.out.println(
                    "FAIL - Medicine not found"
            );

            allPassed = false;
        }

        // =========================================
        // 7. BILLING
        // =========================================

        System.out.println("\n[7] Billing Module");

        BillService billService =
                new BillService();

        List<Bill> bills =
                billService.getPatientBills(1);

        if (bills != null
                && !bills.isEmpty()) {

            Bill bill =
                    bills.get(0);

            System.out.println(
                    "PASS - Bill found: "
                            + bill.getBillId()
            );

            System.out.println(
                    "      Total: "
                            + bill.getTotalAmount()
            );

            System.out.println(
                    "      Status: "
                            + bill.getPaymentStatus()
            );

        } else {

            System.out.println(
                    "FAIL - Bill not found"
            );

            allPassed = false;
        }

        // =========================================
        // 8. PAYMENT
        // =========================================

        System.out.println("\n[8] Payment Module");

        PaymentService paymentService =
                new PaymentService();

        List<Payment> payments =
                paymentService.getPatientPayments(1);

        if (payments != null
                && !payments.isEmpty()) {

            Payment payment =
                    payments.get(0);

            System.out.println(
                    "PASS - Payment found: "
                            + payment.getPaymentId()
            );

            System.out.println(
                    "      Amount: "
                            + payment.getAmount()
            );

            System.out.println(
                    "      Method: "
                            + payment.getPaymentMethod()
            );

            System.out.println(
                    "      Status: "
                            + payment.getStatus()
            );

        } else {

            System.out.println(
                    "FAIL - Payment not found"
            );

            allPassed = false;
        }

        // =========================================
        // 9. NOTIFICATIONS
        // =========================================

        System.out.println("\n[9] Notification Module");

        NotificationService notificationService =
                new NotificationService();

        List<Notification> notifications =
                notificationService
                        .getUserNotifications(1);

        if (notifications != null
                && !notifications.isEmpty()) {

            System.out.println(
                    "PASS - Notifications found: "
                            + notifications.size()
            );

            Notification notification =
                    notifications.get(0);

            System.out.println(
                    "      Latest: "
                            + notification.getTitle()
            );

        } else {

            System.out.println(
                    "FAIL - No notifications found"
            );

            allPassed = false;
        }

        // =========================================
        // FINAL RESULT
        // =========================================

        System.out.println(
                "\n=============================================="
        );

        if (allPassed) {

            System.out.println(
                    "       ALL INTEGRATION TESTS PASSED"
            );

        } else {

            System.out.println(
                    "       SOME INTEGRATION TESTS FAILED"
            );
        }

        System.out.println(
                "=============================================="
        );
    }
}