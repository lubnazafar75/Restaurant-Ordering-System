package com.restaurant.restaurant.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.restaurant.restaurant.App;
import com.restaurant.restaurant.StaffDashboard;

/**
 * Central View Routing Orchestrator.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static StackPane innerContentArea;

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

        // ✅ Customer Home (placeholder)
        register(NavigationUtil.CUSTOMER_HOME,
                NavigationUtil.buildPlaceholderRoot("Customer Workspace Root", NavigationUtil.CUSTOMER_HOME));

        // ✅ Customer Menu — order.fxml integrated
        try {
            Parent orderRoot = FXMLLoader.load(SceneManager.class.getResource("/fxml/order.fxml"));
            register(NavigationUtil.CUSTOMER_MENU, orderRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load order.fxml: " + e.getMessage());
            register(NavigationUtil.CUSTOMER_MENU,
                    NavigationUtil.buildPlaceholderRoot("Customer Ordering Menu", NavigationUtil.CUSTOMER_MENU));
        }

        // ✅ Admin Menu — menu.fxml integrated
        try {
            Parent menuRoot = FXMLLoader.load(SceneManager.class.getResource("/fxml/menu.fxml"));
            register(NavigationUtil.ADMIN_MENU, menuRoot);
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load menu.fxml: " + e.getMessage());
            register(NavigationUtil.ADMIN_MENU,
                    NavigationUtil.buildPlaceholderRoot("Admin Menu", NavigationUtil.ADMIN_MENU));
        }

        // ✅ Remaining placeholders
        register(NavigationUtil.ORDER_TRACKING,
                NavigationUtil.buildPlaceholderRoot("Real-Time Order Tracking", NavigationUtil.ORDER_TRACKING));

        register(NavigationUtil.CUSTOMER_RECEIPT,
                NavigationUtil.buildPlaceholderRoot("Your Digital Bill Receipt", NavigationUtil.CUSTOMER_RECEIPT));

        register(NavigationUtil.STAFF_LOGIN,
                NavigationUtil.buildPlaceholderRoot("Staff Authorization Access Gate", NavigationUtil.STAFF_LOGIN));

        register(NavigationUtil.STAFF_DASHBOARD, StaffDashboard.getRoot());

        register(NavigationUtil.ORDER_CHECKING,
                NavigationUtil.buildPlaceholderRoot("Hostess Order Intake Checking Panel", NavigationUtil.ORDER_CHECKING));

        register(NavigationUtil.KITCHEN,
                NavigationUtil.buildPlaceholderRoot("Kitchen Monitor Pipeline", NavigationUtil.KITCHEN));

        register(NavigationUtil.ADMIN_STAFF,
                NavigationUtil.buildPlaceholderRoot("Managerial System: Employee Accounts Management", NavigationUtil.ADMIN_STAFF));

        register(NavigationUtil.ADMIN_SALES,
                NavigationUtil.buildPlaceholderRoot("Managerial System: Revenue Analytics", NavigationUtil.ADMIN_SALES));
    }

    public static void register(String key, Parent root) {
        screenRegistry.put(key, root);
    }

    public static void navigateTo(String sceneKey) {
        if (!screenRegistry.containsKey(sceneKey)) {
            System.err.println("[SceneManager] Execution Error: Route tracking target key not found -> " + sceneKey);
            return;
        }

        if (sceneKey.equals(NavigationUtil.MAIN_ENTRY) || sceneKey.equals(NavigationUtil.CUSTOMER_HOME) ||
                sceneKey.equals(NavigationUtil.CUSTOMER_MENU) || sceneKey.equals(NavigationUtil.ORDER_TRACKING) ||
                sceneKey.equals(NavigationUtil.CUSTOMER_RECEIPT) || sceneKey.equals(NavigationUtil.STAFF_LOGIN)) {
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