package com.spu.restaurantmanagementsystem.models;

import java.util.Date;

public class User {
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_WAITER = "waiter";
    public static final String ROLE_CHEF = "chef";
    public static final String ROLE_MANAGER = "manager";

    private String userId;
    private String name;
    private String email;
    private String role;
    private Date createdAt;

    // Empty constructor needed for Firebase
    public User() {
    }

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = new Date();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(role);
    }

    public boolean isWaiter() {
        return ROLE_WAITER.equals(role);
    }

    public boolean isChef() {
        return ROLE_CHEF.equals(role);
    }

    public boolean isManager() {
        return ROLE_MANAGER.equals(role);
    }
}
