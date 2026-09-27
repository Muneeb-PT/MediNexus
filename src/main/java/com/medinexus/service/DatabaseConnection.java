package com.medinexus.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3307/medinexus"
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Kolkata"
                    + "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    /*
     * Keep your actual MySQL password here.
     * Do not send the password to me.
     */
    private static final String PASSWORD = "Muneebpt@25";

    private DatabaseConnection() {
        // Prevent object creation.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}