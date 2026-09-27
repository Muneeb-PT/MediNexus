package com.medinexus.controller;

import com.medinexus.model.EmergencyRequest;
import com.medinexus.model.EmergencyStatus;
import com.medinexus.model.EmergencyVehicle;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.model.VehicleStatus;
import com.medinexus.service.EmergencyService;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class EmergencyController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<EmergencyRequest> emergencyTable;

    @FXML
    private TableColumn<EmergencyRequest, Number> idColumn;

    @FXML
    private TableColumn<EmergencyRequest, Number> patientColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> typeColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> severityColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> pickupColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> destinationColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> statusColumn;

    @FXML
    private TableColumn<EmergencyRequest, String> vehicleColumn;

    @FXML
    private TextField emergencyTypeField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> severityComboBox;

    @FXML
    private TextField pickupLocationField;

    @FXML
    private TextField destinationField;

    @FXML
    private ComboBox<EmergencyVehicle> vehicleComboBox;

    @FXML
    private Button requestButton;

    @FXML
    private Button dispatchButton;

    @FXML
    private Button enRouteButton;

    @FXML
    private Button onSceneButton;

    @FXML
    private Button completeButton;

    @FXML
    private Button returnVehicleButton;

    @FXML
    private Button cancelButton;

    private final EmergencyService emergencyService =
            new EmergencyService();

    @FXML
    public void initialize() {

        configureColumns();

        severityComboBox.setItems(
                FXCollections.observableArrayList(
                        "EMERGENCY",
                        "CRITICAL",
                        "URGENT"
                )
        );

        severityComboBox.setValue(
                "EMERGENCY"
        );

        emergencyTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateActionButtons(newValue)
                );

        vehicleComboBox
                .valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateActionButtons(
                                        emergencyTable
                                                .getSelectionModel()
                                                .getSelectedItem()
                                )
                );

        loadRoleBasedView();
    }

    private void configureColumns() {

        idColumn.setCellValueFactory(
                cell ->
                        new SimpleLongProperty(
                                cell.getValue()
                                        .getEmergencyId()
                        )
        );

        patientColumn.setCellValueFactory(
                cell ->
                        new SimpleIntegerProperty(
                                cell.getValue()
                                        .getPatientId()
                        )
        );

        typeColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getEmergencyType()
                                )
                        )
        );

        severityColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getSeverity()
                                )
                        )
        );

        pickupColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getPickupLocation()
                                )
                        )
        );

        destinationColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safe(
                                        cell.getValue()
                                                .getDestination()
                                )
                        )
        );

        statusColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue()
                                        .getStatus()
                                        .name()
                        )
        );

        vehicleColumn.setCellValueFactory(
                cell -> {

                    Long vehicleId =
                            cell.getValue()
                                    .getAssignedVehicleId();

                    return new SimpleStringProperty(
                            vehicleId == null
                                    ? "Not assigned"
                                    : String.valueOf(vehicleId)
                    );
                }
        );
    }

    private void loadRoleBasedView() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {

            messageLabel.setText(
                    "You must be logged in."
            );

            disableAllActions();

            return;
        }

        Role role =
                user.getRole();

        boolean patient =
                role == Role.PATIENT;

        boolean staff =
                role == Role.ADMIN
                        || role == Role.RECEPTIONIST;

        boolean doctor =
                role == Role.DOCTOR;

        titleLabel.setText(
                patient
                        ? "My Emergency Requests"
                        : "Emergency Control Center"
        );

        requestButton.setVisible(patient);
        requestButton.setManaged(patient);

        emergencyTypeField.setDisable(!patient);
        descriptionField.setDisable(!patient);
        severityComboBox.setDisable(!patient);
        pickupLocationField.setDisable(!patient);
        destinationField.setDisable(!patient);

        vehicleComboBox.setVisible(staff);
        vehicleComboBox.setManaged(staff);

        dispatchButton.setVisible(staff);
        dispatchButton.setManaged(staff);

        enRouteButton.setVisible(staff);
        enRouteButton.setManaged(staff);

        onSceneButton.setVisible(staff);
        onSceneButton.setManaged(staff);

        completeButton.setVisible(staff);
        completeButton.setManaged(staff);

        returnVehicleButton.setVisible(staff);
        returnVehicleButton.setManaged(staff);

        cancelButton.setVisible(
                patient || staff
        );

        cancelButton.setManaged(
                patient || staff
        );

        if (doctor) {

            titleLabel.setText(
                    "Emergency Monitoring"
            );
        }

        loadEmergencies();

        if (staff) {
            loadAvailableVehicles();
        }
    }

    private void loadEmergencies() {

        try {

            List<EmergencyRequest> requests =
                    emergencyService
                            .getEmergencyRequests();

            emergencyTable.setItems(
                    FXCollections.observableArrayList(
                            requests
                    )
            );

            updateActionButtons(
                    emergencyTable
                            .getSelectionModel()
                            .getSelectedItem()
            );

            messageLabel.setText(
                    requests.isEmpty()
                            ? "No emergency requests."
                            : requests.size()
                            + " emergency request(s) loaded."
            );

        } catch (SecurityException e) {

            showError(
                    "Access Denied",
                    e.getMessage()
            );

        } catch (Exception e) {

            showError(
                    "Unable to Load",
                    e.getMessage()
            );
        }
    }

    private void loadAvailableVehicles() {

        try {

            List<EmergencyVehicle> vehicles =
                    emergencyService
                            .getAvailableVehicles();

            vehicleComboBox.setItems(
                    FXCollections.observableArrayList(
                            vehicles
                    )
            );

        } catch (Exception e) {

            showError(
                    "Vehicle Loading Error",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleRequestEmergency() {

        try {

            User user =
                    requireLoggedInUser();

            if (user.getRole()
                    != Role.PATIENT) {

                throw new SecurityException(
                        "Only patients can create emergency requests."
                );
            }

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null) {

                throw new IllegalStateException(
                        "Patient profile not found."
                );
            }

            EmergencyRequest request =
                    new EmergencyRequest();

            request.setPatientId(
                    patient.getPatientId()
            );

            request.setEmergencyType(
                    emergencyTypeField
                            .getText()
                            .trim()
            );

            request.setDescription(
                    descriptionField
                            .getText()
                            .trim()
            );

            request.setSeverity(
                    severityComboBox
                            .getValue()
            );

            request.setPickupLocation(
                    pickupLocationField
                            .getText()
                            .trim()
            );

            request.setDestination(
                    destinationField
                            .getText()
                            .trim()
            );

            long emergencyId =
                    emergencyService
                            .requestEmergency(
                                    request
                            );

            showInfo(
                    "Emergency Requested",
                    "Emergency request #"
                            + emergencyId
                            + " has been created."
            );

            clearRequestForm();

            loadEmergencies();

        } catch (Exception e) {

            showError(
                    "Emergency Request Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleDispatch() {

        EmergencyRequest request =
                getSelectedRequest();

        if (request == null) {
            return;
        }

        EmergencyVehicle vehicle =
                vehicleComboBox.getValue();

        if (vehicle == null) {

            showError(
                    "Vehicle Required",
                    "Select an available emergency vehicle."
            );

            return;
        }

        try {

            emergencyService.dispatchVehicle(
                    request.getEmergencyId(),
                    vehicle.getVehicleId()
            );

            showInfo(
                    "Vehicle Dispatched",
                    "Vehicle "
                            + vehicle.getVehicleNumber()
                            + " has been assigned."
            );

            loadRoleBasedView();

        } catch (Exception e) {

            showError(
                    "Dispatch Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleEnRoute() {

        changeStatus(
                "EN_ROUTE",
                () ->
                        emergencyService.markEnRoute(
                                getSelectedRequestId()
                        )
        );
    }

    @FXML
    private void handleOnScene() {

        changeStatus(
                "ON_SCENE",
                () ->
                        emergencyService.markOnScene(
                                getSelectedRequestId()
                        )
        );
    }

    @FXML
    private void handleComplete() {

        changeStatus(
                "COMPLETED",
                () ->
                        emergencyService.completeEmergency(
                                getSelectedRequestId()
                        )
        );
    }

    @FXML
    private void handleReturnVehicle() {

        EmergencyRequest request =
                getSelectedRequest();

        if (request == null) {
            return;
        }

        Long vehicleId =
                request.getAssignedVehicleId();

        if (vehicleId == null) {

            showError(
                    "No Vehicle",
                    "This emergency does not have an assigned vehicle."
            );

            return;
        }

        try {

            emergencyService.returnVehicle(
                    vehicleId
            );

            showInfo(
                    "Vehicle Available",
                    "Emergency vehicle #"
                            + vehicleId
                            + " has returned and is now AVAILABLE."
            );

            loadRoleBasedView();

        } catch (Exception e) {

            showError(
                    "Vehicle Recovery Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleCancel() {

        EmergencyRequest request =
                getSelectedRequest();

        if (request == null) {
            return;
        }

        try {

            emergencyService.cancelEmergency(
                    request.getEmergencyId()
            );

            showInfo(
                    "Emergency Cancelled",
                    "Emergency request #"
                            + request.getEmergencyId()
                            + " was cancelled."
            );

            loadRoleBasedView();

        } catch (Exception e) {

            showError(
                    "Cancellation Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleRefresh() {

        loadRoleBasedView();
    }

    private void changeStatus(
            String status,
            Runnable action) {

        EmergencyRequest request =
                getSelectedRequest();

        if (request == null) {
            return;
        }

        try {

            action.run();

            showInfo(
                    "Emergency Updated",
                    "Emergency #"
                            + request.getEmergencyId()
                            + " is now "
                            + status
            );

            loadRoleBasedView();

        } catch (Exception e) {

            showError(
                    "Status Update Failed",
                    e.getMessage()
            );
        }
    }

    private void updateActionButtons(
            EmergencyRequest request) {

        User user =
                SessionManager.getCurrentUser();

        if (user == null
                || request == null) {

            disableAllActions();
            return;
        }

        boolean staff =
                user.getRole() == Role.ADMIN
                        || user.getRole()
                        == Role.RECEPTIONIST;

        if (!staff) {

            dispatchButton.setDisable(true);
            enRouteButton.setDisable(true);
            onSceneButton.setDisable(true);
            completeButton.setDisable(true);
            returnVehicleButton.setDisable(true);

            boolean patient =
                    user.getRole()
                            == Role.PATIENT;

            cancelButton.setDisable(
                    !patient
            );

            return;
        }

        EmergencyStatus status =
                request.getStatus();

        dispatchButton.setDisable(
                status != EmergencyStatus.REQUESTED
                        || vehicleComboBox
                        .getValue() == null
        );

        enRouteButton.setDisable(
                status != EmergencyStatus.DISPATCHED
        );

        onSceneButton.setDisable(
                status != EmergencyStatus.EN_ROUTE
        );

        completeButton.setDisable(
                status != EmergencyStatus.ON_SCENE
        );

        /*
         * Vehicle recovery is available only after
         * the emergency has been completed and the
         * vehicle has entered RETURNING state.
         */
        returnVehicleButton.setDisable(
                status != EmergencyStatus.COMPLETED
                        || request.getAssignedVehicleId()
                        == null
        );

        cancelButton.setDisable(
                status == EmergencyStatus.COMPLETED
                        || status
                        == EmergencyStatus.CANCELLED
        );
    }

    private void disableAllActions() {

        dispatchButton.setDisable(true);
        enRouteButton.setDisable(true);
        onSceneButton.setDisable(true);
        completeButton.setDisable(true);
        returnVehicleButton.setDisable(true);
        cancelButton.setDisable(true);
    }

    private EmergencyRequest getSelectedRequest() {

        EmergencyRequest request =
                emergencyTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (request == null) {

            showError(
                    "No Emergency Selected",
                    "Select an emergency request first."
            );
        }

        return request;
    }

    private long getSelectedRequestId() {

        EmergencyRequest request =
                getSelectedRequest();

        if (request == null) {

            throw new IllegalStateException(
                    "No emergency request selected."
            );
        }

        return request.getEmergencyId();
    }

    private User requireLoggedInUser() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {

            throw new SecurityException(
                    "You must be logged in."
            );
        }

        return user;
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }

    private void clearRequestForm() {

        emergencyTypeField.clear();
        descriptionField.clear();
        pickupLocationField.clear();
        destinationField.clear();

        severityComboBox.setValue(
                "EMERGENCY"
        );
    }

    private void showInfo(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(
                message == null
                        ? "An unexpected error occurred."
                        : message
        );

        alert.showAndWait();
    }
}