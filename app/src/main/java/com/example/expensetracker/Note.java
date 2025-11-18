package com.example.expensetracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {
    private int id;
    private String title;
    private String content;
    private long timestamp;
    private long reminderTime; // NEW

    public Note(int id, String title, String content, long timestamp) {
        this(id, title, content, timestamp, 0);
    }

    public Note(int id, String title, String content, long timestamp, long reminderTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.reminderTime = reminderTime;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public long getReminderTime() { return reminderTime; }

    // Format timestamp for display
    public static String formatTime(long timeMillis) {
        if (timeMillis <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}