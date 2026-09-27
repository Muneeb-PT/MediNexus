package com.medinexus;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;
import com.medinexus.service.PriorityService;
import com.medinexus.strategy.RuleBasedPriorityStrategy;
import com.medinexus.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class PriorityTest {

    public static void main(String[] args) {

        PriorityService priorityService =
                new PriorityService(
                        new RuleBasedPriorityStrategy()
                );

        Appointment routine =
                new Appointment(
                        1001,
                        1,
                        101,
                        "Mohammed Muneeb",
                        "Dr. Ahmed",
                        LocalDate.now(),
                        LocalTime.of(10, 30),
                        "Regular check-up",
                        "Routine",
                        AppointmentStatus.SCHEDULED
                );

        Appointment chronic =
                new Appointment(
                        1002,
                        2,
                        102,
                        "Aisha Rahman",
                        "Dr. Fathima",
                        LocalDate.now(),
                        LocalTime.of(11, 0),
                        "Follow-up",
                        "Chronic",
                        AppointmentStatus.SCHEDULED
                );

        Appointment emergency =
                new Appointment(
                        1003,
                        3,
                        101,
                        "Ahmed Ali",
                        "Dr. Ahmed",
                        LocalDate.now(),
                        LocalTime.of(11, 30),
                        "Urgent symptoms",
                        "Emergency",
                        AppointmentStatus.SCHEDULED
                );

        testAppointment(
                priorityService,
                routine
        );

        testAppointment(
                priorityService,
                chronic
        );

        testAppointment(
                priorityService,
                emergency
        );
    }

    private static void testAppointment(
            PriorityService service,
            Appointment appointment) {

        PriorityResult result =
                service.calculatePriority(
                        appointment
                );

        System.out.println(
                "Patient: "
                        + appointment.getPatientName()
        );

        System.out.println(
                "Urgency: "
                        + appointment.getUrgency()
        );

        System.out.println(
                "Reason: "
                        + appointment.getReason()
        );

        System.out.println(
                "Priority Score: "
                        + result.getScore()
        );

        System.out.println(
                "Category: "
                        + result.getCategory()
        );

        System.out.println(
                "Explanation: "
                        + result.getExplanation()
        );

        System.out.println(
                "================================"
        );
    }
}