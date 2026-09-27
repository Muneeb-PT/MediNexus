package com.medinexus.controller;

import com.medinexus.service.PasswordResetService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label messageLabel;

    private final PasswordResetService resetService =
            new PasswordResetService();

    @FXML
    private void handleRequestReset() {

        String username =
                usernameField.getText().trim();

        if (username.isEmpty()) {

            showMessage(
                    "Please enter your username."
            );

            return;
        }

        try {

            String resetToken =
                    resetService.requestPasswordReset(
                            username
                    );

            /*
             * Do not reveal whether the username exists.
             *
             * For the development/demo version, an existing
             * account receives a temporary token that can be
             * entered on the reset-password screen.
             */
            if (resetToken == null) {

                showMessage(
                        "If the account exists, "
                                + "password recovery is available."
                );

                return;
            }

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/ResetPasswordView.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            ResetPasswordController controller =
                    loader.getController();

            controller.setResetToken(
                    resetToken
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

            showMessage(
                    "Unable to start password recovery."
            );
        }
    }

    @FXML
    private void handleBackToLogin() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/LoginView.fxml"
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

            showMessage(
                    "Unable to return to login."
            );
        }
    }

    private void showMessage(
            String message) {

        messageLabel.setText(
                message == null
                        ? "Unable to complete request."
                        : message
        );
    }
}