package com.restaurant.restaurant.tracking;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        // Use a percentage-based binding relative to the parent StackPane width
        progressFill.maxWidthProperty().bind(
                ((Region) progressFill.getParent()).widthProperty().multiply(percent));
    }

    /**
     * Updates a single step's circle and title style.
     * Uses style classes from application.css:
     *   step-circle-done     -> steps already completed
     *   step-circle-active   -> the current in-progress step
     *   step-circle-inactive -> steps not yet reached
     */
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