package com.example.expensetracker;

public class Note {
    private int id;
    private String title;
    private String content;
    private long reminderTime; // for reminder
    private boolean important; // NEW field

    public Note() {}

    // Constructor without reminder
    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.reminderTime = 0;
        this.important = false; // default
    }

    // Constructor with reminder
    public Note(int id, String title, String content, long reminderTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.reminderTime = reminderTime;
        this.important = false; // default
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }

    public boolean isImportant() { return important; } // NEW getter
    public void setImportant(boolean important) { this.important = important; } // NEW setter
}