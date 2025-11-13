package com.example.expensetracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerNotes;
    private NotesAdapter notesAdapter;
    private ArrayList<Note> noteList;
    private FloatingActionButton fabAddNote;
    private BottomNavigationView bottomNavigation;
    private NotesDBHelper dbHelper;

    // Input fields for new notes
    private EditText editTitle, editContent;
    private Button btnSetReminder, btnSaveNote;
    private long reminderTimeInMillis = 0;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        dbHelper = new NotesDBHelper(this);

        // Initialize views
        recyclerNotes = findViewById(R.id.recyclerNotes);
        fabAddNote = findViewById(R.id.fabAddNote);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        editTitle = findViewById(R.id.editTitle);
        editContent = findViewById(R.id.editContent);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnSaveNote = findViewById(R.id.btnSaveNote);

        // Notification permission and channel
        checkNotificationPermission();
        createNotificationChannel();

        // Setup RecyclerView
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(notesAdapter);

        loadNotes();

        // FAB opens AddEditNoteActivity
        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });

        // Set reminder date/time
        btnSetReminder.setOnClickListener(v -> pickDateTime());
        btnSaveNote.setOnClickListener(v -> saveNote());

        // Highlight Notes in bottom nav
        bottomNavigation.setSelectedItemId(R.id.nav_notes);
        updateBottomNavColors();

        // Bottom navigation click
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_notes) {
                Toast.makeText(this, "Notes selected", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            } else if (id == R.id.nav_summary) {
                startActivity(new Intent(this, SummaryActivity.class));
                finish();
            }
            return true;
        });
    }

    /** -------------------- Notification Permission -------------------- **/
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders will not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** -------------------- Notes & Reminders -------------------- **/
    private void pickDateTime() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                reminderTimeInMillis = calendar.getTimeInMillis();
                                Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveNote() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSaveNote.setEnabled(false);

        // Create note object with reminder timestamp
        Note note = new Note(0, title, content, reminderTimeInMillis);

        // Save note to database
        dbHelper.addNote(note);

        // Schedule reminder if needed
        if (reminderTimeInMillis > 0 &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED)) {
            setReminder(title, reminderTimeInMillis);
        }

        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();

        // Reset input fields and state
        editTitle.setText("");
        editContent.setText("");
        reminderTimeInMillis = 0;
        btnSaveNote.setEnabled(true);

        // Refresh RecyclerView
        loadNotes();
    }
    private void setReminder(String noteTitle, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("note_title", noteTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        Toast.makeText(this, "Reminder scheduled!", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "notes_channel",
                    "Notes Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /** -------------------- RecyclerView & Bottom Nav -------------------- **/
    private void updateBottomNavColors() {
        bottomNavigation.getMenu().findItem(R.id.nav_home)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.green));
        bottomNavigation.getMenu().findItem(R.id.nav_notes)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.blue));
        bottomNavigation.getMenu().findItem(R.id.nav_profile)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.purple));
        bottomNavigation.getMenu().findItem(R.id.nav_settings)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.orange));
        bottomNavigation.getMenu().findItem(R.id.nav_summary)
                .getIcon().setTint(ContextCompat.getColor(this, R.color.red));
    }

    private void loadNotes() {
        noteList.clear();
        noteList.addAll(dbHelper.getAllNotes());
        notesAdapter.notifyDataSetChanged(); // Adapter handles displaying reminder & color


    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
}