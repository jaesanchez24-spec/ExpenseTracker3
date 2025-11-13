package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications, switchTheme;
    private SharedPreferences prefs;
    private BottomNavigationView bottomNavigation;
    private MaterialCardView cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        switchNotifications = findViewById(R.id.switchNotifications);
        switchTheme = findViewById(R.id.switchTheme);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        cardLogout = findViewById(R.id.cardLogout);

        // SharedPreferences to save user choices
        prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);

        // === DARK MODE ===
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        switchTheme.setChecked(isDarkMode);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("isDarkMode", isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            Toast.makeText(this,
                    isChecked ? "Dark Mode Enabled 🌙" : "Light Mode Enabled ☀️",
                    Toast.LENGTH_SHORT).show();
        });

        // === NOTIFICATIONS ===
        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", true);
        switchNotifications.setChecked(notificationsEnabled);
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notificationsEnabled", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Notifications Enabled 🔔" : "Notifications Disabled 🔕",
                    Toast.LENGTH_SHORT).show();
        });

        // === LOGOUT CARD ===
        cardLogout.setOnClickListener(v -> logoutUser());

        // === BOTTOM NAVIGATION ===
        setupBottomNavigation();
    }

    private void logoutUser() {
        // Clear user session
        SharedPreferences loginPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        loginPrefs.edit().clear().apply();

        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_settings);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_summary) {
                startActivity(new Intent(this, SummaryActivity.class));
                finish();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            }
            return true;
        });
    }
}