// FIXED: Shifted package route to match your actual workspace namespace
package com.restaurant.restaurant.navigation;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

// FIXED: Updated imports to match your project's true folder tree location
import com.restaurant.restaurant.App;
import com.restaurant.restaurant.StaffDashboard;

/**
 * Central View Routing Orchestrator.
 * Manages screen instantiation, navigation tracking, state shell configuration changes,
 * and handles live file integration hook maps.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static StackPane innerContentArea;

    // Hash registry store preserving memory locations of all initialized layouts
    private static final Map<String, Parent> screenRegistry = new HashMap<>();
    // History stack layout keeping records of forward path flows to make back navigation operations operational
    private static final Deque<String> history = new ArrayDeque<>();
    private static String currentSceneKey = null;

    /**
     * Initializes structural connections linking navigation maps directly over to your global window view shell.
     */
    public static void initialize(Stage stage, StackPane content) {
        primaryStage = stage;
        innerContentArea  = content;
        registerAllScreens();
    }

    /**
     * Primary Application Layout Entry Registry Mapping Matrix.
     */
    private static final void registerAllScreens() {
        // Portal Hub Gateway Entry View State
        register(NavigationUtil.MAIN_ENTRY, App.buildMainPortalView());

        // ── [INTEGRATION ZONE: CUSTOMER APP VIEWS] ──────────────────────────────────────────────
        // When your team hooks their components together, modify these values to read layouts properly.
        register(NavigationUtil.CUSTOMER_HOME,   NavigationUtil.buildPlaceholderRoot("Customer Workspace Root", NavigationUtil.CUSTOMER_HOME));

        // 📥 INTEGRATION POINT: - Drop Menu System layout here
        register(NavigationUtil.CUSTOMER_MENU,   NavigationUtil.buildPlaceholderRoot("Customer Ordering Menu", NavigationUtil.CUSTOMER_MENU));

        // 📥 INTEGRATION POINT:  - Drop Live Order Progress Screen layout here
        register(NavigationUtil.ORDER_TRACKING,  NavigationUtil.buildPlaceholderRoot("Real-Time Order Tracking", NavigationUtil.ORDER_TRACKING));

        // 📥 INTEGRATION POINT: - Drop Digital Receipts / Billing Layout node right here
        register(NavigationUtil.CUSTOMER_RECEIPT, NavigationUtil.buildPlaceholderRoot("Your Digital Bill Receipt", NavigationUtil.CUSTOMER_RECEIPT));
        // ────────────────────────────────────────────────────────────────────────────────────────

        // ── [INTEGRATION ZONE: VERIFICATION OPERATIONS] ─────────────────────────────────────────
        // 📥 INTEGRATION POINT:  - Link security authentication layout view block
        register(NavigationUtil.STAFF_LOGIN,     NavigationUtil.buildPlaceholderRoot("Staff Authorization Access Gate", NavigationUtil.STAFF_LOGIN));
        // ────────────────────────────────────────────────────────────────────────────────────────

        // Shared Central Dashboard Interface Panel
        register(NavigationUtil.STAFF_DASHBOARD, StaffDashboard.getRoot());

        // ── [INTEGRATION ZONE: RESTAURANT OPERATIONS PANEL WORKSPACES] ──────────────────────────
        // 📥 INTEGRATION POINT: - Connect Hostess Intake Checklist panel tracking view
        register(NavigationUtil.ORDER_CHECKING,  NavigationUtil.buildPlaceholderRoot("Hostess Order Intake Checking Panel", NavigationUtil.ORDER_CHECKING));

        // 📥 INTEGRATION POINT: - Wire up Kitchen monitoring display interface
        register(NavigationUtil.KITCHEN,         NavigationUtil.buildPlaceholderRoot("Kitchen Monitor Pipeline", NavigationUtil.KITCHEN));
        // ────────────────────────────────────────────────────────────────────────────────────────

        // ── [INTEGRATION ZONE: ADMIN RESTRICTED CHANNELS] ────────────────────────────────────────
        // 📥 INTEGRATION POINT: - Insert Admin Menu Editor view controls
        register(NavigationUtil.ADMIN_MENU,      NavigationUtil.buildPlaceholderRoot("Managerial System: Menu Configuration", NavigationUtil.ADMIN_MENU));

        // 📥 INTEGRATION POINT: - Wire up User Account / Employee records editor view
        register(NavigationUtil.ADMIN_STAFF,     NavigationUtil.buildPlaceholderRoot("Managerial System: Employee Accounts Management", NavigationUtil.ADMIN_STAFF));

        // 📥 INTEGRATION POINT: - Feed your historical analytical sales visualization cards right here
        register(NavigationUtil.ADMIN_SALES,     NavigationUtil.buildPlaceholderRoot("Managerial System: Revenue Analytics", NavigationUtil.ADMIN_SALES));
        // ────────────────────────────────────────────────────────────────────────────────────────
    }

    /**
     * Commits view paths to map registries.
     */
    public static void register(String key, Parent root) {
        screenRegistry.put(key, root);
    }

    /**
     * Executes context scene swaps inside the inner view layout panel.
     */
    public static void navigateTo(String sceneKey) {
        if (!screenRegistry.containsKey(sceneKey)) {
            System.err.println("[SceneManager] Execution Error: Route tracking target key not found -> " + sceneKey);
            return;
        }

        // 🛡 SECURE PRIVILEGE SEPARATION DECK LOGIC
        // Evaluates target paths. If public or non-restricted views are targeted, it hides the control drawer shell sidebar.
        if (sceneKey.equals(NavigationUtil.MAIN_ENTRY) || sceneKey.equals(NavigationUtil.CUSTOMER_HOME) ||
                sceneKey.equals(NavigationUtil.CUSTOMER_MENU) || sceneKey.equals(NavigationUtil.ORDER_TRACKING) ||
                sceneKey.equals(NavigationUtil.CUSTOMER_RECEIPT) || sceneKey.equals(NavigationUtil.STAFF_LOGIN)) {

            // Hides structural layout blocks for public panels
            App.setSidebarVisibility(false);
        } else {
            // Forces sidebar visible if accessing internal staff/manager systems
            App.setSidebarVisibility(true);
        }

        // Push previous configurations on history record queues to safely execute tracking back-jumps
        if (currentSceneKey != null && !currentSceneKey.equals(sceneKey)) {
            history.push(currentSceneKey);
        }

        // Set context values and alter content workspace nodes dynamically
        currentSceneKey = sceneKey;
        innerContentArea.getChildren().setAll(screenRegistry.get(sceneKey));
    }

    /**
     * Safely returns back a layer in your tracking path maps.
     */
    public static void navigateBack() {
        if (history.isEmpty()) return;
        String previous = history.pop();
        currentSceneKey = previous;
        navigateTo(previous);
    }

    /**
     * Clears history traces to avoid cyclic loop memory paths when changing access profiles.
     */
    public static void clearHistory() {
        history.clear();
    }

    /**
     * 📥 INTEGRATION CONTROL HOOK: Allows you to programmatically inject completed team files
     * live inside your main driver instance from outside packages seamlessly without altering core logic.
     */
    public static void updateScreenRegistryMapping(String key, Parent structuralNode) {
        screenRegistry.put(key, structuralNode);
        if (key.equals(currentSceneKey)) {
            innerContentArea.getChildren().setAll(structuralNode);
        }
    }
}