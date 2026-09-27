package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.SupportTicket;
import com.medinexus.model.User;
import com.medinexus.repository.SupportTicketRepository;

import java.util.List;

public class SupportTicketService {

    private final SupportTicketRepository repository;

    public SupportTicketService() {
        this.repository = new SupportTicketRepository();
    }

    public SupportTicket createTicket(
            String type,
            String category,
            String subject,
            String description) {

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "You must be logged in to create a support ticket."
            );
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket type is required."
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket category is required."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket subject is required."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket description is required."
            );
        }

        if (description.trim().length() < 10) {
            throw new IllegalArgumentException(
                    "Please provide more information."
            );
        }

        String priority = determinePriority(type);

        SupportTicket ticket =
                new SupportTicket(
                        0,
                        currentUser.getUserId(),
                        type.trim(),
                        category.trim(),
                        subject.trim(),
                        description.trim(),
                        priority,
                        "OPEN",
                        null,
                        null,
                        null,
                        null
                );

        return repository.add(ticket);
    }

    public List<SupportTicket> getMyTickets() {

        User currentUser = requireUser();

        return repository.findByUserId(
                currentUser.getUserId()
        );
    }

    public List<SupportTicket> getAllTickets() {

        User currentUser = requireUser();

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to view all support tickets."
            );
        }

        return repository.findAll();
    }

    public void updateStatus(
            long ticketId,
            String status) {

        User currentUser = requireUser();

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to update support tickets."
            );
        }

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException(
                    "Invalid support ticket status."
            );
        }

        SupportTicket ticket =
                repository.findById(ticketId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Support ticket not found."
            );
        }

        repository.updateStatus(
                ticketId,
                status
        );
    }

    public void assignTicket(
            long ticketId,
            long assignedTo) {

        User currentUser = requireUser();

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to assign support tickets."
            );
        }

        repository.assignTicket(
                ticketId,
                assignedTo
        );
    }

    private User requireUser() {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw new SecurityException(
                    "You must be logged in."
            );
        }

        return currentUser;
    }

    private String determinePriority(String type) {

        if ("Complaint".equalsIgnoreCase(type)) {
            return "HIGH";
        }

        if ("Support Request".equalsIgnoreCase(type)) {
            return "NORMAL";
        }

        if ("Feedback".equalsIgnoreCase(type)
                || "Suggestion".equalsIgnoreCase(type)) {
            return "LOW";
        }

        return "NORMAL";
    }

    private boolean isValidStatus(String status) {

        return "OPEN".equals(status)
                || "IN_PROGRESS".equals(status)
                || "RESOLVED".equals(status)
                || "CLOSED".equals(status);
    }

    public List<User> getSupportStaff() {

        requireUser();

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to view support staff."
            );
        }

        return repository.findSupportStaff();
    }

}