package com.restaurant.restaurant.staff;

import com.restaurant.restaurant.login.LoginController;
import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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

    @FXML
    public void initialize() {
        staffNameLabel.setText(LoginController.sessionStaffName);
        staffRoleLabel.setText(LoginController.sessionRole);
        staffIdLabel.setText("ID: " + LoginController.sessionStaffId);
        activeBtn = btnDashboard;
        showDashboard();
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
            Parent billingRoot = FXMLLoader.load(
                    getClass().getResource(
                            "/com/restaurant/restaurant/billing/billing.fxml"));
            contentArea.getChildren().setAll(billingRoot);
        } catch (IOException e) {
            contentArea.getChildren().setAll(
                    buildPlaceholder("🧾 Billing", "Billing module loading..."));
        }
    }

    @FXML public void showMenuManagement() {
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
        setActive(btnSales);
        contentArea.getChildren().setAll(buildSalesView());
    }

    @FXML public void showStaffManagement() {
        setActive(btnStaff);
        contentArea.getChildren().setAll(buildStaffManagementView());
    }

    @FXML public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be returned to the home screen.");
        confirm.getDialogPane().setStyle(
                "-fx-background-color: #161D30;");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                LoginController.sessionStaffId = "";
                LoginController.sessionStaffName = "Staff Member";
                LoginController.sessionRole = "Waiter";
                SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY);
            }
        });
    }

    // ── ACTIVE BUTTON STYLING ─────────────────────────────────
    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #a0aec0; -fx-font-size: 13px; " +
                            "-fx-alignment: CENTER_LEFT; -fx-background-radius: 8; " +
                            "-fx-padding: 10 14; -fx-cursor: hand;");
        }
        btn.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-alignment: CENTER_LEFT; " +
                        "-fx-background-radius: 8; -fx-padding: 10 14; " +
                        "-fx-cursor: hand;");
        activeBtn = btn;
    }

    // ── DASHBOARD VIEW ────────────────────────────────────────
    private Parent buildDashboardView() {
        VBox view = new VBox(24);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #0B0F19;");

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Dashboard Overview");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; " +
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
        ordersTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-font-weight: bold;");

        TableView<String[]> table = buildOrdersTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        ordersSection.getChildren().addAll(ordersTitle, table);

        // Quick actions
        HBox quickActions = new HBox(12);
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-font-weight: bold;");

        Button viewOrdersBtn = buildActionBtn(
                "📋 View Orders", "#10B981");
        viewOrdersBtn.setOnAction(e -> showOrders());

        Button kitchenBtn = buildActionBtn(
                "👨‍🍳 Kitchen Monitor", "#3B82F6");
        kitchenBtn.setOnAction(e -> showKitchen());

        Button billingBtn = buildActionBtn(
                "🧾 Process Payment", "#F59E0B");
        billingBtn.setOnAction(e -> showBilling());

        quickActions.getChildren().addAll(
                viewOrdersBtn, kitchenBtn, billingBtn);

        VBox qaSection = new VBox(12);
        qaSection.getChildren().addAll(qaTitle, quickActions);

        view.getChildren().addAll(
                header, stats, ordersSection, qaSection);
        return view;
    }

    private HBox buildStatCard(String icon, String label,
                               String value, String color) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle("-fx-background-color: #161D30; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #1E2740; " +
                "-fx-border-radius: 12; -fx-border-width: 1;");

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
        val.setStyle("-fx-text-fill: white; -fx-font-size: 26px; " +
                "-fx-font-weight: bold;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        txt.getChildren().addAll(val, lbl);

        card.getChildren().addAll(iconBox, txt);
        return card;
    }

    private TableView<String[]> buildOrdersTable() {
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #161D30; " +
                "-fx-control-inner-background: #161D30; " +
                "-fx-table-cell-border-color: #1E2740;");
        table.setPrefHeight(200);

        TableColumn<String[], String> tableCol = new TableColumn<>("Table");
        tableCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));
        tableCol.setPrefWidth(80);

        TableColumn<String[], String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        itemsCol.setPrefWidth(250);

        TableColumn<String[], String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        timeCol.setPrefWidth(100);

        TableColumn<String[], String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(tableCol, itemsCol, timeCol, statusCol);

        // Sample data
        table.getItems().addAll(
                new String[]{"Table 3", "Jollof Rice, Chicken", "10:32", "Preparing"},
                new String[]{"Table 7", "Fried Rice, Juice", "10:45", "Ready"},
                new String[]{"Table 1", "Fufu & Soup", "11:02", "Pending"},
                new String[]{"Table 5", "Burger, Chips, Malt", "11:15", "Delivered"}
        );
        return table;
    }

    private Button buildActionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(44);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-cursor: hand;");
        return btn;
    }

    // ── ORDERS VIEW ───────────────────────────────────────────
    private Parent buildOrdersView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #0B0F19;");

        Label title = new Label("📋 Order Management");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");

        // Filter tabs
        HBox tabs = new HBox(8);
        String[] tabNames = {"All Orders", "Incoming", "Active", "Completed"};
        String[] tabColors = {"#10B981", "#2A3350", "#2A3350", "#2A3350"};
        for (int i = 0; i < tabNames.length; i++) {
            Button tab = new Button(tabNames[i]);
            final String color = tabColors[i];
            tab.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-text-fill: " + (i == 0 ? "white" : "#a0aec0") + "; " +
                    "-fx-font-size: 12px; -fx-background-radius: 20; " +
                    "-fx-padding: 7 16; -fx-cursor: hand;");
            tabs.getChildren().add(tab);
        }

        // Orders list
        VBox ordersList = new VBox(10);
        VBox.setVgrow(ordersList, Priority.ALWAYS);

        String[][] orders = {
                {"Table 3", "Jollof Rice × 2, Chicken × 1", "10:32 AM",
                        "PREPARING", "#F59E0B"},
                {"Table 7", "Fried Rice × 1, Juice × 2", "10:45 AM",
                        "READY", "#10B981"},
                {"Table 1", "Fufu & Soup × 3", "11:02 AM",
                        "PENDING", "#6B7280"},
                {"Table 5", "Burger × 2, Chips × 2, Malt × 2", "11:15 AM",
                        "DELIVERED", "#3B82F6"},
                {"Table 9", "Kelewele × 1, Spring Rolls × 2", "11:28 AM",
                        "PREPARING", "#F59E0B"}
        };

        for (String[] order : orders) {
            ordersList.getChildren().add(buildOrderCard(order));
        }

        ScrollPane scroll = new ScrollPane(ordersList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(title, tabs, scroll);
        return view;
    }

    private HBox buildOrderCard(String[] order) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setStyle("-fx-background-color: #161D30; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #1E2740; " +
                "-fx-border-radius: 12; -fx-border-width: 1;");

        // Table number badge
        StackPane badge = new StackPane();
        badge.setMinSize(52, 52);
        badge.setMaxSize(52, 52);
        badge.setStyle("-fx-background-color: #10B98122; " +
                "-fx-background-radius: 10;");
        Label tableLabel = new Label(order[0].replace("Table ", "T"));
        tableLabel.setStyle("-fx-text-fill: #10B981; " +
                "-fx-font-size: 13px; -fx-font-weight: bold;");
        badge.getChildren().add(tableLabel);

        // Order details
        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);
        Label tableName = new Label(order[0]);
        tableName.setStyle("-fx-text-fill: white; -fx-font-size: 15px; " +
                "-fx-font-weight: bold;");
        Label items = new Label(order[1]);
        items.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        details.getChildren().addAll(tableName, items);

        // Time
        Label time = new Label(order[2]);
        time.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12px;");

        // Status badge
        Label status = new Label(order[3]);
        status.setPadding(new Insets(4, 12, 4, 12));
        status.setStyle("-fx-background-color: " + order[4] + "22; " +
                "-fx-text-fill: " + order[4] + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-background-radius: 20;");

        card.getChildren().addAll(badge, details, time, status);
        return card;
    }

    // ── KITCHEN VIEW ──────────────────────────────────────────
    private Parent buildKitchenView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #0B0F19;");

        Label title = new Label("👨‍🍳 Kitchen Monitor");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");
        Label subtitle = new Label(
                "Update order status — changes reflect on customer screen instantly");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        // Status columns
        HBox columns = new HBox(12);
        columns.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(columns, Priority.ALWAYS);

        String[][] statuses = {
                {"⏳ Pending", "#6B7280",
                        "Table 1|Fufu & Soup × 3", "Table 8|Waakye × 2"},
                {"👨‍🍳 Preparing", "#F59E0B",
                        "Table 3|Jollof Rice × 2", "Table 9|Kelewele × 1"},
                {"✅ Ready", "#10B981",
                        "Table 7|Fried Rice × 1", ""},
                {"🚀 Delivered", "#3B82F6",
                        "Table 5|Burger × 2", "Table 2|Salad × 1"}
        };

        for (String[] col : statuses) {
            VBox column = buildKitchenColumn(col[0], col[1],
                    new String[]{col[2], col[3]});
            HBox.setHgrow(column, Priority.ALWAYS);
            columns.getChildren().add(column);
        }

        ScrollPane scroll = new ScrollPane(columns);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent; " +
                "-fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(title, subtitle, scroll);
        return view;
    }

    private VBox buildKitchenColumn(String status, String color,
                                    String[] orders) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(14));
        col.setStyle("-fx-background-color: #161D30; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + color + "44; " +
                "-fx-border-radius: 12; -fx-border-width: 1;");

        // Column header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 8, 0));
        header.setStyle("-fx-border-color: " + color + "44; " +
                "-fx-border-width: 0 0 1 0;");
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-text-fill: " + color + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold;");
        header.getChildren().add(statusLabel);
        col.getChildren().add(header);

        // Order cards in column
        for (String order : orders) {
            if (order == null || order.isEmpty()) continue;
            String[] parts = order.split("\\|");
            VBox card = new VBox(4);
            card.setPadding(new Insets(10, 12, 10, 12));
            card.setStyle("-fx-background-color: #0B0F19; " +
                    "-fx-background-radius: 8;");
            Label tableL = new Label(parts[0]);
            tableL.setStyle("-fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold;");
            Label itemsL = new Label(parts.length > 1 ? parts[1] : "");
            itemsL.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

            // Status update buttons
            HBox btns = new HBox(6);
            btns.setPadding(new Insets(6, 0, 0, 0));
            Button nextBtn = new Button("→ Next Status");
            nextBtn.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-text-fill: white; -fx-font-size: 10px; " +
                    "-fx-background-radius: 6; -fx-padding: 4 10; " +
                    "-fx-cursor: hand;");
            btns.getChildren().add(nextBtn);

            card.getChildren().addAll(tableL, itemsL, btns);
            col.getChildren().add(card);
        }
        return col;
    }

    // ── SALES VIEW ────────────────────────────────────────────
    private Parent buildSalesView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(28, 32, 28, 32));
        view.setStyle("-fx-background-color: #0B0F19;");

        Label title = new Label("📈 Sales Analytics");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");

        // Period tabs
        HBox tabs = new HBox(8);
        String[] periods = {"Today", "This Week", "This Month"};
        for (int i = 0; i < periods.length; i++) {
            Button tab = new Button(periods[i]);
            tab.setStyle("-fx-background-color: " +
                    (i == 0 ? "#10B981" : "#2A3350") + "; " +
                    "-fx-text-fill: " + (i == 0 ? "white" : "#a0aec0") + "; " +
                    "-fx-font-size: 12px; -fx-background-radius: 20; " +
                    "-fx-padding: 7 16; -fx-cursor: hand;");
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
        topTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; " +
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
        row.setStyle("-fx-background-color: #161D30; " +
                "-fx-background-radius: 10;");

        Label name = new Label(item[0]);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold;");
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);

        // Progress bar
        StackPane barBg = new StackPane();
        barBg.setPrefSize(120, 6);
        barBg.setStyle("-fx-background-color: #2A3350; " +
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
        view.setStyle("-fx-background-color: #0B0F19;");

        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("👥 Staff Management");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; " +
                "-fx-font-weight: bold;");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = new Button("+ Add Staff");
        addBtn.setStyle("-fx-background-color: #10B981; " +
                "-fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-background-radius: 10; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        headerRow.getChildren().addAll(title, addBtn);

        // Staff table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: #161D30; " +
                "-fx-control-inner-background: #161D30;");
        VBox.setVgrow(table, Priority.ALWAYS);

        String[] cols = {"Staff ID", "Name", "Role", "Status"};
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

        table.getItems().addAll(
                new String[]{"001", "Admin User",  "Admin",        "Active"},
                new String[]{"002", "John Mensah", "Waiter",       "Active"},
                new String[]{"003", "Chef Mary",   "Kitchen Staff","Active"},
                new String[]{"004", "Ama Owusu",   "Cashier",      "Active"},
                new String[]{"005", "Kofi Asante", "Supervisor",   "Active"}
        );

        view.getChildren().addAll(headerRow, table);
        return view;
    }

    // ── PLACEHOLDER ───────────────────────────────────────────
    private Parent buildPlaceholder(String icon, String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #0B0F19;");
        Label lbl = new Label(icon);
        lbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 18px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        box.getChildren().addAll(lbl, msg);
        return box;
    }
}