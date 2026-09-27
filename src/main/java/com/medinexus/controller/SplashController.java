package com.medinexus.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private StackPane splashRoot;

    @FXML
    public void initialize() {

        PauseTransition pause =
                new PauseTransition(Duration.seconds(2));

        pause.setOnFinished(event -> openLoginScreen());

        pause.play();
    }

    private void openLoginScreen() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("/fxml/LoginView.fxml")
                    );

            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);

            Stage stage =
                    (Stage) splashRoot.getScene().getWindow();

            stage.setScene(scene);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}