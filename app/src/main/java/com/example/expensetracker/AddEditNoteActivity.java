package com.example.expensetracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSave;
    private NotesDBHelper dbHelper;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new NotesDBHelper(this);

        // Check if editing an existing note
        if (getIntent().hasExtra("noteId")) {
            noteId = getIntent().getIntExtra("noteId", -1);
            Note note = dbHelper.getNoteById(noteId);
            if (note != null) {
                etTitle.setText(note.getTitle());
                etContent.setText(note.getContent());
            }
        }

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return;
        }

        Note note = new Note(noteId, title, content);

        if (noteId == -1) {
            dbHelper.addNote(note);
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateNote(note);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }

        finish(); // Go back to NotesActivity
    }
}
