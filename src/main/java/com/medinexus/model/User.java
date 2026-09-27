package com.medinexus.model;

public class User {


    private int userId;
    private String username;
    private String fullName;
    private Role role;

    /*
     * Passwords are never stored as plaintext.
     * This field contains the BCrypt password hash.
     */
    private String passwordHash;

    public User() {
    }

    public User(
            int userId,
            String username,
            String fullName,
            Role role) {

        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public User(
            int userId,
            String username,
            String fullName,
            Role role,
            String passwordHash) {

        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


}
