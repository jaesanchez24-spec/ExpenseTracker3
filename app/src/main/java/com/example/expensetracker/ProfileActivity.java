package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvTotalExpenses, tvTotalTransactions, tvUserName, tvUserEmail;
    private Button btnLogout;
    private ExpenseDBHelper dbHelper;
    private BottomNavigationView bottomNavigation;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // =======================
        // Initialize components
        // =======================
        dbHelper = new ExpenseDBHelper(this);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // =======================
        // Load user info from UserSession (same key as LoginActivity)
        // =======================
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String firstName = preferences.getString("firstName", "User");
        String lastName = preferences.getString("lastName", "");
        userEmail = preferences.getString("user_email", "example@example.com");

        tvUserName.setText(String.format(Locale.getDefault(), "%s %s", firstName, lastName));
        tvUserEmail.setText(userEmail);

        // =======================
        // Load expense stats
        // =======================
        loadStats();

        // =======================
        // Logout button
        // =======================
        btnLogout.setOnClickListener(v -> {
            // Clear session
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();

            // Open LoginActivity and clear back stack
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // =======================
        // Bottom navigation setup
        // =======================
        setupBottomNavigation();
        updateBottomNavColors();
    }

    private void loadStats() {
        double total = dbHelper.getTotalExpenses(userEmail);
        int count = dbHelper.getExpenseCount(userEmail);

        tvTotalExpenses.setText(String.format(Locale.getDefault(), "₱%,.2f", total));
        tvTotalTransactions.setText(String.valueOf(count));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_summary) {
                startActivity(new Intent(this, SummaryActivity.class));
            } else if (id == R.id.nav_notes) {
                startActivity(new Intent(this, NotesActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_profile) {
                return true; // already here
            }

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    private void updateBottomNavColors() {
        bottomNavigation.getMenu().findItem(R.id.nav_home)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.green));
        bottomNavigation.getMenu().findItem(R.id.nav_notes)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.blue));
        bottomNavigation.getMenu().findItem(R.id.nav_profile)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.purple));
        bottomNavigation.getMenu().findItem(R.id.nav_settings)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.orange));
        bottomNavigation.getMenu().findItem(R.id.nav_summary)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.red));
    }
}