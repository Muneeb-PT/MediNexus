package com.medinexus.observer;

import com.medinexus.model.Appointment;

public interface AppointmentObserver {

    void onAppointmentChanged(
            Appointment appointment,
            String event
    );
}