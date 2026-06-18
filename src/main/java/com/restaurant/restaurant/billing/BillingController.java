package com.restaurant.restaurant.billing;

import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class BillingController {

    // ── Panels ──────────────────────────────────────────────
    @FXML private VBox enjoyMealPanel;
    @FXML private VBox receiptPanel;
    @FXML private VBox paymentConfirmedPanel;

    // ── Receipt card bindings ────────────────────────────────
    @FXML private Label tableNumberLabel;   // "Table 20"
    @FXML private VBox  itemsVBox;          // dynamic item rows injected here
    @FXML private Label subtotalLabel;      // "GHS 18.50"
    @FXML private Label vatLabel;           // "GHS 0.00"
    @FXML private Label totalLabel;         // "GHS 18.50"

    // ── Star-rating HBoxes ───────────────────────────────────
    @FXML private HBox foodQualityStars;
    @FXML private HBox serviceHospitalityStars;
    @FXML private HBox deliverySpeedStars;
    @FXML private HBox overallExperienceStars;

    // ── Comments ─────────────────────────────────────────────
    @FXML private TextArea commentsArea;

    // ── In-memory rating state (1–5, 0 = unset) ─────────────
    private int ratingFoodQuality       = 0;
    private int ratingServiceHospitality = 0;
    private int ratingDeliverySpeed     = 0;
    private int ratingOverallExperience = 0;

    // ── Static order data (set by upstream controller) ───────
    private static int    currentTable   = 0;
    private static String currentOrderId = "";
    private static double currentTotal   = 0.0;
    private static double currentVat     = 0.0;
    private static List<OrderLineItem> currentItems = new ArrayList<>();

    // ── Data model for a single order line ──────────────────
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

    /**
     * Called by the ordering/tracking flow before navigating here.
     * Pass table number, order id, pre-calculated subtotal & vat,
     * and the list of line items.
     */
    public static void setOrderData(int table, String orderId,
                                    double subtotal, double vat,
                                    List<OrderLineItem> items) {
        currentTable   = table;
        currentOrderId = orderId;
        currentVat     = vat;
        currentTotal   = subtotal + vat;
        currentItems   = items != null ? items : new ArrayList<>();
    }

    // ── JavaFX lifecycle ─────────────────────────────────────

    @FXML
    public void initialize() {
        showPanel(enjoyMealPanel);
        buildStarRows();
    }

    // ── Navigation ───────────────────────────────────────────

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

    // ── Receipt population ───────────────────────────────────

    private void populateReceipt() {
        // Table badge
        tableNumberLabel.setText(
                currentTable > 0 ? "Table " + currentTable : "Table —");

        // Item rows
        itemsVBox.getChildren().clear();
        double subtotal = 0;
        for (OrderLineItem item : currentItems) {
            double lineTotal = item.qty * item.unitPrice;
            subtotal += lineTotal;
            itemsVBox.getChildren().add(buildItemRow(item, lineTotal));
        }

        // Totals
        subtotalLabel.setText(formatGHS(subtotal));
        vatLabel.setText(formatGHS(currentVat));
        totalLabel.setText(formatGHS(subtotal + currentVat));
    }

    /** Builds one "Item name  qty x price  lineTotal" row */
    private VBox buildItemRow(OrderLineItem item, double lineTotal) {
        // Top row: name | line total
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.name);
        nameLabel.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label priceLabel = new Label(formatGHS(lineTotal));
        priceLabel.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 14px; -fx-font-weight: bold;");

        topRow.getChildren().addAll(nameLabel, priceLabel);

        // Sub-row: qty x unit price
        Label qtyLabel = new Label(item.qty + " x " + formatGHS(item.unitPrice));
        qtyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        VBox row = new VBox(4, topRow, qtyLabel);
        return row;
    }

    private static String formatGHS(double amount) {
        return String.format("GHS %.2f", amount);
    }

    // ── Physical receipt request ─────────────────────────────

    @FXML
    public void handleRequestPhysicalReceipt() {
        // TODO: Insert into receipt_requests table — DB hookup goes here
        System.out.println("[Receipt] Physical receipt requested — Order: "
                + currentOrderId + ", Table: " + currentTable);

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Receipt Requested");
        info.setHeaderText(null);
        info.setContentText(
                "Your receipt request has been sent.\nPlease visit the cashier for collection.");
        info.showAndWait();
    }

    // ── Star ratings ─────────────────────────────────────────

    /**
     * Builds five clickable star Labels inside each HBox.
     * Clicking a star sets the rating and re-renders the row.
     */
    private void buildStarRows() {
        buildStarRow(foodQualityStars,        () -> ratingFoodQuality,
                v -> ratingFoodQuality = v);
        buildStarRow(serviceHospitalityStars, () -> ratingServiceHospitality,
                v -> ratingServiceHospitality = v);
        buildStarRow(deliverySpeedStars,      () -> ratingDeliverySpeed,
                v -> ratingDeliverySpeed = v);
        buildStarRow(overallExperienceStars,  () -> ratingOverallExperience,
                v -> ratingOverallExperience = v);
    }

    private void buildStarRow(HBox container,
                              IntSupplier getter,
                              IntConsumer setter) {
        container.getChildren().clear();
        int current = getter.get();
        for (int i = 1; i <= 5; i++) {
            final int starIndex = i;
            Label star = new Label(i <= current ? "★" : "☆");
            star.setStyle(
                    "-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: "
                            + (i <= current ? "#F59E0B;" : "#D1D5DB;"));

            // Hover — preview fill
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < container.getChildren().size(); j++) {
                    Label s = (Label) container.getChildren().get(j);
                    s.setText(j < starIndex ? "★" : "☆");
                    s.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-text-fill: "
                            + (j < starIndex ? "#F59E0B;" : "#D1D5DB;"));
                }
            });

            // Mouse exit — restore committed rating
            star.setOnMouseExited(e -> refreshStarRow(container, getter.get()));

            // Click — commit rating
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

    // ── Feedback submission ──────────────────────────────────

    @FXML
    public void handleSubmitFeedback() {
        // Validate: overall is required
        if (ratingOverallExperience == 0) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Rating Required");
            warn.setHeaderText(null);
            warn.setContentText("Please rate your Overall Experience before submitting.");
            warn.showAndWait();
            return;
        }

        String comments = commentsArea.getText().trim();

        // TODO: Insert into feedback / ratings table — DB hookup goes here
        System.out.println("[Feedback] Order: " + currentOrderId
                + " | Table: " + currentTable
                + " | Food: "        + ratingFoodQuality
                + " | Service: "     + ratingServiceHospitality
                + " | Delivery: "    + ratingDeliverySpeed
                + " | Overall: "     + ratingOverallExperience
                + " | Comments: "    + comments);

        showPanel(paymentConfirmedPanel);
    }

    // ── Panel switching helper ───────────────────────────────

    private void showPanel(VBox target) {
        for (VBox panel : new VBox[]{enjoyMealPanel, receiptPanel, paymentConfirmedPanel}) {
            boolean visible = panel == target;
            panel.setVisible(visible);
            panel.setManaged(visible);
        }
    }

    // ── Functional interfaces (avoids java.util.function import issues) ──

    @FunctionalInterface
    interface IntSupplier { int get(); }

    @FunctionalInterface
    interface IntConsumer { void accept(int v); }
}
