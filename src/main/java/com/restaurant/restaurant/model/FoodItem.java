package com.restaurant.restaurant.model;public class FoodItem {
    private int itemId;
    private String name;
    private String category;
    private double price;
    private String availability; // 'available' or 'unavailable'

    // Default Constructor
    public FoodItem() {}

    // Full 5-Parameter Constructor (Fixes your SQLiteFoodItemDAO compile errors)
    public FoodItem(int itemId, String name, String category, double price, String availability) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.availability = availability;
    }

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}