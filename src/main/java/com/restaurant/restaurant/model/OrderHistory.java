package com.restaurant.restaurant.model;
public class OrderHistory {
    private int orderId;
    private int tableId;
    private String customerName;
    private double totalAmount;
    private String completionDate;
    private String status;

    public OrderHistory() {}

    public OrderHistory(int orderId, int tableId, String customerName, double totalAmount, String completionDate, String status) {
        this.orderId = orderId;
        this.tableId = tableId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.completionDate = completionDate;
        this.status = status;
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCompletionDate() { return completionDate; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}