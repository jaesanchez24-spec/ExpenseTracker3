package com.example.expensetracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotes;
    private FloatingActionButton fabAddNote;
    private NotesDBHelper dbHelper;
    private ArrayList<Note> notesList;
    private NotesAdapter adapter;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        initViews();
        setupRecyclerView();
        setupFab();
        setupBottomNavigation();
        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void initViews() {
        recyclerNotes = findViewById(R.id.recyclerNotes);
        fabAddNote = findViewById(R.id.fabAddNote);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        dbHelper = new NotesDBHelper(this);
        notesList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new NotesAdapter(this, notesList, new NotesAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int noteId) {
                deleteNote(noteId);
            }

            @Override
            public void onItemClick(Note note) {
                openEditNote(note.getId());
            }
        });
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddNote.setOnClickListener(v -> openAddEditNote());
    }

    private void openAddEditNote() {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void deleteNote(int noteId) {
        int indexToRemove = -1;
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getId() == noteId) {
                indexToRemove = i;
                break;
            }
        }

        int rows = dbHelper.deleteNote(noteId);
        if (rows > 0 && indexToRemove != -1) {
            notesList.remove(indexToRemove);
            adapter.notifyItemRemoved(indexToRemove);
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditNote(int noteId) {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        intent.putExtra("noteId", noteId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadNotes() {
        notesList.clear();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT * FROM " + NotesDBHelper.TABLE_NOTES +
                             " ORDER BY " + NotesDBHelper.COL_TIME + " DESC",
                     null)) {

            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow(NotesDBHelper.COL_ID));
                    String title = c.getString(c.getColumnIndexOrThrow(NotesDBHelper.COL_TITLE));
                    String content = c.getString(c.getColumnIndexOrThrow(NotesDBHelper.COL_CONTENT));
                    long timestamp = c.getLong(c.getColumnIndexOrThrow(NotesDBHelper.COL_TIME));
                    long reminderTime = c.getLong(c.getColumnIndexOrThrow(NotesDBHelper.COL_REMINDER));

                    notesList.add(new Note(id, title, content, timestamp, reminderTime));
                } while (c.moveToNext());
            }

        } catch (Exception e) {
            Log.e("NotesActivity", "Error loading notes", e);
        }

        adapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_notes);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_notes) {
                Toast.makeText(this, "Note selected", Toast.LENGTH_SHORT).show();

            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));

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
}