package com.restaurant.restaurant.dao;
import com.restaurant.restaurant.model.Billing;
import java.util.List;
import java.util.Optional;

/**
 * BillingDAO - Handles payment and billing operations
 * Responsible for: billing records, payment tracking, receipt generation
 */
public interface BillingDAO {
    
    /**
     * Create billing record for completed order
     */
    Billing createBilling(Billing billing);
    
    /**
     * Get billing record by ID
     */
    Optional<Billing> getBillingById(int billingId);
    
    /**
     * Get billing by order ID
     */
    Optional<Billing> getBillingByOrderId(int orderId);
    
    /**
     * Update payment status (pending → completed/failed)
     */
    boolean updatePaymentStatus(int billingId, String status);
    
    /**
     * Apply discount to billing
     */
    boolean applyDiscount(int billingId, double discountAmount);
    
    /**
     * Confirm payment received
     */
    boolean confirmPayment(int billingId, String paymentMethod);
    
    /**
     * Generate receipt number
     */
    String generateReceiptNumber();
    
    /**
     * Get billing records for a date (for reconciliation)
     */
    List<Billing> getBillingByDate(String date);
    
    /**
     * Get total revenue for a date
     */
    double getTotalRevenueByDate(String date);
    
    /**
     * Get payment breakdown by method (cash, card, momo)
     */
    java.util.Map<String, Double> getRevenueByPaymentMethod(String date);
}