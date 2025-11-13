package com.example.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NotesDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notesDB";
    private static final int DATABASE_VERSION = 2; // incremented because we added a column
    private static final String TABLE_NAME = "notes";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_REMINDER = "reminder_time"; // new column

    public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_TITLE + " TEXT," +
                COL_CONTENT + " TEXT," +
                COL_REMINDER + " INTEGER DEFAULT 0)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop and recreate table (you can do proper migration if needed)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, note.getTitle());
        cv.put(COL_CONTENT, note.getContent());
        cv.put(COL_REMINDER, note.getReminderTime()); // store reminder time
        return db.insert(TABLE_NAME, null, cv);
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, note.getTitle());
        cv.put(COL_CONTENT, note.getContent());
        cv.put(COL_REMINDER, note.getReminderTime());
        return db.update(TABLE_NAME, cv, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    public int deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_REMINDER))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});
        if (cursor != null && cursor.moveToFirst()) {
            Note note = new Note(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_REMINDER))
            );
            cursor.close();
            return note;
        }
        return null;
    }
}