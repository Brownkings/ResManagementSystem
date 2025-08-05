package com.spu.restaurantmanagementsystem.models;

public class OrderItem {
    private String orderItemId;
    private String orderId;
    private String itemId;
    private String itemName;
    private int quantity;
    private double price;
    private String notes;

    // Empty constructor needed for Firebase
    public OrderItem() {
    }

    public OrderItem(String orderItemId, String orderId, String itemId, String itemName, 
                    int quantity, double price, String notes) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.notes = notes;
    }

    // Getters and setters
    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getSubtotal() {
        return quantity * price;
    }

    public String getFormattedSubtotal() {
        return String.format("R%.2f", getSubtotal());
    }

    public String getFormattedPrice() {
        return String.format("R%.2f", price);
    }
}
