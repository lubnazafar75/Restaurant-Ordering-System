package com.restaurant.restaurant.dao.impl;

import com.restaurant.restaurant.dao.TableDAO;
import com.restaurant.restaurant.model.Table;
import com.restaurant.restaurant.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteTableDAO implements TableDAO {
    private final Connection connection =  DBConnection.getConnection();

    @Override
    public List<Table> getAllTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM tables";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Table> getTableById(int tableId) {
        String sql = "SELECT * FROM tables WHERE table_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Table(
                        rs.getInt("table_id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
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
    public Optional<Table> getTableByNumber(int tableNumber) {
        String sql = "SELECT * FROM tables WHERE table_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Table(
                        rs.getInt("table_id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
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
    public List<Table> getAvailableTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = 'available'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Table> getOccupiedTables() {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = 'occupied'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Table> getAvailableTablesByCapacity(int minCapacity) {
        List<Table> list = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = 'available' AND capacity >= ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, minCapacity);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Table(
                        rs.getInt("table_id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
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
    public Optional<Integer> getCurrentOrderId(int tableId) {
        String sql = "SELECT order_id FROM orders WHERE table_id = ? AND status != 'completed' AND status != 'cancelled' LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("order_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Table createTable(Table table) {
        String sql = "INSERT INTO tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus() != null ? table.getStatus() : "available");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        table.setTableId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return table;
    }

    @Override
    public boolean updateTable(Table table) {
        String sql = "UPDATE tables SET table_number = ?, capacity = ?, status = ? WHERE table_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus());
            pstmt.setInt(4, table.getTableId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean markTableAvailable(int tableId) {
        String sql = "UPDATE tables SET status = 'available' WHERE table_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean markTableOccupied(int tableId, int orderId) {
        String sql = "UPDATE tables SET status = 'occupied' WHERE table_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}