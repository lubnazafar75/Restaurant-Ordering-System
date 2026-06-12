package com.restaurant.dao;

import com.restaurant.model.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderItemDAO {
    OrderItem addItemToOrder(OrderItem item);
    List<OrderItem> getOrderItems(int orderId);
    Optional<OrderItem> getOrderItemById(int orderItemId);
    boolean updateQuantity(int orderItemId, int newQuantity);
    boolean addSpecialInstructions(int orderItemId, String instructions);
    boolean removeItemFromOrder(int orderItemId);
    double getOrderTotal(int orderId);
}