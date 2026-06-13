module com.restaurant.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens com.restaurant.restaurant to javafx.fxml, javafx.graphics;
    opens com.restaurant.restaurant.menu to javafx.fxml;
    opens com.restaurant.restaurant.navigation to javafx.fxml;
    opens com.restaurant.restaurant.ordering to javafx.fxml, javafx.graphics;
    opens com.restaurant.restaurant.tracking to javafx.fxml;
    opens com.restaurant.restaurant.billing to javafx.fxml;
    opens com.restaurant.restaurant.dao to javafx.fxml;
    opens com.restaurant.restaurant.dao.impl to javafx.fxml;
    opens com.restaurant.restaurant.model to javafx.fxml;
    opens com.restaurant.restaurant.database to javafx.fxml;
    opens com.restaurant.restaurant.login to javafx.fxml;
    opens com.restaurant.restaurant.staff to javafx.fxml;

    exports com.restaurant.restaurant;
    exports com.restaurant.restaurant.menu;
    exports com.restaurant.restaurant.navigation;
    exports com.restaurant.restaurant.ordering;
    exports com.restaurant.restaurant.tracking;
    exports com.restaurant.restaurant.billing;
    exports com.restaurant.restaurant.login;

}