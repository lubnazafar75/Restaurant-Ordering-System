// FIXED: Shifted package route to match your actual workspace namespace
package com.restaurant.restaurant;

import com.restaurant.restaurant.navigation.NavigationUtil;
import com.restaurant.restaurant.navigation.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Primary Shell View Blueprint Window Framework.
 * Sets theme styling properties, builds custom title frames, constructs side navigation decks,
 * and controls outer container layouts.
 */
public class App extends Application {

    // Track mouse locations for dragging custom undecorated application frames safely
    private double xOffset = 0;
    private double yOffset = 0;

    // Shared pointer access references to alter drawer shell components across dynamic class paths
    private static VBox globalLeftSidebarContainer;

    @Override
    public void start(Stage stage) {
        // Disables default platform window styling layouts
        stage.initStyle(StageStyle.UNDECORATED);

        // Frame and structural container setup calls
        HBox titleBar = buildCustomApplicationTitleBar(stage);
        globalLeftSidebarContainer = buildPersistentSystemSidebar();

        // Hides sidebar component on boot initialization sequences
        globalLeftSidebarContainer.setVisible(false);
        globalLeftSidebarContainer.setManaged(false);
        globalLeftSidebarContainer.setPrefWidth(0);

        // Core view central workspace viewport block styled in primary Deep Night Blue (#0B0F19)
        StackPane contentViewportPane = new StackPane();
        contentViewportPane.setStyle("-fx-background-color: #0B0F19;");
        HBox.setHgrow(contentViewportPane, Priority.ALWAYS);

        // Joins structural side drawers and center viewport panes together horizontally
// Removed globalLeftSidebarContainer - dashboard has its own sidebar
        HBox workBodyLayoutFrame = new HBox(contentViewportPane);        VBox.setVgrow(workBodyLayoutFrame, Priority.ALWAYS);

        // Root container vertically matching custom title rows and inner layout body grids
        VBox layoutContainerRoot = new VBox(titleBar, workBodyLayoutFrame);
        layoutContainerRoot.setStyle(
                "-fx-background-color: #0B0F19; " +
                        "-fx-border-color: #161D30; " + // Border wrapped in Dark Navy Slate
                        "-fx-border-width: 1;"
        );

        // Build main scene with responsive safety defaults
        // Get the actual screen size of the monitor
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();

// Set window to 90% of screen size
        double windowWidth  = screenBounds.getWidth()  * 0.90;
        double windowHeight = screenBounds.getHeight() * 0.90;

        Scene mainApplicationWindowScene = new Scene(layoutContainerRoot, windowWidth, windowHeight);
        stage.setScene(mainApplicationWindowScene);
        stage.setMinWidth(960);
        stage.setMinHeight(650);

// Center the window on screen
        stage.setX((screenBounds.getWidth()  - windowWidth)  / 2);
        stage.setY((screenBounds.getHeight() - windowHeight) / 2);

        stage.setMaximized(true);
        stage.show();

        // Boot and link SceneManager systems up live to content area contexts
        SceneManager.initialize(stage, contentViewportPane);
        SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY);
    }

    /**
     * Public visibility tracking control method managed dynamically by your SceneManager routing framework.
     */
    public static void setSidebarVisibility(boolean visibleState) {
        // ALWAYS keep sidebar hidden - dashboard has its own sidebar
        if (globalLeftSidebarContainer != null) {
            globalLeftSidebarContainer.setVisible(false);
            globalLeftSidebarContainer.setManaged(false);
        }
    }

    /**
     * Builds custom operational title panels.
     */
    private HBox buildCustomApplicationTitleBar(Stage stage) {
        Label brandingTitleText = new Label(" 🍽  QuickServe Enterprise — Management System");
        brandingTitleText.setFont(Font.font("System", FontWeight.BOLD, 13));
        brandingTitleText.setTextFill(Color.WHITE);
        Region layoutSpacer = new Region();
        HBox.setHgrow(layoutSpacer, Priority.ALWAYS);

        // Generates structural management layout window frame controls
        Button btnMin = createTitleFrameWindowControlActionBtn("─", "#4a5568", "#FF4A85");
        Button btnMax = createTitleFrameWindowControlActionBtn("□", "#4a5568", "#00E676");
        Button btnCls = createTitleFrameWindowControlActionBtn("✕", "#e53e3e", "#c0392b");

        btnMin.setOnAction(e -> stage.setIconified(true));
        btnMax.setOnAction(e -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                btnMax.setText("□");
            } else {
                stage.setMaximized(true);
                btnMax.setText("❐");
            }
        });
        btnCls.setOnAction(e -> Platform.exit());

        HBox customTitleBarLayout = new HBox(brandingTitleText, layoutSpacer, btnMin, btnMax, btnCls);
        customTitleBarLayout.setAlignment(Pos.CENTER_LEFT);
        customTitleBarLayout.setPadding(new Insets(0, 0, 0, 14));
        customTitleBarLayout.setPrefHeight(38);
        customTitleBarLayout.setStyle("-fx-background-color: #0B0F19; -fx-border-color: #161D30; -fx-border-width: 0 0 1 0;");

        // Drag listeners
        customTitleBarLayout.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        customTitleBarLayout.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });

        return customTitleBarLayout;
    }

    /**
     * Builds corporate sidebar layout drawer decks styled in Dark Navy Slate (#161D30).
     */
    private VBox buildPersistentSystemSidebar() {
        // Branding Box area
        VBox logoIdentityArea = new VBox(4,
                new Label("🍽") {{ setFont(Font.font("System", 32)); }},
                new Label("QuickServe") {{ setFont(Font.font("System", FontWeight.BOLD, 16)); setTextFill(Color.WHITE); }},
                new Label("SYSTEM INTEGRATOR CONSOLE") {{ setFont(Font.font("System", 9)); setTextFill(Color.web("#FF4A85")); }} // Cyber Pink Accent
        );
        logoIdentityArea.setAlignment(Pos.CENTER);
        logoIdentityArea.setPadding(new Insets(22, 10, 22, 10));

        // Employee Operational Shortcut layouts
        VBox operationalActionLayoutContainer = new VBox(4,
                createSidebarNavigationButton("📋", "Order Intake Deck", NavigationUtil.ORDER_CHECKING),
                createSidebarNavigationButton("👨‍🍳", "Kitchen Monitor",   NavigationUtil.KITCHEN),
                createSidebarNavigationButton("📊", "Staff Hub Main",    NavigationUtil.STAFF_DASHBOARD)
        );

        // High Priority Management Restricted administrative layout container
        VBox administrativeActionLayoutContainer = new VBox(4,
                new Label("MANAGEMENT PRIVILEGES") {{ setFont(Font.font("System", FontWeight.BOLD, 10)); setPadding(new Insets(12,0,4,14)); setTextFill(Color.web("#FF4A85")); }},
                createSidebarNavigationButton("🍔", "Menu Customizer",   NavigationUtil.ADMIN_MENU),
                createSidebarNavigationButton("👥", "Manage Personnel",  NavigationUtil.ADMIN_STAFF),
                createSidebarNavigationButton("📈", "Revenue Reports",   NavigationUtil.ADMIN_SALES),

                // ── [INTEGRATION NODE: BILLING & RECEIPTS LINK] ──────────────────────────────────────────
                // Injected the tracking key route so you can jump to Eunice's billing layouts directly from the sidebar.
                createSidebarNavigationButton("🧾", "Billing & Receipts",
                        NavigationUtil.CUSTOMER_RECEIPT_STAFF)        );

        Region layoutVerticalSpacer = new Region();
        VBox.setVgrow(layoutVerticalSpacer, Priority.ALWAYS);

        // System shutdown logout escape control button mapped in Crimson Red Warning tokens
        Button systemExitLogoutActionBtn = new Button("← Exit Session");
        systemExitLogoutActionBtn.setPrefSize(166, 36);
        systemExitLogoutActionBtn.setStyle(
                "-fx-background-color: #FF3333; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;"
        );
        systemExitLogoutActionBtn.setOnAction(e -> NavigationUtil.logout());

        VBox structuralLayoutSidebar = new VBox(
                logoIdentityArea, createHorizontalDividerComponent(),
                operationalActionLayoutContainer, administrativeActionLayoutContainer,
                layoutVerticalSpacer, new VBox(systemExitLogoutActionBtn) {{ setPadding(new Insets(10)); setAlignment(Pos.CENTER); }}
        );
        structuralLayoutSidebar.setPrefWidth(190);
        structuralLayoutSidebar.setStyle("-fx-background-color: #161D30; -fx-border-color: #161D30; -fx-border-width: 0 1 0 0;");

        return structuralLayoutSidebar;
    }

    /**
     * Profile Selection Center Portal Welcome Hub.
     * Displays clean profile paths immediately on startup.
     */
    public static Parent buildMainPortalView() {
        Label welcomeTextHeader = new Label("Welcome to QuickServe System");
        welcomeTextHeader.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeTextHeader.setTextFill(Color.WHITE);

        Label subPromptTextText = new Label("Please click your identity profile path to access interface systems:");
        subPromptTextText.setFont(Font.font("System", 14));
        subPromptTextText.setTextFill(Color.web("#a0aec0"));

        // Route Profile 1: Customer path maps directly into Electric Neon Green (#00E676)
        Button profileRouteCustomerActionBtn = new Button("Customer Ordering Terminal");
        profileRouteCustomerActionBtn.setPrefSize(380, 54);
        profileRouteCustomerActionBtn.setStyle(
                "-fx-background-color: #00E676; -fx-text-fill: #0B0F19; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        profileRouteCustomerActionBtn.setOnAction(e -> NavigationUtil.goTo(NavigationUtil.CUSTOMER_MENU));

        // Route Profile 2: Employee path maps directly into custom Cyber Pink borders (#FF4A85)
        Button profileRouteStaffActionBtn = new Button("🔐 Staff Log In");
        profileRouteStaffActionBtn.setPrefSize(380, 54);
        profileRouteStaffActionBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #FF4A85; " +
                        "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-border-color: #FF4A85; -fx-border-width: 2; -fx-cursor: hand;"
        );
        profileRouteStaffActionBtn.setOnAction(e -> NavigationUtil.goTo(NavigationUtil.STAFF_LOGIN));

        VBox interactiveMenuBlockLayout = new VBox(18, profileRouteCustomerActionBtn, profileRouteStaffActionBtn);
        interactiveMenuBlockLayout.setAlignment(Pos.CENTER);
        interactiveMenuBlockLayout.setPadding(new Insets(20));

        VBox frameContainerBlockLayout = new VBox(30, welcomeTextHeader, subPromptTextText, interactiveMenuBlockLayout);
        frameContainerBlockLayout.setAlignment(Pos.CENTER);
        frameContainerBlockLayout.setStyle("-fx-background-color: #0B0F19;");

        return frameContainerBlockLayout;
    }

    /**
     * Dynamic Navigation Link Button Factory with specialized Cyber Pink left anchor-bar hover metrics.
     */
    private Button createSidebarNavigationButton(String visualIconSymbol, String componentLabelTitle, String sceneRoutingTargetKey) {
        Button sidebarNavigationControlInstanceButton = new Button(visualIconSymbol + "  " + componentLabelTitle);
        sidebarNavigationControlInstanceButton.setPrefSize(174, 38);
        sidebarNavigationControlInstanceButton.setAlignment(Pos.CENTER_LEFT);
        sidebarNavigationControlInstanceButton.setFont(Font.font("System", 13));
        sidebarNavigationControlInstanceButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0aec0; " +
                        "-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 0 0 0 14;"
        );

        // Hover layout styling maps: transforms text Green and drops a Pink anchor line block
        sidebarNavigationControlInstanceButton.setOnMouseEntered(e -> sidebarNavigationControlInstanceButton.setStyle(
                "-fx-background-color: #0B0F19; -fx-text-fill: #00E676; " +
                        "-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 0 0 0 14; -fx-border-color: #FF4A85; -fx-border-width: 0 0 0 3;"
        ));

        sidebarNavigationControlInstanceButton.setOnMouseExited(e -> sidebarNavigationControlInstanceButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0aec0; " +
                        "-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 0 0 0 14;"
        ));

        sidebarNavigationControlInstanceButton.setOnAction(e -> SceneManager.navigateTo(sceneRoutingTargetKey));
        return sidebarNavigationControlInstanceButton;
    }

    private Separator createHorizontalDividerComponent() {
        Separator structuralSeparatorWidgetElement = new Separator();
        structuralSeparatorWidgetElement.setStyle("-fx-background-color: #161D30;");
        VBox.setMargin(structuralSeparatorWidgetElement, new Insets(6, 12, 6, 12));
        return structuralSeparatorWidgetElement;
    }

    private Button createTitleFrameWindowControlActionBtn(
            String characterSymbolSign,
            String primaryHoverColorHexVal,
            String structuralPressColorToken) {

        Button btn = new Button(characterSymbolSign);
        btn.setPrefSize(46, 38);
        btn.setFont(Font.font("System", 13));

        // Close button (✕) gets permanent red background so it is always visible
        boolean isCloseBtn = characterSymbolSign.equals("✕");

        if (isCloseBtn) {
            btn.setStyle(
                    "-fx-background-color: #c0392b; -fx-text-fill: white; " +
                            "-fx-background-radius: 0; -fx-font-weight: bold;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: #e53e3e; -fx-text-fill: white; " +
                            "-fx-background-radius: 0; -fx-font-weight: bold;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: #c0392b; -fx-text-fill: white; " +
                            "-fx-background-radius: 0; -fx-font-weight: bold;"
            ));
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; " +
                            "-fx-background-radius: 0;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: " + primaryHoverColorHexVal + "; " +
                            "-fx-text-fill: white; -fx-background-radius: 0;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; " +
                            "-fx-background-radius: 0;"
            ));
        }

        return btn;
    }

    public static void main(String[] args) {
        // Run core JavaFX system processes loop instances
        launch(args);
    }
}