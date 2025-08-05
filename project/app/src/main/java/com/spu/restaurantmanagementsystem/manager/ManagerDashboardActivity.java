package com.spu.restaurantmanagementsystem.manager;

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
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

public class ManagerDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView, menuItemsCountTextView, reservationsCountTextView, staffCountTextView;
    private CardView menuManagementCardView, reservationManagementCardView, inventoryCardView, staffCardView, reportsCardView;
    
    private SharedPreferences preferences;
    private String userId;
    private String userName;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        
        // Get shared preferences
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Manager");
        
        // Check if we have valid user data
        if (userId == null || auth.getCurrentUser() == null) {
            // Not properly logged in, go back to login
            goToLogin();
            return;
        }
        
        // Initialize UI elements
        welcomeTextView = findViewById(R.id.tv_welcome);
        menuItemsCountTextView = findViewById(R.id.tv_menu_items_count);
        reservationsCountTextView = findViewById(R.id.tv_reservations_count);
        staffCountTextView = findViewById(R.id.tv_staff_count);
        menuManagementCardView = findViewById(R.id.card_menu_management);
        reservationManagementCardView = findViewById(R.id.card_reservation_management);
        inventoryCardView = findViewById(R.id.card_inventory);
        staffCardView = findViewById(R.id.card_staff);
        reportsCardView = findViewById(R.id.card_reports);
        
        // Set welcome message
        welcomeTextView.setText("Welcome, " + userName);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load counts
        loadMenuItemsCount();
        loadReservationsCount();
        loadStaffCount();
    }

    private void setupClickListeners() {
        menuManagementCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, MenuManagementActivity.class);
            startActivity(intent);
        });
        
        reservationManagementCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, ReservationManagementActivity.class);
            startActivity(intent);
        });
        
        inventoryCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, InventoryActivity.class);
            startActivity(intent);
        });
        
        staffCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, StaffManagementActivity.class);
            startActivity(intent);
        });
        
        reportsCardView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, ReportsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadMenuItemsCount() {
        DatabaseReference menuItemsRef = FirebaseUtil.getMenuItemsRef();
        menuItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                menuItemsCountTextView.setText("Menu Items: " + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManagerDashboardActivity.this, 
                        "Error loading menu items: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadReservationsCount() {
        DatabaseReference reservationsRef = FirebaseUtil.getReservationsRef();
        reservationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                reservationsCountTextView.setText("Reservations: " + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManagerDashboardActivity.this, 
                        "Error loading reservations: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadStaffCount() {
        DatabaseReference usersRef = FirebaseUtil.getUsersRef();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null && !role.equals(Constants.ROLE_CUSTOMER)) {
                        count++;
                    }
                }
                staffCountTextView.setText("Staff Members: " + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManagerDashboardActivity.this, 
                        "Error loading staff: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
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
        Intent intent = new Intent(ManagerDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
