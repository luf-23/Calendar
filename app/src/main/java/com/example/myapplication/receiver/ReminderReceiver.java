package com.example.myapplication.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.ui.activity.AlarmActivity;
import com.example.myapplication.ui.activity.EventEditActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//接收 AlarmManager 触发的提醒，显示通知和响铃
public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "calendar_reminder_channel";
    private static final String CHANNEL_NAME = "日程提醒";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        long eventId = intent.getLongExtra("event_id", -1);
        String title = intent.getStringExtra("event_title");
        String description = intent.getStringExtra("event_description");
        String location = intent.getStringExtra("event_location");
        long startTime = intent.getLongExtra("event_start_time", 0);
        boolean soundEnabled = intent.getBooleanExtra("sound_enabled", false);
        

        // 创建通知渠道
        createNotificationChannel(context);
        
        // 显示通知
        showNotification(context, eventId, title, description, location, startTime);
        
        // 如果开启了响铃，启动响铃Activity
        if (soundEnabled) {
            try {
                Intent alarmIntent = new Intent(context, AlarmActivity.class);
                alarmIntent.putExtra("event_id", eventId);
                alarmIntent.putExtra("event_title", title);
                alarmIntent.putExtra("event_description", description);
                alarmIntent.putExtra("event_location", location);
                alarmIntent.putExtra("event_start_time", startTime);
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(alarmIntent);
            } catch (Exception e) {
                // 失败静默处理
            }
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日程提醒通知");
            channel.enableVibration(true);
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void showNotification(Context context, long eventId, String title, 
                                  String description, String location, long startTime) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return;
        }
        
        // 构建通知内容
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        String timeText = timeFormat.format(new Date(startTime));
        
        StringBuilder contentText = new StringBuilder();
        contentText.append("时间: ").append(timeText);
        if (location != null && !location.isEmpty()) {
            contentText.append("\n地点: ").append(location);
        }
        if (description != null && !description.isEmpty()) {
            contentText.append("\n").append(description);
        }
        
        // 点击通知打开日程详情
        Intent intent = new Intent(context, EventEditActivity.class);
        intent.putExtra("event_id", eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle(title)
                .setContentText(contentText.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});
        
        try {
            notificationManager.notify((int) eventId, builder.build());
        } catch (Exception e) {
            // 失败静默处理
        }
    }
}
