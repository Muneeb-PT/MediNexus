package com.medinexus.model;

import java.time.LocalDateTime;

public class EmergencyRequest {

    private long emergencyId;

    private int patientId;
    private int requestedBy;

    private String emergencyType;
    private String description;

    private String severity;

    private String pickupLocation;
    private String destination;

    private EmergencyStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;

    private Long assignedVehicleId;

    public EmergencyRequest() {
    }

    public EmergencyRequest(
            long emergencyId,
            int patientId,
            int requestedBy,
            String emergencyType,
            String description,
            String severity,
            String pickupLocation,
            String destination,
            EmergencyStatus status,
            LocalDateTime requestedAt,
            LocalDateTime dispatchedAt,
            LocalDateTime arrivedAt,
            LocalDateTime completedAt,
            Long assignedVehicleId) {

        this.emergencyId = emergencyId;
        this.patientId = patientId;
        this.requestedBy = requestedBy;
        this.emergencyType = emergencyType;
        this.description = description;
        this.severity = severity;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.status = status;
        this.requestedAt = requestedAt;
        this.dispatchedAt = dispatchedAt;
        this.arrivedAt = arrivedAt;
        this.completedAt = completedAt;
        this.assignedVehicleId = assignedVehicleId;
    }

    public long getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(long emergencyId) {
        this.emergencyId = emergencyId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public LocalDateTime getArrivedAt() {
        return arrivedAt;
    }

    public void setArrivedAt(LocalDateTime arrivedAt) {
        this.arrivedAt = arrivedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getAssignedVehicleId() {
        return assignedVehicleId;
    }

    public void setAssignedVehicleId(Long assignedVehicleId) {
        this.assignedVehicleId = assignedVehicleId;
    }

    public boolean hasAssignedVehicle() {
        return assignedVehicleId != null;
    }
}