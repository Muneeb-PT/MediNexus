package com.medinexus;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.SessionManager;

public class SessionTest {

    public static void main(String[] args) {

        User user =
                new User(
                        101,
                        "muneeb",
                        "Mohammed Muneeb",
                        Role.PATIENT
                );

        SessionManager.login(user);


        System.out.println(
                "Logged in: "
                        + SessionManager.isLoggedIn()
        );

        System.out.println(
                "Username: "
                        + SessionManager
                        .getCurrentUser()
                        .getUsername()
        );

        System.out.println(
                "Role: "
                        + SessionManager
                        .getCurrentUser()
                        .getRole()
        );


        SessionManager.logout();


        System.out.println(
                "Logged in after logout: "
                        + SessionManager.isLoggedIn()
        );
    }
}