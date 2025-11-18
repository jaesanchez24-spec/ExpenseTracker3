package com.example.expensetracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int noteId = intent.getIntExtra("noteId", -1);
        String title = intent.getStringExtra("noteTitle");
        String content = intent.getStringExtra("noteContent");

        if (noteId == -1 || title == null) return;

        // Android 13+ requires POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return; // Permission denied → don't crash
            }
        }

        // When user taps the notification → open note
        Intent clickIntent = new Intent(context, AddEditNoteActivity.class);
        clickIntent.putExtra("noteId", noteId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                noteId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Use NotificationHelper to show actual notification
        NotificationHelper helper = new NotificationHelper(context);
        helper.showNotification(noteId, title, content, pendingIntent);
    }
}