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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.spu.restaurantmanagementsystem.customer.CustomerDashboardActivity;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLinkTextView;
    private ProgressBar progressBar;
    
    private FirebaseAuth auth;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize Firebase
        auth = FirebaseUtil.getAuth();
        preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        
        // Initialize UI elements
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        confirmPasswordEditText = findViewById(R.id.et_confirm_password);
        registerButton = findViewById(R.id.btn_register);
        loginLinkTextView = findViewById(R.id.tv_login_link);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set up button click listeners
        registerButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
            }
        });
        
        loginLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return false;
        }
        
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
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        
        // Create a new user with the Customer role by default
        // Only customers can register through the app, staff are added by managers
        FirebaseUtil.createNewUser(email, password, name, Constants.ROLE_CUSTOMER, task -> {
            // Hide progress bar
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                // Save user info to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.PREF_USER_ROLE, Constants.ROLE_CUSTOMER);
                editor.putString(Constants.PREF_USER_ID, FirebaseUtil.getCurrentUserId());
                editor.putString(Constants.PREF_USER_NAME, name);
                editor.apply();
                
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                
                // Navigate to customer dashboard
                Intent intent = new Intent(RegisterActivity.this, CustomerDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Handle specific registration errors
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    emailEditText.setError("Email already registered");
                    emailEditText.requestFocus();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
