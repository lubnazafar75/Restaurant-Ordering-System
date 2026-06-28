package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBConnection {

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            // Reuse existing connection if still open
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(getDbUrl());

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA busy_timeout=5000;");
            }

            System.out.println("Success: Connected to SQLite database file.");
            return connection;

        } catch (Exception e) {
            System.err.println("[DBConnection] Failed: " + e.getMessage());
            connection = null;
            return null;
        }
    }

    private static String getDbUrl() {
        try {
            // ── Try classpath resource first (works in compiled jar + IDE) ──
            var resource = DBConnection.class
                    .getClassLoader()
                    .getResource("database/restaurant.db");

            if (resource != null) {
                String path = java.nio.file.Paths
                        .get(resource.toURI())
                        .toString();
                System.out.println("[DBConnection] Using DB at: " + path);
                return "jdbc:sqlite:" + path;
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] URL build error: " + e.getMessage());
        }

        // ── Fallback to source path (when running directly in IDE) ──
        return "jdbc:sqlite:src/main/resources/database/restaurant.db";
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DBConnection] Connection closed.");
            } catch (Exception e) {
                System.err.println("[DBConnection] Close error: " + e.getMessage());
            }
        }
    }
}