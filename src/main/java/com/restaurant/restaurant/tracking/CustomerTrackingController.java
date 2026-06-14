package com.restaurant.restaurant.tracking;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CustomerTrackingController {

    @FXML private VBox trackingScreen;
    @FXML private VBox receivedScreen;
    @FXML private Label orderInfoLabel;
    @FXML private Label statusEmojiLabel;
    @FXML private Label currentStatusLabel;
    @FXML private Label statusDescLabel;
    @FXML private Label step1Circle, step2Circle, step3Circle, step4Circle;
    @FXML private Label step1Check, step2Check, step3Check, step4Check;
    @FXML private Button foodReceivedBtn;
    @FXML private Label estimatedTimeLabel;
    @FXML private Label step2Title, step3Title, step4Title;

    // Default starting step — kitchen staff will manually advance this
    // via the Kitchen Monitor screen (status update buttons).
    private int currentStep = 1;

    @FXML
    public void initialize() {
        updateProgressDisplay();
    }

    /**
     * Called externally (e.g. by kitchen staff updates / database polling)
     * to manually advance the customer's order tracking status.
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
                foodReceivedBtn.setVisible(true);
                foodReceivedBtn.setManaged(true);
                break;
        }
        updateStep(step1Circle, step1Check, null, 1);
        updateStep(step2Circle, step2Check, step2Title, 2);
        updateStep(step3Circle, step3Check, step3Title, 3);
        updateStep(step4Circle, step4Check, step4Title, 4);
    }
    /**
     * Updates a single step's circle and checkmark style based on
     * its position relative to the current step.
     * Uses style classes from application.css for consistency:
     *   step-circle-done     -> steps already completed
     *   step-circle-active   -> the current in-progress step
     *   step-circle-inactive -> steps not yet reached
     */
    private void updateStep(Label circle, Label check, Label title, int stepNumber) {
        circle.getStyleClass().removeAll(
                "step-circle-done", "step-circle-active", "step-circle-inactive");

        if (stepNumber < currentStep) {
            circle.getStyleClass().add("step-circle-done");
            check.setStyle(
                    "-fx-text-fill: #10B981; -fx-font-size: 18px; -fx-font-weight: bold;");
            if (title != null) {
                title.setStyle(
                        "-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        } else if (stepNumber == currentStep) {
            circle.getStyleClass().add("step-circle-active");
            check.setStyle("-fx-text-fill: #E5E7EB; -fx-font-size: 18px;");
            if (title != null) {
                title.setStyle(
                        "-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        } else {
            circle.getStyleClass().add("step-circle-inactive");
            check.setStyle("-fx-text-fill: #E5E7EB; -fx-font-size: 18px;");
            if (title != null) {
                title.setStyle(
                        "-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        }
    }

    // ─── BUTTON HANDLERS ─────────────────────────────────────

    @FXML
    public void handleFoodReceived() {
        showScreen(receivedScreen);
    }

    @FXML
    public void handleRequestReceipt() {
        SceneManager.navigateTo(NavigationUtil.customer_billing);
    }

    @FXML
    public void handleOrderMore() {
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleBackToMenu() {
        SceneManager.navigateToMenu();
    }

    // ─── BOTTOM NAV ──────────────────────────────────────────

    @FXML
    public void handleNavMenu() {
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleNavBill() {
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