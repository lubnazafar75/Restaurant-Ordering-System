module com.restaurant.demo {
    // Requires the core JavaFX UI controls module
    requires javafx.controls;

    // Requires the JavaFX FXML module if your team decides to utilize FXML resource layouts
    requires javafx.fxml;

    // Requires the underlying JavaFX window rendering engine
    requires javafx.graphics;

    // Requires the Java standard database connectivity package for SQLite integration
    requires java.sql;

    // FIXED: Updated package paths to match your actual folders
    opens com.restaurant.restaurant to javafx.fxml;
    opens com.restaurant.restaurant.navigation to javafx.fxml;

    // FIXED: Updated package exports to match your actual folders
    exports com.restaurant.restaurant;
    exports com.restaurant.restaurant.navigation;
}