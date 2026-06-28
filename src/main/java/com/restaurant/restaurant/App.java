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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Primary Application Shell.
 * Builds the custom title bar and the central content viewport
 * that SceneManager swaps screens into.
 */
public class App extends Application {

    // Track mouse locations for dragging the undecorated window
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);

        HBox titleBar = buildCustomApplicationTitleBar(stage);

        // Central workspace viewport — screens are swapped in here by SceneManager
        StackPane contentViewportPane = new StackPane();
        contentViewportPane.setStyle("-fx-background-color: #F8FAFC;");
        contentViewportPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(contentViewportPane, Priority.ALWAYS);
        VBox.setVgrow(contentViewportPane, Priority.ALWAYS);

        HBox workBodyLayoutFrame = new HBox(contentViewportPane);
        VBox.setVgrow(workBodyLayoutFrame, Priority.ALWAYS);

        VBox layoutContainerRoot = new VBox(titleBar, workBodyLayoutFrame);
        layoutContainerRoot.setStyle(
                "-fx-background-color: #c7052e; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-width: 1;"
        );

        // Get the actual screen size of the monitor
        javafx.geometry.Rectangle2D screenBounds =
                javafx.stage.Screen.getPrimary().getVisualBounds();

        // Set window to 90% of screen size
        double windowWidth  = screenBounds.getWidth()  * 0.90;
        double windowHeight = screenBounds.getHeight() * 0.90;

        Scene mainApplicationWindowScene =
                new Scene(layoutContainerRoot, windowWidth, windowHeight);

        // Apply Savoria global design system
        mainApplicationWindowScene.getStylesheets().add(
                App.class
                        .getResource("/css/application.css")
                        .toExternalForm()
        );

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
        // Ensure schema/seed data exists. Launcher.main() already does this
        // as the normal entry point, but App also has its own main() and can
        // be launched directly (e.g. from an IDE), so we keep this call as a
        // safety net. It's safe to call twice: every CREATE TABLE uses
        // IF NOT EXISTS and seeding only runs against empty tables. The
        // previous bug was not the double call itself, but that it used to
        // go through two DIFFERENT connections to two different files
        // (see SQLiteDatabaseConnection) — now both paths share one
        // connection via DBConnection, so this is harmless.
        com.restaurant.restaurant.database.DatabaseInitializer.initializeDatabase();
        SceneManager.navigateTo(NavigationUtil.MAIN_ENTRY);
    }

    /**
     * Public visibility hook kept for SceneManager compatibility.
     * The app no longer has a global sidebar — the staff dashboard
     * manages its own sidebar internally.
     */
    public static void setSidebarVisibility(boolean visibleState) {
        // No-op: no global sidebar exists anymore.
    }

    /**
     * Builds the custom window title bar (minimize / maximize / close).
     */
    private HBox buildCustomApplicationTitleBar(Stage stage) {
        Label brandingTitleText = new Label(" 🍽  Savoria — Restaurant Management System");
        brandingTitleText.setFont(Font.font("System", FontWeight.BOLD, 13));
        brandingTitleText.setTextFill(Color.WHITE);

        Region layoutSpacer = new Region();
        HBox.setHgrow(layoutSpacer, Priority.ALWAYS);

        Button btnMin = createTitleFrameWindowControlActionBtn("─", "#374151");
        Button btnMax = createTitleFrameWindowControlActionBtn("□", "#374151");
        Button btnCls = createTitleFrameWindowControlActionBtn("✕", "#374151");

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
        btnCls.getStyleClass().add("title-bar-close");

        HBox customTitleBarLayout = new HBox(brandingTitleText, layoutSpacer, btnMin, btnMax, btnCls);
        customTitleBarLayout.setAlignment(Pos.CENTER_LEFT);
        customTitleBarLayout.setPadding(new Insets(0, 0, 0, 14));
        customTitleBarLayout.setPrefHeight(38);
        customTitleBarLayout.getStyleClass().add("title-bar");

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
     * Landing page — Savoria brand light theme.
     * Split layout: branding/visual panel on the left, welcome content on the right.
     */
    public static Parent buildMainPortalView() {

        // ── ROOT ─────────────────────────────────────────────
        HBox root = new HBox();
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setStyle("-fx-background-color: #F8FAFC;");

        // ── LEFT PANEL: Branding visual ─────────────────────
        StackPane leftPanel = new StackPane();
        leftPanel.setMinWidth(380);
        leftPanel.setPrefWidth(480);
        leftPanel.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #a6092a, #ff2929);");

        VBox brandContent = new VBox(14);
        brandContent.setAlignment(Pos.CENTER);
        brandContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label logoIcon = new Label("🍽");
        logoIcon.setStyle("-fx-font-size: 172px;-fx-text-fill: #ffffff;");

        Label brandName = new Label("Savoria");
        brandName.setStyle(
                "-fx-text-fill: white; -fx-font-size: 48px; " +
                        "-fx-font-weight: bold;");

        Label brandSub = new Label("— RESTAURANT —");
        brandSub.setStyle(
                "-fx-text-fill: #FDE68A; -fx-font-size: 13px; " +
                        "-fx-font-weight: bold;");

        Label brandTagline = new Label("Savor Every Moment");
        brandTagline.setStyle(
                "-fx-text-fill: white; -fx-font-size: 16px;");

        VBox.setMargin(brandTagline, new Insets(8, 0, 0, 0));

        Label brandDesc = new Label(
                "Order your favorite meals directly from\nyour table and enjoy a seamless experience.");
        brandDesc.setStyle(
                "-fx-text-fill: #D1FAE5; -fx-font-size: 13px;");
        brandDesc.setTextAlignment(TextAlignment.CENTER);
        brandDesc.setWrapText(true);
        VBox.setMargin(brandDesc, new Insets(12, 0, 0, 0));

        brandContent.getChildren().addAll(
                logoIcon, brandName, brandSub, brandTagline, brandDesc);
        leftPanel.getChildren().add(brandContent);

        // ── RIGHT PANEL: Welcome + Actions ───────────────────
        VBox rightPanel = new VBox();
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setMinWidth(380);
        rightPanel.setPrefWidth(540);
        rightPanel.setMaxHeight(Double.MAX_VALUE);
        rightPanel.setStyle("-fx-background-color: #F8FAFC;");
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(420);
        content.setPadding(new Insets(40));

        Label welcomeSmall = new Label("Welcome to");
        welcomeSmall.setStyle(
                "-fx-text-fill: #747474; -fx-font-size: 18px;");

        Label savoriaTitle = new Label("Savoria");
        savoriaTitle.setStyle(
                "-fx-text-fill: rgb(199 5 46 / 0.45); -fx-font-size: 52px; " +
                        "-fx-font-weight: bold;");

        // Orange divider with fork icon
        HBox divider = new HBox(8);
        divider.setAlignment(Pos.CENTER);
        Region l1 = new Region();
        l1.setPrefSize(60, 2);
        l1.setStyle("-fx-background-color: #F59E0B;");
        Label forkIcon = new Label("🍴");
        forkIcon.setStyle("-fx-font-size: 14px;");
        Region l2 = new Region();
        l2.setPrefSize(60, 2);
        l2.setStyle("-fx-background-color: #F59E0B;");
        divider.getChildren().addAll(l1, forkIcon, l2);
        VBox.setMargin(divider, new Insets(4, 0, 0, 0));

        Label tagline = new Label("Savor Every Moment");
        tagline.setStyle(
                "-fx-text-fill: #F59E0B; -fx-font-size: 16px; " +
                        "-fx-font-weight: bold;");

        Label description = new Label(
                "Order your favorite meals directly from your table\n" +
                        "and enjoy a seamless dining experience.");
        description.setStyle(
                "-fx-text-fill: #6B7280; -fx-font-size: 14px;");
        description.setTextAlignment(TextAlignment.CENTER);
        description.setWrapText(true);
        VBox.setMargin(description, new Insets(0, 0, 8, 0));

        // ── Start Ordering — Primary button ──────────────────
        Button startBtn = new Button("🍽   Start Ordering   ›");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setPrefHeight(56);
        startBtn.getStyleClass().add("btn-primary");
        startBtn.setStyle(startBtn.getStyle() + "-fx-font-size: 16px;");
        startBtn.setOnAction(
                e -> NavigationUtil.goTo(NavigationUtil.CUSTOMER_MENU));

        // ── Staff Access divider ──────────────────────────────
        HBox staffDiv = new HBox(8);
        staffDiv.setAlignment(Pos.CENTER);
        Region sd1 = new Region();
        sd1.setPrefSize(70, 1);
        sd1.setStyle("-fx-background-color: #E5E7EB;");
        Label staffLabel = new Label("Staff Access");
        staffLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
        Region sd2 = new Region();
        sd2.setPrefSize(70, 1);
        sd2.setStyle("-fx-background-color: #E5E7EB;");
        staffDiv.getChildren().addAll(sd1, staffLabel, sd2);
        VBox.setMargin(staffDiv, new Insets(8, 0, 0, 0));

        // ── Staff Login — Secondary button ────────────────────
        Button staffBtn = new Button("👤   Staff Login   ›");
        staffBtn.setMaxWidth(Double.MAX_VALUE);
        staffBtn.setPrefHeight(52);
        staffBtn.getStyleClass().add("btn-secondary");
        staffBtn.setStyle(staffBtn.getStyle() + "-fx-font-size: 15px;");
        staffBtn.setOnAction(
                e -> NavigationUtil.goTo(NavigationUtil.STAFF_LOGIN));

        content.getChildren().addAll(
                welcomeSmall, savoriaTitle, divider, tagline,
                description, startBtn, staffDiv, staffBtn);
        rightPanel.getChildren().add(content);

        // ── Footer features bar ───────────────────────────────
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(16, 24, 16, 24));
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-width: 1 0 0 0;");

        String[][] features = {
                {"⚡", "Fast Service", "Quick and efficient"},
                {"📦", "Real-Time Tracking", "Track your order live"},
                {"🛡", "Easy Payment", "Secure and simple"},
                {"🌿", "Freshly Prepared", "Made with love"}
        };

        for (int i = 0; i < features.length; i++) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(item, Priority.ALWAYS);
            item.setPadding(new Insets(0, 14, 0, 14));

            StackPane circle = new StackPane();
            circle.setMinSize(36, 36);
            circle.setMaxSize(36, 36);
            circle.setStyle(
                    "-fx-background-color: #D1FAE5; " +
                            "-fx-background-radius: 50;");
            Label ico = new Label(features[i][0]);
            ico.setStyle("-fx-font-size: 14px;");
            circle.getChildren().add(ico);

            VBox txt = new VBox(1);
            Label t1 = new Label(features[i][1]);
            t1.setStyle(
                    "-fx-text-fill: rgb(199 5 46 / 0.45); -fx-font-size: 12px; " +
                            "-fx-font-weight: bold;");
            Label t2 = new Label(features[i][2]);
            t2.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px;");
            txt.getChildren().addAll(t1, t2);
            item.getChildren().addAll(circle, txt);
            footer.getChildren().add(item);

            if (i < features.length - 1) {
                Region vd = new Region();
                vd.setPrefSize(1, 36);
                vd.setStyle("-fx-background-color: #E5E7EB;");
                footer.getChildren().add(vd);
            }
        }

        VBox rightWithFooter = new VBox(rightPanel, footer);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        HBox.setHgrow(rightWithFooter, Priority.ALWAYS);
        rightWithFooter.setMinWidth(380);

        root.getChildren().addAll(leftPanel, rightWithFooter);
        return root;
    }

    /**
     * Title bar window control button (minimize/maximize/close).
     */
    private Button createTitleFrameWindowControlActionBtn(
            String characterSymbolSign, String hoverColor) {

        Button btn = new Button(characterSymbolSign);
        btn.setPrefSize(46, 38);
        btn.setFont(Font.font("System", 13));
        btn.getStyleClass().add("title-bar-btn");

        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
