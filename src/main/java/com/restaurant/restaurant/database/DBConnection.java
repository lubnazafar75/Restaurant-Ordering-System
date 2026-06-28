package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            String url = getDbUrl();
            Connection conn = DriverManager.getConnection(url);

            try (var stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }

            return conn;

        } catch (Exception e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            return null;
        }
    }

    private static String getDbUrl() {
        return "jdbc:sqlite:G:\\\\Restaurant-Ordering-System-master\\\\restaurant.db";
    }
}
