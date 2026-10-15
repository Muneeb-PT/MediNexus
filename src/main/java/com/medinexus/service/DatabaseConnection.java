
package com.medinexus.service;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
        // Prevent object creation.
    }

    public static Connection getConnection() throws SQLException {
        return com.medinexus.config.DatabaseConnection.getConnection();
    }
}
