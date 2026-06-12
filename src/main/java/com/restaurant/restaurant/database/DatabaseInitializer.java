package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        System.out.println("[Initializer] Verifying database tables...");
        
        // Grab our active connection loop from DBConnection
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("[Initializer Error] Cannot initialize schema: Connection is null.");
            return;
        }

        // SQL scripts to dynamically build your entire system schema if missing
        String createStaffTable = 
            "CREATE TABLE IF NOT EXISTS staff (" +
            "  staff_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT NOT NULL UNIQUE," +
            "  password TEXT NOT NULL," +
            "  role TEXT NOT NULL," +
            "  status TEXT DEFAULT 'active'" +
            ");";

        String createFoodItemsTable = 
            "CREATE TABLE IF NOT EXISTS food_items (" +
            "  item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL UNIQUE," +
            "  category TEXT NOT NULL," +
            "  price REAL NOT NULL," +
            "  availability TEXT DEFAULT 'available'" +
            ");";

        String createTablesSchema = 
            "CREATE TABLE IF NOT EXISTS tables (" +
            "  table_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  table_number INTEGER NOT NULL UNIQUE," +
            "  capacity INTEGER NOT NULL," +
            "  status TEXT DEFAULT 'available'" +
            ");";

        String createOrdersTable = 
            "CREATE TABLE IF NOT EXISTS orders (" +
            "  order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  table_id INTEGER," +
            "  staff_id INTEGER," +
            "  order_date TEXT NOT NULL," +
            "  total_amount REAL DEFAULT 0.0," +
            "  status TEXT DEFAULT 'pending'," +
            "  FOREIGN KEY(table_id) REFERENCES tables(table_id)," +
            "  FOREIGN KEY(staff_id) REFERENCES staff(staff_id)" +
            ");";

        String createOrderItemsTable = 
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "  order_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  order_id INTEGER NOT NULL," +
            "  item_id INTEGER NOT NULL," +
            "  quantity INTEGER NOT NULL," +
            "  subtotal REAL NOT NULL," +
            "  special_notes TEXT," +
            "  FOREIGN KEY(order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
            "  FOREIGN KEY(item_id) REFERENCES food_items(item_id)" +
            ");";

        try (Statement stmt = conn.createStatement()) {
            // Execute batch definitions securely
            stmt.execute(createStaffTable);
            stmt.execute(createFoodItemsTable);
            stmt.execute(createTablesSchema);
            stmt.execute(createOrdersTable);
            stmt.execute(createOrderItemsTable);
            
            System.out.println("[Initializer] Schema verification complete! All core tables validated.");
        } catch (SQLException e) {
            System.err.println("[Initializer Error] Table structural compilation failed:");
            e.printStackTrace();
        }
    }
}