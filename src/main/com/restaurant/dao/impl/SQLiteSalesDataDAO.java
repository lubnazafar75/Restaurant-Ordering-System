package com.restaurant.dao.impl;

import com.restaurant.dao.SalesDataDAO;
import com.restaurant.model.SalesData;
import database.SQLiteDatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteSalesDataDAO implements SalesDataDAO {
    private final Connection connection = SQLiteDatabaseConnection.getInstance().getConnection();

    @Override
    public SalesData getTodaysSalesData() {
        String today = LocalDate.now().toString();
        Optional<SalesData> existing = getSalesDataByDate(today);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        SalesData data = new SalesData(today, getTodayRevenue(), getTodayOrderCount(), getTodayCustomerCount(), getTodayAverageOrderValue(), 13);
        String sql = "INSERT INTO sales_data (date, total_revenue, total_orders, total_customers, average_order_value, peak_hour) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getDate());
            pstmt.setDouble(2, data.getTotalRevenue());
            pstmt.setInt(3, data.getTotalOrders());
            pstmt.setInt(4, data.getTotalCustomers());
            pstmt.setDouble(5, data.getAverageOrderValue());
            pstmt.setInt(6, data.getPeakHour());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public Optional<SalesData> getSalesDataByDate(String date) {
        String sql = "SELECT * FROM sales_data WHERE date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new SalesData(
                        rs.getString("date"),
                        rs.getDouble("total_revenue"),
                        rs.getInt("total_orders"),
                        rs.getInt("total_customers"),
                        rs.getDouble("average_order_value"),
                        rs.getInt("peak_hour")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<SalesData> getSalesDataRange(String startDate, String endDate) {
        List<SalesData> list = new ArrayList<>();
        String sql = "SELECT * FROM sales_data WHERE date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SalesData(
                        rs.getString("date"),
                        rs.getDouble("total_revenue"),
                        rs.getInt("total_orders"),
                        rs.getInt("total_customers"),
                        rs.getDouble("average_order_value"),
                        rs.getInt("peak_hour")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateDailySales(String date) {
        double revenue = getTodayRevenue();
        int orders = getTodayOrderCount();
        double avgValue = getTodayAverageOrderValue();

        String sql = "UPDATE sales_data SET total_revenue = ?, total_orders = ?, average_order_value = ? WHERE date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, revenue);
            pstmt.setInt(2, orders);
            pstmt.setDouble(3, avgValue);
            pstmt.setString(4, date);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double getTodayRevenue() {
        String sql = "SELECT SUM(final_amount) FROM billing WHERE DATE(billing_date) = DATE('now') AND payment_status = 'completed'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public double getWeeklyRevenue() {
        String sql = "SELECT SUM(final_amount) FROM billing WHERE billing_date >= DATE('now', '-7 days') AND payment_status = 'completed'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public double getMonthlyRevenue() {
        String sql = "SELECT SUM(final_amount) FROM billing WHERE billing_date >= DATE('now', '-30 days') AND payment_status = 'completed'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public int getTodayOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = DATE('now')";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public double getTodayAverageOrderValue() {
        String sql = "SELECT AVG(final_amount) FROM billing WHERE DATE(billing_date) = DATE('now') AND payment_status = 'completed'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public Optional<Integer> getPeakHour(String date) {
        return Optional.of(13); // Default standard lunch rush timestamp representation
    }

    @Override
    public int getTodayCustomerCount() {
        String sql = "SELECT COUNT(DISTINCT table_id) FROM orders WHERE DATE(order_date) = DATE('now')";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public double getCurrentOccupancyRate() {
        String sql = "SELECT (SELECT COUNT(*) FROM tables WHERE status='occupied') * 1.0 / (SELECT COUNT(*) FROM tables)";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}