package com.medinexus.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3307/medinexus"
                    + "?serverTimezone=Asia/Kolkata";

    private static final String URL =
            setting("MEDINEXUS_DB_URL", "medinexus.db.url", DEFAULT_URL);

    private static final String USER =
            setting("MEDINEXUS_DB_USER", "medinexus.db.user", "root");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String password =
                setting("MEDINEXUS_DB_PASSWORD", "medinexus.db.password", "");

        if (password.isBlank()) {
            throw new SQLException(
                    "Database password is not configured. Set the "
                            + "MEDINEXUS_DB_PASSWORD environment variable "
                            + "or the -Dmedinexus.db.password JVM property."
            );
        }

        return DriverManager.getConnection(URL, USER, password);
    }

    private static String setting(
            String environmentVariable,
            String systemProperty,
            String defaultValue) {

        String propertyValue = System.getProperty(systemProperty);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String environmentValue = System.getenv(environmentVariable);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        return defaultValue;
    }
}
