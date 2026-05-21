module orderingsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens orders to javafx.fxml;
    opens application to javafx.graphics;
}