package com.restaurant.restaurant.dao.impl;

import com.restaurant.restaurant.dao.BillingDAO;
import com.restaurant.restaurant.model.Billing;
import com.restaurant.restaurant.database.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SQLiteBillingDAO implements BillingDAO {
    private final Connection connection =  DBConnection.getConnection();

    @Override
    public Billing createBilling(Billing billing) {
        String sql = "INSERT INTO billing (order_id, total_amount, discount_amount, final_amount, payment_method, payment_status, receipt_number, billing_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, billing.getOrderId());
            pstmt.setDouble(2, billing.getTotalAmount());
            pstmt.setDouble(3, billing.getDiscountAmount());
            pstmt.setDouble(4, billing.getFinalAmount());
            pstmt.setString(5, billing.getPaymentMethod());
            pstmt.setString(6, billing.getPaymentStatus() != null ? billing.getPaymentStatus() : "pending");
            pstmt.setString(7, billing.getReceiptNumber() != null ? billing.getReceiptNumber() : generateReceiptNumber());
            pstmt.setString(8, billing.getBillingDate() != null ? billing.getBillingDate() : LocalDate.now().toString());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billing.setBillingId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return billing;
    }

    @Override
    public Optional<Billing> getBillingById(int billingId) {
        String sql = "SELECT * FROM billing WHERE billing_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, billingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractBillingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Billing> getBillingByOrderId(int orderId) {
        String sql = "SELECT * FROM billing WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractBillingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean updatePaymentStatus(int billingId, String status) {
        String sql = "UPDATE billing SET payment_status = ? WHERE billing_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, billingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean applyDiscount(int billingId, double discountAmount) {
        Optional<Billing> billOpt = getBillingById(billingId);
        if (!billOpt.isPresent()) return false;
        Billing bill = billOpt.get();
        double newFinal = bill.getTotalAmount() - discountAmount;

        String sql = "UPDATE billing SET discount_amount = ?, final_amount = ? WHERE billing_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, discountAmount);
            pstmt.setDouble(2, newFinal);
            pstmt.setInt(3, billingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean confirmPayment(int billingId, String paymentMethod) {
        String sql = "UPDATE billing SET payment_method = ?, payment_status = 'completed' WHERE billing_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paymentMethod);
            pstmt.setInt(2, billingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateReceiptNumber() {
        return "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public List<Billing> getBillingByDate(String date) {
        List<Billing> list = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE DATE(billing_date) = DATE(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBillingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public double getTotalRevenueByDate(String date) {
        String sql = "SELECT SUM(final_amount) FROM billing WHERE DATE(billing_date) = DATE(?) AND payment_status = 'completed'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
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

    @Override
    public Map<String, Double> getRevenueByPaymentMethod(String date) {
        Map<String, Double> report = new HashMap<>();
        String sql = "SELECT payment_method, SUM(final_amount) FROM billing WHERE DATE(billing_date) = DATE(?) AND payment_status = 'completed' GROUP BY payment_method";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    report.put(rs.getString(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    private Billing extractBillingFromResultSet(ResultSet rs) throws SQLException {
        return new Billing(
            rs.getInt("billing_id"),
            rs.getInt("order_id"),
            rs.getDouble("total_amount"),
            rs.getDouble("discount_amount"),
            rs.getDouble("final_amount"),
            rs.getString("payment_method"),
            rs.getString("payment_status"),
            rs.getString("receipt_number"),
            rs.getString("billing_date")
        );
    }
}