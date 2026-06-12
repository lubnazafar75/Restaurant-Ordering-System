package com.restaurant.dao.impl;

import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.OrderItemDAO;
import com.restaurant.model.Order;
import com.restaurant.model.OrderDetail;
import com.restaurant.model.OrderItem;
import database.SQLiteDatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteOrderDAO implements OrderDAO {
    private final Connection connection = SQLiteDatabaseConnection.getInstance().getConnection();

    @Override
    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (table_id, staff_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, order.getTableId());
            pstmt.setInt(2, order.getStaffId());
            pstmt.setString(3, order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now().toString());
            pstmt.setDouble(4, order.getTotalAmount());
            pstmt.setString(5, order.getStatus() != null ? order.getStatus() : "pending");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    @Override
    public Optional<Order> getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractOrderFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getOrdersByTableId(int tableId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE table_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractOrderFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Order> getActiveOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status NOT IN ('completed', 'cancelled')";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractOrderFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Order> getTodaysOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE DATE(order_date) = DATE('now')";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePreparationStatus(int orderId, String prepStatus) {
        // maps to your internal status lifecycle transitions
        return updateOrderStatus(orderId, prepStatus);
    }

    @Override
    public boolean setEstimatedWaitTime(int orderId, int minutes) {
        // Extensible tracker metadata field (returns true to fulfill UI hooks safely)
        return true;
    }

    @Override
    public Optional<OrderDetail> getOrderDetail(int orderId) {
        Optional<Order> orderOpt = getOrderById(orderId);
        if (orderOpt.isPresent()) {
            OrderItemDAO itemDAO = new SQLiteOrderItemDAO();
            List<OrderItem> items = itemDAO.getOrderItems(orderId);
            return Optional.of(new OrderDetail(orderOpt.get(), items));
        }
        return Optional.empty();
    }

    @Override
    public boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    @Override
    public boolean markOrderServed(int orderId) {
        return updateOrderStatus(orderId, "served");
    }

    @Override
    public int getOrderCountByDate(String date) {
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = DATE(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setTableId(rs.getInt("table_id"));
        order.setStaffId(rs.getInt("staff_id"));
        order.setOrderDate(rs.getString("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        return order;
    }
}