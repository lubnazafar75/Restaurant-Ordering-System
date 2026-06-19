// FIXED: Updated package declaration to match your actual project namespace structure
package com.restaurant.restaurant.navigation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * System String Routes and Navigation Helper Utilities.
 */
public class NavigationUtil {

    // Unique navigation lookup routes tracking throughout the whole application ecosystem
    public static final String MAIN_ENTRY       = "MAIN_ENTRY";
    public static final String CUSTOMER_HOME    = "CUSTOMER_HOME";
    public static final String CUSTOMER_MENU    = "CUSTOMER_MENU";
    public static final String ORDER_TRACKING   = "ORDER_TRACKING";
    public static final String CUSTOMER_TRACKING = "CUSTOMER_TRACKING";
    public static final String customer_billing = "customer_billing";
    public static final String customer_billing_STAFF = "customer_billing_STAFF";
    public static final String STAFF_LOGIN = "/fxml/login.fxml";
    public static final String STAFF_DASHBOARD = "/fxml/staff_dashboard.fxml";
    public static final String KITCHEN          = "KITCHEN";
    public static final String ORDER_CHECKING   = "ORDER_CHECKING";

    public static final String ADMIN_MENU       = "ADMIN_MENU";
    public static final String ADMIN_SALES      = "ADMIN_SALES";
    public static final String ADMIN_STAFF      = "ADMIN_STAFF";

    /**
     * Orders SceneManager to jump directly to a target view node route.
     */
    public static void goTo(String sceneKey) {
        // This will cleanly link to your SceneManager once we verify that file next
        SceneManager.navigateTo(sceneKey);
    }

    /**
     * Pulls the previous location route off the execution history stack.
     */
    public static void goBack() {
        SceneManager.navigateBack();
    }

    /**
     * Purges systemic screen track memory and flushes navigation context paths back to the welcome gate.
     */
    public static void logout() {
        SceneManager.clearHistory();
        SceneManager.navigateTo(MAIN_ENTRY);
    }

    /**
     * Safe Visual Placeholder Generator.
     * Prevents NullPointer crashes by creating temporary styled placeholder screens
     * matching your dark night-blue palette while waiting on team submissions.
     */
    public static Parent buildPlaceholderRoot(String titleText, String sceneKey) {
        // Build the view title text using an Electric Neon Green hue
        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#00E676"));

        // Informational subtext flagged in Cyber Pink
        Label statusLabel = new Label("Waiting for Member module integration file...");
        statusLabel.setFont(Font.font("System", 14));
        statusLabel.setTextFill(Color.web("#FF4A85"));

        Label keyLabel = new Label("Target Registry Key: " + sceneKey);
        keyLabel.setFont(Font.font("Monospace", 12));
        keyLabel.setTextFill(Color.web("#a0aec0"));

        Button backBtn = new Button("← Return");
        backBtn.setOnAction(e -> goBack());
        backBtn.setStyle(
                "-fx-background-color: #161D30; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-padding: 8 22; -fx-background-radius: 6; " +
                        "-fx-border-color: #FF4A85; -fx-border-radius: 6; -fx-cursor: hand;"
        );

        // Core compilation structure box using the Deep Night Blue theme background
        VBox layout = new VBox(20, title, statusLabel, keyLabel, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #0B0F19;");

        return layout;
    }
}