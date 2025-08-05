package com.spu.restaurantmanagementsystem.models;

import java.util.HashMap;
import java.util.Map;

public class MenuItem {
    private String itemId;
    private String name;
    private String description;
    private double price;
    private boolean availability;
    private String category;
    private String imageUrl;
    private Map<String, Integer> ingredientsUsed; // Mapping of inventory item IDs to quantities used

    // Empty constructor needed for Firebase
    public MenuItem() {
        this.ingredientsUsed = new HashMap<>();
    }

    public MenuItem(String itemId, String name, String description, double price, 
                   boolean availability, String category, String imageUrl) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ingredientsUsed = new HashMap<>();
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, Integer> getIngredientsUsed() {
        return ingredientsUsed;
    }

    public void setIngredientsUsed(Map<String, Integer> ingredientsUsed) {
        this.ingredientsUsed = ingredientsUsed;
    }

    public void addIngredient(String inventoryItemId, int quantity) {
        ingredientsUsed.put(inventoryItemId, quantity);
    }

    public void removeIngredient(String inventoryItemId) {
        ingredientsUsed.remove(inventoryItemId);
    }

    public String getFormattedPrice() {
        return String.format("R%.2f", price);
    }
}
