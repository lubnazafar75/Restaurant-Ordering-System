package com.restaurant.restaurant.billing;

import com.restaurant.restaurant.database.DBConnection;
import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingController {

    // Customer Billing Panels
    @FXML private VBox enjoyMealPanel;
    @FXML private VBox receiptPanel;
    @FXML private VBox paymentConfirmedPanel;

    // Customer Receipt Labels
    @FXML private Label tableNumberLabel;
    @FXML private VBox  itemsVBox;
    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalLabel;

    // Star Rating HBoxes
    @FXML private HBox foodQualityStars;
    @FXML private HBox serviceHospitalityStars;
    @FXML private HBox deliverySpeedStars;
    @FXML private HBox overallExperienceStars;

    // ── Comments ─────────────────────────────────────────────
    @FXML private TextArea commentsArea;

    // ── Staff Billing Fields (only used in billing.fxml) ─────
    @FXML private TextField  searchField;
    @FXML private ListView<String> orderListView;
    @FXML private VBox  receiptDetailPanel;
    @FXML private Label orderIdLabel;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private Label discountLabel;
    @FXML private VBox  cashPanel;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeAmountLabel;
    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentReceivedLabel;
    @FXML private Label paymentChangeLabel;
    @FXML private Button confirmPaymentButton;
    @FXML private Button printBillButton;

    // ── Rating state ─────────────────────────────────────────
    private int ratingFoodQuality        = 0;
    private int ratingServiceHospitality = 0;
    private int ratingDeliverySpeed      = 0;
    private int ratingOverallExperience  = 0;

    // ── Staff billing selected order state ───────────────────
    private int    selectedOrderId  = -1;
    private int    selectedTable    = -1;
    private double selectedTotal    = 0.0;
    private double selectedSubtotal = 0.0;
    private double selectedVat      = 0.0;

    // ── Static order data (set by upstream or loaded from DB) ─
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

    //JavaFX Lifecycle

    @FXML
    public void initialize() {
        // Detect which FXML loaded this controller
        // customer_billing.fxml → enjoyMealPanel exists
        // billing.fxml (staff)  → orderListView exists

        if (enjoyMealPanel != null) {
            // ── CUSTOMER BILLING INIT ──────────────────────────
            showPanel(enjoyMealPanel);

            // Load real order data from DB using lastOrderId
            loadOrderDataFromDB();

            // Build star rows — THIS WAS MISSING
            buildStarRows();

        } else if (orderListView != null) {
            // ── STAFF BILLING INIT ─────────────────────────────
            loadPendingOrders();
            setupOrderListSelection();
        }
    }

    // ── Load real order data from DB ─────────────────────────

    /**
     * Reads the most recent order from OrderController.lastOrderId
     * and populates the static fields so the receipt shows real data.
     */
    private void loadOrderDataFromDB() {
        int orderId = com.restaurant.restaurant.ordering.OrderController.lastOrderId;

        if (orderId == -1) {
            System.out.println("[CustomerBilling] No order ID found.");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try {
            // Load order header
            String orderSql =
                    "SELECT table_number, total_amount FROM orders WHERE order_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentTable   = rs.getInt("table_number");
                        double rawTotal = rs.getDouble("total_amount");
                        // total_amount already includes VAT (10%)
                        // subtotal = total / 1.10
                        double subtotal = rawTotal / 1.10;
                        currentVat     = rawTotal - subtotal;
                        currentTotal   = rawTotal;
                        currentOrderId = "ORD-" + String.format("%04d", orderId);
                    }
                }
            }

            // Load items
            currentItems = new ArrayList<>();
            String itemsSql =
                    "SELECT f.name, oi.quantity, f.price " +
                            "FROM order_items oi " +
                            "JOIN food_items f ON oi.item_id = f.item_id " +
                            "WHERE oi.order_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        currentItems.add(new OrderLineItem(
                                rs.getString("name"),
                                rs.getInt("quantity"),
                                rs.getDouble("price")
                        ));
                    }
                }
            }

            System.out.println("[CustomerBilling] Loaded Order #" + orderId
                    + " for Table " + currentTable
                    + " with " + currentItems.size() + " items");

        } catch (SQLException e) {
            System.err.println("[CustomerBilling] Error loading order: "
                    + e.getMessage());
        }
    }

    // ── Customer Panel Handlers ───────────────────────────────

    @FXML
    public void handleShowReceipt() {
        populateReceipt();
        showPanel(receiptPanel);
    }

    @FXML
    public void handleOrderMore() {
        SceneManager.navigateToMenu();
    }

    @FXML
    public void handleBackToEnjoyMeal() {
        showPanel(enjoyMealPanel);
    }

    // ── Receipt population ────────────────────────────────────

    private void populateReceipt() {
        // Table label
        tableNumberLabel.setText(
                currentTable > 0 ? "Table " + currentTable : "Table —"
        );

        // Items
        itemsVBox.getChildren().clear();
        double subtotal = 0;

        if (currentItems.isEmpty()) {
            Label empty = new Label("No items found for this order.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
            itemsVBox.getChildren().add(empty);
        } else {
            for (OrderLineItem item : currentItems) {
                double lineTotal = item.qty * item.unitPrice;
                subtotal += lineTotal;
                itemsVBox.getChildren().add(buildItemRow(item, lineTotal));
            }
        }

        // Totals
        subtotalLabel.setText(formatGHS(subtotal));
        vatLabel.setText(formatGHS(currentVat));
        totalLabel.setText(formatGHS(currentTotal));
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

        Label qtyLabel = new Label(
                item.qty + " × " + formatGHS(item.unitPrice)
        );
        qtyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        return new VBox(4, topRow, qtyLabel);
    }

    private static String formatGHS(double amount) {
        return String.format("GHS %.2f", amount);
    }

    // ── Physical receipt ──────────────────────────────────────

    @FXML
    public void handleRequestPhysicalReceipt() {
        System.out.println("[Receipt] Physical receipt requested — Order: "
                + currentOrderId + ", Table: " + currentTable);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Receipt Requested");
        info.setHeaderText(null);
        info.setContentText(
                "Your receipt request has been sent.\n" +
                        "Please visit the cashier for collection."
        );
        info.getDialogPane().getStyleClass().add("dialog-pane");
        info.showAndWait();
    }

    // ── Star Ratings ─────────────────────────────────────────

    private void buildStarRows() {
        if (foodQualityStars       != null)
            buildStarRow(foodQualityStars,
                    () -> ratingFoodQuality,
                    v  -> ratingFoodQuality = v);

        if (serviceHospitalityStars != null)
            buildStarRow(serviceHospitalityStars,
                    () -> ratingServiceHospitality,
                    v  -> ratingServiceHospitality = v);

        if (deliverySpeedStars     != null)
            buildStarRow(deliverySpeedStars,
                    () -> ratingDeliverySpeed,
                    v  -> ratingDeliverySpeed = v);

        if (overallExperienceStars != null)
            buildStarRow(overallExperienceStars,
                    () -> ratingOverallExperience,
                    v  -> ratingOverallExperience = v);
    }

    private void buildStarRow(HBox container,
                              IntSupplier getter,
                              IntConsumer setter) {
        container.getChildren().clear();
        int current = getter.get();

        for (int i = 1; i <= 5; i++) {
            final int starIndex = i;
            Label star = new Label(i <= current ? "★" : "★");
            star.setStyle(
                    "-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: "
                            + (i <= current ? "#F59E0B;" : "#D1D5DB;")
            );

            // Hover — preview
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < container.getChildren().size(); j++) {
                    Label s = (Label) container.getChildren().get(j);
                    s.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: "
                            + (j < starIndex ? "#F59E0B;" : "#D1D5DB;"));
                }
            });

            // Exit — restore committed state
            star.setOnMouseExited(e ->
                    refreshStarRow(container, getter.get())
            );

            // Click — commit
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
            s.setStyle("-fx-font-size: 24px; -fx-cursor: hand; -fx-text-fill: "
                    + (j < rating ? "#F59E0B;" : "#D1D5DB;"));
        }
    }

    // ── Feedback submission ───────────────────────────────────

    @FXML
    public void handleSubmitFeedback() {
        if (ratingOverallExperience == 0) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Rating Required");
            warn.setHeaderText(null);
            warn.setContentText("Please rate your Overall Experience before submitting.");
            warn.getDialogPane().getStyleClass().add("dialog-pane");
            warn.showAndWait();
            return;
        }

        String comments = commentsArea != null
                ? commentsArea.getText().trim() : "";

        // ── Save to database ──────────────────────────────────
        int orderId = com.restaurant.restaurant.ordering.OrderController.lastOrderId;
        saveFeedback(orderId, ratingFoodQuality, ratingServiceHospitality,
                ratingDeliverySpeed, ratingOverallExperience, comments);

        // ── Show confirmation then go home ────────────────────
        showPanel(paymentConfirmedPanel);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY));
        pause.play();
    }

    private void saveFeedback(int orderId, int foodQuality, int serviceQuality,
                              int deliverySpeed, int overall, String comment) {
        if (orderId == -1) {
            System.err.println("[Feedback] No order ID — feedback not saved.");
            return;
        }
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql =
                "INSERT INTO feedback " +
                        "(order_id, food_quality, service_quality, delivery_speed, overall, comment) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, foodQuality);
            stmt.setInt(3, serviceQuality);
            stmt.setInt(4, deliverySpeed);
            stmt.setInt(5, overall);
            stmt.setString(6, comment.isEmpty() ? null : comment);
            stmt.executeUpdate();
            System.out.println("[Feedback] Saved for order #" + orderId
                    + " — overall: " + overall + "/5");
        } catch (SQLException e) {
            System.err.println("[Feedback] Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Staff Billing: Back button ────────────────────────────

    @FXML
    public void handleBack() {
        SceneManager.navigateTo(NavigationUtil.STAFF_DASHBOARD);
    }

    // ── Staff Billing: Load Orders ────────────────────────────

    private void loadPendingOrders() {
        if (orderListView == null) return;
        orderListView.getItems().clear();

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String sql =
                "SELECT order_id, table_number, total_amount, order_timestamp " +
                        "FROM orders WHERE status IN ('ready','delivered') " +
                        "ORDER BY order_timestamp ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orderListView.getItems().add(String.format(
                        "Table %d  ·  Order #%d  ·  GHS %.2f",
                        rs.getInt("table_number"),
                        rs.getInt("order_id"),
                        rs.getDouble("total_amount")
                ));
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
                    try {
                        int orderId = Integer.parseInt(
                                selected.split("#")[1].split("·")[0].trim()
                        );
                        loadReceiptForOrder(orderId);
                    } catch (Exception e) {
                        System.err.println("[Billing] Parse error: " + e.getMessage());
                    }
                });
    }

    private void loadReceiptForOrder(int orderId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try {
            String orderSql =
                    "SELECT table_number, total_amount, order_timestamp " +
                            "FROM orders WHERE order_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        selectedOrderId  = orderId;
                        selectedTable    = rs.getInt("table_number");
                        double raw       = rs.getDouble("total_amount");
                        selectedSubtotal = raw / 1.10;
                        selectedVat      = raw - selectedSubtotal;
                        selectedTotal    = raw;
                        String ts        = rs.getString("order_timestamp");

                        if (tableNumberLabel != null)
                            tableNumberLabel.setText("Table " + selectedTable);
                        if (orderIdLabel != null)
                            orderIdLabel.setText("Order #" + orderId);

                        if (ts != null && ts.contains(" ") && dateLabel != null) {
                            String[] parts = ts.split(" ");
                            dateLabel.setText(parts[0]);
                            if (timeLabel != null) timeLabel.setText(parts[1]);
                        }
                    }
                }
            }

            if (itemsVBox != null) {
                itemsVBox.getChildren().clear();
                String itemsSql =
                        "SELECT f.name, oi.quantity, f.price, oi.subtotal " +
                                "FROM order_items oi " +
                                "JOIN food_items f ON oi.item_id = f.item_id " +
                                "WHERE oi.order_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(itemsSql)) {
                    stmt.setInt(1, orderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            itemsVBox.getChildren().add(buildItemRow(
                                    new OrderLineItem(
                                            rs.getString("name"),
                                            rs.getInt("quantity"),
                                            rs.getDouble("price")
                                    ),
                                    rs.getDouble("subtotal")
                            ));
                        }
                    }
                }
            }

            if (subtotalLabel    != null) subtotalLabel.setText(formatGHS(selectedSubtotal));
            if (vatLabel         != null) vatLabel.setText(formatGHS(selectedVat));
            if (totalLabel       != null) totalLabel.setText(formatGHS(selectedTotal));
            if (paymentTotalLabel != null) paymentTotalLabel.setText(formatGHS(selectedTotal));
            if (cashReceivedField != null) cashReceivedField.clear();
            if (changeAmountLabel != null) changeAmountLabel.setText("Enter amount above");
            if (paymentReceivedLabel != null) paymentReceivedLabel.setText("—");
            if (paymentChangeLabel   != null) paymentChangeLabel.setText("—");

        } catch (SQLException e) {
            System.err.println("[Billing] Error loading receipt: " + e.getMessage());
        }
    }

    @FXML
    public void handleCashInput() {
        if (selectedOrderId == -1 || cashReceivedField == null) return;
        try {
            double received = Double.parseDouble(
                    cashReceivedField.getText().trim()
            );
            double change = received - selectedTotal;

            if (paymentReceivedLabel != null)
                paymentReceivedLabel.setText(formatGHS(received));

            String color = change >= 0 ? "#10B981" : "#EF4444";
            String text  = change >= 0
                    ? formatGHS(change)
                    : "Short " + formatGHS(Math.abs(change));

            if (changeAmountLabel != null) {
                changeAmountLabel.setText(text);
                changeAmountLabel.setStyle(
                        "-fx-text-fill: " + color + "; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold;"
                );
            }
            if (paymentChangeLabel != null) {
                paymentChangeLabel.setText(text);
                paymentChangeLabel.setStyle(
                        "-fx-text-fill: " + color + "; " +
                                "-fx-font-size: 13px; -fx-font-weight: bold;"
                );
            }
        } catch (NumberFormatException e) {
            if (changeAmountLabel != null)
                changeAmountLabel.setText("Enter valid amount");
        }
    }

    @FXML
    public void handleConfirmPayment() {
        if (selectedOrderId == -1) {
            showAlert("No Order Selected",
                    "Please select an order to process payment.");
            return;
        }
        double received;
        try {
            received = Double.parseDouble(
                    cashReceivedField.getText().trim()
            );
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount",
                    "Please enter a valid cash amount.");
            return;
        }
        if (received < selectedTotal) {
            showAlert("Insufficient Payment",
                    "Cash received is less than total.\n" +
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
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) processPaymentInDB();
        });
    }

    private void processPaymentInDB() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement s = conn.prepareStatement(
                    "UPDATE orders SET status = 'paid' WHERE order_id = ?")) {
                s.setInt(1, selectedOrderId);
                s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "UPDATE tables SET status = 'available' WHERE table_number = ?")) {
                s.setInt(1, selectedTable);
                s.executeUpdate();
            }

            conn.commit();
            loadPendingOrders();
            clearReceiptPanel();
            showAlert("Payment Confirmed",
                    "Payment processed!\nTable " + selectedTable +
                            " is now available.");
            selectedOrderId = -1;
            selectedTable   = -1;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            showAlert("Payment Failed",
                    "Could not process payment: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private void clearReceiptPanel() {
        if (tableNumberLabel    != null) tableNumberLabel.setText("Select an order");
        if (orderIdLabel        != null) orderIdLabel.setText("");
        if (timeLabel           != null) timeLabel.setText("");
        if (dateLabel           != null) dateLabel.setText("");
        if (itemsVBox           != null) itemsVBox.getChildren().clear();
        if (subtotalLabel       != null) subtotalLabel.setText("—");
        if (vatLabel            != null) vatLabel.setText("—");
        if (totalLabel          != null) totalLabel.setText("—");
        if (paymentTotalLabel   != null) paymentTotalLabel.setText("—");
        if (paymentReceivedLabel != null) paymentReceivedLabel.setText("—");
        if (paymentChangeLabel  != null) paymentChangeLabel.setText("—");
        if (changeAmountLabel   != null) changeAmountLabel.setText("Enter amount above");
        if (cashReceivedField   != null) cashReceivedField.clear();
    }

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

    private void showPanel(VBox target) {
        for (VBox p : new VBox[]{
                enjoyMealPanel, receiptPanel, paymentConfirmedPanel}) {
            if (p == null) continue;
            p.setVisible(p == target);
            p.setManaged(p == target);
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