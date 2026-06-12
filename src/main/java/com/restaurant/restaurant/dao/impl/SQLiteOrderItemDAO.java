package com.restaurant.restaurant.dao.impl;

import com.restaurant.restaurant.dao.OrderItemDAO;
import com.restaurant.restaurant.model.OrderItem;
import com.restaurant.restaurant.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteOrderItemDAO implements OrderItemDAO {
    private final Connection connection =  DBConnection.getConnection();

    @Override
    public OrderItem addItemToOrder(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity, subtotal, special_notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getOrderId());
            pstmt.setInt(2, item.getItemId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getSubtotal());
            pstmt.setString(5, item.getSpecialNotes());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setOrderItemId(generatedKeys.getInt(1));
                    }
                }
                syncOrderTotal(item.getOrderId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("order_id"),
                        rs.getInt("item_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("subtotal")
                    );
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setSpecialNotes(rs.getString("special_notes"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<OrderItem> getOrderItemById(int orderItemId) {
        String sql = "SELECT * FROM order_items WHERE order_item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("order_id"),
                        rs.getInt("item_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("subtotal")
                    );
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setSpecialNotes(rs.getString("special_notes"));
                    return Optional.of(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean updateQuantity(int orderItemId, int newQuantity) {
        Optional<OrderItem> itemOpt = getOrderItemById(orderItemId);
        if (!itemOpt.isPresent()) return false;
        OrderItem item = itemOpt.get();
        
        double unitPrice = item.getSubtotal() / item.getQuantity();
        double newSubtotal = unitPrice * newQuantity;

        String sql = "UPDATE order_items SET quantity = ?, subtotal = ? WHERE order_item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setDouble(2, newSubtotal);
            pstmt.setInt(3, orderItemId);
            boolean updated = pstmt.executeUpdate() > 0;
            if (updated) {
                syncOrderTotal(item.getOrderId());
            }
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addSpecialInstructions(int orderItemId, String instructions) {
        String sql = "UPDATE order_items SET special_notes = ? WHERE order_item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, instructions);
            pstmt.setInt(2, orderItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeItemFromOrder(int orderItemId) {
        Optional<OrderItem> itemOpt = getOrderItemById(orderItemId);
        if (!itemOpt.isPresent()) return false;
        int orderId = itemOpt.get().getOrderId();

        String sql = "DELETE FROM order_items WHERE order_item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderItemId);
            boolean removed = pstmt.executeUpdate() > 0;
            if (removed) {
                syncOrderTotal(orderId);
            }
            return removed;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double getOrderTotal(int orderId) {
        String sql = "SELECT SUM(subtotal) FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void syncOrderTotal(int orderId) throws SQLException {
        double total = getOrderTotal(orderId);
        String sql = "UPDATE orders SET total_amount = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, total);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }
}