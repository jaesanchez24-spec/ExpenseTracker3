package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpenseDBHelper extends SQLiteOpenHelper {

    // ============================
    // DATABASE INFO
    // ============================
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 3; // incremented for chart optimization

    // ============================
    // EXPENSES TABLE
    // ============================
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_USER_EMAIL = "user_email";

    // ============================
    // USERS TABLE
    // ============================
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_FIRST_NAME + " TEXT NOT NULL," +
            COL_LAST_NAME + " TEXT NOT NULL," +
            COL_EMAIL + " TEXT NOT NULL UNIQUE," +
            COL_PASSWORD + " TEXT NOT NULL" +
            ");";

    // ============================
    // CONSTRUCTOR
    // ============================
    public ExpenseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ============================
    // CREATE TABLES
    // ============================
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExpenses = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_AMOUNT + " REAL NOT NULL, " +
                COLUMN_USER_EMAIL + " TEXT NOT NULL)";
        db.execSQL(createExpenses);

        db.execSQL(CREATE_USERS_TABLE);

        // Default test user (optional)
        db.execSQL("INSERT INTO " + TABLE_USERS +
                " (first_name, last_name, email, password) VALUES ('Jade', 'Sanchez', 'test@example.com', '1234')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ============================
    // EXPENSE METHODS
    // ============================

    // Add a new expense
    public void addExpense(String title, String category, String date, double amount, String userEmail) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_DATE, date != null ? date : "");
            values.put(COLUMN_AMOUNT, amount);
            values.put(COLUMN_USER_EMAIL, userEmail);
            db.insert(TABLE_EXPENSES, null, values);
        }
    }

    // Get Total Expenses by User

    public double getTotalExpenses(String userEmail) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{userEmail});

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }

    // Get all expenses for a user
    public ArrayList<Expense> getAllExpenses(String userEmail) {
        ArrayList<Expense> expenseList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_EXPENSES,
                     null,
                     COLUMN_USER_EMAIL + "=?",
                     new String[]{userEmail},
                     null, null,
                     COLUMN_ID + " DESC")) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                expenseList.add(new Expense(id, title, category, date, amount));
            }
        }
        System.out.println("✅ getAllExpenses loaded " + expenseList.size() + " records for " + userEmail);
        return expenseList;
    }

    // Count total expenses
    public int getExpenseCount(String userEmail) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT COUNT(*) FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_USER_EMAIL + "=?",
                     new String[]{userEmail})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    // ✅ Get category totals (used for charts)
    public Map<String, Double> getCategoryTotals(String userEmail) {
        Map<String, Double> categoryTotals = new HashMap<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(
                     "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") AS total " +
                             "FROM " + TABLE_EXPENSES +
                             " WHERE " + COLUMN_USER_EMAIL + "=? GROUP BY " + COLUMN_CATEGORY,
                     new String[]{userEmail})) {

            while (cursor.moveToNext()) {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                categoryTotals.put(category, total);
            }
        }
        System.out.println("✅ getCategoryTotals loaded " + categoryTotals.size() + " categories");
        return categoryTotals;
    }
}