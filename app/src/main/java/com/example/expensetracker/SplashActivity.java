package com.example.expensetracker;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3500; // 3.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView coin = findViewById(R.id.coinImage);
        ImageView logo = findViewById(R.id.logoImage);
        TextView appName = findViewById(R.id.appName);
        TextView tagline = findViewById(R.id.tagline);

        // Load fade animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Coin drop animation
        ObjectAnimator coinDrop = ObjectAnimator.ofPropertyValuesHolder(
                coin,
                PropertyValuesHolder.ofFloat("translationY", -300f, 100f),
                PropertyValuesHolder.ofFloat("rotation", 0f, 720f)
        );
        coinDrop.setDuration(1200);
        coinDrop.start();

        // Delay wallet and text animations slightly after coin drops
        new Handler().postDelayed(() -> {
            logo.startAnimation(slideDown);
            appName.startAnimation(fadeIn);
            tagline.startAnimation(fadeIn);
        }, 1000);

        // Move to next screen after splash
        new Handler().postDelayed(this::checkLoginStatus, SPLASH_TIME);
    }

    // ✅ Check if user is already logged in
    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);

        Intent intent;
        if (userEmail == null || userEmail.trim().isEmpty()) {
            // No user logged in → go to LoginActivity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            // User already logged in → go to MainActivity
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        // Clear activity stack to prevent back navigation
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
