package com.medinexus.controller;

import com.medinexus.model.Notification;
import com.medinexus.model.User;
import com.medinexus.service.NotificationService;
import com.medinexus.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationController {

    @FXML
    private TableView<Notification> notificationTable;

    @FXML
    private TableColumn<Notification, String> typeColumn;

    @FXML
    private TableColumn<Notification, String> messageColumn;

    @FXML
    private TableColumn<Notification, String> dateColumn;

    @FXML
    private TableColumn<Notification, String> statusColumn;

    @FXML
    private Button markReadButton;

    private final NotificationService notificationService =
            new NotificationService();

    private final ObservableList<Notification> notifications =
            FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        safe(cellData.getValue().getType())
                )
        );

        messageColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        safe(cellData.getValue().getMessage())
                )
        );

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatDate(cellData.getValue())
                )
        );

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().isRead()
                                ? "READ"
                                : "UNREAD"
                )
        );

        notificationTable.setItems(notifications);

        loadNotifications();
    }

    private void loadNotifications() {

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert(
                    "Session Error",
                    "No user is currently logged in."
            );
            return;
        }

        List<Notification> result =
                notificationService.getUserNotifications(
                        currentUser.getUserId()
                );

        notifications.setAll(result);

        updateMarkReadButton();
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }

    @FXML
    private void handleMarkAsRead() {

        Notification selected =
                notificationTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            showAlert(
                    "No Selection",
                    "Please select a notification."
            );
            return;
        }

        if (selected.isRead()) {
            showAlert(
                    "Already Read",
                    "This notification is already marked as read."
            );
            return;
        }

        notificationService.markAsRead(
                selected.getNotificationId()
        );

        loadNotifications();
    }

    @FXML
    private void handleMarkAllAsRead() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert(
                    "Session Error",
                    "No user is currently logged in."
            );
            return;
        }

        notificationService.markAllAsRead(
                currentUser.getUserId()
        );

        loadNotifications();
    }

    private void updateMarkReadButton() {

        Notification selected =
                notificationTable.getSelectionModel()
                        .getSelectedItem();

        if (markReadButton != null) {
            markReadButton.setDisable(
                    selected == null || selected.isRead()
            );
        }
    }

    @FXML
    private void handleNotificationSelection() {
        updateMarkReadButton();
    }

    private String formatDate(Notification notification) {

        if (notification.getCreatedAt() == null) {
            return "";
        }

        return notification.getCreatedAt()
                .format(DATE_FORMATTER);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}