package com.restaurant.restaurant.ordering;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderController {



    // ─── SCREEN PANELS ───────────────────────────────────────
    @FXML private VBox tableEntryScreen;
    @FXML private HBox menuScreen;
    @FXML private VBox confirmScreen;


    // ─── TABLE ENTRY ─────────────────────────────────────────
    @FXML private TextField tableNumberField;

    // ─── MENU ────────────────────────────────────────────────
    @FXML private Label tableLabel;
    @FXML private TextField searchField;
    @FXML private HBox categoryBar;
    @FXML private FlowPane foodGrid;

    // ─── CART ────────────────────────────────────────────────
    @FXML private VBox cartItemsBox;
    @FXML private Label cartCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalLabel;
    @FXML private Button trackOrderBtn;

    // ─── CONFIRM ─────────────────────────────────────────────
    @FXML private Label confirmMessageLabel;
    @FXML private Label orderSummaryLabel;

    // ─── STATE ───────────────────────────────────────────────
    public static int lastOrderId = -1;
    public static int lastTableNumber = 0;
    public static double lastOrderTotal  = 0.0;
    private static final int MAX_TABLES = 20; // ← single constant for easy changes
    private int tableNumber = 0;
    private Order order = new Order();
    private String currentCategory = "All";

    private final Map<String, List<String[]>> menuData = new LinkedHashMap<>();

    // ─── INIT ────────────────────────────────────────────────
    @FXML
    public void initialize() {
        buildMenuData();
        showScreen(tableEntryScreen);

        if (OrderController.lastOrderId != -1) {
            trackOrderBtn.setVisible(true);
            trackOrderBtn.setManaged(true);
        } else {
            trackOrderBtn.setVisible(false);
            trackOrderBtn.setManaged(false);
        }
    }

    // ─── MENU DATA ───────────────────────────────────────────
    private void buildMenuData() {
        List<String[]> mains = new ArrayList<>();
        mains.add(new String[]{"Jollof Rice",      "45.00"});
        mains.add(new String[]{"Fried Rice",        "40.00"});
        mains.add(new String[]{"Banku & Tilapia",   "55.00"});
        mains.add(new String[]{"Fufu & Soup",       "50.00"});
        mains.add(new String[]{"Waakye",            "35.00"});
        mains.add(new String[]{"Kenkey & Fish",     "40.00"});

        List<String[]> chicken = new ArrayList<>();
        chicken.add(new String[]{"Grilled Chicken",  "65.00"});
        chicken.add(new String[]{"Fried Chicken",    "60.00"});
        chicken.add(new String[]{"Chicken Burger",   "50.00"});
        chicken.add(new String[]{"Chicken Sandwich", "45.00"});

        List<String[]> appetizers = new ArrayList<>();
        appetizers.add(new String[]{"Spring Rolls", "25.00"});
        appetizers.add(new String[]{"Kelewele",     "20.00"});
        appetizers.add(new String[]{"Salad",        "22.00"});
        appetizers.add(new String[]{"Chips",        "18.00"});

        List<String[]> drinks = new ArrayList<>();
        drinks.add(new String[]{"Coca-Cola",   "8.00"});
        drinks.add(new String[]{"Malt Drink",  "10.00"});
        drinks.add(new String[]{"Fresh Juice", "15.00"});
        drinks.add(new String[]{"Water",       "5.00"});
        drinks.add(new String[]{"Sobolo",      "12.00"});

        List<String[]> desserts = new ArrayList<>();
        desserts.add(new String[]{"Ice Cream",   "20.00"});
        desserts.add(new String[]{"Cake Slice",  "25.00"});
        desserts.add(new String[]{"Fruit Salad", "18.00"});

        menuData.put("Main Meals",  mains);
        menuData.put("Chicken",     chicken);
        menuData.put("Appetizers",  appetizers);
        menuData.put("Drinks",      drinks);
        menuData.put("Desserts",    desserts);
    }

    // ─── SCREEN 1: TABLE ENTRY ───────────────────────────────
    @FXML
    public void handleStartOrder() {
        String input = tableNumberField.getText().trim();

        // Empty check
        if (input.isEmpty()) {
            showAlert("Table Number Required",
                    "Please enter your table number to continue.");
            return;
        }

        // Numeric check
        int num;
        try {
            num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input",
                    "Table number must be a number.");
            tableNumberField.clear();
            tableNumberField.requestFocus();
            return;
        }

        // Lower bound check
        if (num < 1) {
            showAlert("Invalid Table Number",
                    "Table number must be at least 1.");
            tableNumberField.clear();
            tableNumberField.requestFocus();
            return;
        }

        // Upper bound check — MAX_TABLES constant
        if (num > MAX_TABLES) {
            showAlert("Table Not Found",
                    "This restaurant only has " + MAX_TABLES + " tables.\n" +
                            "Please enter a table number between 1 and " + MAX_TABLES + ".");
            tableNumberField.clear();
            tableNumberField.requestFocus();
            return;
        }

        // DB availability check
        if (!isTableAvailable(num)) {
            showAlert("Table Unavailable",
                    "Table " + num + " is currently occupied.\n" +
                            "Please speak to a staff member for assistance.");
            tableNumberField.clear();
            tableNumberField.requestFocus();
            return;
        }

        // All checks passed
        tableNumber = num;
        tableLabel.setText("Table " + tableNumber);
        buildCategoryBar();
        loadFoodItems("All");
        showScreen(menuScreen);

        if (OrderController.lastOrderId != -1) {
            trackOrderBtn.setVisible(true);
            trackOrderBtn.setManaged(true);
        }
    }

    /**
     * Checks the DB to confirm the table exists and is available.
     * Returns true (fail open) if no DB connection.
     */
    private boolean isTableAvailable(int tableNum) {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return true;

        String sql = "SELECT status FROM tables WHERE table_number = ?";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableNum);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false; // not found in DB
                return "available".equalsIgnoreCase(rs.getString("status"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[OrderController] Table check error: "
                    + e.getMessage());
            return true; // fail open
        }
    }

    // ─── SCREEN 2: MENU ──────────────────────────────────────
    private void buildCategoryBar() {
        categoryBar.getChildren().clear();
        addCategoryButton("All", true);
        for (String category : menuData.keySet()) {
            addCategoryButton(category, false);
        }
    }

    private void addCategoryButton(String name, boolean active) {
        Button btn = new Button(name);
        btn.getStyleClass().add(active ? "btn-pill-active" : "btn-pill");

        btn.setOnAction(e -> {
            currentCategory = name;
            categoryBar.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    ((Button) node).getStyleClass().removeAll("btn-pill-active");
                    if (!((Button) node).getStyleClass().contains("btn-pill"))
                        ((Button) node).getStyleClass().add("btn-pill");
                }
            });
            btn.getStyleClass().remove("btn-pill");
            btn.getStyleClass().add("btn-pill-active");
            loadFoodItems(name);
        });

        categoryBar.getChildren().add(btn);
    }

    private void loadFoodItems(String category) {
        foodGrid.getChildren().clear();
        String search = searchField != null
                ? searchField.getText().toLowerCase().trim() : "";

        List<String[]> items = new ArrayList<>();
        if (category.equals("All")) {
            for (List<String[]> list : menuData.values()) items.addAll(list);
        } else {
            items = menuData.getOrDefault(category, new ArrayList<>());
        }

        for (String[] item : items) {
            if (!search.isEmpty() &&
                    !item[0].toLowerCase().contains(search)) continue;
            foodGrid.getChildren().add(buildFoodCard(item[0], item[1]));
        }
    }

    @FXML
    public void handleSearch() {
        loadFoodItems(currentCategory);
    }

    private VBox buildFoodCard(String name, String priceStr) {
        double price = Double.parseDouble(priceStr);

        VBox card = new VBox(8);
        card.setPrefWidth(160);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("food-card");

        ImageView imageView = new ImageView();

        imageView.setStyle(
                "-fx-background-color: #f0f0f0;-fx-border-radius: 60;-fx-max-width: 200"

        );
        imageView.setFitWidth(120);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

// get image for this food
        String path = foodImages.get(name);

        Image image;

        if (path != null) {
            image = new Image(getClass().getResourceAsStream(path));
            imageView.setImage(image);
        }

        Label nameLabel = new Label(name);
        nameLabel.setStyle(
                "-fx-text-fill: #747474; -fx-font-size: 13px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label priceLabel = new Label("GHS " + priceStr);
        priceLabel.setStyle(
                "-fx-text-fill: #c7052e; -fx-font-size: 13px; -fx-font-weight: bold;");

        Button addBtn = new Button("+ Add");
        addBtn.setPrefWidth(120);
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setStyle(addBtn.getStyle() + "-fx-font-size: 12px; -fx-padding: 8 6;");
        addBtn.setOnAction(e -> addToCart(name, price));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, addBtn);

        return card;
    }

    private String getFoodEmoji(String name) {
        String n = name.toLowerCase();
        if (n.contains("rice"))                        return "🍚";
        if (n.contains("chicken"))                     return "🍗";
        if (n.contains("burger"))                      return "🍔";
        if (n.contains("sandwich"))                    return "🥪";
        if (n.contains("salad"))                       return "🥗";
        if (n.contains("chips") || n.contains("fries"))return "🍟";
        if (n.contains("juice") || n.contains("sobolo"))return "🧃";
        if (n.contains("water"))                       return "💧";
        if (n.contains("cola") || n.contains("malt")) return "🥤";
        if (n.contains("ice cream"))                   return "🍦";
        if (n.contains("cake"))                        return "🍰";
        if (n.contains("fruit"))                       return "🍓";
        if (n.contains("fish") || n.contains("tilapia"))return "🐟";
        if (n.contains("soup") || n.contains("fufu")) return "🍲";
        if (n.contains("roll"))                        return "🥟";
        return "🍽";
    }
    // ─── FOOD IMAGE URLs ─────────────────────────────────────
// Paste the image URL for each food item here.
// If a URL is missing or fails to load, falls back to emoji.
    private static final Map<String, String> foodImages = new LinkedHashMap<>();

    static {
        // ── Main Meals ────────────────────────────────────────
        foodImages.put("Jollof Rice", "/images/jollof.jpg");

        foodImages.put("Fried Rice",
                "/images/friedRice.jpg");  // ← paste URL here
        foodImages.put("Banku & Tilapia",
                "/images/banku.jpg");
        foodImages.put("Fufu & Soup",
                "/images/fufu.jpg");
        foodImages.put("Waakye",
                "/images/waakye.jpg");
        foodImages.put("Kenkey & Fish",
                "/images/kenkey.jpg");

        // ── Chicken ───────────────────────────────────────────
        foodImages.put("Grilled Chicken",
                "/images/roastedChick.jpg");
        foodImages.put("Fried Chicken",
                "/images/friedChick.jpg");
        foodImages.put("Chicken Burger",
                "/images/burger.jpg");
        foodImages.put("Chicken Sandwich",
                "/images/sandW.jpg");

        // ── Appetizers ────────────────────────────────────────
        foodImages.put("Spring Rolls",
                "/images/springRoll.jpg");
        foodImages.put("Kelewele",
                "/images/Kelewele.jpg");
        foodImages.put("Salad",
                "/images/Salad.jpg");
        foodImages.put("Chips",
                "/images/Chips.jpg");

        // ── Drinks ────────────────────────────────────────────
        foodImages.put("Coca-Cola",
                "/images/coca.jpg");
        foodImages.put("Malt Drink",
                "/images/malt.jpg");
        foodImages.put("Fresh Juice",
                "/images/Juice.jpg");
        foodImages.put("Water",
                "/images/water.jpg");
        foodImages.put("Sobolo",
                "/images/sobolo.jpg");

        // ── Desserts ──────────────────────────────────────────
        foodImages.put("Ice Cream",
                "/images/iceCream.jpg");
        foodImages.put("Cake Slice",
                "/images/cake.jpg");
        foodImages.put("Fruit Salad",
                "/images/Fruit_Salad.jpg");
    }
    public void showMenuDirectly() {
        if (tableNumber > 0) showScreen(menuScreen);
        else showScreen(tableEntryScreen);
    }

    // ─── CART ────────────────────────────────────────────────
    private void addToCart(String name, double price) {
        for (OrderItem existing : order.getItems()) {
            if (existing.getName().equals(name)) {
                existing.incrementQuantity();
                refreshCart();
                return;
            }
        }
        order.addItem(new OrderItem(name, price, 1));
        refreshCart();
    }

    private void refreshCart() {
        cartItemsBox.getChildren().clear();

        if (order.getItems().isEmpty()) {
            Label empty = new Label("Your cart is empty");
            empty.setStyle("-fx-text-fill: #747474; -fx-font-size: 13px;");
            cartItemsBox.getChildren().add(empty);
            cartCountLabel.setText("0 items");
        } else {
            cartCountLabel.setText(order.getItems().size() + " item(s)");
            for (OrderItem item : order.getItems())
                cartItemsBox.getChildren().add(buildCartRow(item));
        }

        double subtotal = order.getTotal();
        double vat      = subtotal * 0.10;
        double total    = subtotal + vat;

        subtotalLabel.setText(String.format("GHS %.2f", subtotal));
        vatLabel.setText(String.format("GHS %.2f", vat));
        totalLabel.setText(String.format("GHS %.2f", total));
    }

    private HBox buildCartRow(OrderItem item) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 10; " +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-text-fill: #747474; -fx-font-size: 13px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Button minusBtn = new Button("−");
        minusBtn.setPrefSize(28, 28);
        minusBtn.setStyle(
                "-fx-background-color: #F1F5F9; -fx-text-fill: #c7052e; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; " +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 6; -fx-border-width: 1;");
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 1) item.decrementQuantity();
            else order.removeItem(item);
            refreshCart();
        });

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setPrefWidth(28);
        qtyLabel.setAlignment(Pos.CENTER);
        qtyLabel.setStyle(
                "-fx-text-fill: #c7052e; -fx-font-size: 13px; -fx-font-weight: bold;");

        Button plusBtn = new Button("+");
        plusBtn.setPrefSize(28, 28);
        plusBtn.setStyle(
                "-fx-background-color: #c7052e; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> { item.incrementQuantity(); refreshCart(); });

        Label priceLabel = new Label(String.format("GHS %.2f", item.getSubtotal()));
        priceLabel.setStyle(
                "-fx-text-fill: #c7052e; -fx-font-size: 13px; -fx-font-weight: bold;");
        priceLabel.setPrefWidth(80);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLabel, minusBtn, qtyLabel, plusBtn, priceLabel);
        return row;
    }

    // ─── SCREEN 3: CONFIRM ───────────────────────────────────
    @FXML
    public void handleConfirmOrder() {
        if (order.getItems().isEmpty()) {
            showAlert("Empty Cart",
                    "Please add items to your cart before placing an order.");
            return;
        }

        double subtotal = order.getTotal();
        double vat      = subtotal * 0.10;
        double total    = subtotal + vat;

        StringBuilder summary = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            summary.append(item.getQuantity())
                    .append("x ").append(item.getName())
                    .append(" — GHS ")
                    .append(String.format("%.2f", item.getSubtotal()))
                    .append("\n");
        }
        summary.append("\nVAT (10%): GHS ").append(String.format("%.2f", vat));
        summary.append("\nTotal: GHS ").append(String.format("%.2f", total));

        int newOrderId = saveOrderToDatabase(total);

        if (newOrderId == -1) {
            showAlert("Order Failed",
                    "Could not save your order. Please try again or notify staff.");
            return;
        }

        OrderController.lastOrderId = newOrderId;
        OrderController.lastTableNumber = tableNumber;
        OrderController.lastOrderTotal  = total;

        trackOrderBtn.setVisible(true);
        trackOrderBtn.setManaged(true);

        confirmMessageLabel.setText(
                "Order placed for Table " + tableNumber +
                        ". Your food is being prepared!");
        orderSummaryLabel.setText(summary.toString());

        showScreen(confirmScreen);
    }

    private int saveOrderToDatabase(double total) {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) {
            System.err.println("[OrderController] No DB connection.");
            return -1;
        }

        String insertOrderSql =
                "INSERT INTO orders (table_number, total_amount, status, order_timestamp) " +
                        "VALUES (?, ?, 'pending', datetime('now','localtime'))";

        int orderId = -1;

        try {
            conn.setAutoCommit(false);

            // 1. Insert order header
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    insertOrderSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, tableNumber);
                stmt.setDouble(2, total);
                stmt.executeUpdate();
                try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) orderId = rs.getInt(1);
                }
            }

            if (orderId == -1) { conn.rollback(); return -1; }

            // 2. Insert order items
            String insertItemSql =
                    "INSERT INTO order_items (order_id, item_id, quantity, subtotal) " +
                            "VALUES (?, ?, ?, ?)";

            try (java.sql.PreparedStatement stmt =
                         conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : order.getItems()) {
                    int foodItemId = lookupFoodItemId(conn, item.getName());
                    if (foodItemId == -1) {
                        System.err.println("[OrderController] Item not found: "
                                + item.getName());
                        continue;
                    }
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, foodItemId);
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getSubtotal());
                    stmt.executeUpdate();
                }
            }

            // 3. Log initial history
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO order_history (order_id, status, changed_at) " +
                            "VALUES (?, 'pending', datetime('now','localtime'))")) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }

            // 4. Mark table occupied
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE tables SET status = 'occupied' WHERE table_number = ?")) {
                stmt.setInt(1, tableNumber);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("[OrderController] Order #" + orderId
                    + " saved for Table " + tableNumber);
            return orderId;

        } catch (java.sql.SQLException e) {
            try { conn.rollback(); } catch (java.sql.SQLException ignored) {}
            System.err.println("[OrderController] DB error: " + e.getMessage());
            return -1;
        } finally {
            try { conn.setAutoCommit(true); } catch (java.sql.SQLException ignored) {}
        }
    }

    private int lookupFoodItemId(java.sql.Connection conn, String foodName)
            throws java.sql.SQLException {
        String sql = "SELECT item_id FROM food_items WHERE name = ?";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, foodName);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("item_id");
            }
        }
        return -1;
    }

    // ─── BUTTON HANDLERS ─────────────────────────────────────

    @FXML
    public void handleTrackOrder() {
        SceneManager.navigateTo(NavigationUtil.CUSTOMER_TRACKING);
    }

    @FXML
    public void handleBack() {
        showScreen(tableEntryScreen);
        order = new Order();
        refreshCart();
        SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY);
    }

    // ─── HELPERS ─────────────────────────────────────────────
    private void showScreen(javafx.scene.Node screen) {
        tableEntryScreen.setVisible(false); tableEntryScreen.setManaged(false);
        menuScreen.setVisible(false);       menuScreen.setManaged(false);
        confirmScreen.setVisible(false);    confirmScreen.setManaged(false);
        screen.setVisible(true);
        screen.setManaged(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}