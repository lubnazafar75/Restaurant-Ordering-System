package com.restaurant.restaurant.login;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import com.restaurant.restaurant.util.AnimationUtil;
import javafx.scene.layout.VBox;

import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField staffIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox loginCard;
    @FXML private Button loginButton;

    // ── Session fields — ALL empty by default ─
    public static String sessionStaffId   = "";
    public static String sessionStaffName = "";
    public static String sessionRole      = "";

    @FXML
    public void initialize() {
        // ── Always clear fields on load so previous credentials don't show ──
        if (staffIdField   != null) staffIdField.clear();
        if (passwordField  != null) passwordField.clear();
        if (errorLabel     != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    @FXML
    public void handleLogin() {
        String staffId  = staffIdField.getText().trim();
        String password = passwordField.getText().trim();

        if (staffId.isEmpty()) {
            showError("Please enter your Staff ID.");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            return;
        }

        if (authenticate(staffId, password)) {
            hideError();
            SceneManager.navigateTo(NavigationUtil.STAFF_DASHBOARD);
        } else {
            showError("Invalid Staff ID or password. Please try again.");
            passwordField.clear();
        }
    }


    private boolean authenticate(String staffId, String password) {
        // Try database first
        try {
            java.sql.Connection conn =
                    com.restaurant.restaurant.database.DBConnection.getConnection();
            if (conn != null) {
                String sql =
                        "SELECT * FROM staff WHERE " +
                                "(username = ? OR CAST(staff_id AS TEXT) = ?) " +
                                "AND password = ? AND status = 'active'";
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, staffId);
                stmt.setString(2, staffId);
                stmt.setString(3, password);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    sessionStaffId   = String.valueOf(rs.getInt("staff_id"));
                    sessionStaffName = rs.getString("username");
                    sessionRole      = rs.getString("role");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[Login] DB error: " + e.getMessage());
        }

        // Fallback test accounts
        if (staffId.equals("admin") && password.equals("admin123")) {
            sessionStaffId   = "001";
            sessionStaffName = "Admin User";
            sessionRole      = "Admin";
            return true;
        }
        if (staffId.equals("waiter") && password.equals("waiter123")) {
            sessionStaffId   = "002";
            sessionStaffName = "John Waiter";
            sessionRole      = "Waiter";
            return true;
        }
        if (staffId.equals("kitchen") && password.equals("kitchen123")) {
            sessionStaffId   = "003";
            sessionStaffName = "Chef Mary";
            sessionRole      = "Kitchen Staff";
            return true;
        }
        return false;
    }

    @FXML
    public void handleBack() {
        SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}