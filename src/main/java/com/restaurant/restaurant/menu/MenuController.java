package com.restaurant.restaurant.menu;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

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

    // Search
    @FXML private TextField searchField;

    private MenuService menuService = new MenuService();
    private ObservableList<FoodItem> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Set up category options
        categoryCombo.setItems(FXCollections.observableArrayList(
            "Main Course", "Drinks", "Desserts", "Starters", "Sides"
        ));

        // Load all items into table
        loadItems();

        // Click on table row to fill form
        menuTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    nameField.setText(newVal.getName());
                    priceField.setText(String.valueOf(newVal.getPrice()));
                    categoryCombo.setValue(newVal.getCategory());
                    availableCheck.setSelected(newVal.isAvailable());
                }
            }
        );
    }

    private void loadItems() {
        itemList.clear();
        itemList.addAll(menuService.getAllItems());
        menuTable.setItems(itemList);
    }

    @FXML
    private void handleAdd() {
        FoodItem item = new FoodItem(
            0,
            nameField.getText(),
            categoryCombo.getValue(),
            Double.parseDouble(priceField.getText()),
            availableCheck.isSelected()
        );
        boolean success = menuService.addItem(item);
        if (success) {
            showAlert("Success", "Food item added successfully!");
            clearForm();
            loadItems();
        } else {
            showAlert("Error", "Please fill in all fields correctly.");
        }
    }

    @FXML
    private void handleUpdate() {
        FoodItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an item to update.");
            return;
        }
        selected.setName(nameField.getText());
        selected.setCategory(categoryCombo.getValue());
        selected.setPrice(Double.parseDouble(priceField.getText()));
        selected.setAvailable(availableCheck.isSelected());

        boolean success = menuService.updateItem(selected);
        if (success) {
            showAlert("Success", "Food item updated successfully!");
            clearForm();
            loadItems();
        }
    }

    @FXML
    private void handleDelete() {
        FoodItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an item to delete.");
            return;
        }
        menuService.deleteItem(selected.getId());
        showAlert("Success", "Food item deleted successfully!");
        clearForm();
        loadItems();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        itemList.clear();
        itemList.addAll(menuService.searchItems(keyword));
        menuTable.setItems(itemList);
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        categoryCombo.setValue(null);
        availableCheck.setSelected(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static Parent getRoot() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MenuController.class.getResource("/com/restaurant/restaurant/menu/menu.fxml")
            );
            return loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[MenuController] Could not load menu.fxml");
            return new javafx.scene.layout.VBox(
                    new javafx.scene.control.Label("Menu screen failed to load.")
            );
        }
    }
}