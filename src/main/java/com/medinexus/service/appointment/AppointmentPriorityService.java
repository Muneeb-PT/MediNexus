package com.medinexus.service.appointment;

import com.medinexus.model.Appointment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppointmentPriorityService {

    public List<Appointment> sortByPriority(
            List<Appointment> appointments) {

        if (appointments == null || appointments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Appointment> sortedAppointments =
                new ArrayList<>(appointments);

        sortedAppointments.sort(
                Comparator
                        .comparingInt(this::calculatePriority)
                        .reversed()
                        .thenComparing(
                                Appointment::getAppointmentDate
                        )
                        .thenComparing(
                                Appointment::getAppointmentTime
                        )
        );

        return sortedAppointments;
    }

    public int calculatePriority(Appointment appointment) {

        if (appointment == null
                || appointment.getUrgency() == null) {
            return 0;
        }

        String urgency =
                appointment.getUrgency()
                        .trim()
                        .toUpperCase();

        AppointmentPriorityStrategy strategy;

        switch (urgency) {

            case "EMERGENCY":
                strategy =
                        new EmergencyPriorityStrategy();
                break;

            case "CHRONIC":
                strategy =
                        new ChronicPriorityStrategy();
                break;

            case "ROUTINE":
                strategy =
                        new RoutinePriorityStrategy();
                break;

            default:
                return 0;
        }

        return strategy.calculatePriority(appointment);
    }
}