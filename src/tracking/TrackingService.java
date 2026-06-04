package tracking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TrackingService {

    public ObservableList<Order> getOrders() {

        ObservableList<Order> orders =
                FXCollections.observableArrayList();

        orders.add(
                new Order(
                        101,
                        4,
                        OrderStatus.PENDING
                )
        );

        orders.add(
                new Order(
                        102,
                        5,
                        OrderStatus.PREPARING
                )
        );

        orders.add(
                new Order(
                        103,
                        7,
                        OrderStatus.READY
                )
        );
        orders.add(
                new Order(
                        104,
                        9,
                        OrderStatus.COMPLETED
                )
        );

        return orders;
    }
}