package tracking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TrackingController {

    // These fields match the fx:id names in your tracking.fxml file
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<OrderStatus> actionStatusComboBox;
    
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> idColumn;
    @FXML private TableColumn<Order, String> nameColumn;
    @FXML private TableColumn<Order, OrderStatus> statusColumn;

    private final TrackingService trackingService = new TrackingService();
    private ObservableList<Order> masterOrderList;
    private FilteredList<Order> filteredOrderList;

    /**
     * This runs automatically when JavaFX loads your tracking.fxml screen.
     * It sets up the table columns, search bars, and loads your mock data.
     */
    @FXML
    public void initialize() {
        // 1. Link table columns to properties inside your Order.java class
        idColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("tableNo")); // Using tableNo as customer name identifier for now
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Fetch the orders from your Service
        masterOrderList = trackingService.getOrders();
        
        // Wrap it in a FilteredList so search bars update the screen dynamically
        filteredOrderList = new FilteredList<>(masterOrderList, p -> true);
        ordersTable.setItems(filteredOrderList);

        // 3. Populate your update status choice box
        actionStatusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        // 4. Populate your top search filter choice box
        statusFilterComboBox.getItems().add("All");
        for (OrderStatus status : OrderStatus.values()) {
            statusFilterComboBox.getItems().add(status.name());
        }
        statusFilterComboBox.getSelectionModel().selectFirst();

        // 5. Watch for changes in text fields or dropdown selections to instantly sort rows
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Filters the grid items dynamically as you change selections or type text
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatusFilter = statusFilterComboBox.getValue();

        filteredOrderList.setPredicate(order -> {
            // FIX: This now checks ONLY the table number field
            boolean matchesSearch = String.valueOf(order.getTableNo()).toLowerCase().contains(searchText);
            
            // Check Status match
            boolean matchesStatus = selectedStatusFilter.equals("All") || 
                                    order.getStatus().name().equals(selectedStatusFilter);

            return matchesSearch && matchesStatus;
        });
    }

    /**
     * Triggered automatically whenever you click the green "Update Status" button
     */
    @FXML
    private void handleUpdateStatus() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        OrderStatus nextStatus = actionStatusComboBox.getValue();

        if (selectedOrder == null) {
            showAlert("No Selection", "Please select an order from the table to update.");
            return;
        }

        if (nextStatus == null) {
            showAlert("No Status Selected", "Please select a target status from the dropdown choice.");
            return;
        }

        // Apply new status value assignment update
        selectedOrder.setStatus(nextStatus);
        
        // Visually redraw graphics array rows cleanly
        ordersTable.refresh();
        applyFilters(); 
    }

    /**
     * Displays warnings styled with your custom Crimson Red and Dark Navy slate theme palette
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Inject Custom Danger/Warning Palette to match your chosen style guidelines
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #161D30; " +        // Dark Navy Slate
            "-fx-content-text-fill: #FFFFFF; " +       // White Text
            "-fx-border-color: #FF3333; " +            // Vibrant Crimson Warning line
            "-fx-border-width: 2px;"
        );
        
        // Style the confirmation button neatly
        dialogPane.lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: #FF3333; -fx-text-fill: white; -fx-font-weight: bold;"
        );

        alert.showAndWait();
    }
}