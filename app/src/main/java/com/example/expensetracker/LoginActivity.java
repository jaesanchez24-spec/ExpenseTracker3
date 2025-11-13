package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // ✅ Check if already logged in
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String loggedEmail = preferences.getString("user_email", null); // ✅ changed key here

        if (loggedEmail != null) {
            // User already logged in → go to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ For now, accept any email/password
            saveUserData(email);

            // Open MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        );
    }

    // ✅ Save the logged-in user's data
    private void saveUserData(String email) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Derive first and last name from email if possible
        String namePart = email.split("@")[0];
        String[] names = namePart.split("\\.");
        String firstName = names.length > 0 ? capitalize(names[0]) : "User";
        String lastName = names.length > 1 ? capitalize(names[1]) : "";

        // Reset stats if a new user logs in
        String currentEmail = prefs.getString("user_email", ""); // ✅ changed key here
        if (!currentEmail.equals(email)) {
            editor.putFloat("totalExpenses", 0);
            editor.putInt("totalTransactions", 0);
            editor.putString("monthlySummary", "");
        }

        // Save user data with key "email" to match SummaryActivity
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("user_email", email); // ✅ changed key here
        editor.apply();
    }


    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}