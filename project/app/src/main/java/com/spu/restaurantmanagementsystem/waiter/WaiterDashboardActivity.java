package com.spu.restaurantmanagementsystem.waiter;

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

public class WaiterDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView, activeOrdersTextView;
    private CardView newOrderCardView, myOrdersCardView, tableAssignmentCardView;
    
    private SharedPreferences preferences;
    private String userId;
    private String userName;
    private FirebaseAuth auth;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_dashboard);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        ordersRef = FirebaseUtil.getOrdersRef();
        
        // Get shared preferences
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Waiter");
        
        // Check if we have valid user data
        if (userId == null || auth.getCurrentUser() == null) {
            // Not properly logged in, go back to login
            goToLogin();
            return;
        }
        
        // Initialize UI elements
        welcomeTextView = findViewById(R.id.tv_welcome);
        activeOrdersTextView = findViewById(R.id.tv_active_orders);
        newOrderCardView = findViewById(R.id.card_new_order);
        myOrdersCardView = findViewById(R.id.card_my_orders);
        tableAssignmentCardView = findViewById(R.id.card_table_assignment);
        
        // Set welcome message
        welcomeTextView.setText("Welcome, " + userName);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load active orders count
        loadActiveOrders();
    }

    private void setupClickListeners() {
        newOrderCardView.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterDashboardActivity.this, OrderActivity.class);
            intent.putExtra("is_new_order", true);
            startActivity(intent);
        });
        
        myOrdersCardView.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterDashboardActivity.this, OrderActivity.class);
            intent.putExtra("is_new_order", false);
            startActivity(intent);
        });
        
        tableAssignmentCardView.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterDashboardActivity.this, TableAssignmentActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadActiveOrders() {
        ordersRef.orderByChild("waiterId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int activeCount = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            if (order != null && !order.isServed()) {
                                activeCount++;
                            }
                        }
                        
                        activeOrdersTextView.setText("Active Orders: " + activeCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(WaiterDashboardActivity.this, 
                                "Error loading orders: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_waiter, menu);
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
        Intent intent = new Intent(WaiterDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
