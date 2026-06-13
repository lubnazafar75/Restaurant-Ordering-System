package com.restaurant.restaurant.menu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {


    private static String getDbUrl() {
        try {
            var resource = MenuDAO.class.getClassLoader()
                    .getResource("database/restaurant.db");
            if (resource != null) {
                return "jdbc:sqlite:" +
                        java.nio.file.Paths.get(resource.toURI()).toString();
            }
        } catch (Exception e) {
            System.err.println("MenuDAO DB path error: " + e.getMessage());
        }
        return "jdbc:sqlite:src/main/resources/database/restaurant.db";
    }
    private Connection getConnection() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return DriverManager.getConnection(getDbUrl());
    }

    public List<FoodItem> getAllItems() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void addItem(FoodItem item) {
        String sql = "INSERT INTO food_items (name, category, price, available) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isAvailable());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateItem(FoodItem item) {
        String sql = "UPDATE food_items SET name=?, category=?, price=?, available=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getCategory());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isAvailable());
            stmt.setInt(5, item.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(int id) {
        String sql = "DELETE FROM food_items WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FoodItem> searchItems(String keyword) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE name LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}