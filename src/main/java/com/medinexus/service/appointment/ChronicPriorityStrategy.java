package com.medinexus.service.appointment;

import com.medinexus.model.Appointment;

public class ChronicPriorityStrategy
        implements AppointmentPriorityStrategy {

    @Override
    public int calculatePriority(Appointment appointment) {
        return 50;
    }
}