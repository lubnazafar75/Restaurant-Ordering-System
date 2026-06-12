package com.restaurant.restaurant.billing;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BillingController — Final production version.
 * Connect to database by replacing billingService.getServedOrders()
 * and billingService.buildReceipt(orderId) calls.
 * Lubna's SceneManager handles navigation between screens.
 */
public class BillingController {

    // ---------------------------------------------------------------
    // FXML FIELDS — Waiter Screen
    // ---------------------------------------------------------------
    @FXML private Button backButton;
    @FXML private ListView<BillingService.OrderSummary> orderListView;
    @FXML private TextField searchField;
    @FXML private VBox receiptDetailPanel;
    @FXML private VBox cashPanel;
    @FXML private Button confirmPaymentButton;
    @FXML private Button printBillButton;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeAmountLabel;
    @FXML private Label paymentTotalLabel;
    @FXML private Label paymentReceivedLabel;
    @FXML private Label paymentChangeLabel;

    // ---------------------------------------------------------------
    // FXML FIELDS — Receipt Detail
    // ---------------------------------------------------------------
    @FXML private Label orderIdLabel;
    @FXML private Label tableNumberLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private VBox itemsVBox;
    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    // ---------------------------------------------------------------
    // FXML FIELDS — Customer Screen
    // ---------------------------------------------------------------
    @FXML private VBox enjoyMealPanel;
    @FXML private VBox receiptPanel;
    @FXML private VBox paymentConfirmedPanel;
    @FXML private Label enjoyMealTitle;
    @FXML private HBox enjoyMealSubtitle;
    @FXML private Button showReceiptButton;
    @FXML private Label plateIcon;

    // ---------------------------------------------------------------
    // PRIVATE STATE
    // ---------------------------------------------------------------
    private final BillingService billingService = new BillingService();
    private Receipt currentReceipt = null;


    // ---------------------------------------------------------------
    // INITIALIZE
    // ---------------------------------------------------------------
    @FXML
    public void initialize() {
        if (orderListView != null) {
            setupOrderList();
        }
        if (enjoyMealPanel != null) {
            showEnjoyMealScreen();
            playEnjoyMealAnimations();
        }
    }


    // ---------------------------------------------------------------
    // WAITER SCREEN — Order List Setup
    // ---------------------------------------------------------------
    private void setupOrderList() {
        // Load real served orders from database
        List<BillingService.OrderSummary> orders =
            billingService.getServedOrders();

        // Keep full list for search filtering
        List<BillingService.OrderSummary> allOrders =
            new ArrayList<>(orders);

        orderListView.getItems().addAll(orders);

        BillingController controller = this;

        orderListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(
                    BillingService.OrderSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(controller.buildOrderCard(item));
                    setText(null);
                    setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 4 8 4 8;"
                    );
                }
            }
        });

        orderListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, selectedOrder) -> {
                if (selectedOrder != null) {
                    // Reset cash panel for each new selection
                    if (cashReceivedField != null) cashReceivedField.clear();
                    if (changeAmountLabel != null) {
                        changeAmountLabel.setText("Enter amount above");
                        changeAmountLabel.setStyle(
                            "-fx-text-fill: #888888; -fx-font-size: 14px;"
                        );
                    }
                    if (paymentReceivedLabel != null)
                        paymentReceivedLabel.setText("—");
                    if (paymentChangeLabel != null)
                        paymentChangeLabel.setText("—");

                    // Load receipt from database
                    currentReceipt = billingService.buildReceipt(
                        selectedOrder.getOrderId()
                    );
                    displayReceipt(currentReceipt);
                    receiptDetailPanel.setVisible(true);
                    receiptDetailPanel.setManaged(true);
                    if (cashPanel != null) {
                        cashPanel.setVisible(true);
                        cashPanel.setManaged(true);
                    }
                    confirmPaymentButton.setDisable(false);
                    updateCashSummary();
                }
            }
        );

        if (confirmPaymentButton != null)
            confirmPaymentButton.setDisable(true);
        if (receiptDetailPanel != null) {
            receiptDetailPanel.setVisible(false);
            receiptDetailPanel.setManaged(false);
        }
        if (cashPanel != null) {
            cashPanel.setVisible(false);
            cashPanel.setManaged(false);
        }

        setupSearch(allOrders);
    }


    // ---------------------------------------------------------------
    // SEARCH — table number only, exact prefix match
    // ---------------------------------------------------------------
    private void setupSearch(List<BillingService.OrderSummary> allOrders) {
        if (searchField == null) return;
        searchField.setOnKeyReleased(e -> {
            String query = searchField.getText().trim();
            orderListView.getItems().clear();
            if (query.isEmpty()) {
                orderListView.getItems().addAll(allOrders);
            } else {
                for (BillingService.OrderSummary o : allOrders) {
                    // Match only on table number — exact start match
                    String tableStr = String.valueOf(o.getTableNumber());
                    if (tableStr.equals(query)) {
                        orderListView.getItems().add(o);
                    }
                }
            }
        });
    }


    // ---------------------------------------------------------------
    // WAITER SCREEN — Build Order Card
    // ---------------------------------------------------------------
    public VBox buildOrderCard(BillingService.OrderSummary item) {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: #161D30;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12;" +
            "-fx-border-color: #2A3350;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );

        HBox topRow = new HBox();
        Label tableLabel = new Label("Table " + item.getTableNumber());
        tableLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );
        Label tsLabel = new Label(item.getTimestamp());
        tsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().addAll(tableLabel, spacer, tsLabel);

        HBox bottomRow = new HBox(10);
        Label badge = new Label("Ready to Bill");
        badge.setStyle(
            "-fx-background-color: #003320;" +
            "-fx-text-fill: #00E676;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 3 8 3 8;"
        );
        Label amountLabel = new Label(
            String.format("GHS %.2f", item.getTotalAmount())
        );
        amountLabel.setStyle(
            "-fx-text-fill: #FF4A85;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        HBox spacer2 = new HBox();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        bottomRow.getChildren().addAll(badge, spacer2, amountLabel);

        card.getChildren().addAll(topRow, bottomRow);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #1E2740;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12;" +
            "-fx-border-color: #FF4A85;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #161D30;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12;" +
            "-fx-border-color: #2A3350;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        ));

        return card;
    }


    // ---------------------------------------------------------------
    // DISPLAY RECEIPT
    // ---------------------------------------------------------------
    private void displayReceipt(Receipt receipt) {
        orderIdLabel.setText("Order ID: " + receipt.getFormattedOrderId());
        tableNumberLabel.setText("Table " + receipt.getTableNumber());
        dateLabel.setText(receipt.getFormattedDate());
        timeLabel.setText(receipt.getFormattedTime());

        itemsVBox.getChildren().clear();

        // Column headers
        HBox header = new HBox();
        header.setStyle("-fx-padding: 2 0 6 0;");

        Label hItem = new Label("ITEM");
        hItem.setStyle(
            "-fx-text-fill: #555555; -fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        hItem.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(hItem, javafx.scene.layout.Priority.ALWAYS);

        Label hQty = new Label("QTY");
        hQty.setPrefWidth(50);
        hQty.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        hQty.setStyle(
            "-fx-text-fill: #555555; -fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Label hAmount = new Label("AMOUNT");
        hAmount.setPrefWidth(90);
        hAmount.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        hAmount.setStyle(
            "-fx-text-fill: #555555; -fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(hItem, hQty, hAmount);
        itemsVBox.getChildren().add(header);

        Separator headerSep = new Separator();
        headerSep.setStyle("-fx-background-color: #2A3350;");
        itemsVBox.getChildren().add(headerSep);

        // Item rows
        boolean alternate = false;
        for (Receipt.ReceiptItem item : receipt.getItems()) {
            HBox row = new HBox();
            row.setStyle(
                "-fx-background-color: " +
                (alternate ? "#1A2235" : "transparent") + ";" +
                "-fx-padding: 7 4 7 4;" +
                "-fx-background-radius: 5;"
            );

            Label nameLabel = new Label(item.getFoodName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

            Label qtyLabel = new Label("x" + item.getQuantity());
            qtyLabel.setPrefWidth(50);
            qtyLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            qtyLabel.setStyle(
                "-fx-text-fill: #aaaaaa; -fx-font-size: 14px;"
            );

            Label priceLabel = new Label(
                String.format("GHS %.2f", item.getLineTotal())
            );
            priceLabel.setPrefWidth(90);
            priceLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            row.getChildren().addAll(nameLabel, qtyLabel, priceLabel);
            itemsVBox.getChildren().add(row);
            alternate = !alternate;
        }

        // Totals
        subtotalLabel.setText(
            String.format("GHS %.2f", receipt.getSubtotal())
        );
        vatLabel.setText(
            String.format("GHS %.2f", receipt.getVatAmount())
        );
        totalLabel.setText(
            String.format("GHS %.2f", receipt.getTotal())
        );

        if (discountLabel != null) {
            if (receipt.getDiscountApplied() > 0) {
                discountLabel.setText(
                    String.format("- GHS %.2f", receipt.getDiscountApplied())
                );
                discountLabel.setVisible(true);
                discountLabel.setManaged(true);
            } else {
                discountLabel.setVisible(false);
                discountLabel.setManaged(false);
            }
        }

        if (paymentTotalLabel != null) {
            paymentTotalLabel.setText(
                String.format("GHS %.2f", receipt.getTotal())
            );
        }

        if (receiptPanel != null) {
            receiptPanel.setVisible(true);
            receiptPanel.setManaged(true);
        }
    }


    // ---------------------------------------------------------------
    // WAITER — Cash Calculator
    // ---------------------------------------------------------------
    @FXML
    public void handleCashInput() {
        updateCashSummary();
    }

    private void updateCashSummary() {
        if (currentReceipt == null || cashReceivedField == null) return;

        try {
            double received = Double.parseDouble(
                cashReceivedField.getText().trim()
            );
            double total  = currentReceipt.getTotal();
            double change = received - total;

            if (paymentReceivedLabel != null) {
                paymentReceivedLabel.setText(
                    String.format("GHS %.2f", received)
                );
            }
            if (paymentChangeLabel != null) {
                paymentChangeLabel.setText(
                    String.format("GHS %.2f", Math.abs(change))
                );
            }
            if (changeAmountLabel != null) {
                if (change >= 0) {
                    changeAmountLabel.setText(
                        String.format("GHS %.2f", change)
                    );
                    changeAmountLabel.setStyle(
                        "-fx-text-fill: #00E676;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
                    );
                } else {
                    changeAmountLabel.setText(
                        String.format("Short GHS %.2f", Math.abs(change))
                    );
                    changeAmountLabel.setStyle(
                        "-fx-text-fill: #FF3333;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
                    );
                }
            }
        } catch (NumberFormatException e) {
            if (changeAmountLabel != null) {
                changeAmountLabel.setText("Enter amount above");
                changeAmountLabel.setStyle(
                    "-fx-text-fill: #888888; -fx-font-size: 14px;"
                );
            }
        }
    }


    // ---------------------------------------------------------------
    // WAITER — Confirm Payment
    // ---------------------------------------------------------------
    @FXML
    public void handleConfirmPayment() {
        if (currentReceipt == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Payment");
        alert.setHeaderText(
            "Confirm payment for Table " +
            currentReceipt.getTableNumber() + "?"
        );
        alert.setContentText(
            "Order " + currentReceipt.getFormattedOrderId() +
            "\nTotal: GHS " +
            String.format("%.2f", currentReceipt.getTotal()) +
            "\n\nThis action cannot be undone."
        );

        alert.getDialogPane().setStyle("-fx-background-color: #161D30;");
        alert.getDialogPane().lookup(".content.label")
            .setStyle("-fx-text-fill: white;");
        alert.getDialogPane().lookup(".header-panel")
            .setStyle("-fx-background-color: #0B0F19;");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = billingService.confirmPayment(currentReceipt);

            if (success) {
                orderListView.getItems().remove(
                    orderListView.getSelectionModel().getSelectedItem()
                );
                receiptDetailPanel.setVisible(false);
                receiptDetailPanel.setManaged(false);
                if (cashPanel != null) {
                    cashPanel.setVisible(false);
                    cashPanel.setManaged(false);
                }
                confirmPaymentButton.setDisable(true);
                currentReceipt = null;
                System.out.println(
                    "Payment confirmed. Table reset to available."
                );
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Payment Failed");
                errorAlert.setHeaderText("Could not confirm payment.");
                errorAlert.setContentText(
                    "Please check the database connection."
                );
                errorAlert.showAndWait();
            }
        }
    }


    // ---------------------------------------------------------------
    // PRINT
    // ---------------------------------------------------------------
    @FXML
    public void handlePrintReceipt() {
        Node targetNode =
            (receiptPanel != null && receiptPanel.isVisible())
            ? receiptPanel
            : receiptDetailPanel;

        if (targetNode == null) return;

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(
                targetNode.getScene().getWindow())) {

            javafx.print.PageLayout pageLayout =
                job.getPrinter().getDefaultPageLayout();
            double pageWidth =
                pageLayout.getPrintableWidth();
            double nodeWidth =
                targetNode.getBoundsInParent().getWidth();
            double scale = pageWidth / nodeWidth;

            javafx.scene.transform.Scale scaleTransform =
                new javafx.scene.transform.Scale(scale, scale);
            targetNode.getTransforms().add(scaleTransform);
            boolean printed = job.printPage(pageLayout, targetNode);
            targetNode.getTransforms().remove(scaleTransform);

            if (printed) job.endJob();
        }
    }


    // ---------------------------------------------------------------
    // CUSTOMER SCREEN — Animations
    // ---------------------------------------------------------------
    private void playEnjoyMealAnimations() {

        if (plateIcon != null) {
            ScaleTransition pulse = new ScaleTransition(
                Duration.seconds(1.8), plateIcon
            );
            pulse.setFromX(1.0); pulse.setToX(1.12);
            pulse.setFromY(1.0); pulse.setToY(1.12);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(ScaleTransition.INDEFINITE);
            pulse.setInterpolator(Interpolator.EASE_BOTH);
            pulse.play();
        }

        if (enjoyMealTitle != null) {
            enjoyMealTitle.setOpacity(0);
            FadeTransition titleFade = new FadeTransition(
                Duration.seconds(1.5), enjoyMealTitle
            );
            titleFade.setFromValue(0);
            titleFade.setToValue(1);
            titleFade.play();
        }

        if (enjoyMealSubtitle != null) {
            enjoyMealSubtitle.setOpacity(0);
            enjoyMealSubtitle.setTranslateY(20);

            FadeTransition subFade = new FadeTransition(
                Duration.seconds(1.2), enjoyMealSubtitle
            );
            subFade.setFromValue(0);
            subFade.setToValue(1);
            subFade.setDelay(Duration.seconds(0.5));

            TranslateTransition subSlide = new TranslateTransition(
                Duration.seconds(1.2), enjoyMealSubtitle
            );
            subSlide.setFromY(20);
            subSlide.setToY(0);
            subSlide.setDelay(Duration.seconds(0.5));
            subSlide.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(subFade, subSlide).play();
        }

        if (showReceiptButton != null) {
            FadeTransition shimmer = new FadeTransition(
                Duration.seconds(1.2), showReceiptButton
            );
            shimmer.setFromValue(0.75);
            shimmer.setToValue(1.0);
            shimmer.setAutoReverse(true);
            shimmer.setCycleCount(FadeTransition.INDEFINITE);
            shimmer.setDelay(Duration.seconds(1.5));
            shimmer.play();

            showReceiptButton.setOnMouseEntered(e ->
                showReceiptButton.setStyle(
                    "-fx-background-color: #E7447A;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: #FF4A85;" +
                    "-fx-border-radius: 30;" +
                    "-fx-background-radius: 30;" +
                    "-fx-border-width: 3;" +
                    "-fx-padding: 14 30 14 30;" +
                    "-fx-cursor: hand;"
                )
            );
            showReceiptButton.setOnMouseExited(e ->
                showReceiptButton.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #FF4A85;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: #FF4A85;" +
                    "-fx-border-radius: 30;" +
                    "-fx-background-radius: 30;" +
                    "-fx-border-width: 3;" +
                    "-fx-padding: 14 30 14 30;" +
                    "-fx-cursor: hand;"
                )
            );
        }
    }


    // ---------------------------------------------------------------
    // CUSTOMER SCREEN — Navigation
    // ---------------------------------------------------------------

    // Called by tracking module after food is delivered
    public void loadReceiptForOrder(int orderId) {
        currentReceipt = billingService.buildReceipt(orderId);
        displayReceipt(currentReceipt);
        setCustomerPanel(enjoyMealPanel);
        playEnjoyMealAnimations();
    }

    @FXML
    public void handleShowReceipt() {
        if (currentReceipt != null) {
            displayReceipt(currentReceipt);
        }
        setCustomerPanel(receiptPanel);
        playReceiptRevealAnimation();
    }

    @FXML
    public void handleBackToEnjoyMeal() {
        setCustomerPanel(enjoyMealPanel);
    }

    @FXML
    public void handleBackHoverIn() {
        if (backButton == null) return;
        backButton.setStyle(
            "-fx-background-color: #FF6B9D;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #FF6B9D;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18 8 18;"
        );
    }

    @FXML
    public void handleBackHoverOut() {
        if (backButton == null) return;
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #FF4A85;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #FF4A85;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 18 8 18;"
        );
    }

    // Called by Lubna's SceneManager after waiter confirms payment
    public void showPaymentConfirmed() {
        setCustomerPanel(paymentConfirmedPanel);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> navigateToHome());
        pause.play();
    }

    private void playReceiptRevealAnimation() {
        if (receiptPanel == null) return;

        receiptPanel.setOpacity(0);
        receiptPanel.setScaleX(0.85);
        receiptPanel.setScaleY(0.85);

        FadeTransition fade = new FadeTransition(
            Duration.millis(400), receiptPanel
        );
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale = new ScaleTransition(
            Duration.millis(400), receiptPanel
        );
        scale.setFromX(0.85); scale.setToX(1.0);
        scale.setFromY(0.85); scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }


    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    private void showEnjoyMealScreen() {
        setCustomerPanel(enjoyMealPanel);
    }

    private void setCustomerPanel(VBox panelToShow) {
        if (enjoyMealPanel != null) {
            enjoyMealPanel.setVisible(false);
            enjoyMealPanel.setManaged(false);
        }
        if (receiptPanel != null) {
            receiptPanel.setVisible(false);
            receiptPanel.setManaged(false);
        }
        if (paymentConfirmedPanel != null) {
            paymentConfirmedPanel.setVisible(false);
            paymentConfirmedPanel.setManaged(false);
        }
        if (panelToShow != null) {
            panelToShow.setVisible(true);
            panelToShow.setManaged(true);
        }
    }

    private void navigateToHome() {
        // Replace with: SceneManager.navigateTo("home");
        System.out.println("Navigating to home screen...");
    }
}