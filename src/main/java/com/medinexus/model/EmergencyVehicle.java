package com.medinexus.model;

public class EmergencyVehicle {

    private long vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    private String driverName;
    private String driverPhone;
    private VehicleStatus status;
    private String currentLocation;

    public EmergencyVehicle() {
    }

    public EmergencyVehicle(
            long vehicleId,
            String vehicleNumber,
            String vehicleType,
            String driverName,
            String driverPhone,
            VehicleStatus status,
            String currentLocation) {

        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.status = status;
        this.currentLocation = currentLocation;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }
}