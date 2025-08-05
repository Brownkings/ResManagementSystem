package com.spu.restaurantmanagementsystem.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.spu.restaurantmanagementsystem.R;
import com.spu.restaurantmanagementsystem.utils.Constants;
import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar foodRatingBar, serviceRatingBar, atmosphereRatingBar;
    private EditText feedbackEditText;
    private Button submitButton;
    private ProgressBar progressBar;
    
    private String userId;
    private String userName;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        
        // Set up toolbar
        getSupportActionBar().setTitle("Provide Feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize Firebase
        feedbackRef = FirebaseUtil.getDatabase().getReference().child("feedback");
        
        // Get user info from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getString(Constants.PREF_USER_ID, null);
        userName = preferences.getString(Constants.PREF_USER_NAME, "Anonymous");
        
        // Initialize UI elements
        foodRatingBar = findViewById(R.id.rating_food);
        serviceRatingBar = findViewById(R.id.rating_service);
        atmosphereRatingBar = findViewById(R.id.rating_atmosphere);
        feedbackEditText = findViewById(R.id.et_feedback);
        submitButton = findViewById(R.id.btn_submit_feedback);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set up submit button
        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                submitFeedback();
            }
        });
    }

    private boolean validateInput() {
        if (foodRatingBar.getRating() == 0 || 
            serviceRatingBar.getRating() == 0 || 
            atmosphereRatingBar.getRating() == 0) {
            Toast.makeText(this, "Please rate all categories", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void submitFeedback() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        
        // Prepare feedback data
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("userId", userId);
        feedback.put("userName", userName);
        feedback.put("foodRating", foodRatingBar.getRating());
        feedback.put("serviceRating", serviceRatingBar.getRating());
        feedback.put("atmosphereRating", atmosphereRatingBar.getRating());
        feedback.put("comments", feedbackEditText.getText().toString().trim());
        feedback.put("timestamp", System.currentTimeMillis());
        
        // Calculate average rating
        float avgRating = (foodRatingBar.getRating() + serviceRatingBar.getRating() + atmosphereRatingBar.getRating()) / 3;
        feedback.put("averageRating", avgRating);
        
        // Generate a key for the feedback
        String feedbackId = feedbackRef.push().getKey();
        
        // Save to Firebase
        feedbackRef.child(feedbackId).setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(FeedbackActivity.this, 
                                  "Thank you for your feedback!", 
                                  Toast.LENGTH_SHORT).show();
                    
                    // Reset form
                    foodRatingBar.setRating(0);
                    serviceRatingBar.setRating(0);
                    atmosphereRatingBar.setRating(0);
                    feedbackEditText.setText("");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(FeedbackActivity.this, 
                                  "Failed to submit feedback: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
