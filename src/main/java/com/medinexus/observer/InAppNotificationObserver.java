package com.medinexus.observer;

import com.medinexus.model.Appointment;
import com.medinexus.model.Patient;
import com.medinexus.service.NotificationService;
import com.medinexus.service.PatientService;

public class InAppNotificationObserver
        implements AppointmentObserver {

    private final NotificationService notificationService =
            new NotificationService();

    private final PatientService patientService =
            new PatientService();

    @Override
    public void onAppointmentChanged(
            Appointment appointment,
            String event) {

        if (appointment == null) {
            return;
        }

        /*
         * Appointment stores patientId.
         *
         * notifications.user_id is a foreign key to users.user_id,
         * so we must resolve the Patient first and use Patient.userId.
         */
        Patient patient =
                patientService.getPatientById(
                        appointment.getPatientId()
                );

        if (patient == null) {
            System.err.println(
                    "[IN-APP NOTIFICATION] Patient not found: "
                            + appointment.getPatientId()
            );
            return;
        }

        if (patient.getUserId() <= 0) {
            System.err.println(
                    "[IN-APP NOTIFICATION] Invalid user ID for patient: "
                            + patient.getPatientId()
            );
            return;
        }

        String title =
                "Appointment " + event;

        String message =
                buildMessage(
                        appointment,
                        event
                );

        notificationService.createNotification(
                patient.getUserId(),
                title,
                message,
                "APPOINTMENT"
        );
    }

    private String buildMessage(
            Appointment appointment,
            String event) {

        if (event == null) {
            event = "";
        }

        return switch (event.toUpperCase()) {

            case "CREATED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been created for "
                            + appointment.getAppointmentDate()
                            + " at "
                            + appointment.getAppointmentTime()
                            + ".";

            case "UPDATED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been updated.";

            case "CONFIRMED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been confirmed.";

            case "CANCELLED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been cancelled.";

            case "COMPLETED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been completed.";

            case "MISSED" ->
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " has been marked as missed.";

            default ->
                    "Your appointment status has changed.";
        };
    }
}