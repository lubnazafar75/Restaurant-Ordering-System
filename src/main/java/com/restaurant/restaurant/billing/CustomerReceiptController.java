package com.restaurant.restaurant.billing;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import com.restaurant.restaurant.ordering.OrderController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CustomerReceiptController {

    @FXML private VBox thankYouScreen;
    @FXML private VBox receiptRequestedScreen;

    @FXML private Label orderNumberLabel;
    @FXML private Label tableNumberLabel;
    @FXML private Label totalAmountLabel;

    // Pull real order ID and table from OrderController session
    private int currentOrderId;
    private int currentTable;
    private double currentTotal;

    @FXML
    public void initialize() {
        // Use the real order data from the ordering session
        currentOrderId = OrderController.lastOrderId;
        currentTable   = OrderController.lastTableNumber;  // add this field — see note below
        currentTotal   = OrderController.lastOrderTotal;   // add this field — see note below

        refreshDisplay();
        showScreen(thankYouScreen);
    }

    private void refreshDisplay() {
        if (orderNumberLabel != null)
            orderNumberLabel.setText("Order #: " + currentOrderId);
        if (tableNumberLabel != null)
            tableNumberLabel.setText("Table  : " + String.format("%02d", currentTable));
        if (totalAmountLabel != null)
            totalAmountLabel.setText("Total  : GHS " + String.format("%.2f", currentTotal));
    }

    @FXML
    public void handleBack() {
        SceneManager.navigateToMenu();
    }

    // ── RECEIPT REQUEST — actually inserts into DB ─────────────
    @FXML
    public void handleRequestReceipt() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Request Receipt");
        confirm.setHeaderText("Request Receipt?");
        confirm.setContentText("A staff member will prepare your receipt shortly.");
        styleDialog(confirm);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                insertReceiptRequest();
                showScreen(receiptRequestedScreen);
            }
        });
    }

    private void insertReceiptRequest() {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return;
        String sql = "INSERT INTO receipt_requests (order_id, table_number, status) " +
                "VALUES (?, ?, 'pending')";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentOrderId);
            stmt.setInt(2, currentTable);
            stmt.executeUpdate();
            System.out.println("[Receipt] Request inserted for order #"
                    + currentOrderId + " Table " + currentTable);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // ── RATE EXPERIENCE — real rating dialog ───────────────────
    @FXML
    public void handleRateExperience() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate Your Experience");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType submitType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        // Rating sliders
        Label foodLbl     = makeLabel("Food Quality");
        Slider foodSlider = makeSlider();

        Label serviceLbl     = makeLabel("Service Quality");
        Slider serviceSlider = makeSlider();

        Label deliveryLbl     = makeLabel("Delivery Speed");
        Slider deliverySlider = makeSlider();

        Label overallLbl     = makeLabel("Overall Experience");
        Slider overallSlider = makeSlider();

        // Live star display
        Label foodStars     = makeStarLabel(foodSlider);
        Label serviceStars  = makeStarLabel(serviceSlider);
        Label deliveryStars = makeStarLabel(deliverySlider);
        Label overallStars  = makeStarLabel(overallSlider);

        // Wire sliders to star labels
        foodSlider.valueProperty().addListener((o, ov, nv) ->
                foodStars.setText(buildStars(nv.intValue())));
        serviceSlider.valueProperty().addListener((o, ov, nv) ->
                serviceStars.setText(buildStars(nv.intValue())));
        deliverySlider.valueProperty().addListener((o, ov, nv) ->
                deliveryStars.setText(buildStars(nv.intValue())));
        overallSlider.valueProperty().addListener((o, ov, nv) ->
                overallStars.setText(buildStars(nv.intValue())));

        // Comment box
        Label commentLbl = makeLabel("Comments (optional)");
        TextArea commentBox = new TextArea();
        commentBox.setPromptText("Tell us about your experience...");
        commentBox.setPrefRowCount(3);
        commentBox.setWrapText(true);

        // Layout
        VBox content = new VBox(10,
                foodLbl,     buildRatingRow(foodSlider, foodStars),
                serviceLbl,  buildRatingRow(serviceSlider, serviceStars),
                deliveryLbl, buildRatingRow(deliverySlider, deliveryStars),
                overallLbl,  buildRatingRow(overallSlider, overallStars),
                commentLbl,  commentBox
        );
        content.setPadding(new Insets(12));
        content.setPrefWidth(400);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == submitType) {
                saveFeedback(
                        currentOrderId,
                        (int) foodSlider.getValue(),
                        (int) serviceSlider.getValue(),
                        (int) deliverySlider.getValue(),
                        (int) overallSlider.getValue(),
                        commentBox.getText().trim()
                );
                // Show thank you after rating
                Alert thanks = new Alert(Alert.AlertType.INFORMATION);
                thanks.setTitle("Thank You!");
                thanks.setHeaderText(null);
                thanks.setContentText(
                        "Thank you for your feedback! We hope to see you again soon.");
                thanks.showAndWait();
            }
        });
    }

    // ── SAVE FEEDBACK TO DB ───────────────────────────────────
    private void saveFeedback(int orderId, int foodQuality, int serviceQuality,
                              int deliverySpeed, int overall, String comment) {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return;
        String sql = "INSERT INTO feedback " +
                "(order_id, food_quality, service_quality, delivery_speed, overall, comment) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, foodQuality);
            stmt.setInt(3, serviceQuality);
            stmt.setInt(4, deliverySpeed);
            stmt.setInt(5, overall);
            stmt.setString(6, comment.isEmpty() ? null : comment);
            stmt.executeUpdate();
            System.out.println("[Feedback] Saved for order #" + orderId
                    + " — overall: " + overall + "/5");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // ── UI HELPERS ────────────────────────────────────────────
    private Slider makeSlider() {
        Slider s = new Slider(1, 5, 3);
        s.setMajorTickUnit(1);
        s.setMinorTickCount(0);
        s.setSnapToTicks(true);
        s.setShowTickMarks(false);
        s.setPrefWidth(220);
        return s;
    }

    private Label makeStarLabel(Slider slider) {
        Label lbl = new Label(buildStars((int) slider.getValue()));
        lbl.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 18px;");
        lbl.setMinWidth(100);
        return lbl;
    }

    private HBox buildRatingRow(Slider slider, Label stars) {
        HBox row = new HBox(12, slider, stars);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        return lbl;
    }

    private String buildStars(int value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++)
            sb.append(i <= value ? "★" : "☆");
        sb.append("  ").append(value).append("/5");
        return sb.toString();
    }

    private void styleDialog(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #161D30;");
        try {
            alert.getDialogPane().lookup(".content.label")
                    .setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            alert.getDialogPane().lookup(".header-panel")
                    .setStyle("-fx-background-color: #0B0F19;");
            alert.getDialogPane().lookup(".header-panel .label")
                    .setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        } catch (Exception ignored) {}

        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-padding: 8 20;");

        Button cancelBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) cancelBtn.setStyle(
                "-fx-background-color: #2A3350; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 20;");
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