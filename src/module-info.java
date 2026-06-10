module RestaurantSystem {
    //Gives full access to the standard JavaFX UI components and collections
   requires transitive javafx.graphics;
   requires  javafx.controls;
   requires  javafx.fxml;
   requires  javafx.base;
   
   // Allows the JavaFX engine to see and load the tracking files
   exports tracking;
   opens tracking to javafx.fxml;
}