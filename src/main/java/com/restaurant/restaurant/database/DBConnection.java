package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(getDbUrl());
                System.out.println("Success: Connected to SQLite database file.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Resolves the correct JDBC URL for the SQLite database file.
     * Tries to locate database/restaurant.db on the classpath first
     * (works when running from compiled classes/resources).
     * Falls back to the source resources path if not found
     * (works when running directly from IntelliJ before a build).
     */
    private static String getDbUrl() {
        try {
            var resource = DBConnection.class.getClassLoader()
                    .getResource("database/restaurant.db");
            if (resource != null) {
                String path = java.nio.file.Paths.get(resource.toURI()).toString();
                return "jdbc:sqlite:" + path;
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Resource path resolution failed: "
                    + e.getMessage());
        }
        // Fallback — direct path into source resources
        return "jdbc:sqlite:src/main/resources/database/restaurant.db";
    }

    // Aligns with the closeAll() requirement in your interface doc
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed cleanly.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}