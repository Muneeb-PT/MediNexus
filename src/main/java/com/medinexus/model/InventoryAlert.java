package com.medinexus.model;

public class InventoryAlert {

    public enum AlertType {
        LOW_STOCK,
        OUT_OF_STOCK
    }

    private final int medicineId;
    private final String medicineName;
    private final int quantity;
    private final int reorderLevel;
    private final AlertType alertType;

    public InventoryAlert(
            int medicineId,
            String medicineName,
            int quantity,
            int reorderLevel,
            AlertType alertType) {

        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.alertType = alertType;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public boolean isOutOfStock() {
        return alertType == AlertType.OUT_OF_STOCK;
    }

    public boolean isLowStock() {
        return alertType == AlertType.LOW_STOCK;
    }

}