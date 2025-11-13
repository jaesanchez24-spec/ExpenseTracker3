package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditExpenseActivity extends AppCompatActivity {

    private EditText editTitle, editAmount, editCategory, editDate;
    private Button btnSave, btnCancel;
    private ExpenseDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        dbHelper = new ExpenseDBHelper(this);

        // Initialize views
        editTitle = findViewById(R.id.editTitle);
        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editDate = findViewById(R.id.editDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Disable typing for date and show DatePicker
        editDate.setFocusable(false);
        editDate.setClickable(true);
        editDate.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveExpense());

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedMonth + 1, selectedDay, selectedYear);
                    editDate.setText(dateStr);
                }, year, month, day);
        dpd.show();
    }

    private void saveExpense() {
        String title = editTitle.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String amountStr = editAmount.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || date.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get logged-in user email
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");

        // Add to database
        dbHelper.addExpense(title, category, date, amount, userEmail);

        // Notify MainActivity to refresh
        setResult(RESULT_OK);
        finish();
    }
}