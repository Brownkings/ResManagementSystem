package com.spu.restaurantmanagementsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.spu.restaurantmanagementsystem.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase before proceeding
        FirebaseUtil.initialize();
        
        // Set up animations
        setupAnimations();
        
        // Navigate to main activity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
    
    private void setupAnimations() {
        ImageView logoImageView = findViewById(R.id.iv_logo);
        TextView appNameTextView = findViewById(R.id.tv_app_name);
        TextView subtitleTextView = findViewById(R.id.tv_subtitle);
        
        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        
        // Set duration
        fadeIn.setDuration(1000);
        slideUp.setDuration(1000);
        
        // Apply animations
        logoImageView.startAnimation(fadeIn);
        appNameTextView.startAnimation(slideUp);
        subtitleTextView.startAnimation(slideUp);
    }
}
