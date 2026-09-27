package com.medinexus;

import com.medinexus.config.DatabaseConnection;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "MediNexus Database Connection"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "Connection successful!"
            );

            System.out.println(
                    "Database: "
                            + connection.getCatalog()
            );

            System.out.println(
                    "Server: "
                            + connection.getMetaData()
                            .getDatabaseProductName()
            );

            System.out.println(
                    "Version: "
                            + connection.getMetaData()
                            .getDatabaseProductVersion()
            );

            System.out.println(
                    "================================="
            );

        } catch (Exception e) {

            System.out.println(
                    "Database connection failed!"
            );

            e.printStackTrace();
        }
    }
}