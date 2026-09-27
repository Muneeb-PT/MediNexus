package com.medinexus.controller;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.AuthenticationService;
import com.medinexus.service.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {


    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    private final AuthenticationService authenticationService =
            new AuthenticationService();

    @FXML
    public void initialize() {

        roleComboBox.getItems().addAll(
                "Admin",
                "Doctor",
                "Receptionist",
                "Patient"
        );
    }

    @FXML
    private void handleLogin() {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText();

        String selectedRoleText =
                roleComboBox.getValue();

        if (username.isEmpty()
                || password.isEmpty()
                || selectedRoleText == null) {

            messageLabel.setText(
                    "Please enter all login details."
            );

            return;
        }

        Role selectedRole;

        try {

            selectedRole =
                    Role.valueOf(
                            selectedRoleText.toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            messageLabel.setText(
                    "Invalid role selected."
            );

            return;
        }

        /*
         * Authentication is now performed against
         * the database and the stored BCrypt hash.
         *
         * The controller no longer contains hard-coded
         * username-to-ID mappings.
         */
        User authenticatedUser =
                authenticationService.authenticate(
                        username,
                        password,
                        selectedRole
                );

        if (authenticatedUser == null) {

            messageLabel.setText(
                    "Invalid username, password, or role."
            );

            return;
        }

        /*
         * Only authenticated users reach the session.
         */
        SessionManager.login(
                authenticatedUser
        );

        openDashboard();
    }

    private void openDashboard() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/DashboardView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            DashboardController controller =
                    loader.getController();

            controller.setUserRole(
                    SessionManager
                            .getCurrentUser()
                            .getRole()
            );

            Scene scene =
                    new Scene(
                            root,
                            1200,
                            700
                    );

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Unable to open dashboard."
            );
        }
    }
    @FXML
    private void handleOpenSignup() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/SignupView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(
                            root,
                            1200,
                            700
                    );

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Unable to open signup page."
            );
        }
    }
    @FXML
    private void handleForgotPassword() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/ForgotPasswordView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(
                            root,
                            1200,
                            700
                    );

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

            messageLabel.setText(
                    "Unable to open password recovery."
            );
        }
    }

}
