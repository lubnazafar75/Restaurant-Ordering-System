package com.restaurant.restaurant.tracking;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
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

    private int currentStep = 1;

    @FXML
    public void initialize() {
        updateProgressDisplay();
    }

    // Called externally to update status from database later
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
                        "-fx-text-fill: #a0aec0; -fx-font-size: 22px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your order has been received and will be prepared shortly.");
                foodReceivedBtn.setVisible(false);
                foodReceivedBtn.setManaged(false);
                break;
            case 2:
                statusEmojiLabel.setText("👨‍🍳");
                currentStatusLabel.setText("Being Prepared");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #F59E0B; -fx-font-size: 22px; -fx-font-weight: bold;");
                statusDescLabel.setText("Our chefs are working on your order right now!");
                foodReceivedBtn.setVisible(false);
                foodReceivedBtn.setManaged(false);
                break;
            case 3:
                statusEmojiLabel.setText("🍽");
                currentStatusLabel.setText("Ready for Delivery");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #00E676; -fx-font-size: 22px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your order is ready! A waiter will bring it to your table.");
                foodReceivedBtn.setVisible(true);
                foodReceivedBtn.setManaged(true);
                break;
            case 4:
                statusEmojiLabel.setText("✅");
                currentStatusLabel.setText("Delivered!");
                currentStatusLabel.setStyle(
                        "-fx-text-fill: #00E676; -fx-font-size: 22px; -fx-font-weight: bold;");
                statusDescLabel.setText(
                        "Your food has been delivered. Enjoy your meal!");
                foodReceivedBtn.setVisible(true);
                foodReceivedBtn.setManaged(true);
                break;
        }
        updateStep(step1Circle, step1Check, 1);
        updateStep(step2Circle, step2Check, 2);
        updateStep(step3Circle, step3Check, 3);
        updateStep(step4Circle, step4Check, 4);
    }

    private void updateStep(Label circle, Label check, int stepNumber) {
        if (stepNumber < currentStep) {
            circle.setStyle(
                    "-fx-background-color: #00E676; -fx-text-fill: #0B0F19; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50; " +
                            "-fx-min-width: 36; -fx-min-height: 36; -fx-alignment: center;");
            check.setStyle(
                    "-fx-text-fill: #00E676; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else if (stepNumber == currentStep) {
            circle.setStyle(
                    "-fx-background-color: #FF4A85; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50; " +
                            "-fx-min-width: 36; -fx-min-height: 36; -fx-alignment: center;");
            check.setStyle("-fx-text-fill: #2A3350; -fx-font-size: 18px;");
        } else {
            circle.setStyle(
                    "-fx-background-color: #2A3350; -fx-text-fill: #4a5568; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50; " +
                            "-fx-min-width: 36; -fx-min-height: 36; -fx-alignment: center;");
            check.setStyle("-fx-text-fill: #2A3350; -fx-font-size: 18px;");
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
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #161D30;");
        alert.showAndWait();
    }
}