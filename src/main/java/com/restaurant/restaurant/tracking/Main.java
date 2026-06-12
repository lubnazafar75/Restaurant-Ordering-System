package com.restaurant.restaurant.tracking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        try {
            // Load the dark cyber-theme FXML layout file
            Parent root = FXMLLoader.load(getClass().getResource("tracking.fxml"));
            
            // Set up the scene canvas window dimensions
            Scene scene = new Scene(root, 650, 480);
            
            primaryStage.setTitle("Order Status Tracking System");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Launches the JavaFX application life cycle
        launch(args);
    }
}