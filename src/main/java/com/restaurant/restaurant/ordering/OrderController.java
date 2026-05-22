
package ordering;

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
        foodComboBox.setItems(FXCollections.observableArrayList("Pizza", "Burger", "Drink"));

        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        quantityColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        priceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        subtotalColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubtotal()));

        tableView.setItems(cartList);
    }

    private double getPrice(String food) {
        switch (food) {
            case "Pizza": return 50;
            case "Burger": return 30;
            case "Drink": return 10;
            default: return 0;
        }
    }

    @FXML
    public void handleAddToCart() {
        String name = foodComboBox.getValue();
        int qty = Integer.parseInt(quantityField.getText());

        double price = getPrice(name);

        OrderItem item = new OrderItem(name, price, qty);

        order.addItem(item);
        cartList.add(item);

        updateTotal();
    }

    @FXML
    public void handleRemoveItem() {
        OrderItem selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            order.removeItem(selected);
            cartList.remove(selected);
            updateTotal();
        }
    }

    private void updateTotal() {
        totalLabel.setText("Total: GHS " + order.getTotal());
    }

    @FXML
    public void handleConfirmOrder() {
        System.out.println("Order Confirmed! Total = " + order.getTotal());
    }
}