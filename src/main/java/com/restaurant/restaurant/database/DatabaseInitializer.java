package com.restaurant.restaurant.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        System.out.println("[Initializer] Verifying database tables...");

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("[Initializer Error] Cannot initialize schema: Connection is null.");
            return;
        }

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
                        "  table_number INTEGER," +
                        "  staff_id INTEGER," +
                        "  order_timestamp TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                        "  completion_timestamp TEXT," +
                        "  total_amount REAL DEFAULT 0.0," +
                        "  status TEXT DEFAULT 'pending'," +
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

        String createBillingTable =
                "CREATE TABLE IF NOT EXISTS billing (" +
                        "  billing_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  order_id INTEGER NOT NULL," +
                        "  total_amount REAL NOT NULL," +
                        "  discount_applied REAL DEFAULT 0.0," +
                        "  final_amount REAL NOT NULL," +
                        "  payment_status TEXT DEFAULT 'pending'," +
                        "  payment_method TEXT," +
                        "  receipt_number TEXT," +
                        "  billing_date TEXT DEFAULT (datetime('now','localtime'))," +
                        "  FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                        ");";

        String createOrderHistoryTable =
                "CREATE TABLE IF NOT EXISTS order_history (" +
                        "  history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  order_id INTEGER NOT NULL," +
                        "  status TEXT NOT NULL," +
                        "  changed_at TEXT DEFAULT (datetime('now','localtime'))," +
                        "  FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                        ");";

        String createSalesDataTable =
                "CREATE TABLE IF NOT EXISTS sales_data (" +
                        "  sales_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  date TEXT NOT NULL UNIQUE," +
                        "  total_revenue REAL DEFAULT 0.0," +
                        "  total_orders INTEGER DEFAULT 0," +
                        "  total_customers INTEGER DEFAULT 0," +
                        "  average_order_value REAL DEFAULT 0.0," +
                        "  peak_hour INTEGER DEFAULT 0" +
                        ");";

        String createReceiptRequestsTable =
                "CREATE TABLE IF NOT EXISTS receipt_requests (" +
                        "  request_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  order_id INTEGER NOT NULL," +
                        "  table_number INTEGER NOT NULL," +
                        "  status TEXT DEFAULT 'pending'," +
                        "  request_time TEXT DEFAULT (datetime('now','localtime'))," +
                        "  FOREIGN KEY(order_id) REFERENCES orders(order_id)" +
                        ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createStaffTable);
            stmt.execute(createFoodItemsTable);
            stmt.execute(createTablesSchema);
            stmt.execute(createOrdersTable);
            stmt.execute(createOrderItemsTable);
            stmt.execute(createBillingTable);
            stmt.execute(createOrderHistoryTable);
            stmt.execute(createSalesDataTable);
            stmt.execute(createReceiptRequestsTable);

            System.out.println("[Initializer] Schema verification complete! All core tables validated.");

            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[Initializer Error] Table structural compilation failed:");
            e.printStackTrace();
        }
    }

    // ── SEED DEFAULT DATA (only if tables are empty) ──────────
    private static void seedInitialData(Connection conn) {
        try {
            // Seed staff accounts
            if (isEmpty(conn, "staff")) {
                String[] staffInserts = {
                        "INSERT INTO staff (username, password, role, status) VALUES ('admin', 'admin123', 'Admin', 'active')",
                        "INSERT INTO staff (username, password, role, status) VALUES ('waiter', 'waiter123', 'Waiter', 'active')",
                        "INSERT INTO staff (username, password, role, status) VALUES ('kitchen', 'kitchen123', 'Kitchen Staff', 'active')",
                        "INSERT INTO staff (username, password, role, status) VALUES ('cashier', 'cashier123', 'Cashier', 'active')"
                };
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : staffInserts) stmt.execute(sql);
                }
                System.out.println("[Initializer] Seeded default staff accounts.");
            }

            // Seed food items
            if (isEmpty(conn, "food_items")) {
                String[][] items = {
                        {"Jollof Rice", "Main Course", "45.00"},
                        {"Fried Rice", "Main Course", "40.00"},
                        {"Banku & Tilapia", "Main Course", "55.00"},
                        {"Fufu & Soup", "Main Course", "50.00"},
                        {"Waakye", "Main Course", "35.00"},
                        {"Kenkey & Fish", "Main Course", "40.00"},
                        {"Grilled Chicken", "Chicken", "65.00"},
                        {"Fried Chicken", "Chicken", "60.00"},
                        {"Chicken Burger", "Chicken", "50.00"},
                        {"Chicken Sandwich", "Chicken", "45.00"},
                        {"Spring Rolls", "Appetizers", "25.00"},
                        {"Kelewele", "Appetizers", "20.00"},
                        {"Salad", "Appetizers", "22.00"},
                        {"Chips", "Appetizers", "18.00"},
                        {"Coca-Cola", "Drinks", "8.00"},
                        {"Malt Drink", "Drinks", "10.00"},
                        {"Fresh Juice", "Drinks", "15.00"},
                        {"Water", "Drinks", "5.00"},
                        {"Sobolo", "Drinks", "12.00"},
                        {"Ice Cream", "Desserts", "20.00"},
                        {"Cake Slice", "Desserts", "25.00"},
                        {"Fruit Salad", "Desserts", "18.00"}
                };
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO food_items (name, category, price, availability) VALUES (?, ?, ?, 'available')")) {
                    for (String[] item : items) {
                        pstmt.setString(1, item[0]);
                        pstmt.setString(2, item[1]);
                        pstmt.setDouble(3, Double.parseDouble(item[2]));
                        pstmt.executeUpdate();
                    }
                }
                System.out.println("[Initializer] Seeded " + items.length + " food items.");
            }

            // Seed tables (restaurant tables, not DB tables)
            if (isEmpty(conn, "tables")) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO tables (table_number, capacity, status) VALUES (?, ?, 'available')")) {
                    for (int i = 1; i <= 12; i++) {
                        pstmt.setInt(1, i);
                        pstmt.setInt(2, (i % 3 == 0) ? 6 : 4);
                        pstmt.executeUpdate();
                    }
                }
                System.out.println("[Initializer] Seeded 12 restaurant tables.");
            }

        } catch (SQLException e) {
            System.err.println("[Initializer Error] Seeding failed: " + e.getMessage());
        }
    }

    private static boolean isEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }
}