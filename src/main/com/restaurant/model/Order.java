package com.restaurant.model;

public class Order {
    private int orderId;
    private int tableId;
    private int staffId;
    private String orderDate;
    private double totalAmount;
    private String status; // 'pending', 'preparing', 'ready', 'served', 'completed', 'cancelled'

    // Default Constructor
    public Order() {}

    // Full Constructor
    public Order(int orderId, int tableId, int staffId, String orderDate, double totalAmount, String status) {
        this.orderId = orderId;
        this.tableId = tableId;
        this.staffId = staffId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters (Fixes your SQLiteOrderDAO compile errors)
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}