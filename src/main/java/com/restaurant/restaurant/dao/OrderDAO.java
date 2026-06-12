package com.restaurant.restaurant.dao;
import com.restaurant.restaurant.model.Order;
import com.restaurant.restaurant.model.OrderDetail;
import java.util.List;
import java.util.Optional;

/**
 * OrderDAO - Handles order creation and management
 * Responsible for: order CRUD, order status updates, order tracking
 */
public interface OrderDAO {
    
    /**
     * Create new order
     * @param order Order object (will set auto-generated order_id)
     * @return Created order with order_id populated
     */
    Order createOrder(Order order);
    
    /**
     * Get order by ID
     */
    Optional<Order> getOrderById(int orderId);
    
    /**
     * Get all orders for a specific table
     */
    List<Order> getOrdersByTableId(int tableId);
    
    /**
     * Get all active orders (not completed/cancelled)
     */
    List<Order> getActiveOrders();
    
    /**
     * Get orders by status (pending, preparing, ready, served, etc.)
     */
    List<Order> getOrdersByStatus(String status);
    
    /**
     * Get orders from today
     */
    List<Order> getTodaysOrders();
    
    /**
     * Update order status (pending → preparing → ready → served → completed)
     */
    boolean updateOrderStatus(int orderId, String newStatus);
    
    /**
     * Update preparation status for kitchen staff
     */
    boolean updatePreparationStatus(int orderId, String prepStatus);
    
    /**
     * Set estimated wait time for customer
     */
    boolean setEstimatedWaitTime(int orderId, int minutes);
    
    /**
     * Get order with all its items (JOIN order_items)
     */
    Optional<OrderDetail> getOrderDetail(int orderId);
    
    /**
     * Cancel an order
     */
    boolean cancelOrder(int orderId);
    
    /**
     * Mark order as served (ready for payment)
     */
    boolean markOrderServed(int orderId);
    
    /**
     * Get order count for a specific date (for analytics)
     */
    int getOrderCountByDate(String date);
}