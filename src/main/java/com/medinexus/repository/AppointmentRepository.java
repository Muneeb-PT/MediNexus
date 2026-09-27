package com.medinexus.repository;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    private static final List<Appointment> appointments =
            new ArrayList<>();

    public AppointmentRepository() {

        if (appointments.isEmpty()) {
            loadSampleAppointments();
        }
    }

    private void loadSampleAppointments() {

        appointments.add(
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
                )
        );

        appointments.add(
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
                )
        );

        appointments.add(
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
                )
        );
    }

    public List<Appointment> findAll() {

        return new ArrayList<>(appointments);
    }

    public List<Appointment> findByPatientId(
            int patientId) {

        List<Appointment> result =
                new ArrayList<>();

        for (Appointment appointment : appointments) {

            if (appointment.getPatientId() == patientId) {
                result.add(appointment);
            }
        }

        return result;
    }

    public List<Appointment> findByDoctorId(
            int doctorId) {

        List<Appointment> result =
                new ArrayList<>();

        for (Appointment appointment : appointments) {

            if (appointment.getDoctorId() == doctorId) {
                result.add(appointment);
            }
        }

        return result;
    }
    public void add(
            Appointment appointment) {

        if (appointment != null) {
            appointments.add(appointment);
        }
    }
    public boolean cancelAppointment(
            int appointmentId) {

        for (Appointment appointment
                : appointments) {

            if (appointment.getAppointmentId()
                    == appointmentId) {

                appointment.setStatus(
                        AppointmentStatus.CANCELLED
                );

                return true;
            }
        }

        return false;
    }
    public boolean isDoctorAvailable(
            int doctorId,
            LocalDate date,
            LocalTime time) {

        for (Appointment appointment :
                appointments) {

            if (appointment.getDoctorId() == doctorId
                    && appointment.getAppointmentDate()
                    .equals(date)
                    && appointment.getAppointmentTime()
                    .equals(time)
                    && appointment.getStatus()
                    != AppointmentStatus.CANCELLED
                    && appointment.getStatus()
                    != AppointmentStatus.MISSED) {

                return false;
            }
        }

        return true;
    }


}