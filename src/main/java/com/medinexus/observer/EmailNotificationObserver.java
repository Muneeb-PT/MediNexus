package com.medinexus.observer;

import com.medinexus.model.Appointment;

public class EmailNotificationObserver
        implements AppointmentObserver {

    @Override
    public void onAppointmentChanged(
            Appointment appointment,
            String event) {

        if (appointment == null) {
            return;
        }

        System.out.println(
                "[EMAIL NOTIFICATION]"
        );

        System.out.println(
                "Event: " + event
        );

        System.out.println(
                "Patient ID: "
                        + appointment.getPatientId()
        );

        System.out.println(
                "Doctor: "
                        + appointment.getDoctorName()
        );
    }
}