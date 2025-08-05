package com.spu.restaurantmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spu.restaurantmanagementsystem.chef.ChefDashboardActivity;
import com.spu.restaurantmanagementsystem.customer.CustomerDashboardActivity;
import com.spu.restaurantmanagementsystem.manager.ManagerDashboardActivity;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;
import com.spu.restaurantmanagementsystem.waiter.WaiterDashboardActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseUtil.initialize();
        auth = FirebaseUtil.getAuth();
        
        // Get shared preferences
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        checkUserLoginStatus();
    }

    private void checkUserLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser != null) {
            // User is logged in, check their role from SharedPreferences
            String userRole = preferences.getString(Constants.PREF_USER_ROLE, null);
            
            if (userRole != null) {
                // Navigate based on role
                navigateBasedOnRole(userRole);
            } else {
                // Role not in preferences, fetch from database
                FirebaseUtil.getCurrentUserData(new FirebaseUtil.OnUserDataCallback() {
                    @Override
                    public void onUserDataReceived(com.spu.restaurantmanagementsystem.models.User user) {
                        if (user != null) {
                            // Save role to preferences
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(Constants.PREF_USER_ROLE, user.getRole());
                            editor.putString(Constants.PREF_USER_ID, user.getUserId());
                            editor.putString(Constants.PREF_USER_NAME, user.getName());
                            editor.apply();
                            
                            // Navigate based on role
                            navigateBasedOnRole(user.getRole());
                        } else {
                            // Something went wrong, go to login
                            goToLogin();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Error fetching user data, go to login
                        goToLogin();
                    }
                });
            }
        } else {
            // User is not logged in, go to login
            new Handler(Looper.getMainLooper()).postDelayed(this::goToLogin, 500);
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        
        switch (role) {
            case Constants.ROLE_CUSTOMER:
                intent = new Intent(this, CustomerDashboardActivity.class);
                break;
            case Constants.ROLE_WAITER:
                intent = new Intent(this, WaiterDashboardActivity.class);
                break;
            case Constants.ROLE_CHEF:
                intent = new Intent(this, ChefDashboardActivity.class);
                break;
            case Constants.ROLE_MANAGER:
                intent = new Intent(this, ManagerDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, LoginActivity.class);
                break;
        }
        
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
