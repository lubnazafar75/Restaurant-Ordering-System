package com.restaurant.restaurant.model;
public class Billing {
    private int billingId;
    private int orderId;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private String paymentMethod; // 'cash', 'card', 'momo'
    private String paymentStatus; // 'pending', 'completed', 'failed'
    private String receiptNumber;
    private String billingDate;

    // Default Constructor
    public Billing() {}

    // Full Constructor
    public Billing(int billingId, int orderId, double totalAmount, double discountAmount, 
                   double finalAmount, String paymentMethod, String paymentStatus, 
                   String receiptNumber, String billingDate) {
        this.billingId = billingId;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.receiptNumber = receiptNumber;
        this.billingDate = billingDate;
    }

    // Getters and Setters
    public int getBillingId() { return billingId; }
    public void setBillingId(int billingId) { this.billingId = billingId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getBillingDate() { return billingDate; }
    public void setBillingDate(String billingDate) { this.billingDate = billingDate; }
}