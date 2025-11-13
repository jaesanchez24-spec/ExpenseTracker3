package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ExpenseDBHelper dbHelper;
    private NotesDBHelper notesDBHelper; // ✅ for notes/reminders
    private final ArrayList<Expense> expenseList = new ArrayList<>();
    private ExpenseAdapter expenseAdapter;
    private FloatingActionButton fabAdd;
    private RecyclerView recyclerExpenses;
    private TextView amountText, titleText, monthText, tvTotalTransactions;
    private BottomNavigationView bottomNavigation;
    private LinearLayout stickyNotesContainer; // ✅ for sticky notes
    private String userEmail;

    private final ActivityResultLauncher<Intent> addExpenseLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadExpenses(); // refresh after adding
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load user info from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String firstName = preferences.getString("firstName", null);
        String lastName = preferences.getString("lastName", null);
        userEmail = preferences.getString("user_email", null);

        if (firstName == null || lastName == null || userEmail == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new ExpenseDBHelper(this);
        notesDBHelper = new NotesDBHelper(this); // ✅ initialize notes DB

        // Initialize views
        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        fabAdd = findViewById(R.id.fabAdd);
        amountText = findViewById(R.id.amountText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        titleText = findViewById(R.id.titleText);
        monthText = findViewById(R.id.monthText);
        tvTotalTransactions = findViewById(R.id.tvTransactionCount);
        stickyNotesContainer = findViewById(R.id.stickyNotesContainer); // ✅ initialize container

        // Display greeting with first & last name
        titleText.setText(String.format(Locale.getDefault(), "Hello, %s %s!", firstName, lastName));

        updateMonthText();

        // Setup RecyclerView
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerExpenses.setAdapter(expenseAdapter);

        loadExpenses();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditExpenseActivity.class);
            addExpenseLauncher.launch(intent);
        });

        setupBottomNavigation();
    }

    private void updateMonthText() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = monthFormat.format(new Date());
        monthText.setText(currentMonth + " Summary");
    }

    private void loadExpenses() {
        expenseList.clear();
        expenseList.addAll(dbHelper.getAllExpenses(userEmail));
        expenseAdapter.notifyDataSetChanged();

        double total = dbHelper.getTotalExpenses(userEmail);
        amountText.setText(String.format(Locale.getDefault(), "₱%,.2f", total));

        int totalTransactions = dbHelper.getExpenseCount(userEmail);
        tvTotalTransactions.setText(totalTransactions + " Transactions");

        updateMonthText();
        loadImportantReminders(); // ✅ display sticky notes
    }

    // ---------------- Sticky Notes Logic ----------------
    private void loadImportantReminders() {
        stickyNotesContainer.removeAllViews();

        ArrayList<Note> notes = notesDBHelper.getAllNotes();
        long now = System.currentTimeMillis();

        for (Note note : notes) {
            long reminderTime = note.getReminderTime();

            if (reminderTime > 0) { // only notes with reminders
                // Create sticky note layout
                LinearLayout stickyLayout = new LinearLayout(this);
                stickyLayout.setOrientation(LinearLayout.VERTICAL);
                stickyLayout.setPadding(24, 24, 24, 24);
                stickyLayout.setBackgroundResource(R.drawable.sticky_note_bg);

                // Set color based on urgency
                if (reminderTime < now) {
                    stickyLayout.setBackgroundColor(0xFFFF8A80); // past: red
                } else if (reminderTime - now < 3600_000) { // < 1 hour
                    stickyLayout.setBackgroundColor(0xFFFFF176); // urgent: yellow
                } else {
                    stickyLayout.setBackgroundColor(0xFF80D8FF); // upcoming: blue
                }

                // Title
                TextView tvTitle = new TextView(this);
                tvTitle.setText(note.getTitle());
                tvTitle.setTextSize(14f);
                tvTitle.setTextColor(0xFF000000);
                stickyLayout.addView(tvTitle);

                // Reminder time
                TextView tvTime = new TextView(this);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                tvTime.setText(sdf.format(reminderTime));
                tvTime.setTextSize(12f);
                tvTime.setTextColor(0xFF333333);
                stickyLayout.addView(tvTime);

                // Layout parameters
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(12, 12, 12, 12);
                stickyLayout.setLayoutParams(params);

                stickyNotesContainer.addView(stickyLayout);
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();

            } else if (id == R.id.nav_notes) {
                startActivity(new Intent(this, NotesActivity.class));

            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));

            } else if (id == R.id.nav_summary) {
                startActivity(new Intent(this, SummaryActivity.class));

            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));

            } else {
                return false;
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // refresh when returning
    }
}