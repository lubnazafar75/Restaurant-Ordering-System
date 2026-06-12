package com.restaurant.restaurant.ordering;

public class OrderItem {
    private String name;
    private double price;
    private int quantity;

    public OrderItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // ADD THESE TWO:
    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { if (this.quantity > 1) this.quantity--; }

    public double getSubtotal() { return price * quantity; }
}