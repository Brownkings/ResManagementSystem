package com.spu.restaurantmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.spu.restaurantmanagementsystem.chef.ChefDashboardActivity;
import com.spu.restaurantmanagementsystem.customer.CustomerDashboardActivity;
import com.spu.restaurantmanagementsystem.manager.ManagerDashboardActivity;
import com.spu.restaurantmanagementsystem.models.User;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;
import com.spu.restaurantmanagementsystem.waiter.WaiterDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLinkTextView;
    private ProgressBar progressBar;
    
    private FirebaseAuth auth;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        
        // Initialize UI elements
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        registerLinkTextView = findViewById(R.id.tv_register_link);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set up button click listeners
        loginButton.setOnClickListener(v -> {
            if (validateInput()) {
                loginUser();
            }
        });
        
        registerLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        
        // Attempt login
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Get user role from database
                    FirebaseUtil.getCurrentUserData(new FirebaseUtil.OnUserDataCallback() {
                        @Override
                        public void onUserDataReceived(User user) {
                            // Hide progress bar
                            progressBar.setVisibility(View.GONE);
                            
                            if (user != null) {
                                // Save user info to SharedPreferences
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(Constants.PREF_USER_ROLE, user.getRole());
                                editor.putString(Constants.PREF_USER_ID, user.getUserId());
                                editor.putString(Constants.PREF_USER_NAME, user.getName());
                                editor.apply();
                                
                                // Navigate based on role
                                navigateBasedOnRole(user.getRole());
                            } else {
                                Toast.makeText(LoginActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            // Hide progress bar
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);
                    
                    // Handle specific errors
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        emailEditText.setError("Email not registered");
                        emailEditText.requestFocus();
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        passwordEditText.setError("Invalid password");
                        passwordEditText.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show();
                return;
        }
        
        startActivity(intent);
        finish();
    }
}
