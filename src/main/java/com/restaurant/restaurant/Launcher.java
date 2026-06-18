package com.restaurant.restaurant;

import com.restaurant.restaurant.database.SQLiteDatabaseConnection;

public class Launcher {

    public static void main(String[] args) {

        // Initialize database ONCE when app starts
        SQLiteDatabaseConnection.getInstance().initializeDatabase();

        // Start JavaFX application
        App.main(args);
    }
}
