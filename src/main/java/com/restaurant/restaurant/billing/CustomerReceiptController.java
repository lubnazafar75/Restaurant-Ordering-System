package com.restaurant.restaurant.billing;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CustomerReceiptController {

    // Screen panels
    @FXML private VBox thankYouScreen;
    @FXML private VBox receiptRequestedScreen;

    // Thank you screen labels
    @FXML private Label orderNumberLabel;
    @FXML private Label tableNumberLabel;
    @FXML private Label totalAmountLabel;

    // Static order data passed from ordering flow
    private static int currentTable = 5;
    private static String currentOrderId = "ORD-1045";
    private static double currentTotal = 0.0;

    // Called from OrderController after order is confirmed
    public static void setOrderData(int table, String orderId, double total) {
        currentTable = table;
        currentOrderId = orderId;
        currentTotal = total;
    }

    @FXML
    public void initialize() {
        orderNumberLabel.setText("Order #: " + currentOrderId);
        tableNumberLabel.setText("Table  : " +
                String.format("%02d", currentTable));
        totalAmountLabel.setText("Total  : GHS " +
                String.format("%.2f", currentTotal));
        showScreen(thankYouScreen);
    }

    @FXML
    public void handleBack() {
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleRequestReceipt() {
        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Request Receipt");
        confirm.setHeaderText("Request Receipt?");
        confirm.setContentText(
                "A staff member will prepare your receipt shortly.");
        confirm.getDialogPane().setStyle(
                "-fx-background-color: #161D30;");
        confirm.getDialogPane().lookup(".content.label")
                .setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        confirm.getDialogPane().lookup(".header-panel")
                .setStyle("-fx-background-color: #0B0F19;");
        confirm.getDialogPane().lookup(".header-panel .label")
                .setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Style buttons
        Button cancelBtn = (Button) confirm.getDialogPane()
                .lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle(
                "-fx-background-color: #2A3350; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 20;");

        Button okBtn = (Button) confirm.getDialogPane()
                .lookupButton(ButtonType.OK);
        okBtn.setStyle(
                "-fx-background-color: #00E676; -fx-text-fill: #0B0F19; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 20;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Insert into receipt_requests table in database
                System.out.println("[Receipt] Request submitted for " +
                        currentOrderId + " Table " + currentTable);
                showScreen(receiptRequestedScreen);
            }
        });
    }

    @FXML
    public void handleRateExperience() {
        // Navigate to rating screen — placeholder for now
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Rate Experience");
        info.setHeaderText(null);
        info.setContentText("Rating feature coming soon! Thank you for dining with us.");
        info.getDialogPane().setStyle("-fx-background-color: #161D30;");
        info.showAndWait();
    }

    private void showScreen(javafx.scene.Node screen) {
        thankYouScreen.setVisible(false);
        thankYouScreen.setManaged(false);
        receiptRequestedScreen.setVisible(false);
        receiptRequestedScreen.setManaged(false);
        screen.setVisible(true);
        screen.setManaged(true);
    }
}