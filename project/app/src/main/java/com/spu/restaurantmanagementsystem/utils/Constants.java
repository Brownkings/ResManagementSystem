package com.spu.restaurantmanagementsystem.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    // Firebase database nodes
    public static final String NODE_USERS = "users";
    public static final String NODE_RESERVATIONS = "reservations";
    public static final String NODE_MENU_ITEMS = "menu_items";
    public static final String NODE_ORDERS = "orders";
    public static final String NODE_ORDER_ITEMS = "order_items";
    public static final String NODE_INVENTORY = "inventory";

    // User roles
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_WAITER = "waiter";
    public static final String ROLE_CHEF = "chef";
    public static final String ROLE_MANAGER = "manager";

    // Order status
    public static final String ORDER_STATUS_RECEIVED = "received";
    public static final String ORDER_STATUS_PREPARING = "preparing";
    public static final String ORDER_STATUS_READY = "ready";
    public static final String ORDER_STATUS_SERVED = "served";

    // Menu item categories
    public static final String CATEGORY_APPETIZERS = "Appetizers";
    public static final String CATEGORY_MAIN_COURSE = "Main Course";
    public static final String CATEGORY_DESSERTS = "Desserts";
    public static final String CATEGORY_BEVERAGES = "Beverages";
    public static final String CATEGORY_SPECIALS = "Specials";

    public static List<String> getMenuCategories() {
        return new ArrayList<>(Arrays.asList(
                CATEGORY_APPETIZERS,
                CATEGORY_MAIN_COURSE,
                CATEGORY_DESSERTS,
                CATEGORY_BEVERAGES,
                CATEGORY_SPECIALS
        ));
    }

    // Reservation status
    public static final String RESERVATION_STATUS_PENDING = "pending";
    public static final String RESERVATION_STATUS_CONFIRMED = "confirmed";
    public static final String RESERVATION_STATUS_CANCELLED = "cancelled";

    // Table numbers (example)
    public static final int MIN_TABLE_NUMBER = 1;
    public static final int MAX_TABLE_NUMBER = 20;

    public static List<Integer> getTableNumbers() {
        List<Integer> tableNumbers = new ArrayList<>();
        for (int i = MIN_TABLE_NUMBER; i <= MAX_TABLE_NUMBER; i++) {
            tableNumbers.add(i);
        }
        return tableNumbers;
    }

    // Inventory units
    public static final String UNIT_KG = "kg";
    public static final String UNIT_GRAMS = "g";
    public static final String UNIT_LITERS = "L";
    public static final String UNIT_MILLILITERS = "ml";
    public static final String UNIT_PIECES = "pcs";
    public static final String UNIT_PACKAGES = "packages";

    public static List<String> getInventoryUnits() {
        return new ArrayList<>(Arrays.asList(
                UNIT_KG,
                UNIT_GRAMS,
                UNIT_LITERS,
                UNIT_MILLILITERS,
                UNIT_PIECES,
                UNIT_PACKAGES
        ));
    }

    // Time slots for reservations
    public static List<String> getTimeSlots() {
        return new ArrayList<>(Arrays.asList(
                "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
                "20:00", "20:30", "21:00", "21:30", "22:00"
        ));
    }

    // Shared Preferences keys
    public static final String PREFS_NAME = "RestaurantManagementPrefs";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
}
