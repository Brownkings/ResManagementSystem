package com.spu.restaurantmanagementsystem.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_PREPARING = "preparing";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_SERVED = "served";

    private String orderId;
    private int tableNumber;
    private String waiterId;
    private String waiterName;
    private Date orderTime;
    private String status;
    private List<OrderItem> orderItems;
    private String specialNotes;
    private double totalAmount;

    // Empty constructor needed for Firebase
    public Order() {
        this.orderItems = new ArrayList<>();
    }

    public Order(String orderId, int tableNumber, String waiterId, String waiterName,
                Date orderTime, String status, String specialNotes) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.waiterId = waiterId;
        this.waiterName = waiterName;
        this.orderTime = orderTime;
        this.status = status;
        this.orderItems = new ArrayList<>();
        this.specialNotes = specialNotes;
        this.totalAmount = 0.0;
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(String waiterId) {
        this.waiterId = waiterId;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        calculateTotalAmount();
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        double total = 0.0;
        for (OrderItem item : orderItems) {
            total += item.getPrice() * item.getQuantity();
        }
        this.totalAmount = total;
    }

    public boolean isReceived() {
        return STATUS_RECEIVED.equals(status);
    }

    public boolean isPreparing() {
        return STATUS_PREPARING.equals(status);
    }

    public boolean isReady() {
        return STATUS_READY.equals(status);
    }

    public boolean isServed() {
        return STATUS_SERVED.equals(status);
    }

    public String getFormattedTotal() {
        return String.format("R%.2f", totalAmount);
    }
}
