// FIXED: Shifted package route to match your actual workspace namespace
package com.restaurant.restaurant;

// FIXED: Updated imports to point to your real package structure instead of demo
import com.restaurant.restaurant.navigation.SceneManager;
import com.restaurant.restaurant.navigation.NavigationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main Centralized Operational Task Desk Hub.
 */
public class StaffDashboard {

    public static Parent getRoot() {
        Label title = new Label("Staff Management Terminal Workspace");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Select operational task profile card workspace to route monitoring parameters:");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web("#a0aec0"));

        VBox headerAreaContainerPanel = new VBox(6, title, subtitle);
        headerAreaContainerPanel.setPadding(new Insets(28, 32, 20, 32));
        headerAreaContainerPanel.setStyle("-fx-background-color: #161D30;"); // Dark Navy Slate Surface

        // Responsive Flow layout automatically shifts cards cleanly to new lines during screen resizing
        FlowPane modularControlCardsGridDeck = new FlowPane();
        modularControlCardsGridDeck.setHgap(22);
        modularControlCardsGridDeck.setVgap(22);
        modularControlCardsGridDeck.setPadding(new Insets(28, 32, 28, 32));
        modularControlCardsGridDeck.setStyle("-fx-background-color: #0B0F19;"); // Deep Night Blue

        // Injects your operational card system elements mapping accents to designated team targets
        modularControlCardsGridDeck.getChildren().addAll(
                buildInteractiveDashboardCardComponent("📋", "Order Verification Intake", "Process incoming active queue configurations.", "#00E676", NavigationUtil.ORDER_CHECKING),
                buildInteractiveDashboardCardComponent("📦", "Delivery Status Monitor", "Verify handshakes on packaging pipelines.", "#00E676", NavigationUtil.ORDER_TRACKING),
                buildInteractiveDashboardCardComponent("👨‍🍳", "Kitchen Production", "Modify dish prep latency and line logs.", "#FF4A85", NavigationUtil.KITCHEN),
                buildInteractiveDashboardCardComponent("🍔", "Menu Manifest System", "Alter pricing arrays and entry records.", "#FF4A85", NavigationUtil.ADMIN_MENU),
                buildInteractiveDashboardCardComponent("👥", "Roster Account Management", "Provision keys and access clear levels.", "#FF4A85", NavigationUtil.ADMIN_STAFF),
                buildInteractiveDashboardCardComponent("📈", "Revenue Financial Reporting", "Aggregate processing audit calculations.", "#00E676", NavigationUtil.ADMIN_SALES),

                // ── [INTEGRATION NODE: BILLING MODULE DASHBOARD CARD] ────────────────────────────────────
                // Added a dedicated card to allow managers to jump straight to the billing/receipts module.
                buildInteractiveDashboardCardComponent("🧾", "Billing & Auditing", "Review consumer receipts, invoices, and sales data.", "#00E676", NavigationUtil.CUSTOMER_RECEIPT)
        );

        VBox rootMainLayoutBlock = new VBox(headerAreaContainerPanel, modularControlCardsGridDeck);
        rootMainLayoutBlock.setStyle("-fx-background-color: #0B0F19;");
        return rootMainLayoutBlock;
    }

    /**
     * Card Block Component Factory. Generates matching panel widgets with shadow highlights on hover interactions.
     */
    private static VBox buildInteractiveDashboardCardComponent(String decorativeIconSign, String cardTitleLabelText, String descriptiveTextParagraph, String borderAccentColorHex, String targetSceneRoutingKey) {
        Label iconView = new Label(decorativeIconSign) {{ setFont(Font.font("System", 30)); }};
        Label titleView = new Label(cardTitleLabelText) {{ setFont(Font.font("System", FontWeight.BOLD, 14)); setTextFill(Color.WHITE); }};

        Label descView = new Label(descriptiveTextParagraph);
        descView.setFont(Font.font("System", 12));
        descView.setTextFill(Color.web("#a0aec0"));
        descView.setWrapText(true);
        descView.setPrefHeight(38);

        Button actionExecuteButton = new Button("Launch Module →");
        actionExecuteButton.setStyle(
                "-fx-background-color: #161D30; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 14; " +
                        "-fx-background-radius: 4; -fx-border-color: " + borderAccentColorHex + "; -fx-border-radius: 4; -fx-cursor: hand;"
        );
        actionExecuteButton.setOnAction(e -> SceneManager.navigateTo(targetSceneRoutingKey));

        VBox structuralControlCardWidgetBox = new VBox(12, iconView, titleView, descView, actionExecuteButton);
        structuralControlCardWidgetBox.setPrefSize(230, 175);
        structuralControlCardWidgetBox.setPadding(new Insets(18));
        structuralControlCardWidgetBox.setStyle(
                "-fx-background-color: #161D30; -fx-background-radius: 8; " +
                        "-fx-border-color: #161D30; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 3);"
        );

        // Hover animations: adds high-contrast accent glowing rings on active states
        structuralControlCardWidgetBox.setOnMouseEntered(e -> structuralControlCardWidgetBox.setStyle(
                "-fx-background-color: #1F2942; -fx-background-radius: 8; " +
                        "-fx-border-color: " + borderAccentColorHex + "; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, " + borderAccentColorHex + "33, 10, 0, 0, 4);"
        ));

        structuralControlCardWidgetBox.setOnMouseExited(e -> structuralControlCardWidgetBox.setStyle(
                "-fx-background-color: #161D30; -fx-background-radius: 8; " +
                        "-fx-border-color: #161D30; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 3);"
        ));

        return structuralControlCardWidgetBox;
    }
}