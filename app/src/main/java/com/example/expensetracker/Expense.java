package com.example.expensetracker;

public class Expense {
    private int id;
    private String title;
    private String category;
    private String date;
    private double amount;

    public Expense(int id, String title, String category, String date, double amount) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.amount = amount;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
}