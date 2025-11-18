package com.example.expensetracker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvReminder;
    private Button btnSave, btnSetReminder;
    private NotesDBHelper dbHelper;

    private long reminderTime = 0;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvReminder = findViewById(R.id.tvReminderTime);
        btnSave = findViewById(R.id.btnSave);
        btnSetReminder = findViewById(R.id.btnSetReminder);

        dbHelper = new NotesDBHelper(this);

        // Check if editing an existing note
        noteId = getIntent().getIntExtra("noteId", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }

        btnSetReminder.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void loadNote(int id) {
        Note note = dbHelper.getNoteById(id);
        if (note != null) {
            etTitle.setText(note.getTitle());
            etContent.setText(note.getContent());
            reminderTime = note.getReminderTime();

            if (reminderTime > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(reminderTime);
                tvReminder.setText("Reminder: " +
                        android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", cal));
            } else {
                tvReminder.setText("No reminder set");
            }
        }
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                reminderTime = calendar.getTimeInMillis();
                tvReminder.setText("Reminder: " +
                        android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", calendar));

            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false).show();

        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        long timestamp = System.currentTimeMillis();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        long result;
        if (noteId == -1) {
            // Insert new note
            result = dbHelper.insertNote(title, content, timestamp, reminderTime);
        } else {
            // Update existing note
            // Cancel old reminder first
            Note oldNote = dbHelper.getNoteById(noteId);
            if (oldNote != null && oldNote.getReminderTime() > 0) {
                cancelReminder(noteId, oldNote.getReminderTime());
            }

            result = dbHelper.updateNote(noteId, title, content, timestamp, reminderTime);
        }

        if (result != -1) {
            if (reminderTime > 0) {
                scheduleReminder((int) result, title, content, reminderTime);
            }
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    private void scheduleReminder(int noteId, String title, String content, long reminderMillis) {
        if (!canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            Toast.makeText(this, "Please allow exact alarms in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("noteId", noteId);
        intent.putExtra("noteTitle", title);
        intent.putExtra("noteContent", content);

        int pendingIntentId = (int) (noteId + reminderMillis / 1000); // unique ID
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                );
            }
        }

        Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder(int noteId, long oldReminderMillis) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        int pendingIntentId = (int) (noteId + oldReminderMillis / 1000);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}