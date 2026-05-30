package com.restaurant.restaurant.ordering;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		
	    Parent root = FXMLLoader.load(getClass().getResource("/fxml/order.fxml"));

	    stage.setTitle("Ordering System");

	    Scene scene = new Scene(root, 700, 500);  // create scene

	    scene.getStylesheets().add(
	        getClass().getResource("/css/application.css").toExternalForm()
	    ); // add CSS

	    stage.setScene(scene); // set scene

	    stage.show();
	}
    
	   public static void main(String[] args) {
	        launch(args);
	    }
}
