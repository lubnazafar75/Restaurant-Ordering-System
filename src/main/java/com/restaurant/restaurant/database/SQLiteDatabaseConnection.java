package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLiteDatabaseConnection — Singleton facade kept for the DatabaseConnection
 * interface contract.
 *
 * FIXED: this used to open its own independent connection using the URL
 * "jdbc:sqlite::resource:database/restaurant.db". That special ":resource:"
 * form asks the SQLite driver to extract the bundled resource into a private
 * temp copy, which is normally read-only and disconnected from the real
 * database/restaurant.db file that every other DAO writes through via
 * DBConnection. Because Launcher.main() initialized the schema through THIS
 * class while every DAO writes through DBConnection, the app was effectively
 * running two separate "databases" — any data inserted while the app was
 * running could go missing/unreadable from other screens.
 *
 * This class now simply delegates to DBConnection so there is exactly one
 * physical connection (and therefore one source of truth) in the whole app.
 */
public class SQLiteDatabaseConnection implements DatabaseConnection {

    // The single instance of our connection coordinator (Singleton Pattern)
    private static SQLiteDatabaseConnection instance;

    private SQLiteDatabaseConnection() {
        // No-op: connection is fully managed by DBConnection now.
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
        return DBConnection.getConnection();
    }

    @Override
    public void initializeDatabase() {
        System.out.println("Initializing database schema components...");
        DatabaseInitializer.initializeDatabase();
    }

    @Override
    public boolean isConnected() {
        try {
            Connection conn = DBConnection.getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void closeAll() {
        // No global connection to close anymore
        System.out.println("Connections are managed per request — nothing to close.");
    }
    @Override
    public void executeSQLScript(String sqlFilePath) {
        // Fallback or custom script executor if you read external .sql strings
        System.out.println("Executing external script: " + sqlFilePath);
    }
}
