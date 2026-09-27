package com.medinexus.controller;

import com.medinexus.service.PasswordResetService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label tokenLabel;

    private final PasswordResetService resetService =
            new PasswordResetService();

    private String resetToken;

    /**
     * Receives the development/demo reset token.
     */
    public void setResetToken(
            String resetToken) {

        this.resetToken = resetToken;

        /*
         * Development/demo only.
         *
         * A production system would deliver the token
         * through verified email/SMS instead.
         */
        if (tokenLabel != null) {

            tokenLabel.setText(
                    "Demo reset token: "
                            + resetToken
            );
        }
    }

    @FXML
    private void handleResetPassword() {

        String newPassword =
                newPasswordField.getText();

        String confirmPassword =
                confirmPasswordField.getText();

        if (newPassword == null
                || newPassword.isEmpty()) {

            showMessage(
                    "Please enter a new password."
            );

            return;
        }

        if (!newPassword.equals(
                confirmPassword)) {

            showMessage(
                    "Passwords do not match."
            );

            return;
        }

        if (resetToken == null
                || resetToken.isBlank()) {

            showMessage(
                    "Reset token is missing."
            );

            return;
        }

        try {

            resetService.resetPassword(
                    resetToken,
                    newPassword
            );

            showMessage(
                    "Password reset successfully. "
                            + "Please log in."
            );

            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (IllegalArgumentException e) {

            showMessage(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            showMessage(
                    "Unable to reset password. "
                            + "Please try again."
            );
        }
    }

    @FXML
    private void handleBackToLogin() {

        openLogin();
    }

    private void openLogin() {

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
                    (Stage) newPasswordField
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