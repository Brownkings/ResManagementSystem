package com.spu.restaurantmanagementsystem.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    private static FirebaseAuth auth;
    private static FirebaseDatabase database;

    // Initialize Firebase components
    public static void initialize() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true); // Enable offline persistence
        }
    }
    
    // Alias for initialize() method for backward compatibility
    public static void initFirebase() {
        initialize();
    }

    // Get the FirebaseAuth instance
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    // Get the FirebaseDatabase instance
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    // Get the current authenticated user
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    // Get the user ID of the current authenticated user
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get a reference to the users collection
    public static DatabaseReference getUsersRef() {
        return getDatabase().getReference().child("users");
    }

    // Get a reference to a specific user
    public static DatabaseReference getUserRef(String userId) {
        return getUsersRef().child(userId);
    }

    // Get a reference to the menu items collection
    public static DatabaseReference getMenuItemsRef() {
        return getDatabase().getReference().child("menu_items");
    }

    // Get a reference to the orders collection
    public static DatabaseReference getOrdersRef() {
        return getDatabase().getReference().child("orders");
    }

    // Get a reference to the order items collection
    public static DatabaseReference getOrderItemsRef() {
        return getDatabase().getReference().child("order_items");
    }

    // Get a reference to the reservations collection
    public static DatabaseReference getReservationsRef() {
        return getDatabase().getReference().child("reservations");
    }

    // Get a reference to the inventory collection
    public static DatabaseReference getInventoryRef() {
        return getDatabase().getReference().child("inventory");
    }

    // Fetch the current user's data and role
    public static void getCurrentUserData(final OnUserDataCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        getUserRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    callback.onUserDataReceived(user);
                } else {
                    callback.onError("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Create a new user in Firebase Authentication and save user data to Realtime Database
    public static void createNewUser(String email, String password, final String name, final String role, 
                                     final OnCompleteListener onCompleteListener) {
        getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String userId = task.getResult().getUser().getUid();
                        
                        // Create user object using setters to accommodate different User class structures
                        User newUser = new User();
                        newUser.setUserId(userId);
                        newUser.setName(name);
                        newUser.setEmail(email);
                        newUser.setRole(role);
                        newUser.setCreatedAt(new Date()); // Set current date
                        
                        getUserRef(userId).setValue(newUser)
                                .addOnCompleteListener(onCompleteListener);
                    } else {
                        onCompleteListener.onComplete(task);
                    }
                });
    }

    // Generate a new unique key for a database entry
    public static String generateKey(DatabaseReference reference) {
        return reference.push().getKey();
    }

    // Update order status and send notification
    public static void updateOrderStatus(String orderId, String newStatus, final OnCompleteListener onCompleteListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        getOrdersRef().child(orderId).updateChildren(updates)
                .addOnCompleteListener(onCompleteListener);
        
        // In a production app, would send notification based on status change
    }

    // Interface for user data callback
    public interface OnUserDataCallback {
        void onUserDataReceived(User user);
        void onError(String error);
    }
}
