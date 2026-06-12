package com.restaurant.restaurant.billing;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BillingService — handles all database operations for the billing module.
 * Fetches served orders, builds receipts, confirms payment, resets tables.
 */
public class BillingService {

    // ---------------------------------------------------------------
    // DATABASE CONNECTION
    // This connects directly to the SQLite database file.
    // When Esther's DBConnection.java is ready, you can swap this out.
    // For now this lets you work and test independently.
    // ---------------------------------------------------------------
    private static final String DB_URL = "jdbc:sqlite:restaurant.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


    // ---------------------------------------------------------------
    // 1. GET ALL SERVED ORDERS (for the waiter's left panel list)
    // Returns a list of basic order info for orders that are 'served'
    // and have not yet been billed (not in billing table yet).
    // ---------------------------------------------------------------
    public List<OrderSummary> getServedOrders() {
        List<OrderSummary> list = new ArrayList<>();

        String sql = "SELECT o.order_id, o.table_number, o.total_amount, o.order_timestamp "
                   + "FROM orders o "
                   + "WHERE o.status = 'served' "
                   + "AND o.order_id NOT IN (SELECT order_id FROM billing) "
                   + "ORDER BY o.order_timestamp ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId       = rs.getInt("order_id");
                int tableNumber   = rs.getInt("table_number");
                double total      = rs.getDouble("total_amount");
                String timestamp  = rs.getString("order_timestamp");

                list.add(new OrderSummary(orderId, tableNumber, total, timestamp));
            }

        } catch (SQLException e) {
            System.err.println("BillingService - getServedOrders error: " + e.getMessage());
        }

        return list;
    }


    // ---------------------------------------------------------------
    // 2. BUILD A FULL RECEIPT for a given order ID
    // Joins order_items with food_items to get item names and prices.
    // Checks for any active discount set by admin.
    // ---------------------------------------------------------------
    public Receipt buildReceipt(int orderId) {

        List<Receipt.ReceiptItem> items = new ArrayList<>();
        int tableNumber = 0;
        LocalDateTime orderDateTime = LocalDateTime.now();
        double discountApplied = 0.0;

        // --- Fetch order header ---
        String orderSql = "SELECT table_number, order_timestamp FROM orders WHERE order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(orderSql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                tableNumber = rs.getInt("table_number");
                String ts = rs.getString("order_timestamp");
                // Parse the SQLite timestamp string to LocalDateTime
                orderDateTime = LocalDateTime.parse(ts.replace(" ", "T"));
            }

        } catch (SQLException e) {
            System.err.println("BillingService - buildReceipt (header) error: " + e.getMessage());
        }

        // --- Fetch line items ---
        String itemsSql = "SELECT f.name, oi.quantity, f.price "
                        + "FROM order_items oi "
                        + "JOIN food_items f ON oi.item_id = f.item_id "
                        + "WHERE oi.order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(itemsSql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name   = rs.getString("name");
                int qty       = rs.getInt("quantity");
                double price  = rs.getDouble("price");
                items.add(new Receipt.ReceiptItem(name, qty, price));
            }

        } catch (SQLException e) {
            System.err.println("BillingService - buildReceipt (items) error: " + e.getMessage());
        }

        // --- Fetch active discount if any ---
        // When admin discount feature is built, it will write to a 'discounts' table.
        // For now discount defaults to 0.0 safely.
        discountApplied = getActiveDiscount();

        return new Receipt(orderId, tableNumber, items, discountApplied, orderDateTime);
    }


    // ---------------------------------------------------------------
    // 3. CONFIRM PAYMENT
    // Inserts a billing record and flips the table back to 'available'.
    // Called when waiter clicks "Confirm Payment".
    // ---------------------------------------------------------------
    public boolean confirmPayment(Receipt receipt) {

        String insertBilling =
            "INSERT INTO billing (order_id, total_amount, discount_applied, "
          + "final_amount, payment_status, payment_method) "
          + "VALUES (?, ?, ?, ?, 'paid', 'cash')";

        String updateOrder =
            "UPDATE orders SET status = 'served', "
          + "completion_timestamp = CURRENT_TIMESTAMP WHERE order_id = ?";

        String updateTable =
            "UPDATE tables SET status = 'available' WHERE table_number = ?";

        try (Connection conn = getConnection()) {

            // Turn off auto-commit so all three happen together or not at all
            conn.setAutoCommit(false);

            try (PreparedStatement billStmt = conn.prepareStatement(insertBilling);
                 PreparedStatement orderStmt = conn.prepareStatement(updateOrder);
                 PreparedStatement tableStmt = conn.prepareStatement(updateTable)) {

                // Insert billing record
                billStmt.setInt(1, receipt.getOrderId());
                billStmt.setDouble(2, receipt.getSubtotal());
                billStmt.setDouble(3, receipt.getDiscountApplied());
                billStmt.setDouble(4, receipt.getTotal());
                billStmt.executeUpdate();

                // Update order completion timestamp
                orderStmt.setInt(1, receipt.getOrderId());
                orderStmt.executeUpdate();

                // Reset table to available
                tableStmt.setInt(1, receipt.getTableNumber());
                tableStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("BillingService - confirmPayment error: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("BillingService - connection error: " + e.getMessage());
            return false;
        }
    }


    // ---------------------------------------------------------------
    // 4. GET ACTIVE DISCOUNT
    // Returns the current discount amount in GHS.
    // Returns 0.0 if no discount is active.
    // Admin module will populate this when built.
    // ---------------------------------------------------------------
    private double getActiveDiscount() {
        // Placeholder — returns 0.0 until admin discount feature is built.
        // When Esther adds a discounts table, this method queries it.
        return 0.0;
    }


    // ---------------------------------------------------------------
    // INNER CLASS — OrderSummary
    // Lightweight object for the waiter's order list (left panel).
    // We don't need the full Receipt for the list, just basic info.
    // ---------------------------------------------------------------
    public static class OrderSummary {

        private int orderId;
        private int tableNumber;
        private double totalAmount;
        private String timestamp;

        public OrderSummary(int orderId, int tableNumber,
                            double totalAmount, String timestamp) {
            this.orderId     = orderId;
            this.tableNumber = tableNumber;
            this.totalAmount = totalAmount;
            this.timestamp   = timestamp;
        }

        public int getOrderId()       { return orderId; }
        public int getTableNumber()   { return tableNumber; }
        public double getTotalAmount(){ return totalAmount; }
        public String getTimestamp()  { return timestamp; }

        // This is what shows in the ListView on the waiter screen
        @Override
        public String toString() {
            return String.format("Order #%04d  —  Table %d", orderId, tableNumber);
        }
    }
}