package com.medinexus.controller;

import com.medinexus.model.Doctor;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.DoctorService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.Optional;

public class DoctorManagementController {

    @FXML
    private TableView<Doctor> doctorTable;

    @FXML
    private TableColumn<Doctor, Integer> idColumn;

    @FXML
    private TableColumn<Doctor, String> nameColumn;

    @FXML
    private TableColumn<Doctor, String> specializationColumn;

    @FXML
    private TableColumn<Doctor, String> phoneColumn;

    @FXML
    private TableColumn<Doctor, String> emailColumn;

    @FXML
    private TableColumn<Doctor, Boolean> availabilityColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button availabilityButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    private final DoctorService doctorService =
            new DoctorService();

    private final ObservableList<Doctor> doctors =
            FXCollections.observableArrayList();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        configureColumns();

        doctorTable.setItems(doctors);

        configureRoleAccess();

        loadDoctors();
    }


    // =========================================================
    // COLUMN CONFIGURATION
    // =========================================================

    private void configureColumns() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("doctorId")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        specializationColumn.setCellValueFactory(
                new PropertyValueFactory<>("specialization")
        );

        phoneColumn.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        availabilityColumn.setCellValueFactory(
                new PropertyValueFactory<>("available")
        );
    }


    // =========================================================
    // ROLE ACCESS
    // =========================================================

    private void configureRoleAccess() {

        User user =
                SessionManager.getCurrentUser();

        boolean admin =
                user != null
                        && user.getRole() == Role.ADMIN;

        addButton.setDisable(!admin);
        editButton.setDisable(!admin);
        availabilityButton.setDisable(!admin);
        deleteButton.setDisable(!admin);
    }


    // =========================================================
    // LOAD DOCTORS
    // =========================================================

    private void loadDoctors() {

        try {

            doctors.setAll(
                    doctorService.getAllDoctors()
            );

        } catch (Exception e) {

            showError(
                    "Unable to load doctors",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // ADD DOCTOR
    // =========================================================

    @FXML
    private void handleAddDoctor() {

        if (!isAdmin()) {
            return;
        }

        Dialog<Doctor> dialog =
                createDoctorDialog(
                        null
                );

        Optional<Doctor> result =
                dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        try {

            doctorService.addDoctor(
                    result.get()
            );

            loadDoctors();

            showInformation(
                    "Doctor Added",
                    "Doctor was added successfully."
            );

        } catch (Exception e) {

            showError(
                    "Unable to add doctor",
                    getRootMessage(e)
            );
        }
    }


    // =========================================================
    // EDIT DOCTOR
    // =========================================================

    @FXML
    private void handleEditDoctor() {

        if (!isAdmin()) {
            return;
        }

        Doctor selected =
                doctorTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInformation(
                    "Select Doctor",
                    "Please select a doctor to edit."
            );

            return;
        }

        Dialog<Doctor> dialog =
                createDoctorDialog(
                        selected
                );

        Optional<Doctor> result =
                dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        try {

            doctorService.updateDoctor(
                    result.get()
            );

            loadDoctors();

            showInformation(
                    "Doctor Updated",
                    "Doctor details were updated."
            );

        } catch (Exception e) {

            showError(
                    "Unable to update doctor",
                    getRootMessage(e)
            );
        }
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    @FXML
    private void handleToggleAvailability() {

        if (!isAdmin()) {
            return;
        }

        Doctor selected =
                doctorTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInformation(
                    "Select Doctor",
                    "Please select a doctor first."
            );

            return;
        }

        boolean newStatus =
                !selected.isAvailable();

        try {

            doctorService.setDoctorAvailability(
                    selected.getDoctorId(),
                    newStatus
            );

            loadDoctors();

            showInformation(
                    "Availability Updated",
                    selected.getName()
                            + " is now "
                            + (newStatus
                            ? "Available"
                            : "Unavailable")
            );

        } catch (Exception e) {

            showError(
                    "Unable to update availability",
                    getRootMessage(e)
            );
        }
    }


    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    @FXML
    private void handleDeleteDoctor() {

        if (!isAdmin()) {
            return;
        }

        Doctor selected =
                doctorTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInformation(
                    "Select Doctor",
                    "Please select a doctor first."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Delete Doctor"
        );

        confirmation.setHeaderText(
                "Delete " + selected.getName() + "?"
        );

        confirmation.setContentText(
                "This will permanently remove the "
                        + "doctor and linked login account."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get()
                != ButtonType.OK) {

            return;
        }

        try {

            doctorService.deleteDoctor(
                    selected.getDoctorId()
            );

            loadDoctors();

            showInformation(
                    "Doctor Deleted",
                    "Doctor was deleted successfully."
            );

        } catch (Exception e) {

            showError(
                    "Unable to delete doctor",
                    getRootMessage(e)
            );
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadDoctors();
    }


    // =========================================================
    // DOCTOR FORM
    // =========================================================

    private Dialog<Doctor> createDoctorDialog(
            Doctor existingDoctor) {

        boolean editing =
                existingDoctor != null;

        Dialog<Doctor> dialog =
                new Dialog<>();

        dialog.setTitle(
                editing
                        ? "Edit Doctor"
                        : "Add Doctor"
        );

        dialog.setHeaderText(
                editing
                        ? "Update doctor information"
                        : "Enter new doctor information"
        );


        ButtonType saveButton =
                new ButtonType(
                        editing
                                ? "Update"
                                : "Add",
                        javafx.scene.control.ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        saveButton,
                        ButtonType.CANCEL
                );


        TextField idField =
                new TextField();

        TextField nameField =
                new TextField();

        TextField specializationField =
                new TextField();

        TextField phoneField =
                new TextField();

        TextField emailField =
                new TextField();

        CheckBox availableBox =
                new CheckBox(
                        "Doctor is available"
                );


        idField.setPromptText(
                "Doctor ID"
        );

        nameField.setPromptText(
                "Full name"
        );

        specializationField.setPromptText(
                "Specialization"
        );

        phoneField.setPromptText(
                "Phone"
        );

        emailField.setPromptText(
                "Email"
        );


        if (editing) {

            idField.setText(
                    String.valueOf(
                            existingDoctor.getDoctorId()
                    )
            );

            idField.setDisable(true);

            nameField.setText(
                    existingDoctor.getName()
            );

            specializationField.setText(
                    existingDoctor.getSpecialization()
            );

            phoneField.setText(
                    existingDoctor.getPhone()
            );

            emailField.setText(
                    existingDoctor.getEmail()
            );

            availableBox.setSelected(
                    existingDoctor.isAvailable()
            );

        } else {

            availableBox.setSelected(true);
        }


        GridPane grid =
                new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(
                new Insets(20)
        );

        grid.add(
                new Label("Doctor ID:"),
                0,
                0
        );

        grid.add(
                idField,
                1,
                0
        );

        grid.add(
                new Label("Name:"),
                0,
                1
        );

        grid.add(
                nameField,
                1,
                1
        );

        grid.add(
                new Label("Specialization:"),
                0,
                2
        );

        grid.add(
                specializationField,
                1,
                2
        );

        grid.add(
                new Label("Phone:"),
                0,
                3
        );

        grid.add(
                phoneField,
                1,
                3
        );

        grid.add(
                new Label("Email:"),
                0,
                4
        );

        grid.add(
                emailField,
                1,
                4
        );

        grid.add(
                availableBox,
                1,
                5
        );


        dialog.getDialogPane()
                .setContent(grid);


        dialog.setResultConverter(
                button -> {

                    if (button != saveButton) {
                        return null;
                    }

                    try {

                        int doctorId =
                                Integer.parseInt(
                                        idField.getText()
                                                .trim()
                                );

                        String name =
                                nameField.getText()
                                        .trim();

                        String specialization =
                                specializationField
                                        .getText()
                                        .trim();

                        String phone =
                                phoneField.getText()
                                        .trim();

                        String email =
                                emailField.getText()
                                        .trim();


                        if (name.isEmpty()
                                || specialization.isEmpty()
                                || phone.isEmpty()
                                || email.isEmpty()) {

                            showError(
                                    "Invalid Input",
                                    "All doctor fields are required."
                            );

                            return null;
                        }


                        int userId =
                                editing
                                        ? existingDoctor.getUserId()
                                        : 0;


                        return new Doctor(
                                doctorId,
                                userId,
                                name,
                                specialization,
                                phone,
                                email,
                                availableBox.isSelected()
                        );

                    } catch (NumberFormatException e) {

                        showError(
                                "Invalid Doctor ID",
                                "Doctor ID must be a valid number."
                        );

                        return null;
                    }
                }
        );

        return dialog;
    }


    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private boolean isAdmin() {

        User user =
                SessionManager.getCurrentUser();

        return user != null
                && user.getRole() == Role.ADMIN;
    }


    // =========================================================
    // INFORMATION ALERT
    // =========================================================

    private void showInformation(
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


    // =========================================================
    // ERROR ALERT
    // =========================================================

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // =========================================================
    // ROOT ERROR MESSAGE
    // =========================================================

    private String getRootMessage(
            Exception exception) {

        Throwable cause =
                exception;

        while (cause.getCause() != null) {

            cause = cause.getCause();
        }

        return cause.getMessage() != null
                ? cause.getMessage()
                : exception.getMessage();
    }
}