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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerTrackingController {

    @FXML private VBox trackingScreen;
    @FXML private VBox receivedScreen;

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
    @FXML private Label orderInfoLabel;

    private int currentStep = 1;
    private Timeline statusPoller;
    private int trackedOrderId = -1;

    @FXML
    public void initialize() {
        trackedOrderId = OrderController.lastOrderId;

        if (orderInfoLabel != null) {
            orderInfoLabel.setText(trackedOrderId != -1
                    ? "Order #" + trackedOrderId
                    : "Tracking your order...");
        }

        // FIX: Remove the simulation — it was overriding real DB status
        // Load real status from DB instead
        loadOrderDetails();
        loadCulinarySummary();

        // FIX: Wait for layout to complete before starting tracker
        // so progressTrackContainer.getWidth() returns a real value
        Platform.runLater(() -> {
            updateProgressDisplay(); // initial render with correct step
            startTracking();         // then begin polling
        });
    }

    // ── NAV / BUTTON HANDLERS ─────────────────────────────────
    @FXML public void handleBackToMenu()     { SceneManager.navigateToMenu(); }
    @FXML public void handleConfirmDelivery(){ handleFoodReceived(); }
    @FXML public void handleFoodReceived() {
        if (statusPoller != null) statusPoller.stop();
        markOrderDelivered();
        SceneManager.navigateTo(NavigationUtil.customer_billing);
    }
    @FXML public void handleRequestReceipt() { SceneManager.navigateTo(NavigationUtil.customer_billing); }
    @FXML public void handleOrderMore()      { SceneManager.navigateToMenu(); }
    @FXML public void handleNavMenu()        { SceneManager.navigateToMenu(); }
    @FXML public void handleNavTrack()       { /* already here */ }
    @FXML public void handleNavBill()        { SceneManager.navigateTo(NavigationUtil.customer_billing); }
    @FXML public void handleNavRate()        { SceneManager.navigateTo(NavigationUtil.customer_billing); }

    // ── LOAD ORDER HEADER FROM DB ─────────────────────────────
    private void loadOrderDetails() {
        if (trackedOrderId == -1) return;
        // FIX: Don't use try-with-resources on the singleton connection
        // — closing it kills the shared connection for the whole app
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try {
            String sql = "SELECT status, order_timestamp FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql);) {
                stmt.setInt(1, trackedOrderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        String tsStr  = rs.getString("order_timestamp");
                        currentStep   = mapStatusToStep(status);
                        Platform.runLater(() -> {
                            if (culinarySummaryDateLabel != null && tsStr != null) {
                                try {
                                    LocalDateTime dt = LocalDateTime.parse(tsStr,
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                    culinarySummaryDateLabel.setText(
                                            dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
                                } catch (Exception ex) {
                                    culinarySummaryDateLabel.setText(tsStr);
                                }
                            }
                        });
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── LOAD ORDER ITEMS FROM DB ──────────────────────────────
    private void loadCulinarySummary() {
        if (trackedOrderId == -1) return;
        if (orderItemsList != null) {
            Platform.runLater(() -> orderItemsList.getChildren().clear());
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        double[] total = {0.0}; // array to allow lambda mutation

        try {
            String sql = "SELECT f.name, oi.quantity, f.price " +
                    "FROM order_items oi " +
                    "JOIN food_items f ON oi.item_id = f.item_id " +
                    "WHERE oi.order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trackedOrderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name     = rs.getString("name");
                        int    qty      = rs.getInt("quantity");
                        double subtotal = rs.getDouble("price") * qty;
                        total[0] += subtotal;

                        // Build row — must capture final values for lambda
                        final String n = name;
                        final int    q = qty;
                        final double s = subtotal;

                        Platform.runLater(() -> {
                            if (orderItemsList == null) return;
                            HBox row = new HBox(12);
                            row.setAlignment(Pos.CENTER_LEFT);

                            Label qtyBadge = new Label(q + "x");
                            qtyBadge.getStyleClass().add("qty-badge");

                            Label nameLabel = new Label(n);
                            nameLabel.getStyleClass().add("order-item-name");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Label priceLabel = new Label(String.format("GH₵ %.2f", s));
                            priceLabel.getStyleClass().add("order-item-price");

                            row.getChildren().addAll(qtyBadge, nameLabel, spacer, priceLabel);
                            orderItemsList.getChildren().add(row);
                        });
                    }
                }
            }

            final double finalTotal = total[0];
            Platform.runLater(() -> {
                if (itemsTotalLabel != null)
                    itemsTotalLabel.setText(String.format("GH₵ %.2f", finalTotal));
                if (grandTotalLabel != null)
                    grandTotalLabel.setText(String.format("GH₵ %.2f", finalTotal));
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── MARK DELIVERED IN DB ──────────────────────────────────
    private void markOrderDelivered() {
        if (trackedOrderId == -1) return;
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE orders SET status = 'delivered' WHERE order_id = ?")) {
            stmt.setInt(1, trackedOrderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── POLLING ───────────────────────────────────────────────
    private void startTracking() {
        if (trackedOrderId == -1) return;
        statusPoller = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> pollOrderStatus()));
        statusPoller.setCycleCount(Timeline.INDEFINITE);
        statusPoller.play();
    }

    private void pollOrderStatus() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT status FROM orders WHERE order_id = ?")) {
            stmt.setInt(1, trackedOrderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int newStep = mapStatusToStep(rs.getString("status"));
                    if (newStep != currentStep) {
                        currentStep = newStep;
                        Platform.runLater(this::updateProgressDisplay);
                    }
                    if (currentStep == 4 && statusPoller != null)
                        statusPoller.stop();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int mapStatusToStep(String status) {
        if (status == null) return 1;
        return switch (status.toLowerCase()) {
            case "pending"   -> 1;
            case "preparing" -> 2;
            case "ready"     -> 3;
            case "delivered" -> 4;
            default          -> 1;
        };
    }

    public void setStep(int step) {
        this.currentStep = step;
        Platform.runLater(this::updateProgressDisplay);
    }

    // ── UI UPDATE ─────────────────────────────────────────────
    private void updateProgressDisplay() {
        switch (currentStep) {
            case 1 -> updateStatusUI("⏳", "Order Received",
                    "Your order has been received and will be prepared shortly.",
                    "Estimated wait: ~15 minutes");
            case 2 -> updateStatusUI("👨‍🍳", "Being Prepared",
                    "Our chefs are working on your order right now!",
                    "Estimated wait: ~10 minutes");
            case 3 -> updateStatusUI("🍽️", "Ready for Delivery",
                    "Your order is ready! A waiter will bring it to your table.",
                    "Almost there — just a few more minutes!");
            case 4 -> updateStatusUI("✅", "Delivered!",
                    "Your food has been delivered. Enjoy your meal!",
                    "Enjoy your meal! 🎉");
        }

        updateStepCircle(step1Circle, step1Title, 1, "1");
        updateStepCircle(step2Circle, step2Title, 2, "2");
        updateStepCircle(step3Circle, step3Title, 3, "3");
        updateStepCircle(step4Circle, step4Title, 4, "4");
        updateProgressBar();

        if (foodReceivedBtn != null) {
            boolean delivered = (currentStep == 4);
            foodReceivedBtn.setDisable(!delivered);
            foodReceivedBtn.getStyleClass().removeAll(
                    "btn-confirm-persistent-disabled", "btn-confirm-persistent-enabled");
            foodReceivedBtn.getStyleClass().add(delivered
                    ? "btn-confirm-persistent-enabled"
                    : "btn-confirm-persistent-disabled");
        }
        if (confirmationHintLabel != null) {
            confirmationHintLabel.setText(currentStep == 4
                    ? "Your order is here! Press the button to confirm receipt."
                    : "Wait until your order is delivered to confirm receipt.");
        }
    }

    private void updateStatusUI(String emoji, String title, String desc, String time) {
        if (statusEmojiLabel   != null) statusEmojiLabel.setText(emoji);
        if (currentStatusLabel != null) currentStatusLabel.setText(title);
        if (statusDescLabel    != null) statusDescLabel.setText(desc);
        if (estimatedTimeLabel != null) estimatedTimeLabel.setText(time);
    }

    private void updateProgressBar() {
        if (progressFill == null || progressTrackContainer == null) return;
        double percent = (currentStep - 1) / 3.0;

        // FIX: Use scene listener to guarantee layout width is available
        if (progressTrackContainer.getWidth() > 0) {
            progressFill.setPrefWidth(progressTrackContainer.getWidth() * percent);
        } else {
            // Layout not done yet — wait for it
            progressTrackContainer.widthProperty().addListener((obs, oldW, newW) -> {
                if (newW.doubleValue() > 0) {
                    progressFill.setPrefWidth(newW.doubleValue() * percent);
                }
            });
        }
    }

    private void updateStepCircle(Label circle, Label title, int stepNum, String text) {
        if (circle == null || title == null) return;
        circle.getStyleClass().removeAll(
                "step-circle-done", "step-circle-active", "step-circle-inactive");
        if (stepNum < currentStep) {
            circle.getStyleClass().add("step-circle-done");
            circle.setText("✓");
        } else if (stepNum == currentStep) {
            circle.getStyleClass().add("step-circle-active");
            circle.setText(text);
        } else {
            circle.getStyleClass().add("step-circle-inactive");
            circle.setText(text);
        }
    }
}