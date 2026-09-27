package com.medinexus.model;

import java.time.LocalDateTime;

public class Notification {

    private int notificationId;

    private long userId;

    private String title;

    private String message;

    private String type;

    private LocalDateTime createdAt;

    private boolean read;

    // -----------------------------------------
    // Constructors
    // -----------------------------------------

    public Notification() {
    }

    public Notification(
            int notificationId,
            long userId,
            String title,
            String message,
            String type,
            LocalDateTime createdAt) {

        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = false;
    }

    // -----------------------------------------
    // Getters and Setters
    // -----------------------------------------

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(
            int notificationId) {

        this.notificationId = notificationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {

        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {

        this.read = read;
    }

    // -----------------------------------------
    // toString
    // -----------------------------------------

    @Override
    public String toString() {

        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", read=" + read +
                '}';
    }
}