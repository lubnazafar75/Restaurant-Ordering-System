package com.restaurant.restaurant.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

import com.restaurant.restaurant.App;
import com.restaurant.restaurant.StaffDashboard;

/**
 * Central View Routing Orchestrator.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static StackPane innerContentArea;
    private static com.restaurant.restaurant.ordering.OrderController orderController;

    private static final Map<String, Parent> screenRegistry = new HashMap<>();
    private static final Deque<String> history = new ArrayDeque<>();
    private static String currentSceneKey = null;

    public static void initialize(Stage stage, StackPane content) {
        primaryStage = stage;
        innerContentArea = content;
        registerAllScreens();
    }

    private static void registerAllScreens() {

        // ✅ Main Entry
        register(NavigationUtil.MAIN_ENTRY, App.buildMainPortalView());

        // ✅ Customer Home
        register(NavigationUtil.CUSTOMER_HOME,
                NavigationUtil.buildPlaceholderRoot(
                        "Customer Workspace Root", NavigationUtil.CUSTOMER_HOME));

        // ✅ Customer Menu — order.fxml
        try {
            FXMLLoader orderLoader = new FXMLLoader(
                    SceneManager.class.getResource("/fxml/order.fxml"));
            Parent orderRoot = orderLoader.load();
            orderController = orderLoader.getController();
            register(NavigationUtil.CUSTOMER_MENU, orderRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load order.fxml: " + e.getMessage());
            register(NavigationUtil.CUSTOMER_MENU,
                    NavigationUtil.buildPlaceholderRoot(
                            "Customer Ordering Menu", NavigationUtil.CUSTOMER_MENU));
        }

        // ✅ Admin Menu — menu.fxml
        try {
            Parent menuRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/menu.fxml")));
            register(NavigationUtil.ADMIN_MENU, menuRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load menu.fxml: " + e.getMessage());
            register(NavigationUtil.ADMIN_MENU,
                    NavigationUtil.buildPlaceholderRoot(
                            "Admin Menu", NavigationUtil.ADMIN_MENU));
        }

        // ✅ Staff Order Tracking — tracking.fxml
        try {
            Parent trackingRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/tracking.fxml")));
            register(NavigationUtil.ORDER_TRACKING, trackingRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load tracking.fxml: " + e.getMessage());
            register(NavigationUtil.ORDER_TRACKING,
                    NavigationUtil.buildPlaceholderRoot(
                            "Real-Time Order Tracking", NavigationUtil.ORDER_TRACKING));
        }

        // ✅ Customer Tracking — customer_tracking.fxml
        try {
            Parent customerTrackingRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/customer_tracking.fxml")));
            register(NavigationUtil.CUSTOMER_TRACKING, customerTrackingRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load customer_tracking.fxml: "
                    + e.getMessage());
            register(NavigationUtil.CUSTOMER_TRACKING,
                    NavigationUtil.buildPlaceholderRoot(
                            "Customer Order Tracking", NavigationUtil.CUSTOMER_TRACKING));
        }

        // ✅ Customer Billing — customer_billing.fxml
        try {
            Parent customerReceiptRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/customer_receipt.fxml")));
            register(NavigationUtil.CUSTOMER_RECEIPT, customerReceiptRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load customer_receipt.fxml: "
                    + e.getMessage());
            register(NavigationUtil.CUSTOMER_RECEIPT,
                    NavigationUtil.buildPlaceholderRoot(
                            "Your Digital Bill Receipt", NavigationUtil.CUSTOMER_RECEIPT));
        }

        // ✅ Staff Billing — billing.fxml
        try {
            Parent billingRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource(
                            "/com/restaurant/restaurant/billing/billing.fxml")));
            register(NavigationUtil.CUSTOMER_RECEIPT, billingRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load billing.fxml: " + e.getMessage());
            register(NavigationUtil.CUSTOMER_RECEIPT,
                    NavigationUtil.buildPlaceholderRoot(
                            "Billing Management",NavigationUtil.CUSTOMER_RECEIPT));
        }

        // ✅ Staff Login
        try {
            Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/login.fxml")));
            register(NavigationUtil.STAFF_LOGIN, loginRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load login.fxml: "
                    + e.getMessage());
            register(NavigationUtil.STAFF_LOGIN,
                    NavigationUtil.buildPlaceholderRoot(
                            "Staff Login", NavigationUtil.STAFF_LOGIN));
        }
        // ✅ Staff Dashboard

        try {
            Parent staffDashRoot = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/fxml/staff_dashboard.fxml")));
            register(NavigationUtil.STAFF_DASHBOARD, staffDashRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load staff_dashboard.fxml: "
                    + e.getMessage());
            register(NavigationUtil.STAFF_DASHBOARD, StaffDashboard.getRoot());
        }
        // ✅ Order Checking
        register(NavigationUtil.ORDER_CHECKING,
                NavigationUtil.buildPlaceholderRoot(
                        "Hostess Order Intake Checking Panel", NavigationUtil.ORDER_CHECKING));

        // ✅ Kitchen
        register(NavigationUtil.KITCHEN,
                NavigationUtil.buildPlaceholderRoot(
                        "Kitchen Monitor Pipeline", NavigationUtil.KITCHEN));

        // ✅ Admin Staff
        register(NavigationUtil.ADMIN_STAFF,
                NavigationUtil.buildPlaceholderRoot(
                        "Managerial System: Employee Accounts Management",
                        NavigationUtil.ADMIN_STAFF));

        // ✅ Admin Sales
        register(NavigationUtil.ADMIN_SALES,
                NavigationUtil.buildPlaceholderRoot(
                        "Managerial System: Revenue Analytics", NavigationUtil.ADMIN_SALES));
    }

    public static void register(String key, Parent root) {
        screenRegistry.put(key, root);
    }

    public static void navigateTo(String sceneKey) {
        if (!screenRegistry.containsKey(sceneKey)) {
            System.err.println("[SceneManager] Execution Error: Route not found -> " + sceneKey);
            return;
        }

        // Hide sidebar for customer-facing screens
        if (sceneKey.equals(NavigationUtil.MAIN_ENTRY)
                || sceneKey.equals(NavigationUtil.CUSTOMER_HOME)
                || sceneKey.equals(NavigationUtil.CUSTOMER_MENU)
                || sceneKey.equals(NavigationUtil.CUSTOMER_TRACKING)
                || sceneKey.equals(NavigationUtil.CUSTOMER_RECEIPT)
                || sceneKey.equals(NavigationUtil.ORDER_TRACKING)
                || sceneKey.equals(NavigationUtil.STAFF_LOGIN)) {
            App.setSidebarVisibility(false);
        } else {
            App.setSidebarVisibility(true);
        }

        if (currentSceneKey != null && !currentSceneKey.equals(sceneKey)) {
            history.push(currentSceneKey);
        }

        currentSceneKey = sceneKey;
        innerContentArea.getChildren().setAll(screenRegistry.get(sceneKey));
    }

    public static void navigateToMenu() {
        navigateTo(NavigationUtil.CUSTOMER_MENU);
        if (orderController != null) {
            orderController.showMenuDirectly();
        }
    }

    public static void navigateBack() {
        if (history.isEmpty()) return;
        String previous = history.pop();
        currentSceneKey = previous;
        navigateTo(previous);
    }

    public static void clearHistory() {
        history.clear();
    }

    public static void updateScreenRegistryMapping(String key, Parent structuralNode) {
        screenRegistry.put(key, structuralNode);
        if (key.equals(currentSceneKey)) {
            innerContentArea.getChildren().setAll(structuralNode);
        }
    }
}