package com.spu.restaurantmanagementsystem.chef;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spu.restaurantmanagementsystem.LoginActivity;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.models.Order;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

public class ChefDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView, pendingOrdersTextView, preparingOrdersTextView;
    private CardView pendingOrdersCardView, preparingOrdersCardView, readyOrdersCardView;
    
    private SharedPreferences preferences;
    private String userId;
    private String userName;
    private FirebaseAuth auth;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_dashboard);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        ordersRef = FirebaseUtil.getOrdersRef();
        
        // Get shared preferences
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Chef");
        
        // Check if we have valid user data
        if (userId == null || auth.getCurrentUser() == null) {
            // Not properly logged in, go back to login
            goToLogin();
            return;
        }
        
        // Initialize UI elements
        welcomeTextView = findViewById(R.id.tv_welcome);
        pendingOrdersTextView = findViewById(R.id.tv_pending_orders);
        preparingOrdersTextView = findViewById(R.id.tv_preparing_orders);
        pendingOrdersCardView = findViewById(R.id.card_pending_orders);
        preparingOrdersCardView = findViewById(R.id.card_preparing_orders);
        readyOrdersCardView = findViewById(R.id.card_ready_orders);
        
        // Set welcome message
        welcomeTextView.setText("Welcome, " + userName);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load order counts
        loadOrderCounts();
    }

    private void setupClickListeners() {
        pendingOrdersCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ChefDashboardActivity.this, OrderViewActivity.class);
            intent.putExtra("order_status", Order.STATUS_RECEIVED);
            startActivity(intent);
        });
        
        preparingOrdersCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ChefDashboardActivity.this, OrderViewActivity.class);
            intent.putExtra("order_status", Order.STATUS_PREPARING);
            startActivity(intent);
        });
        
        readyOrdersCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ChefDashboardActivity.this, OrderViewActivity.class);
            intent.putExtra("order_status", Order.STATUS_READY);
            startActivity(intent);
        });
    }
    
    private void loadOrderCounts() {
        // Load count of pending orders
        ordersRef.orderByChild("status").equalTo(Order.STATUS_RECEIVED)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = (int) dataSnapshot.getChildrenCount();
                        pendingOrdersTextView.setText("Pending Orders: " + count);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ChefDashboardActivity.this, 
                                "Error loading orders: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
        
        // Load count of orders in preparation
        ordersRef.orderByChild("status").equalTo(Order.STATUS_PREPARING)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = (int) dataSnapshot.getChildrenCount();
                        preparingOrdersTextView.setText("Preparing Orders: " + count);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ChefDashboardActivity.this, 
                                "Error loading orders: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chef, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear preferences and logout
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    
                    auth.signOut();
                    goToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void goToLogin() {
        Intent intent = new Intent(ChefDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
