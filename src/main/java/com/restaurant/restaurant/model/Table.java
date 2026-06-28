package com.restaurant.restaurant.model;

public class Table {
    private int tableId;
    private int tableNumber;
    private int capacity;
    private String status; // 'available' or 'occupied'

    // Default Constructor
    public Table() {}

    // Full 4-Parameter Constructor (Fixes your SQLiteTableDAO compile errors)
    public Table(int tableId, int tableNumber, int capacity, String status) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    // Getters and Setters
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public int capacity() { return capacity; } // Supports standard field mapping
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}