package com.restaurant.restaurant.billing;

import com.restaurant.restaurant.database.DBConnection;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingController {

    // ── Panels ──────────────────────────────────────────────
    @FXML private VBox enjoyMealPanel;
    @FXML private VBox receiptPanel;
    @FXML private VBox paymentConfirmedPanel;

    // ── Left sidebar ─────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private ListView<String> orderListView;

    // ── Center receipt panel ──────────────────────────────────
    @FXML private VBox  receiptDetailPanel;
    @FXML private Label tableNumberLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private VBox  itemsVBox;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalLabel;

    // ── Right cash panel ──────────────────────────────────────
    @FXML private VBox   cashPanel;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeAmountLabel;
    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentReceivedLabel;
    @FXML private Label paymentChangeLabel;
    @FXML private Button confirmPaymentButton;
    @FXML private Button printBillButton;

    // ── Star-rating HBoxes ───────────────────────────────────
    @FXML private HBox foodQualityStars;
    @FXML private HBox serviceHospitalityStars;
    @FXML private HBox deliverySpeedStars;
    @FXML private HBox overallExperienceStars;

    // ── Comments ─────────────────────────────────────────────
    @FXML private TextArea commentsArea;

    // ── Rating state ─────────────────────────────────────────
    private int ratingFoodQuality        = 0;
    private int ratingServiceHospitality = 0;
    private int ratingDeliverySpeed      = 0;
    private int ratingOverallExperience  = 0;

    // ── Selected order state ──────────────────────────────────
    private int    selectedOrderId    = -1;
    private int    selectedTable      = -1;
    private double selectedTotal      = 0.0;
    private double selectedVat        = 0.0;
    private double selectedSubtotal   = 0.0;

    // ── Static order data (set by upstream controller) ────────
    private static int    currentTable   = 0;
    private static String currentOrderId = "";
    private static double currentTotal   = 0.0;
    private static double currentVat     = 0.0;
    private static List<OrderLineItem> currentItems = new ArrayList<>();

    public static class OrderLineItem {
        public final String name;
        public final int    qty;
        public final double unitPrice;
        public OrderLineItem(String name, int qty, double unitPrice) {
            this.name      = name;
            this.qty       = qty;
            this.unitPrice = unitPrice;
        }
    }

    public static void setOrderData(int table, String orderId,
                                    double subtotal, double vat,
                                    List<OrderLineItem> items) {
        currentTable   = table;
        currentOrderId = orderId;
        currentVat     = vat;
        currentTotal   = subtotal + vat;
        currentItems   = items != null ? items : new ArrayList<>();
    }

    // ── JavaFX lifecycle ──────────────────────────────────────

    @FXML
    public void initialize() {
        loadPendingOrders();
        setupOrderListSelection();
    }

    // ── THIS WAS MISSING — fixes billing.fxml error ──────────

    /**
     * Called by the "← Back" button in billing.fxml.
     * Returns to the staff dashboard by popping the content area.
     * Since billing is loaded inside the dashboard's contentArea,
     * we just navigate back to the dashboard.
     */
    @FXML
    public void handleBack() {
        SceneManager.navigateTo(
                com.restaurant.restaurant.navigation.NavigationUtil.STAFF_DASHBOARD
        );
    }

    // ── Load pending orders from DB ───────────────────────────

    private void loadPendingOrders() {
        if (orderListView == null) return;
        orderListView.getItems().clear();

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT order_id, table_number, total_amount, order_timestamp " +
                "FROM orders WHERE status IN ('ready','delivered') " +
                "ORDER BY order_timestamp ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int    orderId     = rs.getInt("order_id");
                int    tableNumber = rs.getInt("table_number");
                double total       = rs.getDouble("total_amount");
                String time        = rs.getString("order_timestamp");

                // Display: "Table 3  ·  Order #5  ·  GHS 151.25"
                orderListView.getItems().add(
                        String.format("Table %d  ·  Order #%d  ·  GHS %.2f",
                                tableNumber, orderId, total)
                );
            }

        } catch (SQLException e) {
            System.err.println("[Billing] Error loading orders: " + e.getMessage());
        }
    }

    private void setupOrderListSelection() {
        if (orderListView == null) return;

        orderListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, selected) -> {
                    if (selected == null) return;
                    // Parse order_id from the display string "Table X  ·  Order #N  ·  GHS Y"
                    try {
                        String[] parts = selected.split("#");
                        int orderId = Integer.parseInt(parts[1].split("·")[0].trim());
                        loadReceiptForOrder(orderId);
                    } catch (Exception e) {
                        System.err.println("[Billing] Could not parse order: " + e.getMessage());
                    }
                });
    }

    private void loadReceiptForOrder(int orderId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try {
            // Load order header
            String orderSql = "SELECT table_number, total_amount, order_timestamp " +
                    "FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        selectedOrderId  = orderId;
                        selectedTable    = rs.getInt("table_number");
                        double rawTotal  = rs.getDouble("total_amount");
                        selectedVat      = rawTotal - (rawTotal / 1.10);
                        selectedSubtotal = rawTotal / 1.10;
                        selectedTotal    = rawTotal;
                        String timestamp = rs.getString("order_timestamp");

                        // Update center panel labels
                        tableNumberLabel.setText("Table " + selectedTable);
                        orderIdLabel.setText("Order #" + orderId);
                        if (timestamp != null && timestamp.contains(" ")) {
                            String[] parts = timestamp.split(" ");
                            dateLabel.setText(parts[0]);
                            timeLabel.setText(parts[1]);
                        } else {
                            dateLabel.setText(timestamp != null ? timestamp : "");
                            timeLabel.setText("");
                        }
                    }
                }
            }

            // Load items
            itemsVBox.getChildren().clear();
            String itemsSql = "SELECT f.name, oi.quantity, f.price, oi.subtotal " +
                    "FROM order_items oi " +
                    "JOIN food_items f ON oi.item_id = f.item_id " +
                    "WHERE oi.order_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name     = rs.getString("name");
                        int    qty      = rs.getInt("quantity");
                        double price    = rs.getDouble("price");
                        double subtotal = rs.getDouble("subtotal");

                        itemsVBox.getChildren().add(
                                buildItemRow(new OrderLineItem(name, qty, price), subtotal)
                        );
                    }
                }
            }

            // Update totals
            subtotalLabel.setText(formatGHS(selectedSubtotal));
            vatLabel.setText(formatGHS(selectedVat));
            totalLabel.setText(formatGHS(selectedTotal));
            paymentTotalLabel.setText(formatGHS(selectedTotal));

            // Reset cash fields
            if (cashReceivedField != null) cashReceivedField.clear();
            if (changeAmountLabel != null) changeAmountLabel.setText("Enter amount above");
            if (paymentReceivedLabel != null) paymentReceivedLabel.setText("—");
            if (paymentChangeLabel   != null) paymentChangeLabel.setText("—");

        } catch (SQLException e) {
            System.err.println("[Billing] Error loading receipt: " + e.getMessage());
        }
    }

    // ── Cash input handler ────────────────────────────────────

    @FXML
    public void handleCashInput() {
        if (selectedOrderId == -1) return;
        try {
            double received = Double.parseDouble(
                    cashReceivedField.getText().trim()
            );
            double change = received - selectedTotal;

            paymentReceivedLabel.setText(formatGHS(received));

            if (change >= 0) {
                changeAmountLabel.setText(formatGHS(change));
                changeAmountLabel.setStyle(
                        "-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;"
                );
                paymentChangeLabel.setText(formatGHS(change));
                paymentChangeLabel.setStyle(
                        "-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: bold;"
                );
            } else {
                changeAmountLabel.setText("Short " + formatGHS(Math.abs(change)));
                changeAmountLabel.setStyle(
                        "-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;"
                );
                paymentChangeLabel.setText("Short " + formatGHS(Math.abs(change)));
                paymentChangeLabel.setStyle(
                        "-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: bold;"
                );
            }
        } catch (NumberFormatException e) {
            changeAmountLabel.setText("Enter valid amount");
            changeAmountLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
        }
    }

    // ── Confirm payment ───────────────────────────────────────

    @FXML
    public void handleConfirmPayment() {
        if (selectedOrderId == -1) {
            showAlert("No Order Selected", "Please select an order to process payment.");
            return;
        }

        double received;
        try {
            received = Double.parseDouble(cashReceivedField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid cash amount.");
            return;
        }

        if (received < selectedTotal) {
            showAlert("Insufficient Payment",
                    "Cash received is less than the total amount.\n" +
                            "Total: " + formatGHS(selectedTotal) + "\n" +
                            "Received: " + formatGHS(received));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Confirm payment for Table " + selectedTable + "?");
        confirm.setContentText(
                "Order #" + selectedOrderId + "\n" +
                        "Total: " + formatGHS(selectedTotal) + "\n" +
                        "Cash: " + formatGHS(received) + "\n" +
                        "Change: " + formatGHS(received - selectedTotal)
        );
        confirm.getDialogPane().getStyleClass().add("dialog-pane");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processPaymentInDB();
            }
        });
    }

    private void processPaymentInDB() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            // Mark order as paid
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE orders SET status = 'paid' WHERE order_id = ?")) {
                stmt.setInt(1, selectedOrderId);
                stmt.executeUpdate();
            }

            // Free up the table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE tables SET status = 'available' WHERE table_number = ?")) {
                stmt.setInt(1, selectedTable);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("[Billing] Payment confirmed for Order #"
                    + selectedOrderId + ", Table " + selectedTable);

            // Refresh order list and clear panels
            loadPendingOrders();
            clearReceiptPanel();
            selectedOrderId = -1;
            selectedTable   = -1;

            showAlert("Payment Confirmed",
                    "Payment processed successfully!\nTable " +
                            selectedTable + " is now available.");

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("[Billing] Payment error: " + e.getMessage());
            showAlert("Payment Failed", "Could not process payment: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void clearReceiptPanel() {
        tableNumberLabel.setText("Select an order");
        orderIdLabel.setText("");
        timeLabel.setText("");
        dateLabel.setText("");
        itemsVBox.getChildren().clear();
        subtotalLabel.setText("—");
        vatLabel.setText("—");
        totalLabel.setText("—");
        paymentTotalLabel.setText("—");
        paymentReceivedLabel.setText("—");
        paymentChangeLabel.setText("—");
        changeAmountLabel.setText("Enter amount above");
        if (cashReceivedField != null) cashReceivedField.clear();
    }

    // ── Print receipt ─────────────────────────────────────────

    @FXML
    public void handlePrintReceipt() {
        if (selectedOrderId == -1) {
            showAlert("No Order", "Please select an order first.");
            return;
        }
        showAlert("Print Receipt",
                "Printing receipt for Order #" + selectedOrderId +
                        ", Table " + selectedTable + "...");
    }

    // ── Receipt population (customer billing panel) ───────────

    @FXML
    public void handleShowReceipt() {
        populateReceipt();
        if (receiptPanel != null) showPanel(receiptPanel);
    }

    @FXML
    public void handleOrderMore() {
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleBackToEnjoyMeal() {
        if (enjoyMealPanel != null) showPanel(enjoyMealPanel);
    }

    private void populateReceipt() {
        if (tableNumberLabel != null)
            tableNumberLabel.setText(
                    currentTable > 0 ? "Table " + currentTable : "Table —");

        if (itemsVBox != null) {
            itemsVBox.getChildren().clear();
            double subtotal = 0;
            for (OrderLineItem item : currentItems) {
                double lineTotal = item.qty * item.unitPrice;
                subtotal += lineTotal;
                itemsVBox.getChildren().add(buildItemRow(item, lineTotal));
            }
            if (subtotalLabel != null) subtotalLabel.setText(formatGHS(subtotal));
            if (vatLabel      != null) vatLabel.setText(formatGHS(currentVat));
            if (totalLabel    != null) totalLabel.setText(formatGHS(subtotal + currentVat));
        }
    }

    private VBox buildItemRow(OrderLineItem item, double lineTotal) {
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.name);
        nameLabel.setStyle(
                "-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;"
        );
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label priceLabel = new Label(formatGHS(lineTotal));
        priceLabel.setStyle(
                "-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;"
        );
        topRow.getChildren().addAll(nameLabel, priceLabel);

        Label qtyLabel = new Label(item.qty + " x " + formatGHS(item.unitPrice));
        qtyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        return new VBox(4, topRow, qtyLabel);
    }

    private static String formatGHS(double amount) {
        return String.format("GHS %.2f", amount);
    }

    // ── Star ratings ─────────────────────────────────────────

    private void buildStarRows() {
        if (foodQualityStars       != null) buildStarRow(foodQualityStars,       () -> ratingFoodQuality,        v -> ratingFoodQuality = v);
        if (serviceHospitalityStars != null) buildStarRow(serviceHospitalityStars,() -> ratingServiceHospitality, v -> ratingServiceHospitality = v);
        if (deliverySpeedStars     != null) buildStarRow(deliverySpeedStars,     () -> ratingDeliverySpeed,      v -> ratingDeliverySpeed = v);
        if (overallExperienceStars != null) buildStarRow(overallExperienceStars, () -> ratingOverallExperience,  v -> ratingOverallExperience = v);
    }

    private void buildStarRow(HBox container, IntSupplier getter, IntConsumer setter) {
        container.getChildren().clear();
        int current = getter.get();
        for (int i = 1; i <= 5; i++) {
            final int starIndex = i;
            Label star = new Label(i <= current ? "★" : "☆");
            star.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: "
                    + (i <= current ? "#F59E0B;" : "#D1D5DB;"));
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < container.getChildren().size(); j++) {
                    Label s = (Label) container.getChildren().get(j);
                    s.setText(j < starIndex ? "★" : "☆");
                    s.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: "
                            + (j < starIndex ? "#F59E0B;" : "#D1D5DB;"));
                }
            });
            star.setOnMouseExited(e -> refreshStarRow(container, getter.get()));
            star.setOnMouseClicked(e -> {
                setter.accept(starIndex);
                refreshStarRow(container, starIndex);
            });
            container.getChildren().add(star);
        }
    }

    private void refreshStarRow(HBox container, int rating) {
        for (int j = 0; j < container.getChildren().size(); j++) {
            Label s = (Label) container.getChildren().get(j);
            boolean filled = j < rating;
            s.setText(filled ? "★" : "☆");
            s.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: "
                    + (filled ? "#F59E0B;" : "#D1D5DB;"));
        }
    }

    @FXML
    public void handleSubmitFeedback() {
        if (ratingOverallExperience == 0) {
            showAlert("Rating Required",
                    "Please rate your Overall Experience before submitting.");
            return;
        }
        System.out.println("[Feedback] Order: " + currentOrderId
                + " | Food: " + ratingFoodQuality
                + " | Service: " + ratingServiceHospitality
                + " | Delivery: " + ratingDeliverySpeed
                + " | Overall: " + ratingOverallExperience
                + " | Comments: " + (commentsArea != null ? commentsArea.getText() : ""));
        if (paymentConfirmedPanel != null) showPanel(paymentConfirmedPanel);
    }

    @FXML
    public void handleRequestPhysicalReceipt() {
        System.out.println("[Receipt] Physical receipt requested — Order: "
                + currentOrderId + ", Table: " + currentTable);
        showAlert("Receipt Requested",
                "Your receipt request has been sent.\n" +
                        "Please visit the cashier for collection.");
    }

    private void showPanel(VBox target) {
        for (VBox panel : new VBox[]{enjoyMealPanel, receiptPanel, paymentConfirmedPanel}) {
            if (panel == null) continue;
            boolean visible = panel == target;
            panel.setVisible(visible);
            panel.setManaged(visible);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }

    @FunctionalInterface interface IntSupplier { int get(); }
    @FunctionalInterface interface IntConsumer { void accept(int v); }
}