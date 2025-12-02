package com.example.myapplication.calendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "calendar_reminders";
    private static final String CHANNEL_NAME = "日程提醒";

    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra("event_id", -1);
        String title = intent.getStringExtra("event_title");
        String description = intent.getStringExtra("event_description");
        long startTime = intent.getLongExtra("event_start_time", 0);

        if (eventId == -1 || title == null) {
            return;
        }

        // 创建通知渠道
        createNotificationChannel(context);

        // 创建点击通知时的Intent
        Intent notificationIntent = new Intent(context, EventEditActivity.class);
        notificationIntent.putExtra("event_id", eventId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        String timeStr = CalendarUtils.formatDate(new java.util.Date(startTime), "HH:mm");
        String contentText = timeStr;
        if (description != null && !description.isEmpty()) {
            contentText += " · " + description;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify((int) eventId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历应用的日程提醒通知");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
