package database;

/**
 * DatabaseConnection - Connection pool and initialization
 * Responsible for: SQLite connection management, schema initialization
 */
public interface DatabaseConnection {
    void initializeDatabase();
    java.sql.Connection getConnection();
    void closeAll();
    boolean isConnected();
    void executeSQLScript(String sqlFilePath);
}