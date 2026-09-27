package com.medinexus.controller;

import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.PatientService;
import com.medinexus.service.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class PatientController implements PatientDataListener {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Patient> patientTable;

    @FXML
    private TableColumn<Patient, Integer> idColumn;

    @FXML
    private TableColumn<Patient, String> nameColumn;

    @FXML
    private TableColumn<Patient, Integer> ageColumn;

    @FXML
    private TableColumn<Patient, String> genderColumn;

    @FXML
    private TableColumn<Patient, String> phoneColumn;

    @FXML
    private TableColumn<Patient, String> bloodGroupColumn;

    private VBox contentArea;

    private final ObservableList<Patient> patients =
            FXCollections.observableArrayList();

    private FilteredList<Patient> filteredPatients;

    private final PatientService patientService =
            new PatientService();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientId")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        ageColumn.setCellValueFactory(
                new PropertyValueFactory<>("age")
        );

        genderColumn.setCellValueFactory(
                new PropertyValueFactory<>("gender")
        );

        phoneColumn.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        bloodGroupColumn.setCellValueFactory(
                new PropertyValueFactory<>("bloodGroup")
        );

        filteredPatients =
                new FilteredList<>(
                        patients,
                        patient -> true
                );

        patientTable.setItems(filteredPatients);

        loadPatients();
    }


    // =========================================================
    // LOAD PATIENTS FROM DATABASE
    // =========================================================

    private void loadPatients() {

        try {

            List<Patient> databasePatients =
                    patientService.getAllPatients();

            patients.setAll(databasePatients);

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Unable to load patients",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // CONTENT AREA
    // =========================================================

    public void setContentArea(VBox contentArea) {

        this.contentArea = contentArea;
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void handleSearch() {

        String searchText =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        filteredPatients.setPredicate(patient -> {

            if (searchText.isEmpty()) {
                return true;
            }

            return patient.getName() != null
                    && patient.getName()
                    .toLowerCase()
                    .contains(searchText);
        });
    }


    // =========================================================
    // CLEAR SEARCH
    // =========================================================

    @FXML
    private void handleClearSearch() {

        searchField.clear();

        filteredPatients.setPredicate(
                patient -> true
        );
    }


    // =========================================================
    // DELETE PATIENT
    // =========================================================

    @FXML
    private void handleDeletePatient() {

        Patient selectedPatient =
                patientTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPatient == null) {

            showWarning(
                    "Delete Patient",
                    "Please select a patient to delete."
            );

            return;
        }


        if (!canModifyPatients()) {

            showWarning(
                    "Access Denied",
                    "You do not have permission to delete patients."
            );

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Delete Patient"
        );

        confirmation.setHeaderText(
                "Delete "
                        + selectedPatient.getName()
                        + "?"
        );

        confirmation.setContentText(
                "This will permanently delete the patient "
                        + "from the database."
        );


        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response ==
                            javafx.scene.control.ButtonType.OK) {

                        try {

                            patientService.deletePatient(
                                    selectedPatient.getPatientId()
                            );

                            loadPatients();

                            showInformation(
                                    "Patient Deleted",
                                    "Patient deleted successfully."
                            );

                        } catch (Exception e) {

                            e.printStackTrace();

                            showError(
                                    "Delete Failed",
                                    e.getMessage()
                            );
                        }
                    }
                });
    }


    // =========================================================
    // EDIT PATIENT
    // =========================================================

    @FXML
    private void handleEditPatient() {

        Patient selectedPatient =
                patientTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedPatient == null) {

            showWarning(
                    "Edit Patient",
                    "Please select a patient to edit."
            );

            return;
        }


        if (!canModifyPatients()) {

            showWarning(
                    "Access Denied",
                    "You do not have permission to edit patients."
            );

            return;
        }


        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/EditPatientView.fxml"
                            )
                    );

            Parent editPatientView =
                    loader.load();

            EditPatientController controller =
                    loader.getController();

            controller.setPatient(
                    selectedPatient
            );

            controller.setPatientController(
                    this
            );

            if (contentArea != null) {

                contentArea.getChildren()
                        .setAll(editPatientView);
            }

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Unable to open Edit Patient",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // ADD PATIENT
    // =========================================================

    @FXML
    private void handleAddPatient() {

        if (!canModifyPatients()) {

            showWarning(
                    "Access Denied",
                    "You do not have permission to add patients."
            );

            return;
        }


        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/AddPatientView.fxml"
                            )
                    );

            Parent addPatientView =
                    loader.load();

            AddPatientController controller =
                    loader.getController();

            controller.setPatientDataListener(
                    this
            );

            if (contentArea != null) {

                contentArea.getChildren()
                        .setAll(addPatientView);
            }

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Unable to open Add Patient",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // PATIENT ADDED
    // =========================================================

    @Override
    public void onPatientAdded(
            Patient patient) {

        loadPatients();
    }


    // =========================================================
    // REFRESH
    // =========================================================

    public void refreshPatientTable() {

        loadPatients();
    }


    // =========================================================
    // SHOW PATIENT LIST
    // =========================================================

    public void showPatientList() {

        if (contentArea == null) {
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

            controller.setContentArea(
                    contentArea
            );

            contentArea.getChildren()
                    .setAll(patientView);

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Unable to load Patient Management",
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // ROLE CHECK
    // =========================================================

    private boolean canModifyPatients() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        Role role =
                currentUser.getRole();

        return role == Role.ADMIN
                || role == Role.RECEPTIONIST;
    }


    // =========================================================
    // ALERT HELPERS
    // =========================================================

    private void showWarning(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


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
                        ? "Unknown error."
                        : message
        );

        alert.showAndWait();
    }
}