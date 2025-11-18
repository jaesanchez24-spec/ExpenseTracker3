package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notes.db";
    public static final int DATABASE_VERSION = 2; // incremented for reminder feature

    public static final String TABLE_NOTES = "notes";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_TIME = "timestamp";
    public static final String COL_REMINDER = "reminderTime";

    public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                COL_TIME + " INTEGER, " +
                COL_REMINDER + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COL_REMINDER + " INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // Insert a new note
    public long insertNote(String title, String content, long timestamp, long reminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_CONTENT, content);
        cv.put(COL_TIME, timestamp);
        cv.put(COL_REMINDER, reminderTime);
        return db.insert(TABLE_NOTES, null, cv);
    }

    // Update existing note
    public int updateNote(int id, String title, String content, long timestamp, long reminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_CONTENT, content);
        cv.put(COL_TIME, timestamp);
        cv.put(COL_REMINDER, reminderTime);
        return db.update(TABLE_NOTES, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete note
    public int deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTES, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get note by ID
    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Note note = null;
        Cursor c = null;
        try {
            c = db.query(
                    TABLE_NOTES,
                    null,
                    COL_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (c != null && c.moveToFirst()) {
                String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
                String content = c.getString(c.getColumnIndexOrThrow(COL_CONTENT));
                long timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIME));
                long reminderTime = c.getLong(c.getColumnIndexOrThrow(COL_REMINDER));

                note = new Note(id, title, content, timestamp, reminderTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        return note;
    }
}