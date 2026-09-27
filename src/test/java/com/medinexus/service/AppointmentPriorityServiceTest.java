package com.medinexus.service;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentPriorityServiceTest {

    private final AppointmentPriorityService service =
            new AppointmentPriorityService();

    @Test
    void emergencyGetsHighestBasePriority() {

        Appointment appointment =
                createAppointment(
                        "EMERGENCY",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        PriorityResult result =
                service.calculatePriority(appointment);

        assertEquals(100, result.getScore());

        assertEquals(
                "EMERGENCY",
                result.getCategory()
        );
    }

    @Test
    void chronicGetsMediumBasePriority() {

        Appointment appointment =
                createAppointment(
                        "CHRONIC",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        PriorityResult result =
                service.calculatePriority(appointment);

        assertEquals(50, result.getScore());

        assertEquals(
                "CHRONIC",
                result.getCategory()
        );
    }

    @Test
    void routineGetsNormalBasePriority() {

        Appointment appointment =
                createAppointment(
                        "ROUTINE",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        PriorityResult result =
                service.calculatePriority(appointment);

        assertEquals(10, result.getScore());

        assertEquals(
                "ROUTINE",
                result.getCategory()
        );
    }

    @Test
    void nullAppointmentReturnsUnknownPriority() {

        PriorityResult result =
                service.calculatePriority(null);

        assertEquals(
                0,
                result.getScore()
        );

        assertEquals(
                "UNKNOWN",
                result.getCategory()
        );
    }

    @Test
    void missingUrgencyReturnsUnknownPriority() {

        Appointment appointment =
                createAppointment(
                        null,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        PriorityResult result =
                service.calculatePriority(appointment);

        assertEquals(
                0,
                result.getScore()
        );

        assertEquals(
                "UNKNOWN",
                result.getCategory()
        );
    }

    @Test
    void futureAppointmentGetsNoWaitingBonus() {

        Appointment appointment =
                createAppointment(
                        "ROUTINE",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        int bonus =
                service.calculateWaitingTimeBonus(
                        appointment
                );

        assertEquals(
                0,
                bonus
        );
    }

    @Test
    void oldAppointmentGetsWaitingBonus() {

        LocalDateTime past =
                LocalDateTime.now()
                        .minusHours(2);

        Appointment appointment =
                createAppointment(
                        "ROUTINE",
                        past.toLocalDate(),
                        past.toLocalTime()
                );

        int bonus =
                service.calculateWaitingTimeBonus(
                        appointment
                );

        assertTrue(
                bonus > 0,
                "An old appointment should receive a waiting-time bonus."
        );
    }

    @Test
    void waitingBonusIsCapped() {

        LocalDateTime past =
                LocalDateTime.now()
                        .minusDays(10);

        Appointment appointment =
                createAppointment(
                        "ROUTINE",
                        past.toLocalDate(),
                        past.toLocalTime()
                );

        int bonus =
                service.calculateWaitingTimeBonus(
                        appointment
                );

        assertEquals(
                30,
                bonus
        );
    }

    @Test
    void emergencyGetsHigherTriageScoreThanRoutineAtSameTime() {

        LocalDateTime past =
                LocalDateTime.now()
                        .minusHours(1);

        Appointment emergency =
                createAppointment(
                        "EMERGENCY",
                        past.toLocalDate(),
                        past.toLocalTime()
                );

        Appointment routine =
                createAppointment(
                        "ROUTINE",
                        past.toLocalDate(),
                        past.toLocalTime()
                );

        int emergencyScore =
                service.calculateTriageScore(
                        emergency
                );

        int routineScore =
                service.calculateTriageScore(
                        routine
                );

        assertTrue(
                emergencyScore > routineScore
        );
    }

    @Test
    void triageScoreIncludesWaitingBonus() {

        LocalDateTime past =
                LocalDateTime.now()
                        .minusHours(2);

        Appointment appointment =
                createAppointment(
                        "ROUTINE",
                        past.toLocalDate(),
                        past.toLocalTime()
                );

        PriorityResult priority =
                service.calculatePriority(
                        appointment
                );

        int waitingBonus =
                service.calculateWaitingTimeBonus(
                        appointment
                );

        int triageScore =
                service.calculateTriageScore(
                        appointment
                );

        assertEquals(
                priority.getScore()
                        + waitingBonus,
                triageScore
        );
    }

    @Test
    void sortingPlacesHigherTriageScoreFirst() {

        LocalDate date =
                LocalDate.now().plusDays(1);

        Appointment routine =
                createAppointment(
                        "ROUTINE",
                        date,
                        LocalTime.of(10, 0)
                );

        Appointment chronic =
                createAppointment(
                        "CHRONIC",
                        date,
                        LocalTime.of(11, 0)
                );

        Appointment emergency =
                createAppointment(
                        "EMERGENCY",
                        date,
                        LocalTime.of(12, 0)
                );

        List<Appointment> appointments =
                new ArrayList<>(
                        List.of(
                                routine,
                                chronic,
                                emergency
                        )
                );

        service.sortByPriority(
                appointments
        );

        assertSame(
                emergency,
                appointments.get(0)
        );

        assertSame(
                chronic,
                appointments.get(1)
        );

        assertSame(
                routine,
                appointments.get(2)
        );
    }

    @Test
    void sortingUsesEarlierTimeWhenScoresAreEqual() {

        LocalDate date =
                LocalDate.now().plusDays(1);

        Appointment earlier =
                createAppointment(
                        "ROUTINE",
                        date,
                        LocalTime.of(9, 0)
                );

        Appointment later =
                createAppointment(
                        "ROUTINE",
                        date,
                        LocalTime.of(11, 0)
                );

        List<Appointment> appointments =
                new ArrayList<>(
                        List.of(
                                later,
                                earlier
                        )
                );

        service.sortByPriority(
                appointments
        );

        assertSame(
                earlier,
                appointments.get(0)
        );

        assertSame(
                later,
                appointments.get(1)
        );
    }

    @Test
    void emptyListRemainsEmpty() {

        List<Appointment> appointments =
                new ArrayList<>();

        List<Appointment> result =
                service.sortByPriority(
                        appointments
                );

        assertSame(
                appointments,
                result
        );

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void nullListReturnsNull() {

        assertNull(
                service.sortByPriority(null)
        );
    }

    @Test
    void explanationContainsTriageInformation() {

        Appointment appointment =
                createAppointment(
                        "EMERGENCY",
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                );

        String explanation =
                service.explainTriageScore(
                        appointment
                );

        assertTrue(
                explanation.contains(
                        "EMERGENCY"
                )
        );

        assertTrue(
                explanation.contains(
                        "final triage score"
                )
        );
    }

    private Appointment createAppointment(
            String urgency,
            LocalDate date,
            LocalTime time) {

        Appointment appointment =
                new Appointment();

        appointment.setUrgency(
                urgency
        );

        appointment.setAppointmentDate(
                date
        );

        appointment.setAppointmentTime(
                time
        );

        return appointment;
    }
}