package com.medinexus.service;

import com.medinexus.model.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {
        // Prevent creating objects of this class.
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}