package com.restaurant.restaurant.menu;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MenuController {

    // Table and columns
    @FXML private TableView<FoodItem> menuTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, String> colCategory;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Boolean> colAvailable;

    // Form fields
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private CheckBox availableCheck;

    // Search + filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;

    // Item count label
    @FXML private Label itemCountLabel;

    private final MenuService menuService = new MenuService();
    private ObservableList<FoodItem> itemList = FXCollections.observableArrayList();

    private static final ObservableList<String> CATEGORIES =
            FXCollections.observableArrayList(
                    "Main Course", "Chicken", "Appetizers",
                    "Drinks", "Desserts", "Sides"
            );

    @FXML
    public void initialize() {
        // Table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Style table
        menuTable.setStyle(
                "-fx-background-color: #a6092a; " +
                        "-fx-control-inner-background: #ff2929;");

        // Category combo for form
        categoryCombo.setItems(CATEGORIES);

        // Filter combo — All + categories
        ObservableList<String> filterOptions =
                FXCollections.observableArrayList("All Categories");
        filterOptions.addAll(CATEGORIES);
        filterCombo.setItems(filterOptions);
        filterCombo.setValue("All Categories");

        // Load items
        loadItems();

        // Row click fills form
        menuTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) fillForm(newVal);
                }
        );
    }

    private void loadItems() {
        itemList.clear();
        itemList.addAll(menuService.getAllItems());
        menuTable.setItems(itemList);
        updateCount();
    }

    private void fillForm(FoodItem item) {
        nameField.setText(item.getName());
        priceField.setText(String.format("%.2f", item.getPrice()));
        categoryCombo.setValue(item.getCategory());
        availableCheck.setSelected(item.isAvailable());
    }

    // ── HANDLERS ─────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        if (!validateForm()) return;

        try {
            FoodItem item = new FoodItem(
                    0,
                    nameField.getText().trim(),
                    categoryCombo.getValue(),
                    Double.parseDouble(priceField.getText().trim()),
                    availableCheck.isSelected()
            );
            boolean success = menuService.addItem(item);
            if (success) {
                showSuccess("✅ Item added successfully!");
                handleClear();
                loadItems();
            } else {
                showError("Failed to add item. Check your inputs.");
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number (e.g. 45.00)");
        }
    }

    @FXML
    private void handleUpdate() {
        FoodItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an item from the table to update.");
            return;
        }
        if (!validateForm()) return;

        try {
            selected.setName(nameField.getText().trim());
            selected.setCategory(categoryCombo.getValue());
            selected.setPrice(Double.parseDouble(priceField.getText().trim()));
            selected.setAvailable(availableCheck.isSelected());

            boolean success = menuService.updateItem(selected);
            if (success) {
                showSuccess("✅ Item updated successfully!");
                handleClear();
                loadItems();
            } else {
                showError("Failed to update item.");
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number (e.g. 45.00)");
        }
    }

    @FXML
    private void handleDelete() {
        FoodItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an item from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Item");
        confirm.setHeaderText("Delete \"" + selected.getName() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.getDialogPane().setStyle("-fx-background-color: #00fa3f;");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                menuService.deleteItem(selected.getId());
                showSuccess("✅ Item deleted successfully!");
                handleClear();
                loadItems();
            }
        });
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadItems();
            return;
        }
        itemList.clear();
        itemList.addAll(menuService.searchItems(keyword));
        menuTable.setItems(itemList);
        updateCount();
    }

    @FXML
    private void handleFilterCategory() {
        String selected = filterCombo.getValue();
        if (selected == null || selected.equals("All Categories")) {
            loadItems();
            return;
        }
        itemList.clear();
        itemList.addAll(menuService.getAllItems().stream()
                .filter(item -> item.getCategory().equals(selected))
                .collect(java.util.stream.Collectors.toList()));
        menuTable.setItems(itemList);
        updateCount();
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        priceField.clear();
        categoryCombo.setValue(null);
        availableCheck.setSelected(true);
        menuTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        SceneManager.navigateTo(NavigationUtil.STAFF_DASHBOARD);
    }

    // ── HELPERS ──────────────────────────────────────────────

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter the item name.");
            return false;
        }
        if (categoryCombo.getValue() == null) {
            showError("Please select a category.");
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            showError("Please enter the price.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                showError("Price must be greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number.");
            return false;
        }
        return true;
    }

    private void updateCount() {
        if (itemCountLabel != null) {
            itemCountLabel.setText(itemList.size() + " item(s) in menu");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #00fa3f;");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #00fa3f;");
        alert.showAndWait();
    }

    public static Parent getRoot() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MenuController.class.getResource("/fxml/menu.fxml"));
            return loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            return new javafx.scene.layout.VBox(
                    new javafx.scene.control.Label("Menu screen failed to load."));
        }
    }
}