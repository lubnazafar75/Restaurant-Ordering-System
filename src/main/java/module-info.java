module com.restaurant.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens com.restaurant.restaurant to javafx.fxml, javafx.graphics;
    opens com.restaurant.restaurant.menu to javafx.fxml;
    opens com.restaurant.restaurant.navigation to javafx.fxml;
    opens com.restaurant.restaurant.ordering to javafx.fxml, javafx.graphics;

    exports com.restaurant.restaurant;
    exports com.restaurant.restaurant.menu;
    exports com.restaurant.restaurant.navigation;
    exports com.restaurant.restaurant.ordering;
}