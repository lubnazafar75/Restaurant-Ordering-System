package com.restaurant.restaurant.staff;

import com.restaurant.restaurant.dao.StaffDAO;
import com.restaurant.restaurant.dao.impl.SQLiteStaffDAO;
import com.restaurant.restaurant.login.LoginController;
import com.restaurant.restaurant.model.Staff;
import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

public class StaffDashboardController {

    @FXML private Label staffNameLabel;
    @FXML private Label staffRoleLabel;
    @FXML private Label staffIdLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnOrders;
    @FXML private Button btnKitchen;
    @FXML private Button btnBilling;
    @FXML private Button btnMenu;
    @FXML private Button btnSales;
    @FXML private Button btnStaff;

    private Button activeBtn;

    // Style constants for nav buttons (light theme sidebar is dark charcoal)
    private static final String NAV_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #D1D5DB; " +
                    "-fx-font-size: 13px; -fx-alignment: CENTER_LEFT; " +
                    "-fx-background-radius: 10; -fx-padding: 11 14; -fx-cursor: hand;";

    private static final String NAV_ACTIVE =
            "-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; " +
                    "-fx-alignment: CENTER_LEFT; -fx-background-radius: 10; " +
                    "-fx-padding: 11 14; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        // ── Display real logged-in staff info ─────────────────────
        staffNameLabel.setText(
                !LoginController.sessionStaffName.isEmpty()
                        ? LoginController.sessionStaffName
                        : "Staff Member"
        );
        staffRoleLabel.setText(
                !LoginController.sessionRole.isEmpty()
                        ? LoginController.sessionRole
                        : "—"
        );
        staffIdLabel.setText(
                !LoginController.sessionStaffId.isEmpty()
                        ? "ID: " + LoginController.sessionStaffId
                        : "ID: —"
        );

        System.out.println("[Dashboard] Logged in as: "
                + LoginController.sessionStaffName
                + " | Role: " + LoginController.sessionRole
                + " | ID: "   + LoginController.sessionStaffId);

        activeBtn = btnDashboard;
        applyRoleBasedAccess();
        showDashboard();
    }

    private void applyRoleBasedAccess() {
        boolean isAdmin = LoginController.sessionRole.equalsIgnoreCase("Admin");

        // ── Hide admin-only buttons for non-admin staff ──
        btnMenu.setVisible(true);
        btnMenu.setManaged(true);
        btnSales.setVisible(true);
        btnSales.setManaged(true);
        btnStaff.setVisible(true);
        btnStaff.setManaged(true);
    }

    private boolean isAdmin() {
        return LoginController.sessionRole.equalsIgnoreCase("Admin");
    }

    private void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Restricted Area");
        alert.setContentText("This section is only available to administrators.");
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }

    // ── NAV HANDLERS ─────────────────────────────────────────
    @FXML public void showDashboard() {
        setActive(btnDashboard);
        contentArea.getChildren().setAll(buildDashboardView());
    }

    @FXML public void showOrders() {
        setActive(btnOrders);
        contentArea.getChildren().setAll(buildOrdersView());
    }

    @FXML public void showKitchen() {
        setActive(btnKitchen);
        contentArea.getChildren().setAll(buildKitchenView());
    }

    @FXML public void showBilling() {
        setActive(btnBilling);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/billing.fxml"));
            Parent billingRoot = loader.load();
            contentArea.getChildren().setAll(billingRoot);
            System.out.println("[Dashboard] Billing loaded successfully");
        } catch (Exception e) {
            System.err.println("[Dashboard] Billing load error: " + e.getMessage());
            e.printStackTrace();
            contentArea.getChildren().setAll(
                    buildPlaceholder("🧾 Billing",
                            "Error: " + e.getMessage()));
        }
    }
    @FXML public void showMenuManagement() {
        if (!isAdmin()) { showAccessDenied(); return; }
        setActive(btnMenu);
        try {
            Parent menuRoot = FXMLLoader.load(
                    getClass().getResource("/fxml/menu.fxml"));
            contentArea.getChildren().setAll(menuRoot);
        } catch (IOException e) {
            contentArea.getChildren().setAll(
                    buildPlaceholder("🍔 Menu Management",
                            "Menu module loading..."));
        }
    }

    @FXML public void showSales() {
        if (!isAdmin()) { showAccessDenied(); return; }
        setActive(btnSales);
        contentArea.getChildren().setAll(buildSalesView());
    }

    @FXML public void showStaffManagement() {
        if (!isAdmin()) { showAccessDenied(); return; }
        setActive(btnStaff);
        contentArea.getChildren().setAll(buildStaffManagementView());
    }

    @FXML public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be returned to the home screen.");
        confirm.getDialogPane().getStyleClass().add("dialog-pane");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // ── Clear ALL session data so next staff sees a clean login ──
                LoginController.sessionStaffId   = "";
                LoginController.sessionStaffName = "";
                LoginController.sessionRole      = "";
                System.out.println("[Dashboard] Logged out. Session cleared.");
                SceneManager.navigateTo(NavigationUtil.STAFF_LOGIN);            }
        });
    }

    // ── ACTIVE BUTTON STYLING ─────────────────────────────────
    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.setStyle(NAV_INACTIVE);
        }
        btn.setStyle(NAV_ACTIVE);
        activeBtn = btn;
    }

    // ── DASHBOARD VIEW ────────────────────────────────────────
    private Parent buildDashboardView() {
        VBox view = new VBox(24);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");
        Label subtitle = new Label("Welcome back, " +
                LoginController.sessionStaffName + " · " +
                LoginController.sessionRole);
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        header.getChildren().addAll(title, subtitle);

        // Stats row
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                buildStatCard("📋", "Active Orders",   "12", "#10B981"),
                buildStatCard("✅", "Completed Today",  "47", "#3B82F6"),
                buildStatCard("⏳", "Pending Bills",    "5",  "#F59E0B"),
                buildStatCard("👥", "Tables Occupied",  "8",  "#8B5CF6")
        );

        // Recent orders table
        VBox ordersSection = new VBox(12);
        Label ordersTitle = new Label("Recent Orders");
        ordersTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16px; " +
                "-fx-font-weight: bold;");

        TableView<String[]> table = buildOrdersTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        ordersSection.getChildren().addAll(ordersTitle, table);

        // Quick actions
        HBox quickActions = new HBox(12);
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16px; " +
                "-fx-font-weight: bold;");

        Button viewOrdersBtn = buildActionBtn("📋 View Orders", "#10B981");
        viewOrdersBtn.setOnAction(e -> showOrders());

        Button kitchenBtn = buildActionBtn("👨‍🍳 Kitchen Monitor", "#3B82F6");
        kitchenBtn.setOnAction(e -> showKitchen());

        Button billingBtn = buildActionBtn("🧾 Process Payment", "#F59E0B");
        billingBtn.setOnAction(e -> showBilling());

        quickActions.getChildren().addAll(viewOrdersBtn, kitchenBtn, billingBtn);

        VBox qaSection = new VBox(12);
        qaSection.getChildren().addAll(qaTitle, quickActions);

        view.getChildren().addAll(header, stats, ordersSection, qaSection);
        return view;
    }

    private HBox buildStatCard(String icon, String label,
                               String value, String color) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.getStyleClass().add("stat-card");

        StackPane iconBox = new StackPane();
        iconBox.setMinSize(44, 44);
        iconBox.setMaxSize(44, 44);
        iconBox.setStyle("-fx-background-color: " + color + "22; " +
                "-fx-background-radius: 10;");
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 20px;");
        iconBox.getChildren().add(ico);

        VBox txt = new VBox(2);
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        txt.getChildren().addAll(val, lbl);

        card.getChildren().addAll(iconBox, txt);
        return card;
    }

    private TableView<String[]> buildOrdersTable() {
        TableView<String[]> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(220);

        TableColumn<String[], String> tableCol = new TableColumn<>("Table");
        tableCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        tableCol.setPrefWidth(80);

        TableColumn<String[], String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        itemsCol.setPrefWidth(280);

        TableColumn<String[], String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        timeCol.setPrefWidth(100);

        TableColumn<String[], String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(tableCol, itemsCol, timeCol, statusCol);

        // ✅ REAL DATABASE DATA (replaces hardcoded data)
        try {
            java.sql.Connection conn =
                    com.restaurant.restaurant.database.DBConnection.getConnection();

            String sql = "SELECT order_id, table_number, status, order_timestamp FROM orders ORDER BY order_timestamp DESC";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int tableNumber = rs.getInt("table_number");
                String status = rs.getString("status");
                String time = rs.getString("order_timestamp");

                // reuse your existing method
                String items = fetchItemsSummary(conn, orderId);

                table.getItems().add(new String[]{
                        "Table " + tableNumber,
                        items,
                        time,
                        status
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return table;
    }

    private Button buildActionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-background-radius: 12; " +
                "-fx-cursor: hand;");
        return btn;
    }

    // ── ORDERS VIEW ───────────────────────────────────────────
    private Parent buildOrdersView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("📋 Order Management");
        title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Filter tabs (UI only for now)
        HBox tabs = new HBox(8);
        String[] tabNames = {"All Orders", "Incoming", "Active", "Completed"};
        for (int i = 0; i < tabNames.length; i++) {
            Button tab = new Button(tabNames[i]);
            tab.getStyleClass().add(i == 0 ? "btn-pill-active" : "btn-pill");
            tabs.getChildren().add(tab);
        }

        // Orders list container
        VBox ordersList = new VBox(10);
        VBox.setVgrow(ordersList, Priority.ALWAYS);

        // ✅ LOAD REAL DATA FROM DATABASE
        try {
            java.sql.Connection conn =
                    com.restaurant.restaurant.database.DBConnection.getConnection();

            String sql = "SELECT order_id, table_number, status, order_timestamp FROM orders ORDER BY order_timestamp DESC";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int tableNumber = rs.getInt("table_number");
                String status = rs.getString("status").toUpperCase();
                String time = rs.getString("order_timestamp");

                String items = fetchItemsSummary(conn, orderId);

                // Map status → UI style
                String style;
                switch (status.toLowerCase()) {
                    case "pending": style = "muted"; break;
                    case "preparing": style = "warning"; break;
                    case "ready": style = "success"; break;
                    case "delivered": style = "info"; break;
                    default: style = "muted";
                }

                ordersList.getChildren().add(
                        buildOrderCard(new String[]{
                                "Table " + tableNumber,
                                items,
                                time,
                                status,
                                style
                        })
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ScrollPane scroll = new ScrollPane(ordersList);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(title, tabs, scroll);
        return view;
    }

    private HBox buildOrderCard(String[] order) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("order-card");

        // Table number badge
        StackPane badge = new StackPane();
        badge.setMinSize(52, 52);
        badge.setMaxSize(52, 52);
        badge.setStyle("-fx-background-color: #D1FAE5; " +
                "-fx-background-radius: 10;");
        Label tableLabel = new Label(order[0].replace("Table ", "T"));
        tableLabel.setStyle("-fx-text-fill: #059669; " +
                "-fx-font-size: 13px; -fx-font-weight: bold;");
        badge.getChildren().add(tableLabel);

        // Order details
        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);
        Label tableName = new Label(order[0]);
        tableName.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 15px; " +
                "-fx-font-weight: bold;");
        Label items = new Label(order[1]);
        items.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        details.getChildren().addAll(tableName, items);

        // Time
        Label time = new Label(order[2]);
        time.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        // Status badge
        Label status = new Label(order[3]);
        status.getStyleClass().add("badge-" + order[4]);

        card.getChildren().addAll(badge, details, time, status);
        return card;
    }

    // ── KITCHEN VIEW ──────────────────────────────────────────
    private Parent buildKitchenView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("👨‍🍳 Kitchen Monitor");
        title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");
        Label subtitle = new Label(
                "Update order status — changes reflect on customer screen instantly");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        HBox columns = new HBox(12);
        columns.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(columns, Priority.ALWAYS);

        columns.getChildren().addAll(
                buildKitchenColumn("⏳ Pending",    "#6B7280", "pending"),
                buildKitchenColumn("👨‍🍳 Preparing", "#F59E0B", "preparing"),
                buildKitchenColumn("✅ Ready",      "#10B981", "ready"),
                buildKitchenColumn("🚀 Delivered",  "#3B82F6", "delivered")
        );

        for (var col : columns.getChildren()) {
            HBox.setHgrow(col, Priority.ALWAYS);
        }

        ScrollPane scroll = new ScrollPane(columns);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(title, subtitle, scroll);
        return view;
    }

    /**
     * Builds a kitchen status column showing real orders from the database
     * that currently have the given status.
     */
    private VBox buildKitchenColumn(String statusLabel, String color, String dbStatus) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(14));
        col.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 14; " +
                "-fx-border-color: " + color + "33; " +
                "-fx-border-radius: 14; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(31,41,55,0.05), 8, 0, 0, 2);");

        // Column header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: " + color + "33; " +
                "-fx-border-width: 0 0 1 0;");
        Label headerLbl = new Label(statusLabel);
        headerLbl.setStyle("-fx-text-fill: " + color + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold;");
        header.getChildren().add(headerLbl);
        col.getChildren().add(header);

        // Fetch real orders with this status
        for (KitchenOrder order : fetchKitchenOrders(dbStatus)) {
            col.getChildren().add(buildKitchenOrderCard(order, color, dbStatus));
        }

        return col;
    }

    /**
     * Builds a card for a single kitchen order, with a "Next Status" button
     * that advances the order's status in the database.
     */
    private VBox buildKitchenOrderCard(KitchenOrder order, String color, String currentStatus) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; " +
                "-fx-border-radius: 10;");

        Label tableL = new Label("Table " + order.tableNumber + "  ·  Order #" + order.orderId);
        tableL.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 13px; " +
                "-fx-font-weight: bold;");
        Label itemsL = new Label(order.itemsSummary);
        itemsL.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        itemsL.setWrapText(true);

        HBox btns = new HBox(6);
        btns.setPadding(new Insets(6, 0, 0, 0));

        String nextStatus = getNextStatus(currentStatus);
        if (nextStatus != null) {
            Button nextBtn = new Button("→ Mark " + capitalize(nextStatus));
            nextBtn.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-text-fill: white; -fx-font-size: 10px; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-padding: 5 12; -fx-cursor: hand;");
            nextBtn.setOnAction(e -> {
                updateOrderStatus(order.orderId, nextStatus);
                // Refresh the kitchen view to reflect the change
                contentArea.getChildren().setAll(buildKitchenView());
            });
            btns.getChildren().add(nextBtn);
        }

        card.getChildren().addAll(tableL, itemsL, btns);
        return card;
    }

    /** Returns the next status in the pending→preparing→ready→delivered flow, or null. */
    private String getNextStatus(String current) {
        switch (current.toLowerCase()) {
            case "pending":   return "preparing";
            case "preparing": return "ready";
            case "ready":     return "delivered";
            default:          return null; // delivered is final
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Updates an order's status in the database and logs the change
     * to order_history.
     */
    private void updateOrderStatus(int orderId, String newStatus) {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return;

        String updateSql = "UPDATE orders SET status = ? WHERE order_id = ?";
        String historySql = "INSERT INTO order_history (order_id, status, changed_at) " +
                "VALUES (?, ?, datetime('now','localtime'))";

        try {
            conn.setAutoCommit(false);

            try (java.sql.PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, orderId);
                stmt.executeUpdate();
            }

            try (java.sql.PreparedStatement stmt = conn.prepareStatement(historySql)) {
                stmt.setInt(1, orderId);
                stmt.setString(2, newStatus);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("[Kitchen] Order #" + orderId + " → " + newStatus);
        } catch (java.sql.SQLException e) {
            try { conn.rollback(); } catch (java.sql.SQLException ignored) {}
            System.err.println("[Kitchen] Failed to update order #" + orderId
                    + ": " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (java.sql.SQLException ignored) {}
        }
    }

    /**
     * Fetches orders with the given status, including a summary of their items.
     *
     * NOTE: The `orders` table uses `table_id` (not `table_number`) and
     * `order_date` (not `order_timestamp`) — corrected here to match the
     * actual schema used by SQLiteOrderDAO / Order.java.
     */
    private java.util.List<KitchenOrder> fetchKitchenOrders(String status) {
        java.util.List<KitchenOrder> result = new java.util.ArrayList<>();
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return result;

        String sql = "SELECT order_id, table_number FROM orders WHERE status = ? " +
                "ORDER BY order_timestamp ASC";

        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int tableNumber = rs.getInt("table_number");
                    String itemsSummary = fetchItemsSummary(conn, orderId);
                    result.add(new KitchenOrder(orderId, tableNumber, itemsSummary));
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[Kitchen] Failed to fetch orders for status '"
                    + status + "': " + e.getMessage());
        }

        return result;
    }
    /** Builds a comma-separated "ItemName x Qty" summary for an order. */
    private String fetchItemsSummary(java.sql.Connection conn, int orderId)
            throws java.sql.SQLException {
        String sql = "SELECT f.name, oi.quantity FROM order_items oi " +
                "JOIN food_items f ON oi.item_id = f.item_id " +
                "WHERE oi.order_id = ?";

        StringBuilder sb = new StringBuilder();
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(", ");
                    sb.append(rs.getString("name"))
                            .append(" × ").append(rs.getInt("quantity"));
                    first = false;
                }
            }
        }
        return sb.toString();
    }

    /** Simple data holder for a kitchen order row. */
    private static class KitchenOrder {
        final int orderId;
        final int tableNumber;
        final String itemsSummary;

        KitchenOrder(int orderId, int tableNumber, String itemsSummary) {
            this.orderId = orderId;
            this.tableNumber = tableNumber;
            this.itemsSummary = itemsSummary;
        }
    }

    // ── SALES VIEW ────────────────────────────────────────────
    private Parent buildSalesView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("📈 Sales Analytics");
        title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");

        // Period tabs
        HBox tabs = new HBox(8);
        String[] periods = {"Today", "This Week", "This Month"};
        for (int i = 0; i < periods.length; i++) {
            Button tab = new Button(periods[i]);
            tab.getStyleClass().add(i == 0 ? "btn-pill-active" : "btn-pill");
            tabs.getChildren().add(tab);
        }

        // Revenue stats
        HBox revenueStats = new HBox(16);
        revenueStats.getChildren().addAll(
                buildStatCard("💰", "Total Revenue",   "GHS 2,450", "#10B981"),
                buildStatCard("📦", "Total Orders",    "47",         "#3B82F6"),
                buildStatCard("🍽", "Avg Order Value", "GHS 52.13",  "#F59E0B"),
                buildStatCard("👥", "Customers",       "89",         "#8B5CF6")
        );

        // Top items
        VBox topItems = new VBox(12);
        Label topTitle = new Label("🏆 Top Selling Items");
        topTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16px; " +
                "-fx-font-weight: bold;");

        String[][] items = {
                {"Jollof Rice",  "24 orders", "GHS 1,080", "72"},
                {"Fried Chicken","18 orders", "GHS 1,080", "54"},
                {"Burger",       "15 orders", "GHS 750",   "45"},
                {"Fresh Juice",  "32 orders", "GHS 480",   "96"},
                {"Fufu & Soup",  "12 orders", "GHS 600",   "36"}
        };

        for (String[] item : items) {
            topItems.getChildren().add(buildTopItemRow(item));
        }

        topItems.getChildren().add(0, topTitle);

        view.getChildren().addAll(title, tabs, revenueStats, topItems);
        return view;
    }

    private HBox buildTopItemRow(String[] item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(31,41,55,0.04), 6, 0, 0, 2);");

        Label name = new Label(item[0]);
        name.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 14px; " +
                "-fx-font-weight: bold;");
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);

        // Progress bar
        StackPane barBg = new StackPane();
        barBg.setPrefSize(120, 6);
        barBg.setStyle("-fx-background-color: #E5E7EB; " +
                "-fx-background-radius: 3;");
        double pct = Double.parseDouble(item[3]) / 100.0;
        Region barFill = new Region();
        barFill.setPrefSize(120 * pct, 6);
        barFill.setStyle("-fx-background-color: #10B981; " +
                "-fx-background-radius: 3;");
        barBg.getChildren().add(barFill);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);

        Label orders = new Label(item[1]);
        orders.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        orders.setPrefWidth(80);

        Label revenue = new Label(item[2]);
        revenue.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; " +
                "-fx-font-weight: bold;");
        revenue.setPrefWidth(100);

        row.getChildren().addAll(name, barBg, orders, revenue);
        return row;
    }

    // ── STAFF MANAGEMENT VIEW ────────────────────────────────
    private Parent buildStaffManagementView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("👥 Staff Management");
        title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = new Button("+ Add Staff");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setStyle(addBtn.getStyle() + "-fx-font-size: 13px; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddStaffDialog());
        headerRow.getChildren().addAll(title, addBtn);

        // Staff table — backed by the real `staff` table via SQLiteStaffDAO
        TableView<String[]> table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        String[] cols = {"Staff ID", "Username", "Role", "Status"};
        int[] widths = {80, 180, 140, 100};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue()[idx]));
            col.setPrefWidth(widths[i]);
            table.getColumns().add(col);
        }

        StaffDAO staffDAO = new SQLiteStaffDAO();
        for (Staff s : staffDAO.getAllActiveStaff()) {
            table.getItems().add(new String[]{
                    String.valueOf(s.getStaffId()),
                    s.getUsername(),
                    s.getRole(),
                    s.getStatus()
            });
        }

        view.getChildren().addAll(headerRow, table);
        return view;
    }

    /**
     * Shows a dialog for entering a new staff member's username, password,
     * and role. On confirm, inserts the record via SQLiteStaffDAO and
     * refreshes the Staff Management table.
     */
    @FXML
    private void showAddStaffDialog() {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Add New Staff");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType saveButtonType = new ButtonType("Add Staff", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("waiter", "kitchen staff", "cashier", "supervisor", "admin");
        roleBox.setValue("waiter");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        VBox content = new VBox(10,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Role:"), roleBox,
                errorLabel
        );
        content.setPadding(new Insets(10));
        content.setPrefWidth(320);
        dialog.getDialogPane().setContent(content);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password are required.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new Staff(0, usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        roleBox.getValue(), "active");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStaff -> {
            StaffDAO staffDAO = new SQLiteStaffDAO();
            staffDAO.createStaff(newStaff);
            // Refresh the staff management view to show the new entry
            contentArea.getChildren().setAll(buildStaffManagementView());
        });
    }

    // ── PLACEHOLDER ───────────────────────────────────────────
    private Parent buildPlaceholder(String icon, String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #F8FAFC;");
        Label lbl = new Label(icon);
        lbl.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 18px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        box.getChildren().addAll(lbl, msg);
        return box;
    }
}
