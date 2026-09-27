package com.medinexus.service;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class AppointmentPriorityService {

    /*
     * Base clinical severity scores.
     */
    private static final int EMERGENCY_SCORE = 100;
    private static final int CHRONIC_SCORE = 50;
    private static final int ROUTINE_SCORE = 10;

    /*
     * Maximum waiting-time bonus.
     *
     * This prevents waiting time from completely overpowering
     * clinical severity.
     */
    private static final int MAX_WAITING_BONUS = 30;

    /*
     * Waiting-time escalation:
     *
     * Every 30 minutes contributes one point,
     * up to MAX_WAITING_BONUS.
     */
    private static final long WAITING_INTERVAL_MINUTES = 30;

    /**
     * Calculates the basic clinical priority of an appointment.
     *
     * EMERGENCY = 100
     * CHRONIC   = 50
     * ROUTINE   = 10
     */
    public PriorityResult calculatePriority(
            Appointment appointment) {

        if (appointment == null) {

            return new PriorityResult(
                    0,
                    "UNKNOWN",
                    "Appointment information is not available."
            );
        }

        if (appointment.getUrgency() == null) {

            return new PriorityResult(
                    0,
                    "UNKNOWN",
                    "No urgency level was specified."
            );
        }

        String urgency =
                appointment.getUrgency()
                        .toString()
                        .trim()
                        .toUpperCase();

        return switch (urgency) {

            case "EMERGENCY" -> new PriorityResult(
                    EMERGENCY_SCORE,
                    "EMERGENCY",
                    "Emergency appointment requiring immediate attention."
            );

            case "CHRONIC" -> new PriorityResult(
                    CHRONIC_SCORE,
                    "CHRONIC",
                    "Chronic condition requiring priority medical attention."
            );

            case "ROUTINE" -> new PriorityResult(
                    ROUTINE_SCORE,
                    "ROUTINE",
                    "Routine appointment with normal priority."
            );

            default -> new PriorityResult(
                    0,
                    "UNKNOWN",
                    "Unknown urgency level."
            );
        };
    }

    /**
     * Calculates the waiting-time bonus.
     *
     * The appointment's scheduled date/time is used as the
     * reference point.
     *
     * Future appointments receive no waiting-time bonus.
     */
    public int calculateWaitingTimeBonus(
            Appointment appointment) {

        if (appointment == null) {
            return 0;
        }

        LocalDate appointmentDate =
                appointment.getAppointmentDate();

        LocalTime appointmentTime =
                appointment.getAppointmentTime();

        if (appointmentDate == null
                || appointmentTime == null) {

            return 0;
        }

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(
                        appointmentDate,
                        appointmentTime
                );

        LocalDateTime now =
                LocalDateTime.now();

        if (appointmentDateTime.isAfter(now)) {
            return 0;
        }

        long waitingMinutes =
                Duration.between(
                        appointmentDateTime,
                        now
                ).toMinutes();

        if (waitingMinutes <= 0) {
            return 0;
        }

        long bonus =
                waitingMinutes
                        / WAITING_INTERVAL_MINUTES;

        return (int) Math.min(
                bonus,
                MAX_WAITING_BONUS
        );
    }

    /**
     * Calculates the final smart triage score.
     *
     * Final Score =
     * Clinical Severity + Waiting-Time Bonus
     */
    public int calculateTriageScore(
            Appointment appointment) {

        PriorityResult priority =
                calculatePriority(appointment);

        int waitingBonus =
                calculateWaitingTimeBonus(
                        appointment
                );

        return priority.getScore()
                + waitingBonus;
    }

    /**
     * Returns a human-readable explanation of
     * how the final triage score was calculated.
     */
    public String explainTriageScore(
            Appointment appointment) {

        if (appointment == null) {
            return "Appointment information is unavailable.";
        }

        PriorityResult priority =
                calculatePriority(appointment);

        int waitingBonus =
                calculateWaitingTimeBonus(
                        appointment
                );

        int finalScore =
                priority.getScore()
                        + waitingBonus;

        return String.format(
                "%s priority: %d + waiting-time bonus: %d = final triage score: %d",
                priority.getCategory(),
                priority.getScore(),
                waitingBonus,
                finalScore
        );
    }

    /**
     * Sorts appointments using the Smart Triage Engine.
     *
     * Sorting order:
     *
     * 1. Final triage score - highest first
     * 2. Clinical severity - highest first
     * 3. Appointment date - earliest first
     * 4. Appointment time - earliest first
     */
    public List<Appointment> sortByPriority(
            List<Appointment> appointments) {

        if (appointments == null
                || appointments.isEmpty()) {

            return appointments;
        }

        Comparator<Appointment> triageComparator =
                Comparator.comparingInt(
                        this::calculateTriageScore
                ).reversed();

        triageComparator =
                triageComparator.thenComparing(
                        Comparator.comparingInt(
                                (Appointment appointment) ->
                                        calculatePriority(
                                                appointment
                                        ).getScore()
                        ).reversed()
                );

        triageComparator =
                triageComparator.thenComparing(
                        Appointment::getAppointmentDate,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );

        triageComparator =
                triageComparator.thenComparing(
                        Appointment::getAppointmentTime,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );

        appointments.sort(
                triageComparator
        );

        return appointments;
    }
}