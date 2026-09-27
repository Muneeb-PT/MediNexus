package com.medinexus.service.appointment;

import com.medinexus.model.Appointment;

public class EmergencyPriorityStrategy
        implements AppointmentPriorityStrategy {

    @Override
    public int calculatePriority(Appointment appointment) {
        return 100;
    }
}