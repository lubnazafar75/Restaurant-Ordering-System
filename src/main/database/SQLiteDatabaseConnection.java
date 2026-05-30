package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SQLiteDatabaseConnection implements DatabaseConnection {
    
    // The single instance of our connection coordinator (Singleton Pattern)
    private static SQLiteDatabaseConnection instance;
    private Connection connection;
    private final String URL = "jdbc:sqlite:database/restaurant.db";

    // Private constructor prevents other classes from making copies
    private SQLiteDatabaseConnection() {
        try {
            // Load the SQLite JDBC Driver explicitly
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);
            System.out.println("Successfully connected to restaurant.db");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver missing!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection!");
            e.printStackTrace();
        }
    }

    // Public global access point to get the single instance
    public static synchronized SQLiteDatabaseConnection getInstance() {
        if (instance == null) {
            instance = new SQLiteDatabaseConnection();
        }
        return instance;
    }

    @Override
    public Connection getConnection() {
        try {
            // If connection was closed or dropped, revive it automatically
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public void initializeDatabase() {
        System.out.println("Initializing database schema components...");
        // Delegate to your existing DatabaseInitializer class
        DatabaseInitializer.initializeDatabase();
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void closeAll() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection safely terminated.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection.");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void executeSQLScript(String sqlFilePath) {
        // Fallback or custom script executor if you read external .sql strings
        System.out.println("Executing external script: " + sqlFilePath);
    }
}