package com.medinexus.controller;

import com.medinexus.model.Appointment;
import com.medinexus.model.AppointmentStatus;
import com.medinexus.model.Doctor;
import com.medinexus.model.Patient;
import com.medinexus.model.PriorityResult;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.AppointmentPriorityService;
import com.medinexus.service.AppointmentService;
import com.medinexus.service.DoctorService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAppointmentController {

    @FXML
    private ComboBox<String> patientComboBox;

    @FXML
    private ComboBox<String> doctorComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private TextArea reasonField;

    @FXML
    private ComboBox<String> urgencyComboBox;

    @FXML
    private Label priorityLabel;

    @FXML
    private Label messageLabel;

    private final AppointmentService appointmentService =
            new AppointmentService();

    private final AppointmentPriorityService appointmentPriorityService =
            new AppointmentPriorityService();

    private final PatientService patientService =
            new PatientService();

    private final DoctorService doctorService =
            new DoctorService();

    private final Map<String, Integer> patientIds =
            new HashMap<>();

    private final Map<String, Integer> doctorIds =
            new HashMap<>();

    /*
     * Actual patient ID belonging to the logged-in patient.
     *
     * Important:
     * user_id != necessarily patient_id.
     */
    private Integer currentPatientId;


    // ---------------------------------------------------------
    // INITIALIZATION
    // ---------------------------------------------------------

    @FXML
    public void initialize() {

        configureForCurrentUser();

        loadDoctors();
        loadTimes();
        loadUrgencies();

        setupListeners();

        datePicker.setValue(LocalDate.now());

        updatePriorityPreview();
        updateAvailableTimes();
    }


    // ---------------------------------------------------------
    // LOAD PATIENTS
    // ---------------------------------------------------------

    private void loadPatients() {

        patientComboBox.getItems().clear();
        patientIds.clear();

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        /*
         * PATIENT users must never load the complete patient list.
         */
        if (currentUser.getRole() == Role.PATIENT) {
            return;
        }

        /*
         * Only ADMIN and RECEPTIONIST can select
         * another patient.
         */
        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            return;
        }

        List<Patient> patients =
                patientService.getAllPatients();

        for (Patient patient : patients) {

            patientIds.put(
                    patient.getName(),
                    patient.getPatientId()
            );

            patientComboBox.getItems().add(
                    patient.getName()
            );
        }
    }


    // ---------------------------------------------------------
    // LOAD DOCTORS
    // ---------------------------------------------------------

    private void loadDoctors() {

        doctorComboBox.getItems().clear();
        doctorIds.clear();

        List<Doctor> doctors =
                doctorService.getAllDoctors();

        for (Doctor doctor : doctors) {

            if (!doctor.isAvailable()) {
                continue;
            }

            doctorIds.put(
                    doctor.getName(),
                    doctor.getDoctorId()
            );

            doctorComboBox.getItems().add(
                    doctor.getName()
            );
        }
    }


    // ---------------------------------------------------------
    // LOAD TIME SLOTS
    // ---------------------------------------------------------

    private void loadTimes() {

        timeComboBox.getItems().clear();

        timeComboBox.getItems().addAll(
                "09:00",
                "10:00",
                "11:00",
                "12:00",
                "14:00",
                "15:00",
                "16:00",
                "17:00"
        );
    }


    // ---------------------------------------------------------
    // LOAD URGENCY
    // ---------------------------------------------------------

    private void loadUrgencies() {

        urgencyComboBox.getItems().clear();

        urgencyComboBox.getItems().addAll(
                "Routine",
                "Chronic",
                "Emergency"
        );
    }


    // ---------------------------------------------------------
    // LISTENERS
    // ---------------------------------------------------------

    private void setupListeners() {

        urgencyComboBox.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updatePriorityPreview()
                );

        reasonField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updatePriorityPreview()
                );

        doctorComboBox.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateAvailableTimes()
                );

        datePicker.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateAvailableTimes()
                );
    }


    // ---------------------------------------------------------
    // PRIORITY PREVIEW
    // ---------------------------------------------------------

    private void updatePriorityPreview() {

        String urgency =
                urgencyComboBox.getValue();

        String reason =
                reasonField.getText();

        if (urgency == null) {

            priorityLabel.setText(
                    "Priority: -"
            );

            return;
        }

        Appointment temporaryAppointment =
                new Appointment(
                        0,
                        currentPatientId == null
                                ? 0
                                : currentPatientId,
                        0,
                        "",
                        "",
                        LocalDate.now(),
                        LocalTime.NOON,
                        reason == null ? "" : reason,
                        urgency,
                        AppointmentStatus.SCHEDULED
                );

        PriorityResult result =
                appointmentPriorityService
                        .calculatePriority(
                                temporaryAppointment
                        );

        priorityLabel.setText(
                "Priority: "
                        + result.getCategory()
                        + " (Score: "
                        + result.getScore()
                        + ")"
        );
    }


    // ---------------------------------------------------------
    // AVAILABLE TIME SLOTS
    // ---------------------------------------------------------

    private void updateAvailableTimes() {

        String doctorName =
                doctorComboBox.getValue();

        LocalDate date =
                datePicker.getValue();

        if (doctorName == null || date == null) {
            return;
        }

        Integer doctorId =
                doctorIds.get(doctorName);

        if (doctorId == null) {
            return;
        }

        timeComboBox.getItems().clear();

        String[] availableTimes = {
                "09:00",
                "10:00",
                "11:00",
                "12:00",
                "14:00",
                "15:00",
                "16:00",
                "17:00"
        };

        for (String time : availableTimes) {

            LocalTime localTime =
                    LocalTime.parse(time);

            if (appointmentService.isDoctorAvailable(
                    doctorId,
                    date,
                    localTime
            )) {

                timeComboBox.getItems().add(time);
            }
        }

        timeComboBox.setValue(null);

        if (timeComboBox.getItems().isEmpty()) {

            messageLabel.setText(
                    "No available time slots for this doctor on the selected date."
            );

        } else {

            messageLabel.setText(
                    "Available time slots updated."
            );
        }
    }


    // ---------------------------------------------------------
    // CONFIGURE FOR CURRENT USER
    // ---------------------------------------------------------

    private void configureForCurrentUser() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        Role role =
                currentUser.getRole();


        /*
         * ADMIN / RECEPTIONIST
         *
         * Can create appointments for any patient.
         */
        if (role == Role.ADMIN
                || role == Role.RECEPTIONIST) {

            loadPatients();

            patientComboBox.setDisable(false);

            return;
        }


        /*
         * PATIENT
         *
         * Automatically use the logged-in patient's
         * own patient record.
         */
        if (role == Role.PATIENT) {

            Patient patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient != null) {

                currentPatientId =
                        patient.getPatientId();

                patientIds.clear();

                patientIds.put(
                        patient.getName(),
                        patient.getPatientId()
                );

                patientComboBox.getItems().clear();

                patientComboBox.getItems().add(
                        patient.getName()
                );

                patientComboBox.setValue(
                        patient.getName()
                );

                patientComboBox.setDisable(true);

            } else {

                messageLabel.setText(
                        "No patient profile is linked to your account."
                );
            }

            return;
        }


        /*
         * DOCTOR
         *
         * Doctors cannot create appointments.
         */
        if (role == Role.DOCTOR) {

            patientComboBox.setDisable(true);
            doctorComboBox.setDisable(true);
            datePicker.setDisable(true);
            timeComboBox.setDisable(true);
            reasonField.setDisable(true);
            urgencyComboBox.setDisable(true);

            messageLabel.setText(
                    "Doctors cannot create appointments from this screen."
            );
        }
    }


    // ---------------------------------------------------------
    // CREATE APPOINTMENT
    // ---------------------------------------------------------

    @FXML
    private void handleCreateAppointment() {

        messageLabel.setText("");

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {

            messageLabel.setText(
                    "Please log in before creating an appointment."
            );

            return;
        }


        /*
         * Doctors are not allowed to create appointments.
         */
        if (currentUser.getRole() == Role.DOCTOR) {

            messageLabel.setText(
                    "Doctors cannot create appointments."
            );

            return;
        }


        String patientName =
                patientComboBox.getValue();

        String doctorName =
                doctorComboBox.getValue();

        LocalDate date =
                datePicker.getValue();

        String time =
                timeComboBox.getValue();

        String reason =
                reasonField.getText() == null
                        ? ""
                        : reasonField.getText().trim();

        String urgency =
                urgencyComboBox.getValue();


        // ---------------- VALIDATION ----------------

        if (patientName == null
                || doctorName == null
                || date == null
                || time == null
                || reason.isEmpty()
                || urgency == null) {

            messageLabel.setText(
                    "Please fill in all appointment details."
            );

            return;
        }


        // ---------------- DATE VALIDATION ----------------

        if (date.isBefore(LocalDate.now())) {

            messageLabel.setText(
                    "Appointment date cannot be in the past."
            );

            return;
        }


        Integer patientId;


        /*
         * PATIENT:
         *
         * Never trust the ComboBox value for ownership.
         * Use the patient record linked to the logged-in user.
         */
        if (currentUser.getRole() == Role.PATIENT) {

            patientId =
                    currentPatientId;

            if (patientId == null) {

                Patient patient =
                        patientService.getPatientByUserId(
                                currentUser.getUserId()
                        );

                if (patient == null) {

                    messageLabel.setText(
                            "Unable to find your patient profile."
                    );

                    return;
                }

                patientId =
                        patient.getPatientId();

                currentPatientId =
                        patientId;
            }

        } else {

            patientId =
                    patientIds.get(patientName);
        }


        Integer doctorId =
                doctorIds.get(doctorName);

        if (patientId == null || doctorId == null) {

            messageLabel.setText(
                    "Invalid patient or doctor selection."
            );

            return;
        }


        // ---------------- TIME ----------------

        LocalTime appointmentTime =
                LocalTime.parse(time);


        // ---------------- AVAILABILITY ----------------

        if (!appointmentService.isDoctorAvailable(
                doctorId,
                date,
                appointmentTime
        )) {

            messageLabel.setText(
                    "Selected doctor is not available at this time."
            );

            updateAvailableTimes();

            return;
        }


        // ---------------- CREATE OBJECT ----------------

        /*
         * appointment_id = 0 here.
         *
         * The value is NOT inserted into the database.
         * AppointmentJdbcRepository.add() allows MySQL
         * AUTO_INCREMENT to generate the real ID.
         */
        Appointment appointment =
                new Appointment(
                        0,
                        patientId,
                        doctorId,
                        patientName,
                        doctorName,
                        date,
                        appointmentTime,
                        reason,
                        urgency,
                        AppointmentStatus.SCHEDULED
                );


        // ---------------- PRIORITY ----------------

        PriorityResult priorityResult =
                appointmentPriorityService
                        .calculatePriority(
                                appointment
                        );


        // ---------------- SAVE ----------------

        try {

            appointmentService.createAppointment(
                    appointment
            );

        } catch (Exception e) {

            messageLabel.setText(
                    "Unable to create appointment: "
                            + e.getMessage()
            );

            return;
        }


        // ---------------- SUCCESS MESSAGE ----------------

        messageLabel.setText(
                "Appointment #"
                        + appointment.getAppointmentId()
                        + " booked successfully! "
                        + "Priority: "
                        + priorityResult.getCategory()
                        + " | Score: "
                        + priorityResult.getScore()
        );


        // ---------------- REFRESH ----------------

        updateAvailableTimes();
    }


    // ---------------------------------------------------------
    // CLEAR FORM
    // ---------------------------------------------------------

    @FXML
    private void handleClear() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser != null
                && currentUser.getRole()
                == Role.PATIENT) {

            /*
             * Patient must always remain linked to their own
             * patient record.
             */
            Patient patient =
                    patientService.getPatientByUserId(
                            currentUser.getUserId()
                    );

            if (patient != null) {

                currentPatientId =
                        patient.getPatientId();

                patientComboBox.setValue(
                        patient.getName()
                );
            }

        } else {

            patientComboBox.setValue(null);
        }

        doctorComboBox.setValue(null);

        datePicker.setValue(
                LocalDate.now()
        );

        timeComboBox.setValue(null);

        reasonField.clear();

        urgencyComboBox.setValue(null);

        priorityLabel.setText(
                "Priority: -"
        );

        messageLabel.setText("");

        updateAvailableTimes();
    }
}