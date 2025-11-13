package com.example.expensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SummaryActivity extends AppCompatActivity {

    private ExpenseDBHelper dbHelper;
    private TextView totalExpensesText, totalCountText;
    private BarChart barChart;
    private PieChart pieChart;
    private RecyclerView rvCategoryBreakdown;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        dbHelper = new ExpenseDBHelper(this);
        totalExpensesText = findViewById(R.id.totalExpensesText);
        totalCountText = findViewById(R.id.totalCountText);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        rvCategoryBreakdown = findViewById(R.id.rvCategoryBreakdown);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupBottomNav(bottomNavigation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndUpdateData();
    }


    private void loadAndUpdateData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", null);

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Log.e("SummaryActivity", "No logged-in email found.");
            totalExpensesText.setText("₱0.00");
            totalCountText.setText("No chart data available");
            barChart.clear();
            pieChart.clear();
            barChart.invalidate();
            pieChart.invalidate();
            return;
        }

        List<Expense> expenses = dbHelper.getAllExpenses(userEmail);

        if (expenses.isEmpty()) {
            totalExpensesText.setText("₱0.00");
            totalCountText.setText("No chart data available");
            barChart.clear();
            pieChart.clear();
            barChart.invalidate();
            pieChart.invalidate();
            return;
        }

        // ✅ Update summary and charts
        updateSummary(expenses);

        // ✅ Force charts to redraw in case new data was added
        barChart.invalidate();
        pieChart.invalidate();

        applyFadeAnimation();
    }

    private void updateSummary(List<Expense> expenses) {
        double totalAmount = 0;
        Map<String, Double> categoryMap = new HashMap<>();

        for (Expense exp : expenses) {
            totalAmount += exp.getAmount();
            Double current = categoryMap.get(exp.getCategory());
            if (current == null) current = 0.0;
            categoryMap.put(exp.getCategory(), current + exp.getAmount());
        }

        totalExpensesText.setText(String.format(Locale.getDefault(), "₱%.2f", totalAmount));
        totalCountText.setText(String.format(Locale.getDefault(), "%d expenses", expenses.size()));

        setupCharts(categoryMap);
        setupRecyclerView(categoryMap);
    }

    private void setupCharts(Map<String, Double> categoryMap) {
        if (categoryMap.isEmpty()) {
            barChart.clear();
            pieChart.clear();
            return;
        }

        // Bar Chart
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            barEntries.add(new BarEntry(index, entry.getValue().floatValue()));
            categories.add(entry.getKey());
            index++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Expenses by Category");
        barDataSet.setColors(generateDynamicColors(categories.size()));
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Pie Chart
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Category Breakdown");
        pieDataSet.setColors(generateDynamicColors(categoryMap.size()));
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.animateY(1000);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.WHITE);

        Description pieDesc = new Description();
        pieDesc.setText("");
        pieChart.setDescription(pieDesc);
    }

    private void setupRecyclerView(Map<String, Double> categoryMap) {
        rvCategoryBreakdown.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryBreakdown.setAdapter(new CategoryBreakdownAdapter(categoryMap));
    }

    private void setupBottomNav(BottomNavigationView bottomNavigation) {
        bottomNavigation.setSelectedItemId(R.id.nav_summary);
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
                startActivity(new Intent(this, ProfileActivity.class));
            }

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    private void applyFadeAnimation() {
        Animation fade = new AlphaAnimation(0.3f, 1.0f);
        fade.setDuration(600);

        barChart.startAnimation(fade);
        pieChart.startAnimation(fade);
        totalExpensesText.startAnimation(fade);
        totalCountText.startAnimation(fade);
        rvCategoryBreakdown.startAnimation(fade);
    }

    private List<Integer> generateDynamicColors(int size) {
        List<Integer> colors = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            float hue = random.nextInt(360);
            int color = Color.HSVToColor(new float[]{hue, 0.7f, 0.95f});
            colors.add(color);
        }

        return colors;
    }
}