package com.spu.restaurantmanagementsystem.customer;

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
import com.spu.restaurantmanagementsystem.models.Reservation;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView welcomeTextView, activeReservationsTextView;
    private CardView reservationCardView, menuCardView, feedbackCardView;
    
    private SharedPreferences preferences;
    private String userId;
    private String userName;
    private FirebaseAuth auth;
    private DatabaseReference reservationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        reservationsRef = FirebaseUtil.getReservationsRef();
        
        // Get shared preferences
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Customer");
        
        // Check if we have valid user data
        if (userId == null || auth.getCurrentUser() == null) {
            // Not properly logged in, go back to login
            goToLogin();
            return;
        }
        
        // Initialize UI elements
        welcomeTextView = findViewById(R.id.tv_welcome);
        activeReservationsTextView = findViewById(R.id.tv_active_reservations);
        reservationCardView = findViewById(R.id.card_reservations);
        menuCardView = findViewById(R.id.card_menu);
        feedbackCardView = findViewById(R.id.card_feedback);
        
        // Set welcome message
        welcomeTextView.setText("Welcome, " + userName);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load active reservations count
        loadActiveReservations();
    }

    private void setupClickListeners() {
        reservationCardView.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, ReservationActivity.class);
            startActivity(intent);
        });
        
        menuCardView.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, MenuBrowseActivity.class);
            startActivity(intent);
        });
        
        feedbackCardView.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadActiveReservations() {
        reservationsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int activeCount = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Reservation reservation = snapshot.getValue(Reservation.class);
                            if (reservation != null && 
                                (reservation.isPending() || reservation.isConfirmed())) {
                                activeCount++;
                            }
                        }
                        
                        activeReservationsTextView.setText("Active Reservations: " + activeCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CustomerDashboardActivity.this, 
                                      "Error loading reservations: " + databaseError.getMessage(), 
                                      Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customer, menu);
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
        Intent intent = new Intent(CustomerDashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
