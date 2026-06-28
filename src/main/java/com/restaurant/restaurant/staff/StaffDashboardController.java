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
//
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

    private static final String NAV_INACTIVE =
            "-fx-background-color: #163b2c; -fx-text-fill: #D1D5DB; " +
                    "-fx-font-size: 13px; -fx-alignment: CENTER_LEFT; " +
                    "-fx-background-radius: 10; -fx-padding: 11 14; -fx-cursor: hand;";

    private static final String NAV_ACTIVE =
            "-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; " +
                    "-fx-alignment: CENTER_LEFT; -fx-background-radius: 10; " +
                    "-fx-padding: 11 14; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        staffNameLabel.setText(
                !LoginController.sessionStaffName.isEmpty()
                        ? LoginController.sessionStaffName : "Staff Member");
        staffRoleLabel.setText(
                !LoginController.sessionRole.isEmpty()
                        ? LoginController.sessionRole : "—");
        staffIdLabel.setText(
                !LoginController.sessionStaffId.isEmpty()
                        ? "ID: " + LoginController.sessionStaffId : "ID: —");

        System.out.println("[Dashboard] Logged in as: "
                + LoginController.sessionStaffName
                + " | Role: " + LoginController.sessionRole
                + " | ID: "   + LoginController.sessionStaffId);

        activeBtn = btnDashboard;
        applyRoleBasedAccess();
        showDashboard();
    }

    // ── ROLE-BASED ACCESS ─────────────────────────────────────
    private void applyRoleBasedAccess() {
        boolean isAdmin = LoginController.sessionRole.equalsIgnoreCase("Admin");

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
            contentArea.getChildren().setAll(
                    buildPlaceholder("🧾 Billing", "Error: " + e.getMessage()));
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
                    buildPlaceholder("🍔 Menu Management", "Menu module loading..."));
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
                LoginController.sessionStaffId   = "";
                LoginController.sessionStaffName = "";
                LoginController.sessionRole      = "";
                System.out.println("[Dashboard] Logged out. Session cleared.");
                SceneManager.navigateTo(NavigationUtil.STAFF_LOGIN);
            }
        });
    }

    // ── ACTIVE BUTTON STYLING ─────────────────────────────────
    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.setStyle(NAV_INACTIVE);
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
        title.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitle = new Label("Welcome back, "
                + LoginController.sessionStaffName + " · " + LoginController.sessionRole);
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        header.getChildren().addAll(title, subtitle);

        // Stats row — pulled from DB
        HBox stats = new HBox(16);
        long[] dashStats = fetchDashboardStats();
        stats.getChildren().addAll(
                buildStatCard("📋", "Active Orders",  String.valueOf(dashStats[0]), "#10B981"),
                buildStatCard("✅", "Completed Today", String.valueOf(dashStats[1]), "#3B82F6"),
                buildStatCard("⏳", "Pending Bills",   String.valueOf(dashStats[2]), "#F59E0B"),
                buildStatCard("🪑", "Tables Occupied", String.valueOf(dashStats[3]), "#8B5CF6")
        );

        // Recent orders table
        VBox ordersSection = new VBox(12);
        Label ordersTitle = new Label("Recent Orders");
        ordersTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16px; -fx-font-weight: bold;");
        TableView<String[]> table = buildOrdersTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        ordersSection.getChildren().addAll(ordersTitle, table);

        // Quick actions
        HBox quickActions = new HBox(12);
        Button viewOrdersBtn = buildActionBtn("📋 View Orders", "#10B981");
        viewOrdersBtn.setOnAction(e -> showOrders());
        Button kitchenBtn = buildActionBtn("👨\u200d🍳 Kitchen Monitor", "#3B82F6");
        kitchenBtn.setOnAction(e -> showKitchen());
        Button billingBtn = buildActionBtn("🧾 Process Payment", "#F59E0B");
        billingBtn.setOnAction(e -> showBilling());
        quickActions.getChildren().addAll(viewOrdersBtn, kitchenBtn, billingBtn);

        VBox qaSection = new VBox(12);
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 16px; -fx-font-weight: bold;");
        qaSection.getChildren().addAll(qaTitle, quickActions);

        view.getChildren().addAll(header, stats, ordersSection, qaSection);
        return view;
    }

    /** Returns [activeOrders, completedToday, pendingBills, tablesOccupied] */
    private long[] fetchDashboardStats() {
        long[] s = {0, 0, 0, 0};
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return s;
        try {
            String today = java.time.LocalDate.now().toString();

            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM orders WHERE status IN ('pending','preparing','ready')")) {
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) s[0] = rs.getLong(1);
            }
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM orders WHERE status = 'delivered' AND order_timestamp LIKE ?")) {
                stmt.setString(1, today + "%");
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) s[1] = rs.getLong(1);
            }
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM billing WHERE payment_status = 'pending'")) {
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) s[2] = rs.getLong(1);
            }
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM tables WHERE status = 'occupied'")) {
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) s[3] = rs.getLong(1);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[Dashboard] Stats error: " + e.getMessage());
        }
        return s;
    }

    private HBox buildStatCard(String icon, String label, String value, String color) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.getStyleClass().add("stat-card");

        StackPane iconBox = new StackPane();
        iconBox.setMinSize(44, 44);
        iconBox.setMaxSize(44, 44);
        iconBox.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 10;");
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

        String[] colNames = {"Table", "Items", "Time", "Status"};
        int[]    colWidths = {80, 280, 160, 120};
        for (int i = 0; i < colNames.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(colNames[i]);
            col.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue()[idx]));
            col.setPrefWidth(colWidths[i]);
            table.getColumns().add(col);
        }

        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT order_id, table_number, status, order_timestamp " +
                    "FROM orders ORDER BY order_timestamp DESC LIMIT 20";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int orderId      = rs.getInt("order_id");
                    int tableNumber  = rs.getInt("table_number");
                    String status    = rs.getString("status");
                    String time      = rs.getString("order_timestamp");
                    String items     = fetchItemsSummary(conn, orderId);
                    table.getItems().add(new String[]{
                            "Table " + tableNumber, items, time, status});
                }
            } catch (java.sql.SQLException e) {
                System.err.println("[Dashboard] Orders table error: " + e.getMessage());
            }
        }
        return table;
    }

    private Button buildActionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-cursor: hand;");
        return btn;
    }

    // ── ORDERS VIEW ───────────────────────────────────────────
    private Parent buildOrdersView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        Label title = new Label("📋 Order Management");
        title.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 24px; -fx-font-weight: bold;");

        HBox tabs = new HBox(8);
        String[] tabNames = {"All Orders", "Incoming", "Active", "Completed"};
        for (int i = 0; i < tabNames.length; i++) {
            Button tab = new Button(tabNames[i]);
            tab.getStyleClass().add(i == 0 ? "btn-pill-active1 " : "btn-pill1");
            tabs.getChildren().add(tab);
        }

        VBox ordersList = new VBox(10);
        VBox.setVgrow(ordersList, Priority.ALWAYS);

        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT order_id, table_number, status, order_timestamp " +
                    "FROM orders ORDER BY order_timestamp DESC";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int orderId     = rs.getInt("order_id");
                    int tableNumber = rs.getInt("table_number");
                    String status   = rs.getString("status");
                    String time     = rs.getString("order_timestamp");
                    String items    = fetchItemsSummary(conn, orderId);

                    String style;
                    switch (status.toLowerCase()) {
                        case "pending":   style = "muted";   break;
                        case "preparing": style = "warning"; break;
                        case "ready":     style = "success"; break;
                        case "delivered": style = "info";    break;
                        default:          style = "muted";
                    }
                    ordersList.getChildren().add(buildOrderCard(new String[]{
                            "Table " + tableNumber, items, time,
                            status.toUpperCase(), style}));
                }
            } catch (java.sql.SQLException e) {
                System.err.println("[Orders] Load error: " + e.getMessage());
            }
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

        StackPane badge = new StackPane();
        badge.setMinSize(52, 52);
        badge.setMaxSize(52, 52);
        badge.setStyle("-fx-background-color: #D1FAE5; -fx-background-radius: 10;");
        Label tableLabel = new Label(order[0].replace("Table ", "T"));
        tableLabel.setStyle("-fx-text-fill: #059669; -fx-font-size: 13px; -fx-font-weight: bold;");
        badge.getChildren().add(tableLabel);

        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);
        Label tableName = new Label(order[0]);
        tableName.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label items = new Label(order[1]);
        items.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        details.getChildren().addAll(tableName, items);

        Label time = new Label(order[2]);
        time.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

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

        Label title = new Label("👨\u200d🍳 Kitchen Monitor");
        title.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitle = new Label("Update order status — changes reflect on customer screen instantly");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        HBox columns = new HBox(12);
        columns.setMaxWidth(Double.MAX_VALUE);
        columns.getChildren().addAll(
                buildKitchenColumn("⏳ Pending",     "#6B7280", "pending"),
                buildKitchenColumn("👨\u200d🍳 Preparing", "#F59E0B", "preparing"),
                buildKitchenColumn("✅ Ready",       "#10B981", "ready"),
                buildKitchenColumn("🚀 Delivered",   "#3B82F6", "delivered")
        );
        for (var col : columns.getChildren()) HBox.setHgrow(col, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(columns);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(title, subtitle, scroll);
        return view;
    }

    private VBox buildKitchenColumn(String statusLabel, String color, String dbStatus) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(14));
        col.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: " + color + "33; -fx-border-radius: 14; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(31,41,55,0.05), 8, 0, 0, 2);");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: " + color + "33; -fx-border-width: 0 0 1 0;");
        Label headerLbl = new Label(statusLabel);
        headerLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        header.getChildren().add(headerLbl);
        col.getChildren().add(header);

        for (KitchenOrder order : fetchKitchenOrders(dbStatus)) {
            col.getChildren().add(buildKitchenOrderCard(order, color, dbStatus));
        }
        return col;
    }

    private VBox buildKitchenOrderCard(KitchenOrder order, String color, String currentStatus) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 10;");

        Label tableL = new Label("Table " + order.tableNumber + "  ·  Order #" + order.orderId);
        tableL.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label itemsL = new Label(order.itemsSummary);
        itemsL.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        itemsL.setWrapText(true);

        HBox btns = new HBox(6);
        btns.setPadding(new Insets(6, 0, 0, 0));

        String nextStatus = getNextStatus(currentStatus);
        if (nextStatus != null) {
            Button nextBtn = new Button("→ Mark " + capitalize(nextStatus));
            nextBtn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;");
            nextBtn.setOnAction(e -> {
                updateOrderStatus(order.orderId, nextStatus);
                contentArea.getChildren().setAll(buildKitchenView());
            });
            btns.getChildren().add(nextBtn);
        }

        card.getChildren().addAll(tableL, itemsL, btns);
        return card;
    }

    private String getNextStatus(String current) {
        switch (current.toLowerCase()) {
            case "pending":   return "preparing";
            case "preparing": return "ready";
            case "ready":     return "delivered";
            default:          return null;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return;

        try {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE orders SET status = ? WHERE order_id = ?")) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, orderId);
                stmt.executeUpdate();
            }
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO order_history (order_id, status, changed_at) " +
                            "VALUES (?, ?, datetime('now','localtime'))")) {
                stmt.setInt(1, orderId);
                stmt.setString(2, newStatus);
                stmt.executeUpdate();
            }
            // Free table when order is delivered
            if ("delivered".equals(newStatus)) {
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE tables SET status = 'available' " +
                                "WHERE table_number = (SELECT table_number FROM orders WHERE order_id = ?)")) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }
            }
            conn.commit();
            System.out.println("[Kitchen] Order #" + orderId + " → " + newStatus);
        } catch (java.sql.SQLException e) {
            try { conn.rollback(); } catch (java.sql.SQLException ignored) {}
            System.err.println("[Kitchen] Failed to update order #" + orderId + ": " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (java.sql.SQLException ignored) {}
        }
    }

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
                    int orderId     = rs.getInt("order_id");
                    int tableNumber = rs.getInt("table_number");
                    String summary  = fetchItemsSummary(conn, orderId);
                    result.add(new KitchenOrder(orderId, tableNumber, summary));
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[Kitchen] Fetch error for '" + status + "': " + e.getMessage());
        }
        return result;
    }

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
        title.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 24px; -fx-font-weight: bold;");

        HBox tabs = new HBox(8);
        String[] periods = {"Today", "This Week", "This Month"};
        for (int i = 0; i < periods.length; i++) {
            Button tab = new Button(periods[i]);
            tab.getStyleClass().add(i == 0 ? "btn-pill-active" : "btn-pill");
            tabs.getChildren().add(tab);
        }

        // Revenue stats from DB
        double[] salesStats = fetchSalesStats("today");
        HBox revenueStats = new HBox(16);
        revenueStats.getChildren().addAll(
                buildStatCard("💰", "Total Revenue",
                        String.format("GHS %.2f", salesStats[0]), "#10B981"),
                buildStatCard("📦", "Total Orders",
                        String.valueOf((int) salesStats[1]), "#3B82F6"),
                buildStatCard("🍽", "Avg Order Value",
                        String.format("GHS %.2f", salesStats[2]), "#F59E0B"),
                buildStatCard("🪑", "Tables Used",
                        String.valueOf((int) salesStats[3]), "#8B5CF6")
        );

        // Top selling items from DB
        VBox topItems = new VBox(12);
        Label topTitle = new Label("🏆 Top Selling Items");
        topTitle.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 16px; -fx-font-weight: bold;");
        topItems.getChildren().add(topTitle);

        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT f.name, SUM(oi.quantity) as total_qty, " +
                    "SUM(oi.subtotal) as total_revenue " +
                    "FROM order_items oi " +
                    "JOIN food_items f ON oi.item_id = f.item_id " +
                    "GROUP BY f.name ORDER BY total_qty DESC LIMIT 5";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                long maxQty = 1;
                java.util.List<String[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    long qty = rs.getLong("total_qty");
                    if (qty > maxQty) maxQty = qty;
                    rows.add(new String[]{
                            rs.getString("name"),
                            qty + " orders",
                            String.format("GHS %.2f", rs.getDouble("total_revenue")),
                            String.valueOf(Math.min(100, (qty * 100) / maxQty))
                    });
                }
                for (String[] row : rows)
                    topItems.getChildren().add(buildTopItemRow(row));
            } catch (java.sql.SQLException e) {
                System.err.println("[Sales] Top items error: " + e.getMessage());
            }
        }

        view.getChildren().addAll(title, tabs, revenueStats, topItems);
        return view;
    }

    /** Returns [totalRevenue, totalOrders, avgOrderValue, distinctTables] */
    private double[] fetchSalesStats(String period) {
        double[] s = {0, 0, 0, 0};
        java.sql.Connection conn =
                com.restaurant.restaurant.database.DBConnection.getConnection();
        if (conn == null) return s;

        String dateFilter;
        switch (period) {
            case "week":  dateFilter = "AND order_timestamp >= date('now','-7 days')"; break;
            case "month": dateFilter = "AND order_timestamp >= date('now','-30 days')"; break;
            default:      dateFilter = "AND order_timestamp LIKE '" +
                    java.time.LocalDate.now() + "%'";
        }

        String sql = "SELECT COALESCE(SUM(total_amount),0) as revenue, " +
                "COUNT(*) as orders, " +
                "COALESCE(AVG(total_amount),0) as avg_val, " +
                "COUNT(DISTINCT table_number) as tables " +
                "FROM orders WHERE status = 'delivered' " + dateFilter;
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                s[0] = rs.getDouble("revenue");
                s[1] = rs.getDouble("orders");
                s[2] = rs.getDouble("avg_val");
                s[3] = rs.getDouble("tables");
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[Sales] Stats error: " + e.getMessage());
        }
        return s;
    }

    private HBox buildTopItemRow(String[] item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(31,41,55,0.04), 6, 0, 0, 2);");

        Label name = new Label(item[0]);
        name.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);

        StackPane barBg = new StackPane();
        barBg.setPrefSize(120, 6);
        barBg.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 3;");
        double pct = Double.parseDouble(item[3]) / 100.0;
        Region barFill = new Region();
        barFill.setPrefSize(120 * pct, 6);
        barFill.setStyle("-fx-background-color: #10B981; -fx-background-radius: 3;");
        barBg.getChildren().add(barFill);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);

        Label orders = new Label(item[1]);
        orders.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        orders.setPrefWidth(80);

        Label revenue = new Label(item[2]);
        revenue.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: bold;");
        revenue.setPrefWidth(100);

        row.getChildren().addAll(name, barBg, orders, revenue);
        return row;
    }

    // ── STAFF MANAGEMENT VIEW ─────────────────────────────────
    private Parent buildStaffManagementView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #F8FAFC;");

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("👥 Staff Management");
        title.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 24px; -fx-font-weight: bold;");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = new Button("+ Add Staff");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setStyle(addBtn.getStyle() + "-fx-font-size: 13px; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddStaffDialog());
        headerRow.getChildren().addAll(title, addBtn);

        TableView<String[]> table = new TableView<>();
        table.getStyleClass().add("table-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        String[] cols   = {"Staff ID", "Username", "Role", "Status"};
        int[]    widths = {80, 200, 160, 100};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue()[idx]));
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
        roleBox.getItems().addAll("Waiter", "Kitchen Staff", "Cashier", "Supervisor", "Admin");
        roleBox.setValue("Waiter");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        VBox content = new VBox(10,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Role:"),     roleBox,
                errorLabel
        );
        content.setPadding(new Insets(10));
        content.setPrefWidth(320);
        dialog.getDialogPane().setContent(content);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty()) {
                errorLabel.setText("Username and password are required.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt == saveButtonType) {
                return new Staff(0,
                        usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        roleBox.getValue(),
                        "active");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStaff -> {
            new SQLiteStaffDAO().createStaff(newStaff);
            contentArea.getChildren().setAll(buildStaffManagementView());
        });
    }

    // ── PLACEHOLDER ───────────────────────────────────────────
    private Parent buildPlaceholder(String icon, String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #F8FAFC;");
        Label lbl = new Label(icon);
        lbl.setStyle("-fx-text-fill: #163b2c; -fx-font-size: 18px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        box.getChildren().addAll(lbl, msg);
        return box;
    }
}