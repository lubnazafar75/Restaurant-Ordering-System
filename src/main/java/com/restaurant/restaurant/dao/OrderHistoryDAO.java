package com.restaurant.dao;

import com.restaurant.model.OrderHistory;
import java.util.List;
import java.util.Optional;

/**
 * OrderHistoryDAO - Handles order history archival (1-day retention)
 * Responsible for: archiving completed orders, cleanup of old records
 */
public interface OrderHistoryDAO {
    
    /**
     * Get all completed orders (admin only)
     */
    List<OrderHistory> getCompletedOrders(String date);
    
    /**
     * Get archived order by order_id
     */
    Optional<OrderHistory> getArchivedOrder(int orderId);
    
    /**
     * Delete order history older than 1 day (call this in cleanup task)
     */
    boolean deleteOldHistory(int daysToKeep);
    
    /**
     * Get order history for audit trail
     */
    List<OrderHistory> getHistoryByDateRange(String startDate, String endDate);
}