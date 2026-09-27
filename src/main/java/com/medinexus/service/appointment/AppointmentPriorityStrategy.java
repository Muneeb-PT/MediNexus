package com.medinexus.service.appointment;

import com.medinexus.model.Appointment;

public interface AppointmentPriorityStrategy {

    int calculatePriority(Appointment appointment);
}