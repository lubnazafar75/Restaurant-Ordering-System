package com.restaurant.model;

public class Staff {
    private int staffId;
    private String username;
    private String password;
    private String role;
    private String status; // 'active' or 'inactive'

    // Default Constructor
    public Staff() {}

    // Full 5-Parameter Constructor (Fixes your DAO Compile Error)
    public Staff(int staffId, String username, String password, String role, String status) {
        this.staffId = staffId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    // Getters and Setters
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}