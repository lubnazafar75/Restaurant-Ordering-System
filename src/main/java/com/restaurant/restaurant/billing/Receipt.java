package com.restaurant.restaurant.billing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Data model representing a single receipt.
 * Holds all information needed to display or print a receipt.
 */
public class Receipt {

    // --- Constants ---
    public static final double VAT_RATE = 0.10; // 10% VAT

    // --- Fields ---
    private int orderId;
    private int tableNumber;
    private List<ReceiptItem> items;
    private double subtotal;
    private double vatAmount;
    private double total;
    private double discountApplied;
    private LocalDateTime orderDateTime;

    // --- Constructor ---
    public Receipt(int orderId, int tableNumber, List<ReceiptItem> items,
                   double discountApplied, LocalDateTime orderDateTime) {

        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.items = items;
        this.discountApplied = discountApplied;
        this.orderDateTime = orderDateTime;

        // Calculate totals automatically
        this.subtotal = calculateSubtotal();
        this.vatAmount = (subtotal - discountApplied) * VAT_RATE;
        this.total = (subtotal - discountApplied) + vatAmount;
    }

    // --- Private Calculation ---
    private double calculateSubtotal() {
        double sum = 0;
        for (ReceiptItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }

    // --- Getters ---
    public int getOrderId()               { return orderId; }
    public int getTableNumber()           { return tableNumber; }
    public List<ReceiptItem> getItems()   { return items; }
    public double getSubtotal()           { return subtotal; }
    public double getVatAmount()          { return vatAmount; }
    public double getTotal()              { return total; }
    public double getDiscountApplied()    { return discountApplied; }
    public LocalDateTime getOrderDateTime() { return orderDateTime; }

    // --- Formatted date and time for display ---
    public String getFormattedDate() {
        return orderDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedTime() {
        return orderDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedOrderId() {
        return String.format("#%04d", orderId); // e.g. #0042
    }


    /**
     * Inner class representing one line item on the receipt.
     * e.g. "Jollof Rice  x2  GHS 35.00  GHS 70.00"
     */
    public static class ReceiptItem {

        private String foodName;
        private int quantity;
        private double unitPrice;

        public ReceiptItem(String foodName, int quantity, double unitPrice) {
            this.foodName = foodName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getFoodName()  { return foodName; }
        public int getQuantity()     { return quantity; }
        public double getUnitPrice() { return unitPrice; }

        public double getLineTotal() {
            return quantity * unitPrice;
        }
    }
}