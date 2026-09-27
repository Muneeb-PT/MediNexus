package com.medinexus.controller;

import com.medinexus.model.Role;
import com.medinexus.model.SupportTicket;
import com.medinexus.model.User;
import com.medinexus.service.SessionManager;
import com.medinexus.service.SupportTicketService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupportTicketManagementController {

    @FXML
    private TableView<SupportTicket> ticketTable;

    @FXML
    private TableColumn<SupportTicket, Long> ticketIdColumn;

    @FXML
    private TableColumn<SupportTicket, Long> userIdColumn;

    @FXML
    private TableColumn<SupportTicket, String> typeColumn;

    @FXML
    private TableColumn<SupportTicket, String> categoryColumn;

    @FXML
    private TableColumn<SupportTicket, String> subjectColumn;

    @FXML
    private TableColumn<SupportTicket, String> priorityColumn;

    @FXML
    private TableColumn<SupportTicket, String> statusColumn;

    @FXML
    private TableColumn<SupportTicket, String> assignedToColumn;

    @FXML
    private TableColumn<SupportTicket, String> createdAtColumn;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> priorityFilterComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<User> assignedToComboBox;

    @FXML
    private Label detailsLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button assignButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    private final SupportTicketService supportTicketService =
            new SupportTicketService();

    private final ObservableList<SupportTicket> ticketList =
            FXCollections.observableArrayList();

    private final ObservableList<User> supportStaff =
            FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAccessDenied(
                    "You must be logged in."
            );
            return;
        }

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            showAccessDenied(
                    "Only Admin and Receptionist users can access Support Ticket Management."
            );
            return;
        }

        configureTable();
        configureFilters();
        configureStaffComboBox();
        configureTicketSelection();

        ticketTable.setItems(ticketList);

        loadSupportStaff();
        loadTickets();
    }

    private void configureTable() {

        ticketIdColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "ticketId"
                )
        );

        userIdColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "userId"
                )
        );

        typeColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "type"
                )
        );

        categoryColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "category"
                )
        );

        subjectColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "subject"
                )
        );

        priorityColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "priority"
                )
        );

        statusColumn.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>(
                        "status"
                )
        );

        assignedToColumn.setCellValueFactory(
                cellData -> {

                    Long assignedTo =
                            cellData.getValue().getAssignedTo();

                    String value =
                            assignedTo == null
                                    ? "Unassigned"
                                    : String.valueOf(assignedTo);

                    return new SimpleStringProperty(value);
                }
        );

        createdAtColumn.setCellValueFactory(
                cellData -> {

                    LocalDateTime createdAt =
                            cellData.getValue().getCreatedAt();

                    String value =
                            createdAt == null
                                    ? "-"
                                    : createdAt.format(
                                    DATE_FORMATTER
                            );

                    return new SimpleStringProperty(value);
                }
        );
    }

    private void configureFilters() {

        statusFilterComboBox.setItems(
                FXCollections.observableArrayList(
                        "ALL",
                        "OPEN",
                        "IN_PROGRESS",
                        "RESOLVED",
                        "CLOSED"
                )
        );

        statusFilterComboBox.setValue("ALL");

        priorityFilterComboBox.setItems(
                FXCollections.observableArrayList(
                        "ALL",
                        "HIGH",
                        "NORMAL",
                        "LOW"
                )
        );

        priorityFilterComboBox.setValue("ALL");

        statusComboBox.setItems(
                FXCollections.observableArrayList(
                        "OPEN",
                        "IN_PROGRESS",
                        "RESOLVED",
                        "CLOSED"
                )
        );
    }

    private void configureStaffComboBox() {

        assignedToComboBox.setItems(supportStaff);

        assignedToComboBox.setCellFactory(
                listView -> new ListCell<>() {

                    @Override
                    protected void updateItem(
                            User user,
                            boolean empty) {

                        super.updateItem(user, empty);

                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(
                                    user.getFullName()
                                            + " ("
                                            + user.getRole()
                                            + ")"
                            );
                        }
                    }
                }
        );

        assignedToComboBox.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            User user,
                            boolean empty) {

                        super.updateItem(user, empty);

                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(
                                    user.getFullName()
                                            + " ("
                                            + user.getRole()
                                            + ")"
                            );
                        }
                    }
                }
        );
    }

    private void configureTicketSelection() {

        ticketTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldTicket, newTicket) -> {

                            if (newTicket == null) {
                                return;
                            }

                            displayTicketDetails(newTicket);

                            statusComboBox.setValue(
                                    newTicket.getStatus()
                            );

                            selectAssignedStaff(newTicket);
                        }
                );
    }

    private void selectAssignedStaff(
            SupportTicket ticket) {

        if (ticket.getAssignedTo() == null) {

            assignedToComboBox.setValue(null);
            return;
        }

        User assignedUser =
                supportStaff.stream()
                        .filter(user ->
                                user.getUserId()
                                        == ticket.getAssignedTo()
                        )
                        .findFirst()
                        .orElse(null);

        assignedToComboBox.setValue(assignedUser);
    }

    private void loadSupportStaff() {

        try {

            List<User> staff =
                    supportTicketService.getSupportStaff();

            supportStaff.setAll(staff);

            /*
             * If a ticket is already selected,
             * restore its assigned staff after
             * refreshing the staff list.
             */
            SupportTicket selectedTicket =
                    ticketTable.getSelectionModel()
                            .getSelectedItem();

            if (selectedTicket != null) {
                selectAssignedStaff(selectedTicket);
            }

        } catch (SecurityException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Unable to load support staff."
            );
        }
    }

    @FXML
    private void handleRefresh() {

        loadSupportStaff();
        loadTickets();
    }

    @FXML
    private void handleFilter() {

        loadTickets();
    }

    private void loadTickets() {

        try {

            List<SupportTicket> tickets =
                    supportTicketService.getAllTickets();

            String selectedStatus =
                    statusFilterComboBox.getValue();

            String selectedPriority =
                    priorityFilterComboBox.getValue();

            ticketList.clear();

            for (SupportTicket ticket : tickets) {

                boolean statusMatches =
                        selectedStatus == null
                                || selectedStatus.equals("ALL")
                                || selectedStatus.equals(
                                ticket.getStatus()
                        );

                boolean priorityMatches =
                        selectedPriority == null
                                || selectedPriority.equals("ALL")
                                || selectedPriority.equals(
                                ticket.getPriority()
                        );

                if (statusMatches && priorityMatches) {
                    ticketList.add(ticket);
                }
            }

            messageLabel.setText(
                    ticketList.size()
                            + " ticket(s) found."
            );

        } catch (SecurityException e) {

            showAccessDenied(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Unable to load support tickets."
            );
        }
    }

    private void displayTicketDetails(
            SupportTicket ticket) {

        String assigned =
                ticket.getAssignedTo() == null
                        ? "Unassigned"
                        : String.valueOf(
                        ticket.getAssignedTo()
                );

        String created =
                ticket.getCreatedAt() == null
                        ? "-"
                        : ticket.getCreatedAt()
                        .format(DATE_FORMATTER);

        String updated =
                ticket.getUpdatedAt() == null
                        ? "-"
                        : ticket.getUpdatedAt()
                        .format(DATE_FORMATTER);

        String resolved =
                ticket.getResolvedAt() == null
                        ? "-"
                        : ticket.getResolvedAt()
                        .format(DATE_FORMATTER);

        detailsLabel.setText(
                "Ticket #" + ticket.getTicketId()
                        + "\n\n"
                        + "User ID: " + ticket.getUserId()
                        + "\n"
                        + "Type: " + ticket.getType()
                        + "\n"
                        + "Category: " + ticket.getCategory()
                        + "\n"
                        + "Subject: " + ticket.getSubject()
                        + "\n"
                        + "Priority: " + ticket.getPriority()
                        + "\n"
                        + "Status: " + ticket.getStatus()
                        + "\n"
                        + "Assigned To: " + assigned
                        + "\n"
                        + "Created: " + created
                        + "\n"
                        + "Updated: " + updated
                        + "\n"
                        + "Resolved: " + resolved
                        + "\n\n"
                        + "Description:\n"
                        + ticket.getDescription()
        );
    }

    @FXML
    private void handleUpdateStatus() {

        SupportTicket selectedTicket =
                ticketTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedTicket == null) {

            messageLabel.setText(
                    "Select a ticket first."
            );

            return;
        }

        String newStatus =
                statusComboBox.getValue();

        if (newStatus == null
                || newStatus.isBlank()) {

            messageLabel.setText(
                    "Select a status."
            );

            return;
        }

        try {

            supportTicketService.updateStatus(
                    selectedTicket.getTicketId(),
                    newStatus
            );

            messageLabel.setText(
                    "Ticket #"
                            + selectedTicket.getTicketId()
                            + " status updated to "
                            + newStatus
                            + "."
            );

            loadTickets();

        } catch (SecurityException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Unable to update ticket status."
            );
        }
    }

    @FXML
    private void handleAssign() {

        SupportTicket selectedTicket =
                ticketTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedTicket == null) {

            messageLabel.setText(
                    "Select a ticket first."
            );

            return;
        }

        User assignedUser =
                assignedToComboBox.getValue();

        if (assignedUser == null) {

            messageLabel.setText(
                    "Select a staff member."
            );

            return;
        }

        try {

            supportTicketService.assignTicket(
                    selectedTicket.getTicketId(),
                    assignedUser.getUserId()
            );

            messageLabel.setText(
                    "Ticket #"
                            + selectedTicket.getTicketId()
                            + " assigned to "
                            + assignedUser.getFullName()
                            + "."
            );

            loadTickets();

        } catch (SecurityException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            messageLabel.setText(
                    "Unable to assign the ticket."
            );
        }
    }

    @FXML
    private void handleBack() {

        DashboardController.navigateToDashboard();
    }

    private void showAccessDenied(
            String message) {

        if (messageLabel != null) {
            messageLabel.setText(message);
        }

        if (ticketTable != null) {
            ticketTable.setDisable(true);
        }

        if (updateStatusButton != null) {
            updateStatusButton.setDisable(true);
        }

        if (assignButton != null) {
            assignButton.setDisable(true);
        }

        if (refreshButton != null) {
            refreshButton.setDisable(true);
        }
    }
}