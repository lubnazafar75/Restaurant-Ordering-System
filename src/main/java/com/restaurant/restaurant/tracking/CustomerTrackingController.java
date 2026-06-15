package com.restaurant.restaurant.tracking;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import com.restaurant.restaurant.ordering.OrderController;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerTrackingController {

    @FXML private VBox trackingScreen;
    @FXML private VBox receivedScreen;
    @FXML private Label orderInfoLabel;
    @FXML private Label statusEmojiLabel;
    @FXML private Label currentStatusLabel;
    @FXML private Label statusDescLabel;
    @FXML private Label estimatedTimeLabel;
    @FXML private Label step1Circle, step2Circle, step3Circle, step4Circle;
    @FXML private Label step1Title, step2Title, step3Title, step4Title;
    @FXML private Label stepDescriptionLabel;
    @FXML private Region progressFill;
    @FXML private Button foodReceivedBtn;

    // Default starting step — updated via DB polling once an order is placed
    private int currentStep = 1;

    // Polling timeline that checks the order's status in the database
    private Timeline statusPoller;

    // The order_id this screen is tracking (from the most recently placed order)
    private int trackedOrderId = -1;

    @FXML
    public void initialize() {
        updateProgressDisplay();
        startTracking();
    }

    /**
     * Begins polling the database for status updates on the
     * most recently placed order (com.restaurant.restaurant.order.OrderController.lastOrderId).
     */
    private void startTracking() {
        trackedOrderId = com.restaurant.restaurant.ordering.OrderController.lastOrderId;

        if (trackedOrderId == -1) {
            // No order placed yet — nothing to track
            return;
        }

        // Poll every 3 seconds
        statusPoller = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> pollOrderStatus())
        );
        statusPoller.setCycleCount(Timeline.INDEFINITE);
        statusPoller.play();

        // Run once immediately on load
        pollOrderStatus();
    }

    /**
     * Checks the database for the current status of the tracked order
     * and updates the displayed step accordingly.
     */
    private void pollOrderStatus() {
        if (trackedOrderId == -1) return;

        Connection conn = com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT status FROM orders WHERE order_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trackedOrderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    int step = mapStatusToStep(status);
                    if (step != currentStep) {
                        setStep(step);
                    }
                    // Stop polling once delivered
                    if ("delivered".equalsIgnoreCase(status) && statusPoller != null) {
                        statusPoller.stop();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerTrackingController] Poll error: "
                    + e.getMessage());
        }
    }

    /**
     * Maps the database status string to the tracking step number.
     */
    private int mapStatusToStep(String status) {
        if (status == null) return currentStep;
        switch (status.toLowerCase()) {
            case "pending":   return 1;
            case "preparing": return 2;
            case "ready":     return 3;
            case "delivered": return 4;
            default:          return currentStep;
        }
    }

    /**
     * Called externally (or by DB polling) to manually advance
     * the customer's order tracking status.
     * Step 1 = Order Received
     * Step 2 = Being Prepared
     * Step 3 = Ready for Delivery
     * Step 4 = Delivered
     */
    public void setStep(int step) {
        this.currentStep = step;
        updateProgressDisplay();
    }

    private void updateProgressDisplay() {
        switch (currentStep) {
            case 1:
                statusEmojiLabel.setText("⏳");
                currentStatusLabel.setText("Order Received");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #6B7280; -fx-font-size: 24px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your order has been received and will be prepared shortly.");
                estimatedTimeLabel.setText("Estimated wait: ~15 minutes");
                stepDescriptionLabel.setText("Your order has been placed successfully");
                foodReceivedBtn.setVisible(false);
                foodReceivedBtn.setManaged(false);
                break;
            case 2:
                statusEmojiLabel.setText("👨‍🍳");
                currentStatusLabel.setText("Being Prepared");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #F59E0B; -fx-font-size: 24px; -fx-font-weight: bold;");
                statusDescLabel.setText("Our chefs are working on your order right now!");
                estimatedTimeLabel.setText("Estimated wait: ~10 minutes");
                stepDescriptionLabel.setText("Our chefs are preparing your meal");
                foodReceivedBtn.setVisible(false);
                foodReceivedBtn.setManaged(false);
                break;
            case 3:
                statusEmojiLabel.setText("🍽");
                currentStatusLabel.setText("Ready for Delivery");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #10B981; -fx-font-size: 24px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your order is ready! A waiter will bring it to your table.");
                estimatedTimeLabel.setText("Almost there — just a few more minutes!");
                stepDescriptionLabel.setText("Your order is ready and on its way");
                foodReceivedBtn.setVisible(true);
                foodReceivedBtn.setManaged(true);
                break;
            case 4:
                statusEmojiLabel.setText("✅");
                currentStatusLabel.setText("Delivered!");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #10B981; -fx-font-size: 24px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your food has been delivered. Enjoy your meal!");
                estimatedTimeLabel.setText("Enjoy your meal! 🎉");
                stepDescriptionLabel.setText("Enjoy your meal!");
                foodReceivedBtn.setVisible(true);
                foodReceivedBtn.setManaged(true);
                break;
        }

        updateStep(step1Circle, step1Title, 1, "✓");
        updateStep(step2Circle, step2Title, 2, "2");
        updateStep(step3Circle, step3Title, 3, "3");
        updateStep(step4Circle, step4Title, 4, "4");

        // Update progress bar fill width as a percentage (1->25%, 2->50%, 3->75%, 4->100%)
        double percent = currentStep / 4.0;
        progressFill.prefWidthProperty().unbind();
        progressFill.maxWidthProperty().unbind();
        progressFill.maxWidthProperty().bind(
                ((Region) progressFill.getParent()).widthProperty().multiply(percent));
    }

    private void updateStep(Label circle, Label title, int stepNumber, String activeText) {
        circle.getStyleClass().removeAll(
                "step-circle-done", "step-circle-active", "step-circle-inactive");

        if (stepNumber < currentStep) {
            circle.getStyleClass().add("step-circle-done");
            circle.setText("✓");
            title.setStyle(
                    "-fx-text-fill: #1F2937; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (stepNumber == currentStep) {
            circle.getStyleClass().add("step-circle-active");
            circle.setText(activeText.equals("✓") ? "✓" : String.valueOf(stepNumber));
            title.setStyle(
                    "-fx-text-fill: #1F2937; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            circle.getStyleClass().add("step-circle-inactive");
            circle.setText(String.valueOf(stepNumber));
            title.setStyle(
                    "-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    // ─── BUTTON HANDLERS ─────────────────────────────────────

    @FXML
    public void handleFoodReceived() {
        showScreen(receivedScreen);
    }

    @FXML
    public void handleRequestReceipt() {
        if (statusPoller != null) statusPoller.stop();
        SceneManager.navigateTo(NavigationUtil.customer_billing);
    }

    @FXML
    public void handleOrderMore() {
        if (statusPoller != null) statusPoller.stop();
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleBackToMenu() {
        if (statusPoller != null) statusPoller.stop();
        SceneManager.navigateToMenu();
    }

    // ─── BOTTOM NAV ──────────────────────────────────────────

    @FXML
    public void handleNavMenu() {
        if (statusPoller != null) statusPoller.stop();
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleNavBill() {
        if (statusPoller != null) statusPoller.stop();
        SceneManager.navigateTo(NavigationUtil.customer_billing);
    }

    @FXML
    public void handleNavRate() {
        showAlert("Coming Soon", "Rating feature will be available soon!");
    }

    @FXML
    public void handleNavHelp() {
        showAlert("Need Help?",
                "Please call a waiter or speak to our staff at the counter.");
    }

    // ─── HELPERS ─────────────────────────────────────────────

    private void showScreen(javafx.scene.Node screen) {
        trackingScreen.setVisible(false);
        trackingScreen.setManaged(false);
        receivedScreen.setVisible(false);
        receivedScreen.setManaged(false);
        screen.setVisible(true);
        screen.setManaged(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}