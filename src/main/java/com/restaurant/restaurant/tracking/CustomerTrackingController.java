package com.restaurant.restaurant.tracking;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import com.restaurant.restaurant.ordering.OrderController;
import com.restaurant.restaurant.database.DBConnection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerTrackingController {

    @FXML private VBox trackingScreen;
    @FXML private Label statusEmojiLabel;
    @FXML private Label currentStatusLabel;
    @FXML private Label statusDescLabel;
    @FXML private Label estimatedTimeLabel;

    @FXML private Label step1Circle, step2Circle, step3Circle, step4Circle;
    @FXML private Label step1Title, step2Title, step3Title, step4Title;
    @FXML private Region progressFill;
    @FXML private StackPane progressTrackContainer;

    @FXML private Label culinarySummaryDateLabel;
    @FXML private VBox orderItemsList;
    @FXML private Label itemsTotalLabel;
    @FXML private Label grandTotalLabel;

    @FXML private Label confirmationHintLabel;
    @FXML private Button foodReceivedBtn;

    @FXML private Label navMenuLabel, navTrackLabel, navBillLabel, navRateLabel;

    private int currentStep = 1;
    private Timeline statusPoller;
    private int trackedOrderId = -1;

    @FXML
    public void initialize() {
        trackedOrderId = OrderController.lastOrderId;

        // Simulation for testing (uncomment to test)

        Timeline simulation = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> setStep(1)),
            new KeyFrame(Duration.seconds(4), e -> setStep(2)),
            new KeyFrame(Duration.seconds(8), e -> setStep(3)),
            new KeyFrame(Duration.seconds(12), e -> setStep(4))
        );
        simulation.play();


        loadOrderDetails();
        loadCulinarySummary();
        Platform.runLater(this::startTracking);

        if (navTrackLabel != null) navTrackLabel.getStyleClass().add("nav-label-active");
    }

    private void loadOrderDetails() {
        if (trackedOrderId == -1) return;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT status, order_date FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trackedOrderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        LocalDateTime dt = rs.getTimestamp("order_date").toLocalDateTime();
                        currentStep = mapStatusToStep(status);
                        Platform.runLater(() -> {
                            culinarySummaryDateLabel.setText(dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
                            updateProgressDisplay();
                        });
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadCulinarySummary() {
        if (trackedOrderId == -1) return;
        orderItemsList.getChildren().clear();
        double total = 0.0;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT m.name, oi.quantity, m.price FROM order_items oi JOIN menu m ON oi.item_id = m.item_id WHERE oi.order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trackedOrderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        int qty = rs.getInt("quantity");
                        double subtotal = rs.getDouble("price") * qty;
                        total += subtotal;
                        addSummaryItem(name, qty, subtotal);
                    }
                }
            }
            final double finalTotal = total;
            Platform.runLater(() -> {
                itemsTotalLabel.setText(String.format("GH₵ %.2f", finalTotal));
                grandTotalLabel.setText(String.format("GH₵ %.2f", finalTotal));
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addSummaryItem(String name, int qty, double subtotal) {
        Platform.runLater(() -> {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(12);

            Label qtyBadge = new Label(qty + "x");
            qtyBadge.getStyleClass().add("qty-badge");

            Label nameLabel = new Label(name);
            nameLabel.getStyleClass().add("order-item-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label priceLabel = new Label(String.format("GH₵ %.2f", subtotal));
            priceLabel.getStyleClass().add("order-item-price");

            row.getChildren().addAll(qtyBadge, nameLabel, spacer, priceLabel);
            orderItemsList.getChildren().add(row);
        });
    }

    private void startTracking() {
        if (trackedOrderId == -1) return;
        statusPoller = new Timeline(new KeyFrame(Duration.seconds(3), e -> pollOrderStatus()));
        statusPoller.setCycleCount(Timeline.INDEFINITE);
        statusPoller.play();
    }

    private void pollOrderStatus() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT status FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trackedOrderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int newStep = mapStatusToStep(rs.getString("status"));
                        if (newStep != currentStep) {
                            currentStep = newStep;
                            Platform.runLater(this::updateProgressDisplay);
                        }
                        if (currentStep == 4 && statusPoller != null) statusPoller.stop();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int mapStatusToStep(String status) {
        if (status == null) return 1;
        return switch (status.toLowerCase()) {
            case "pending" -> 1; case "preparing" -> 2; case "ready" -> 3; case "delivered" -> 4; default -> 1;
        };
    }

    public void setStep(int step) {
        this.currentStep = step;
        Platform.runLater(this::updateProgressDisplay);
    }

    private void updateProgressDisplay() {
        switch (currentStep) {
            case 1 -> updateStatusUI("⏳", "Order Received", "Your order has been received and will be prepared shortly.", "Estimated wait: ~15 minutes");
            case 2 -> updateStatusUI("👨‍🍳", "Being Prepared", "Our chefs are working on your order right now!", "Estimated wait: ~10 minutes");
            case 3 -> updateStatusUI("🍽️", "Ready for Delivery", "Your order is ready! A waiter will bring it to your table.", "Almost there — just a few more minutes!");
            case 4 -> updateStatusUI("✅", "Delivered!", "Your food has been delivered. Enjoy your meal!", "Enjoy your meal! 🎉");
        }
        updateStepCircle(step1Circle, step1Title, 1, "1");
        updateStepCircle(step2Circle, step2Title, 2, "2");
        updateStepCircle(step3Circle, step3Title, 3, "3");
        updateStepCircle(step4Circle, step4Title, 4, "4");
        updateProgressBar();

        if (foodReceivedBtn != null) {
            boolean isDelivered = (currentStep == 4);
            foodReceivedBtn.setDisable(!isDelivered);
            if (isDelivered) {
                foodReceivedBtn.getStyleClass().setAll("btn-confirm-persistent-enabled");
                confirmationHintLabel.setText("Your order is here! Please confirm receipt to proceed.");
                confirmationHintLabel.getStyleClass().setAll("hint-label-delivered");
            } else {
                foodReceivedBtn.getStyleClass().setAll("btn-confirm-persistent-disabled");
                confirmationHintLabel.setText("Wait until the progress timeline shifts to 'Delivered' to mark receipt.");
                confirmationHintLabel.getStyleClass().setAll("hint-label-waiting");
            }
        }
    }

    private void updateStatusUI(String emoji, String title, String desc, String time) {
        statusEmojiLabel.setText(emoji);
        currentStatusLabel.setText(title);
        statusDescLabel.setText(desc);
        estimatedTimeLabel.setText(time);
    }

    private void updateProgressBar() {
        if (progressFill == null || progressTrackContainer == null) return;
        double percent = (currentStep - 1) / 3.0;
        Platform.runLater(() -> {
            double totalWidth = progressTrackContainer.getWidth();
            if (totalWidth > 0) progressFill.setPrefWidth(totalWidth * percent);
        });
    }

    private void updateStepCircle(Label circle, Label title, int stepNum, String text) {
        if (circle == null || title == null) return;
        circle.getStyleClass().removeAll("step-circle-done", "step-circle-active", "step-circle-inactive");
        if (stepNum < currentStep) { circle.getStyleClass().add("step-circle-done"); circle.setText("✓"); }
        else if (stepNum == currentStep) { circle.getStyleClass().add("step-circle-active"); circle.setText(text); }
        else { circle.getStyleClass().add("step-circle-inactive"); circle.setText(text); }
    }

    @FXML public void handleConfirmDelivery() { SceneManager.navigateTo(NavigationUtil.customer_billing); }
    @FXML public void handleNavMenu() { SceneManager.navigateToMenu(); }
    @FXML public void handleNavTrack() { /* Active */ }
    @FXML public void handleNavBill() { SceneManager.navigateTo(NavigationUtil.customer_billing); }
    @FXML public void handleNavRate() { SceneManager.navigateTo(NavigationUtil.customer_billing); }
}
