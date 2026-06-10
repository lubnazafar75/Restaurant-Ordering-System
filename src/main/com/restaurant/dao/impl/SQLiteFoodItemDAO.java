package com.restaurant.dao.impl;

import com.restaurant.dao.FoodItemDAO;
import com.restaurant.model.FoodItem;
import database.SQLiteDatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteFoodItemDAO implements FoodItemDAO {
    private final Connection connection = SQLiteDatabaseConnection.getInstance().getConnection();

    @Override
    public List<FoodItem> getAllAvailableItems() {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE availability = 'available'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new FoodItem(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getString("availability")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<FoodItem> getFoodItemById(int foodItemId) {
        String sql = "SELECT * FROM food_items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodItemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("availability")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<FoodItem> getItemsByCategory(String category) {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE category = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("availability")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<FoodItem> searchItemsByName(String namePattern) {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + namePattern + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("availability")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM food_items";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public FoodItem createFoodItem(FoodItem item) {
        String sql = "INSERT INTO food_items (name, category, price, availability) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getAvailability() != null ? item.getAvailability() : "available");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setItemId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public boolean updateFoodItem(FoodItem item) {
        String sql = "UPDATE food_items SET name = ?, category = ?, price = ?, availability = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getAvailability());
            pstmt.setInt(5, item.getItemId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setAvailability(int foodItemId, String availability) {
        String sql = "UPDATE food_items SET availability = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, availability);
            pstmt.setInt(2, foodItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteFoodItem(int foodItemId) {
        String sql = "DELETE FROM food_items WHERE item_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}