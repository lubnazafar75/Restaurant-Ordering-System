package com.restaurant.dao.impl;

import com.restaurant.dao.OrderHistoryDAO;
import com.restaurant.model.OrderHistory;
import database.SQLiteDatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteOrderHistoryDAO implements OrderHistoryDAO {
    private final Connection connection = SQLiteDatabaseConnection.getInstance().getConnection();

    @Override
    public List<OrderHistory> getCompletedOrders(String date) {
        List<OrderHistory> list = new ArrayList<>();
        String sql = "SELECT order_id, table_id, total_amount, order_date, status FROM orders WHERE DATE(order_date) = DATE(?) AND status = 'completed'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderHistory(
                        rs.getInt("order_id"),
                        rs.getInt("table_id"),
                        "Customer",
                        rs.getDouble("total_amount"),
                        rs.getString("order_date"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<OrderHistory> getArchivedOrder(int orderId) {
        String sql = "SELECT order_id, table_id, total_amount, order_date, status FROM orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new OrderHistory(
                        rs.getInt("order_id"),
                        rs.getInt("table_id"),
                        "Customer",
                        rs.getDouble("total_amount"),
                        rs.getString("order_date"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteOldHistory(int daysToKeep) {
        // Enforces the 1-day archival retention policy cleanly
        String sql = "DELETE FROM orders WHERE status = 'completed' AND order_date < DATE('now', '-' || ? || ' days')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, daysToKeep);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<OrderHistory> getHistoryByDateRange(String startDate, String endDate) {
        List<OrderHistory> list = new ArrayList<>();
        String sql = "SELECT order_id, table_id, total_amount, order_date, status FROM orders WHERE order_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderHistory(
                        rs.getInt("order_id"),
                        rs.getInt("table_id"),
                        "Customer",
                        rs.getDouble("total_amount"),
                        rs.getString("order_date"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}