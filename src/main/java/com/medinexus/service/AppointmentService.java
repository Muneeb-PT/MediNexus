package com.medinexus.service;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.observer.AppointmentEventManager;
import com.medinexus.observer.EmailNotificationObserver;
import com.medinexus.observer.InAppNotificationObserver;
import com.medinexus.observer.SMSNotificationObserver;
import com.medinexus.repository.AppointmentJdbcRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentService {

    private final AppointmentJdbcRepository appointmentRepository;
    private final AppointmentEventManager eventManager;
    private final AppointmentPriorityService priorityService;

    public AppointmentService() {

        appointmentRepository =
                new AppointmentJdbcRepository();

        eventManager =
                new AppointmentEventManager();

        priorityService =
                new AppointmentPriorityService();

        registerNotificationObservers();
    }

    // -----------------------------------------
    // Register notification observers
    // -----------------------------------------

    private void registerNotificationObservers() {

        eventManager.addObserver(
                new EmailNotificationObserver()
        );

        eventManager.addObserver(
                new SMSNotificationObserver()
        );

        eventManager.addObserver(
                new InAppNotificationObserver()
        );
    }

    // -----------------------------------------
    // Get appointments visible to current user
    // -----------------------------------------

    public List<Appointment> getAllAppointments() {

        User user = getCurrentUser();

        /*
         * Admin and Receptionist can access
         * all operational appointments.
         */
        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST) {

            return appointmentRepository.findAll();
        }

        /*
         * Doctor must NEVER receive all appointments.
         * Only appointments assigned to the logged-in doctor
         * are returned.
         */
        if (user.getRole() == Role.DOCTOR) {

            return appointmentRepository.findByDoctorId(
                    user.getUserId()
            );
        }

        /*
         * Patients must use getPatientAppointments()
         * so ownership can be verified against their
         * patient record.
         */
        throw new SecurityException(
                "You are not authorized to view these appointments."
        );
    }
// -----------------------------------------
// Smart Triage Queue
// -----------------------------------------

    /**
     * Returns appointments visible to the current
     * operational user, ordered by the Smart Triage Engine.
     *
     * Admin and Receptionist:
     *     All appointments.
     *
     * Doctor:
     *     Only appointments assigned to the
     *     logged-in doctor.
     *
     * Patient:
     *     Not allowed to access the operational
     *     triage queue.
     */
    public List<Appointment> getSmartTriageQueue() {

        User user = getCurrentUser();

        List<Appointment> appointments;

        /*
         * Admin and Receptionist can see the
         * complete operational queue.
         */
        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST) {

            appointments =
                    appointmentRepository.findAll();
        }

        /*
         * Doctor receives only their own appointments.
         */
        else if (user.getRole() == Role.DOCTOR) {

            appointments =
                    appointmentRepository.findByDoctorId(
                            user.getUserId()
                    );
        }

        /*
         * Patients must never access the
         * hospital-wide triage queue.
         */
        else {

            throw new SecurityException(
                    "Patients are not authorized to access the Smart Triage Queue."
            );
        }

        /*
         * The priority engine performs the actual
         * clinical ranking.
         */
        return priorityService.sortByPriority(
                appointments
        );
    }
    // -----------------------------------------
    // Get patient appointments
    // -----------------------------------------

    public List<Appointment> getPatientAppointments(
            int patientId) {

        User user = getCurrentUser();

        /*
         * Staff can access operational appointment
         * information for a selected patient.
         */
        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST) {

            return appointmentRepository.findByPatientId(
                    patientId
            );
        }

        /*
         * Patient can only access their own appointments.
         */
        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId() != patientId) {

                throw new SecurityException(
                        "Patients can only access their own appointments."
                );
            }

            return appointmentRepository.findByPatientId(
                    patientId
            );
        }

        /*
         * Doctor can only access appointments where BOTH:
         *
         * doctor_id  = logged-in doctor
         * patient_id = requested patient
         *
         * The filtering is performed by SQL.
         */
        if (user.getRole() == Role.DOCTOR) {

            return appointmentRepository
                    .findByDoctorAndPatient(
                            user.getUserId(),
                            patientId
                    );
        }

        throw new SecurityException(
                "You are not authorized to access appointments."
        );
    }

    // -----------------------------------------
    // Get doctor appointments
    // -----------------------------------------

    public List<Appointment> getDoctorAppointments(
            int doctorId) {

        User user = getCurrentUser();

        /*
         * Admin and Receptionist may select
         * another doctor for operational purposes.
         */
        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST) {

            return appointmentRepository.findByDoctorId(
                    doctorId
            );
        }

        /*
         * Doctor ownership is mandatory.
         */
        if (user.getRole() == Role.DOCTOR) {

            if (user.getUserId() != doctorId) {

                throw new SecurityException(
                        "Doctors can only access their own appointments."
                );
            }

            return appointmentRepository.findByDoctorId(
                    doctorId
            );
        }

        throw new SecurityException(
                "You are not authorized to access doctor appointments."
        );
    }

    // -----------------------------------------
    // Create appointment
    // -----------------------------------------

    public void createAppointment(
            Appointment appointment) {

        if (appointment == null) {

            throw new IllegalArgumentException(
                    "Appointment cannot be null."
            );
        }

        User user = getCurrentUser();

        Role role = user.getRole();

        /*
         * Only Admin, Receptionist and Patient
         * can create appointments.
         */
        if (role != Role.ADMIN
                && role != Role.RECEPTIONIST
                && role != Role.PATIENT) {

            throw new SecurityException(
                    "Doctors cannot create appointments."
            );
        }

        /*
         * Patient can only create an appointment
         * for their own patient account.
         */
        if (role == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId()
                    != appointment.getPatientId()) {

                throw new SecurityException(
                        "Patients can only create appointments for themselves."
                );
            }
        }

        /*
         * Check doctor availability before creating.
         */
        if (!isDoctorAvailable(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime())) {

            throw new IllegalStateException(
                    "Doctor is not available at the selected time."
            );
        }

        appointmentRepository.add(
                appointment
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "CREATED"
        );
    }

    // -----------------------------------------
    // Confirm appointment
    // -----------------------------------------

    public boolean confirmAppointment(
            int appointmentId) {

        Appointment appointment =
                appointmentRepository.findById(
                        appointmentId
                );

        if (appointment == null) {
            return false;
        }

        User user = getCurrentUser();

        /*
         * Only Admin, Receptionist and the assigned
         * Doctor can confirm an appointment.
         */
        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST
                && !(user.getRole() == Role.DOCTOR
                && user.getUserId()
                == appointment.getDoctorId())) {

            throw new SecurityException(
                    "You are not authorized to confirm this appointment."
            );
        }

        if (appointment.getStatus()
                != AppointmentStatus.SCHEDULED) {

            return false;
        }

        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        appointmentRepository.update(
                appointment
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "CONFIRMED"
        );

        return true;
    }

    // -----------------------------------------
    // Complete appointment
    // -----------------------------------------

    public boolean completeAppointment(
            int appointmentId) {

        Appointment appointment =
                appointmentRepository.findById(
                        appointmentId
                );

        if (appointment == null) {
            return false;
        }

        User user = getCurrentUser();

        /*
         * Only Admin, Receptionist and the assigned
         * Doctor can complete an appointment.
         */
        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST
                && !(user.getRole() == Role.DOCTOR
                && user.getUserId()
                == appointment.getDoctorId())) {

            throw new SecurityException(
                    "You are not authorized to complete this appointment."
            );
        }

        if (appointment.getStatus()
                != AppointmentStatus.CONFIRMED) {

            return false;
        }

        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        appointmentRepository.update(
                appointment
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "COMPLETED"
        );

        return true;
    }

    // -----------------------------------------
    // Cancel appointment
    // -----------------------------------------

    public boolean cancelAppointment(
            int appointmentId) {

        Appointment appointment =
                appointmentRepository.findById(
                        appointmentId
                );

        if (appointment == null) {
            return false;
        }

        User user = getCurrentUser();

        boolean authorized = false;

        /*
         * Staff can cancel operational appointments.
         */
        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST) {

            authorized = true;
        }

        /*
         * Patient can cancel only their own appointment.
         */
        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            authorized =
                    patient != null
                            && patient.getPatientId()
                            == appointment.getPatientId();
        }

        /*
         * Doctor can cancel only their own
         * assigned appointment.
         */
        if (user.getRole() == Role.DOCTOR) {

            authorized =
                    user.getUserId()
                            == appointment.getDoctorId();
        }

        if (!authorized) {

            throw new SecurityException(
                    "You are not authorized to cancel this appointment."
            );
        }

        AppointmentStatus status =
                appointment.getStatus();

        if (status == AppointmentStatus.COMPLETED
                || status == AppointmentStatus.CANCELLED
                || status == AppointmentStatus.MISSED) {

            return false;
        }

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointmentRepository.update(
                appointment
        );

        eventManager.notifyAppointmentChanged(
                appointment,
                "CANCELLED"
        );

        return true;
    }

    // -----------------------------------------
    // Check doctor availability
    // -----------------------------------------

    public boolean isDoctorAvailable(
            int doctorId,
            LocalDate date,
            LocalTime time) {

        return appointmentRepository.isDoctorAvailable(
                doctorId,
                date,
                time
        );
    }

    // -----------------------------------------
    // Get observer count
    // -----------------------------------------

    public int getNotificationObserverCount() {

        return eventManager.getObserverCount();
    }

    // -----------------------------------------
    // Security helpers
    // -----------------------------------------

    private User getCurrentUser() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {

            throw new SecurityException(
                    "You must be logged in to access appointments."
            );
        }

        return user;
    }
}