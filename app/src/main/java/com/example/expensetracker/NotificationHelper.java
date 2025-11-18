package com.example.expensetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "notes_reminder_channel";
    private static final String CHANNEL_NAME = "Notes Reminder";
    private static final String CHANNEL_DESC = "Notifications for scheduled notes";

    private final Context context; // <-- fixed "may be final" warning

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext(); // <-- prevents memory leak
        createNotificationChannel();
    }

    /**
     * Create notification channel (Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                    );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show notification for the reminder
     */
    public void showNotification(int noteId, String title, String content, PendingIntent pendingIntent) {

        // Expandable text para hindi ma-truncate ang long content
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(content);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // <-- safer icon name
                        .setContentTitle(title)
                        .setContentText(content)
                        .setStyle(bigTextStyle)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // Handle missing notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                // User has not yet granted notification permission
                return;
            }
        }

        manager.notify(noteId, builder.build());


    }
}