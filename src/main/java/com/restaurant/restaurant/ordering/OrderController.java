package com.restaurant.restaurant.ordering;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderController {

    @FXML private ComboBox<String> foodComboBox;
    @FXML private TextField quantityField;
    @FXML private TableView<OrderItem> tableView;
    @FXML private TableColumn<OrderItem, String> nameColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, Double> priceColumn;
    @FXML private TableColumn<OrderItem, Double> subtotalColumn;
    @FXML private Label totalLabel;

    private Order order = new Order();
    private ObservableList<OrderItem> cartList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        foodComboBox.setItems(FXCollections.observableArrayList(
                "Pizza", "Burger", "Drink", "Jollof Rice",
                "Fried Rice", "Chicken", "Salad", "Juice"
        ));

        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getName()));
        quantityColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getQuantity()));
        priceColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getPrice()));
        subtotalColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getSubtotal()));

        tableView.setItems(cartList);
        updateTotal();
    }

    private double getPrice(String food) {
        switch (food) {
            case "Pizza": return 50.0;
            case "Burger": return 30.0;
            case "Drink": return 10.0;
            case "Jollof Rice": return 45.0;
            case "Fried Rice": return 40.0;
            case "Chicken": return 35.0;
            case "Salad": return 20.0;
            case "Juice": return 15.0;
            default: return 0.0;
        }
    }

    @FXML
    public void handleAddToCart() {
        // Validate food selection
        String name = foodComboBox.getValue();
        if (name == null || name.isEmpty()) {
            showAlert("No Food Selected", "Please select a food item first.");
            return;
        }

        // Validate quantity
        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            showAlert("No Quantity", "Please enter a quantity.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                showAlert("Invalid Quantity", "Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Quantity", "Please enter a valid number.");
            return;
        }

        double price = getPrice(name);
        OrderItem item = new OrderItem(name, price, qty);
        order.addItem(item);
        cartList.add(item);
        quantityField.clear();
        updateTotal();
    }

    @FXML
    public void handleRemoveItem() {
        OrderItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            order.removeItem(selected);
            cartList.remove(selected);
            updateTotal();
        } else {
            showAlert("No Selection", "Please select an item to remove.");
        }
    }

    @FXML
    public void handleConfirmOrder() {
        if (cartList.isEmpty()) {
            showAlert("Empty Cart", "Please add items before confirming.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Order");
        confirm.setHeaderText("Confirm your order?");
        confirm.setContentText("Total: GHS " +
                String.format("%.2f", order.getTotal()));
        confirm.getDialogPane().setStyle(
                "-fx-background-color: #161D30;");
        confirm.getDialogPane().lookup(".content.label")
                .setStyle("-fx-text-fill: white;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Order Confirmed! Total = GHS " +
                        String.format("%.2f", order.getTotal()));
                cartList.clear();
                order = new Order();
                updateTotal();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Order Placed");
                success.setHeaderText(null);
                success.setContentText(
                        "Your order has been placed successfully!");
                success.showAndWait();
            }
        });
    }

    private void updateTotal() {
        totalLabel.setText("Total: GHS " +
                String.format("%.2f", order.getTotal()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle(
                "-fx-background-color: #161D30;");
        alert.showAndWait();
    }
}