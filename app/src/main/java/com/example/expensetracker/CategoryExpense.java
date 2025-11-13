package com.example.expensetracker;

public class CategoryExpense {
    private String category;
    private float total;

    public CategoryExpense(String category, float total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public float getTotal() {
        return total;
    }
}