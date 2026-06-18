package com.restaurant.restaurant.session;

/**
 * Centralized session management for the application.
 * Follows the Singleton pattern to ensure only one session exists at a time.
 */
public class UserSession {
    private static UserSession instance;

    private String staffId;
    private String staffName;
    private String role;

    private UserSession(String staffId, String staffName, String role) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.role = role;
    }

    public static UserSession getInstance() {
        return instance;
    }

    /**
     * Initializes a new session upon successful login.
     */
    public static void login(String staffId, String staffName, String role) {
        instance = new UserSession(staffId, staffName, role);
    }

    /**
     * Completely clears the session data.
     */
    public static void logout() {
        instance = null;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return instance != null;
    }
}
