package com.spu.restaurantmanagementsystem.models;

import java.util.Date;

public class InventoryItem {
    private String itemId;
    private String itemName;
    private int quantityInStock;
    private int reorderThreshold;
    private Date lastUpdated;
    private String unit; // e.g., kg, liters, pieces
    private double unitCost;

    // Empty constructor needed for Firebase
    public InventoryItem() {
    }

    public InventoryItem(String itemId, String itemName, int quantityInStock, 
                        int reorderThreshold, Date lastUpdated, 
                        String unit, double unitCost) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantityInStock = quantityInStock;
        this.reorderThreshold = reorderThreshold;
        this.lastUpdated = lastUpdated;
        this.unit = unit;
        this.unitCost = unitCost;
    }

    // Getters and setters
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

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
        this.lastUpdated = new Date();
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public boolean isLowStock() {
        return quantityInStock <= reorderThreshold;
    }

    public String getStockStatus() {
        if (isLowStock()) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }

    public String getFormattedQuantity() {
        return quantityInStock + " " + unit;
    }

    public String getFormattedCost() {
        return String.format("R%.2f per %s", unitCost, unit);
    }

    public double getTotalValue() {
        return quantityInStock * unitCost;
    }

    public String getFormattedValue() {
        return String.format("R%.2f", getTotalValue());
    }
}
