package com.medinexus.controller;

import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.DashboardService;
import com.medinexus.service.NotificationService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    // =========================================================
    // HEADER
    // =========================================================

    @FXML
    private Label userLabel;

    @FXML
    private Label dashboardGreetingLabel;

    @FXML
    private Label dashboardSubtitleLabel;


    // =========================================================
    // DASHBOARD STATISTICS
    // =========================================================

    @FXML
    private Label patientCountLabel;

    @FXML
    private Label doctorCountLabel;

    @FXML
    private Label appointmentCountLabel;

    @FXML
    private Label medicineCountLabel;

    @FXML
    private Label medicineInventoryTotalLabel;

    @FXML
    private Label lowStockCountLabel;

    @FXML
    private Label outOfStockCountLabel;

    @FXML
    private Label dashboardStatusLabel;


    // =========================================================
    // COMMON / SERVICE BUTTONS
    // =========================================================

    @FXML
    private Button profileButton;

    @FXML
    private Button onlinePaymentButton;

    @FXML
    private Button notificationsButton;

    @FXML
    private Button emergencyButton;

    @FXML
    private Button identityButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button aiButton;

    @FXML
    private Button feedbackButton;

    @FXML
    private Button supportTicketsButton;


    // =========================================================
    // SIDEBAR BUTTONS
    // =========================================================

    @FXML
    private VBox contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button patientsButton;

    @FXML
    private Button doctorsButton;

    @FXML
    private Button appointmentsButton;

    @FXML
    private Button medicinesButton;

    @FXML
    private Button billingButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button medicalRecordsButton;

    @FXML
    private Button prescriptionsButton;

    @FXML
    private Button paymentHistoryButton;


    // =========================================================
    // QUICK ACTION BUTTONS
    // =========================================================

    @FXML
    private Button quickAppointmentButton;

    @FXML
    private Button quickRecordsButton;

    @FXML
    private Button quickPrescriptionButton;

    @FXML
    private Button quickBillingButton;

    @FXML
    private Button quickNotificationsButton;

    @FXML
    private Button quickInventoryButton;


    // =========================================================
    // RECENT APPOINTMENTS TABLE
    // =========================================================

    @FXML
    private TableView<DashboardService.DashboardAppointment>
            recentAppointmentsTable;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, Integer>
            recentAppointmentIdColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, Long>
            recentPatientColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, Long>
            recentDoctorColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, LocalDate>
            recentDateColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, LocalTime>
            recentTimeColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, String>
            recentUrgencyColumn;

    @FXML
    private TableColumn<DashboardService.DashboardAppointment, String>
            recentStatusColumn;


    // =========================================================
    // INTERNAL STATE
    // =========================================================

    private Role userRole;

    private final DashboardService dashboardService =
            new DashboardService();

    private final NotificationService notificationService =
            new NotificationService();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    private static DashboardController activeDashboardController;

    // Keep the original dashboard nodes so Back/Dashboard navigation can restore them.
    private final List<Node> dashboardContentNodes = new ArrayList<>();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        activeDashboardController = this;

        if (contentArea != null) {
            dashboardContentNodes.clear();
            dashboardContentNodes.addAll(contentArea.getChildren());
        }

        User currentUser =
                SessionManager.getCurrentUser();

        boolean canManageSupportTickets =
                currentUser != null
                        && (currentUser.getRole() == Role.ADMIN
                        || currentUser.getRole() == Role.RECEPTIONIST);

        setButtonVisible(
                supportTicketsButton,
                canManageSupportTickets
        );
    }


    // =========================================================
    // SET USER ROLE
    // =========================================================

    public void setUserRole(Role role) {

        this.userRole = role;

        if (role == null) {
            return;
        }

        configureSidebar();
        configureDashboardForRole();

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser != null) {

            String name =
                    currentUser.getFullName();

            if (name == null || name.isBlank()) {
                name = formatRole(role);
            }

            if (userLabel != null) {
                userLabel.setText(name);
            }

            if (dashboardGreetingLabel != null) {
                dashboardGreetingLabel.setText(
                        "Welcome back, " + name
                );
            }
        }

        showDashboard();
    }


    // =========================================================
    // FORMAT ROLE
    // =========================================================

    private String formatRole(Role role) {

        if (role == null) {
            return "User";
        }

        String roleText =
                role.name().toLowerCase();

        return roleText.substring(0, 1).toUpperCase()
                + roleText.substring(1);
    }


    // =========================================================
    // ROLE DASHBOARD CONFIGURATION
    // =========================================================

    private void configureDashboardForRole() {

        if (dashboardSubtitleLabel == null
                || userRole == null) {

            return;
        }

        switch (userRole) {

            case ADMIN ->
                    dashboardSubtitleLabel.setText(
                            "Hospital administration overview and system operations."
                    );

            case DOCTOR ->
                    dashboardSubtitleLabel.setText(
                            "Your clinical overview, appointments and patient care."
                    );

            case RECEPTIONIST ->
                    dashboardSubtitleLabel.setText(
                            "Patient services, appointments and billing overview."
                    );

            case PATIENT ->
                    dashboardSubtitleLabel.setText(
                            "Your appointments, medical records and healthcare services."
                    );
        }
    }


    // =========================================================
    // SIDEBAR CONFIGURATION
    // =========================================================

    private void configureSidebar() {

        setButtonVisible(dashboardButton, false);
        setButtonVisible(profileButton, false);

        setButtonVisible(patientsButton, false);
        setButtonVisible(doctorsButton, false);
        setButtonVisible(appointmentsButton, false);
        setButtonVisible(medicinesButton, false);
        setButtonVisible(billingButton, false);
        setButtonVisible(reportsButton, false);
        setButtonVisible(settingsButton, false);

        setButtonVisible(onlinePaymentButton, false);
        setButtonVisible(medicalRecordsButton, false);
        setButtonVisible(prescriptionsButton, false);
        setButtonVisible(paymentHistoryButton, false);

        setButtonVisible(notificationsButton, false);
        setButtonVisible(emergencyButton, false);
        setButtonVisible(identityButton, false);
        setButtonVisible(helpButton, false);
        setButtonVisible(aiButton, false);
        setButtonVisible(feedbackButton, false);
        setButtonVisible(supportTicketsButton, false);

        if (userRole == null) {
            return;
        }

        // -----------------------------------------------------
        // COMMON
        // -----------------------------------------------------

        setButtonVisible(dashboardButton, true);
        setButtonVisible(notificationsButton, true);
        setButtonVisible(emergencyButton, true);
        setButtonVisible(helpButton, true);
        setButtonVisible(aiButton, true);
        setButtonVisible(feedbackButton, true);

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        if (userRole == Role.ADMIN) {

            setButtonVisible(patientsButton, true);
            setButtonVisible(doctorsButton, true);
            setButtonVisible(appointmentsButton, true);
            setButtonVisible(medicinesButton, true);
            setButtonVisible(billingButton, true);
            setButtonVisible(reportsButton, true);
            setButtonVisible(settingsButton, true);
            setButtonVisible(identityButton, true);
            setButtonVisible(supportTicketsButton, true);
        }

        // -----------------------------------------------------
        // DOCTOR
        // -----------------------------------------------------

        else if (userRole == Role.DOCTOR) {

            setButtonVisible(appointmentsButton, true);
            setButtonVisible(patientsButton, true);
        }

        // -----------------------------------------------------
        // RECEPTIONIST
        // -----------------------------------------------------

        else if (userRole == Role.RECEPTIONIST) {

            setButtonVisible(patientsButton, true);
            setButtonVisible(appointmentsButton, true);
            setButtonVisible(billingButton, true);
            setButtonVisible(medicinesButton, true);
            setButtonVisible(identityButton, true);
            setButtonVisible(supportTicketsButton, true);
        }

        // -----------------------------------------------------
        // PATIENT
        // -----------------------------------------------------

        else if (userRole == Role.PATIENT) {

            setButtonVisible(profileButton, true);
            setButtonVisible(appointmentsButton, true);
            setButtonVisible(medicalRecordsButton, true);
            setButtonVisible(prescriptionsButton, true);
            setButtonVisible(paymentHistoryButton, true);
            setButtonVisible(billingButton, true);
            setButtonVisible(onlinePaymentButton, true);
            setButtonVisible(identityButton, true);
        }
    }


    // =========================================================
    // BUTTON VISIBILITY
    // =========================================================

    private void setButtonVisible(
            Button button,
            boolean visible) {

        if (button == null) {
            return;
        }

        button.setVisible(visible);
        button.setManaged(visible);
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @FXML
    private void showDashboard() {

        if (contentArea == null) {
            return;
        }

        // Other screens are loaded into contentArea. Restore the dashboard before refreshing it.
        if (!dashboardContentNodes.isEmpty()
                && !contentArea.getChildren().equals(dashboardContentNodes)) {
            contentArea.getChildren().setAll(dashboardContentNodes);
        }

        loadDashboardStatistics();
        loadRecentAppointments();
        configureQuickServices();
        updateNotificationCount();
    }


    // =========================================================
    // DASHBOARD STATISTICS
    // =========================================================

    private void loadDashboardStatistics() {

        try {

            User currentUser =
                    SessionManager.getCurrentUser();

            if (currentUser == null
                    || currentUser.getRole() == null) {
                return;
            }

            Role role =
                    currentUser.getRole();

            if (role == Role.ADMIN) {

                patientCountLabel.setText(
                        String.valueOf(
                                dashboardService.getPatientCount()
                        )
                );

                doctorCountLabel.setText(
                        String.valueOf(
                                dashboardService.getDoctorCount()
                        )
                );

                appointmentCountLabel.setText(
                        String.valueOf(
                                dashboardService.getAppointmentCount()
                        )
                );

                String medicineCount =
                        String.valueOf(
                                dashboardService.getMedicineCount()
                        );

                medicineCountLabel.setText(medicineCount);

                if (medicineInventoryTotalLabel != null) {
                    medicineInventoryTotalLabel.setText(
                            medicineCount
                    );
                }

                lowStockCountLabel.setText(
                        String.valueOf(
                                dashboardService.getLowStockCount()
                        )
                );

                outOfStockCountLabel.setText(
                        String.valueOf(
                                dashboardService.getOutOfStockCount()
                        )
                );

                dashboardStatusLabel.setText(
                        "Hospital administration data loaded successfully"
                );

                return;
            }

            if (role == Role.RECEPTIONIST) {

                patientCountLabel.setText(
                        String.valueOf(
                                dashboardService.getPatientCount()
                        )
                );

                doctorCountLabel.setText(
                        String.valueOf(
                                dashboardService.getDoctorCount()
                        )
                );

                appointmentCountLabel.setText(
                        String.valueOf(
                                dashboardService.getAppointmentCount()
                        )
                );

                String medicineCount =
                        String.valueOf(
                                dashboardService.getMedicineCount()
                        );

                medicineCountLabel.setText(medicineCount);

                if (medicineInventoryTotalLabel != null) {
                    medicineInventoryTotalLabel.setText(
                            medicineCount
                    );
                }

                lowStockCountLabel.setText(
                        String.valueOf(
                                dashboardService.getLowStockCount()
                        )
                );

                outOfStockCountLabel.setText(
                        String.valueOf(
                                dashboardService.getOutOfStockCount()
                        )
                );

                dashboardStatusLabel.setText(
                        "Operational dashboard data loaded successfully"
                );

                return;
            }

            if (role == Role.DOCTOR) {

                patientCountLabel.setText(
                        String.valueOf(
                                dashboardService.getPatientCount()
                        )
                );

                doctorCountLabel.setText("1");

                appointmentCountLabel.setText(
                        String.valueOf(
                                dashboardService.getAppointmentCount()
                        )
                );

                medicineCountLabel.setText("--");

                if (medicineInventoryTotalLabel != null) {
                    medicineInventoryTotalLabel.setText("--");
                }

                lowStockCountLabel.setText("--");
                outOfStockCountLabel.setText("--");

                dashboardStatusLabel.setText(
                        "Your clinical dashboard data loaded successfully"
                );

                return;
            }

            if (role == Role.PATIENT) {

                patientCountLabel.setText("1");

                doctorCountLabel.setText("--");

                appointmentCountLabel.setText(
                        String.valueOf(
                                dashboardService.getAppointmentCount()
                        )
                );

                medicineCountLabel.setText("--");

                if (medicineInventoryTotalLabel != null) {
                    medicineInventoryTotalLabel.setText("--");
                }

                lowStockCountLabel.setText("--");
                outOfStockCountLabel.setText("--");

                dashboardStatusLabel.setText(
                        "Your personal healthcare dashboard loaded successfully"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            setDashboardLabelsToError();

            if (dashboardStatusLabel != null) {
                dashboardStatusLabel.setText(
                        "Unable to load dashboard data"
                );
            }
        }
    }


    // =========================================================
    // DASHBOARD ERROR LABELS
    // =========================================================

    private void setDashboardLabelsToError() {

        if (patientCountLabel != null) {
            patientCountLabel.setText("--");
        }

        if (doctorCountLabel != null) {
            doctorCountLabel.setText("--");
        }

        if (appointmentCountLabel != null) {
            appointmentCountLabel.setText("--");
        }

        if (medicineCountLabel != null) {
            medicineCountLabel.setText("--");
        }

        if (medicineInventoryTotalLabel != null) {
            medicineInventoryTotalLabel.setText("--");
        }

        if (lowStockCountLabel != null) {
            lowStockCountLabel.setText("--");
        }

        if (outOfStockCountLabel != null) {
            outOfStockCountLabel.setText("--");
        }
    }


    // =========================================================
    // RECENT APPOINTMENTS
    // =========================================================

    private void loadRecentAppointments() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || recentAppointmentsTable == null) {
            return;
        }

        recentAppointmentIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentId")
        );

        recentPatientColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientId")
        );

        recentDoctorColumn.setCellValueFactory(
                new PropertyValueFactory<>("doctorId")
        );

        recentDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentDate")
        );

        recentTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("appointmentTime")
        );

        recentUrgencyColumn.setCellValueFactory(
                new PropertyValueFactory<>("urgency")
        );

        recentStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        recentAppointmentsTable.setItems(
                FXCollections.observableArrayList(
                        dashboardService.getRecentAppointments(
                                currentUser.getRole(),
                                currentUser.getUserId()
                        )
                )
        );

        recentTimeColumn.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            LocalTime item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(
                                    item.format(TIME_FORMAT)
                            );
                        }
                    }
                }
        );
    }


    // =========================================================
    // QUICK SERVICES
    // =========================================================

    private void configureQuickServices() {

        setButtonVisible(
                quickAppointmentButton,
                true
        );

        setButtonVisible(
                quickRecordsButton,
                false
        );

        setButtonVisible(
                quickPrescriptionButton,
                false
        );

        setButtonVisible(
                quickBillingButton,
                false
        );

        setButtonVisible(
                quickNotificationsButton,
                true
        );

        setButtonVisible(
                quickInventoryButton,
                false
        );

        if (userRole == Role.ADMIN) {

            setButtonVisible(
                    quickBillingButton,
                    true
            );

            setButtonVisible(
                    quickInventoryButton,
                    true
            );

        } else if (userRole == Role.DOCTOR) {

            setButtonVisible(
                    quickRecordsButton,
                    true
            );

            setButtonVisible(
                    quickPrescriptionButton,
                    true
            );

        } else if (userRole == Role.RECEPTIONIST) {

            setButtonVisible(
                    quickBillingButton,
                    true
            );

            setButtonVisible(
                    quickInventoryButton,
                    true
            );

        } else if (userRole == Role.PATIENT) {

            setButtonVisible(
                    quickRecordsButton,
                    true
            );

            setButtonVisible(
                    quickPrescriptionButton,
                    true
            );

            setButtonVisible(
                    quickBillingButton,
                    true
            );
        }
    }


    // =========================================================
    // QUICK ACTIONS
    // =========================================================

    @FXML
    private void handleQuickAppointment() {
        showAppointments();
    }

    @FXML
    private void handleQuickRecords() {

        if (userRole == Role.PATIENT) {
            showPatientMedicalRecords();
        } else if (userRole == Role.DOCTOR) {
            showPatients();
        }
    }

    @FXML
    private void handleQuickPrescription() {

        if (userRole == Role.PATIENT) {
            showPatientPrescriptions();
        } else if (userRole == Role.DOCTOR) {
            showPatients();
        }
    }

    @FXML
    private void handleQuickBilling() {
        showBilling();
    }

    @FXML
    private void handleQuickNotifications() {
        showNotifications();
    }

    @FXML
    private void handleQuickInventory() {
        showMedicines();
    }


    // =========================================================
    // PATIENTS
    // =========================================================

    @FXML
    private void showPatients() {

        if (userRole == Role.DOCTOR) {
            showDoctorPatients();
            return;
        }

        if (userRole != Role.ADMIN
                && userRole != Role.RECEPTIONIST) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/PatientView.fxml"
                            )
                    );

            Parent patientView =
                    loader.load();

            PatientController controller =
                    loader.getController();

            controller.setContentArea(contentArea);

            contentArea.getChildren()
                    .setAll(patientView);

        } catch (IOException e) {

            showLoadError(
                    "/fxml/PatientView.fxml",
                    e
            );
        }
    }


    // =========================================================
    // DOCTOR PATIENTS
    // =========================================================

    private void showDoctorPatients() {

        if (userRole != Role.DOCTOR) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/DoctorPatientsView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            DoctorPatientsController controller =
                    loader.getController();

            controller.setNavigationHandler(
                    this::showMedicalRecords
            );

            contentArea.getChildren()
                    .setAll(root);

        } catch (IOException e) {

            showLoadError(
                    "/fxml/DoctorPatientsView.fxml",
                    e
            );
        }
    }


    // =========================================================
    // DOCTOR MEDICAL RECORDS
    // =========================================================

    private void showMedicalRecords(
            Patient patient) {

        if (userRole != Role.DOCTOR
                || patient == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/MedicalRecordsView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            MedicalRecordsController controller =
                    loader.getController();

            controller.setPatient(patient);

            contentArea.getChildren()
                    .setAll(root);

        } catch (IOException e) {

            showLoadError(
                    "/fxml/MedicalRecordsView.fxml",
                    e
            );
        }
    }


    // =========================================================
    // PATIENT MEDICAL RECORDS
    // =========================================================

    @FXML
    private void showPatientMedicalRecords() {

        if (userRole != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/PatientMedicalRecordView.fxml"
        );
    }


    // =========================================================
    // PATIENT PRESCRIPTIONS
    // =========================================================

    @FXML
    private void showPatientPrescriptions() {

        if (userRole != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/PatientPrescriptionView.fxml"
        );
    }


    // =========================================================
    // DOCTORS
    // =========================================================

    @FXML
    private void showDoctors() {

        if (userRole != Role.ADMIN) {
            return;
        }

        loadView(
                "/fxml/DoctorManagementView.fxml"
        );
    }


    // =========================================================
    // APPOINTMENTS
    // =========================================================

    @FXML
    private void showAppointments() {

        if (userRole == null) {
            return;
        }

        loadView(
                "/fxml/AppointmentView.fxml"
        );
    }


    // =========================================================
    // MEDICINES
    // =========================================================

    @FXML
    private void showMedicines() {

        if (userRole != Role.ADMIN
                && userRole != Role.RECEPTIONIST) {
            return;
        }

        loadView(
                "/fxml/MedicineView.fxml"
        );
    }


    // =========================================================
    // BILLING
    // =========================================================

    @FXML
    private void showBilling() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        if (currentUser.getRole() == Role.PATIENT) {

            loadView(
                    "/fxml/BillingView.fxml"
            );

        } else if (
                currentUser.getRole() == Role.ADMIN
                        || currentUser.getRole()
                        == Role.RECEPTIONIST) {

            loadView(
                    "/fxml/PharmacyBillingView.fxml"
            );
        }
    }


    // =========================================================
    // ONLINE PAYMENT
    // =========================================================

    @FXML
    private void showOnlinePayment() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || currentUser.getRole() != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/OnlinePaymentView.fxml"
        );
    }


    // =========================================================
    // PROFILE
    // =========================================================

    @FXML
    private void showProfile() {

        if (userRole != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/PatientProfileView.fxml"
        );
    }


    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    @FXML
    private void showNotifications() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        loadView(
                "/fxml/NotificationView.fxml"
        );

        updateNotificationCount();
    }


    // =========================================================
    // NOTIFICATION COUNT
    // =========================================================

    private void updateNotificationCount() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null
                || notificationsButton == null) {
            return;
        }

        try {

            int unreadCount =
                    notificationService.getUnreadCount(
                            currentUser.getUserId()
                    );

            if (unreadCount > 0) {

                notificationsButton.setText(
                        "Notifications (" +
                                unreadCount +
                                ")"
                );

            } else {

                notificationsButton.setText(
                        "Notifications"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // HELP CENTER
    // =========================================================

    @FXML
    private void showHelp() {
        loadView("/fxml/HelpView.fxml");
    }


    // =========================================================
    // AI ASSISTANT
    // =========================================================

    @FXML
    private void showAI() {
        loadView("/fxml/AIAssistantView.fxml");
    }


    // =========================================================
    // FEEDBACK
    // =========================================================

    @FXML
    private void showFeedback() {
        loadView("/fxml/FeedbackView.fxml");
    }


    // =========================================================
    // EMERGENCY
    // =========================================================

    @FXML
    private void showEmergency() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {
            return;
        }

        loadView(
                "/fxml/EmergencyView.fxml"
        );
    }


    // =========================================================
    // IDENTITY VERIFICATION
    // =========================================================

    @FXML
    private void showIdentityVerification() {

        if (userRole != Role.ADMIN
                && userRole != Role.RECEPTIONIST
                && userRole != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/IdentityVerificationView.fxml"
        );
    }


    // =========================================================
    // REPORTS
    // =========================================================

    @FXML
    private void showReports() {

        if (userRole != Role.ADMIN) {
            return;
        }

        loadView(
                "/fxml/ReportsView.fxml"
        );
    }


    // =========================================================
    // SETTINGS
    // =========================================================

    @FXML
    private void showSettings() {

        if (userRole != Role.ADMIN) {
            return;
        }

        contentArea.getChildren().clear();

        Label title =
                new Label("System Settings");

        title.getStyleClass()
                .add("section-title");

        Label message =
                new Label(
                        "System configuration will be available here."
                );

        message.getStyleClass()
                .add("empty-message");

        Button backButton =
                new Button("← Back to Dashboard");

        backButton.setOnAction(
                event -> showDashboard()
        );

        contentArea.getChildren().addAll(
                title,
                message,
                backButton
        );
    }


    // =========================================================
    // PAYMENT HISTORY
    // =========================================================

    @FXML
    private void showPatientPaymentHistory() {

        if (userRole != Role.PATIENT) {
            return;
        }

        loadView(
                "/fxml/PatientPaymentHistoryView.fxml"
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void handleLogout() {

        SessionManager.logout();

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/LoginView.fxml"
                            )
                    );

            Parent loginView =
                    loader.load();

            if (userLabel != null
                    && userLabel.getScene() != null) {

                userLabel.getScene()
                        .setRoot(loginView);
            }

        } catch (IOException e) {

            e.printStackTrace();

            showLoadError(
                    "/fxml/LoginView.fxml",
                    e
            );
        }
    }


    // =========================================================
    // GENERIC VIEW LOADER
    // =========================================================

    private void loadView(
            String fxmlPath) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fxmlPath
                            )
                    );

            Parent root =
                    loader.load();

            contentArea.getChildren()
                    .setAll(root);

        } catch (IOException |
                 NullPointerException e) {

            showLoadError(
                    fxmlPath,
                    e
            );
        }
    }


    // =========================================================
    // PUBLIC VIEW LOADER
    // =========================================================

    public void showView(
            String resource) {

        loadView(resource);
    }


    // =========================================================
    // ERROR DISPLAY
    // =========================================================

    private void showLoadError(
            String fxmlPath,
            Exception e) {

        e.printStackTrace();

        if (contentArea == null) {
            return;
        }

        contentArea.getChildren().clear();

        Label title =
                new Label(
                        "Unable to load view"
                );

        title.getStyleClass()
                .add("section-title");

        String errorMessage =
                e.getMessage();

        if (errorMessage == null
                || errorMessage.isBlank()) {

            errorMessage =
                    "An unexpected error occurred.";
        }

        Label errorLabel =
                new Label(
                        fxmlPath
                                + "\n\n"
                                + errorMessage
                );

        errorLabel.setWrapText(true);

        errorLabel.getStyleClass()
                .add("empty-message");

        Button backButton =
                new Button(
                        "← Back to Dashboard"
                );

        backButton.setOnAction(
                event -> showDashboard()
        );

        contentArea.getChildren().addAll(
                title,
                errorLabel,
                backButton
        );
    }


    // =========================================================
    // STATIC DASHBOARD NAVIGATION
    // =========================================================

    public static void navigateToDashboard() {

        if (activeDashboardController != null) {
            activeDashboardController.showDashboard();
        }
    }


    // =========================================================
    // FXML EVENT HANDLERS
    // =========================================================

    @FXML
    private void handleDashboard() {
        showDashboard();
    }


    @FXML
    private void handleBackToDashboard() {
        showDashboard();
    }


    @FXML
    private void handleNotifications() {
        showNotifications();
    }


    @FXML
    private void handlePatients() {
        showPatients();
    }


    @FXML
    private void handleDoctors() {
        showDoctors();
    }


    @FXML
    private void handleAppointments() {
        showAppointments();
    }


    @FXML
    private void handleMedicines() {
        showMedicines();
    }


    @FXML
    private void handleBilling() {
        showBilling();
    }


    @FXML
    private void handleReports() {
        showReports();
    }


    @FXML
    private void handleProfile() {
        showProfile();
    }


    @FXML
    private void handleMedicalRecords() {
        showPatientMedicalRecords();
    }


    @FXML
    private void handlePrescriptions() {
        showPatientPrescriptions();
    }


    @FXML
    private void handlePaymentHistory() {
        showPatientPaymentHistory();
    }


    @FXML
    private void handleOnlinePayment() {
        showOnlinePayment();
    }


    @FXML
    private void handleIdentity() {
        showIdentityVerification();
    }


    @FXML
    private void handleEmergency() {
        showEmergency();
    }


    @FXML
    private void handleHelp() {
        showHelp();
    }


    @FXML
    private void handleAIAssistant() {
        showAI();
    }
    // =========================================================
    // SUPPORT TICKETS
    // =========================================================

    @FXML
    private void showSupportTickets() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {
            return;
        }

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {
            return;
        }

        loadView(
                "/fxml/SupportTicketManagementView.fxml"
        );
    }

    @FXML
    private void handleSupportTickets() {
        showSupportTickets();
    }


    @FXML
    private void handleSettings() {
        showSettings();
    }


    // =========================================================
    // SUPPORT TICKET FXML HANDLER
    // =========================================================

    @FXML
    private void handleSupportTicketManagement() {
        showSupportTickets();
    }
}