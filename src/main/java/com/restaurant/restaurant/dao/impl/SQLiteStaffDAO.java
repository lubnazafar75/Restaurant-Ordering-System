package com.restaurant.restaurant.dao.impl;
import com.restaurant.restaurant.dao.StaffDAO;
import com.restaurant.restaurant.model.Staff;
import com.restaurant.restaurant.database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteStaffDAO implements StaffDAO {
    private final Connection connection = DBConnection.getConnection();
    @Override
    public Optional<Staff> authenticate(String username, String password) {
        String sql = "SELECT * FROM staff WHERE username = ? AND password = ? AND status = 'active'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, evaluate hash comparisons here
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Staff staff = new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                    );
                    return Optional.of(staff);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Staff> getStaffById(int staffId) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
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
    public List<Staff> getAllActiveStaff() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE status = 'active'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Staff(
                    rs.getInt("staff_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Staff> getStaffByRole(String role) {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE role = ? AND status = 'active'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
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
    public Staff createStaff(Staff staff) {
        String sql = "INSERT INTO staff (username, password, role, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPassword());
            pstmt.setString(3, staff.getRole());
            pstmt.setString(4, staff.getStatus() != null ? staff.getStatus() : "active");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        staff.setStaffId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staff;
    }

    @Override
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE staff SET username = ?, role = ?, status = ? WHERE staff_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getStatus());
            pstmt.setInt(4, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deactivateStaff(int staffId) {
        String sql = "UPDATE staff SET status = 'inactive' WHERE staff_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changePassword(int staffId, String newPassword) {
        String sql = "UPDATE staff SET password = ? WHERE staff_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}