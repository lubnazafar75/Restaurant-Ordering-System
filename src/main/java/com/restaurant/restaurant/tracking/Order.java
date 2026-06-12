package com.restaurant.restaurant.tracking;

public class Order {

    private int orderId;
    private int TableNo;
    private OrderStatus status;

    public Order(int orderId,
    		int TableNo,
                 OrderStatus status) {

        this.orderId = orderId;
        this.TableNo = TableNo;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getTableNo() {
        return TableNo;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}