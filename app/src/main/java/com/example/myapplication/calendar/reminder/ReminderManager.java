package com.example.myapplication.calendar.reminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.R;
import com.example.myapplication.calendar.model.CalendarEvent;
import com.example.myapplication.calendar.model.EventAlarm;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ReminderManager - 提醒管理器
 * 使用 AlarmManager 和 WorkManager 实现可靠的提醒功能
 */
public class ReminderManager {
    
    private static final String CHANNEL_ID = "calendar_reminders";
    private static final String CHANNEL_NAME = "日历提醒";
    
    private final Context context;
    private final AlarmManager alarmManager;
    private final WorkManager workManager;
    
    public ReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.workManager = WorkManager.getInstance(context);
        createNotificationChannel();
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历事件提醒通知");
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * 为事件设置所有提醒
     */
    public void scheduleRemindersForEvent(CalendarEvent event) {
        if (event == null || event.getAlarms() == null || event.getAlarms().isEmpty()) {
            return;
        }
        
        // 取消旧提醒
        cancelRemindersForEvent(event.getId());
        
        // 设置新提醒
        for (EventAlarm alarm : event.getAlarms()) {
            scheduleReminder(event, alarm);
        }
    }
    
    /**
     * 设置单个提醒
     */
    private void scheduleReminder(CalendarEvent event, EventAlarm alarm) {
        if (event.getStartTime() == null) {
            return;
        }
        
        // 计算提醒时间
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTime(event.getStartTime());
        reminderTime.add(Calendar.MINUTE, -alarm.getMinutesBefore());
        
        long reminderTimeMillis = reminderTime.getTimeInMillis();
        long currentTime = System.currentTimeMillis();
        
        // 如果提醒时间已过，不设置
        if (reminderTimeMillis <= currentTime) {
            return;
        }
        
        // 使用 WorkManager 设置延迟任务（更可靠）
        long delayMillis = reminderTimeMillis - currentTime;
        
        Data inputData = new Data.Builder()
                .putLong("event_id", event.getId())
                .putLong("alarm_id", alarm.getId())
                .putString("event_title", event.getTitle())
                .putString("event_desc", event.getDescription())
                .putString("event_location", event.getLocation())
                .putLong("event_start_time", event.getStartTime().getTime())
                .build();
        
        OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("event_" + event.getId())
                .addTag("alarm_" + alarm.getId())
                .build();
        
        workManager.enqueue(reminderWork);
        
        // 备用：使用 AlarmManager（精确提醒）
        scheduleExactAlarm(event, alarm, reminderTimeMillis);
    }
    
    /**
     * 使用 AlarmManager 设置精确提醒
     */
    private void scheduleExactAlarm(CalendarEvent event, EventAlarm alarm, long triggerTime) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_desc", event.getDescription());
        intent.putExtra("event_location", event.getLocation());
        intent.putExtra("event_start_time", event.getStartTime().getTime());
        
        int requestCode = (int) (event.getId() * 1000 + alarm.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 设置精确闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }
    
    /**
     * 取消事件的所有提醒
     */
    public void cancelRemindersForEvent(long eventId) {
        // 取消 WorkManager 任务
        workManager.cancelAllWorkByTag("event_" + eventId);
        
        // 取消 AlarmManager 提醒（需要遍历所有可能的alarm_id）
        // 简化处理：使用固定范围
        for (int i = 0; i < 10; i++) {
            cancelAlarm(eventId, i);
        }
    }
    
    /**
     * 取消特定的闹钟
     */
    private void cancelAlarm(long eventId, long alarmId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        int requestCode = (int) (eventId * 1000 + alarmId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
    
    /**
     * 重新安排所有提醒（系统重启后调用）
     */
    public void rescheduleAllReminders(List<CalendarEvent> events) {
        for (CalendarEvent event : events) {
            scheduleRemindersForEvent(event);
        }
    }
    
    /**
     * 显示提醒通知
     */
    public static void showReminderNotification(
            Context context,
            long eventId,
            String title,
            String description,
            String location,
            long startTime) {
        
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return;
        }
        
        // 构建通知内容
        StringBuilder content = new StringBuilder();
        if (description != null && !description.isEmpty()) {
            content.append(description);
        }
        if (location != null && !location.isEmpty()) {
            if (content.length() > 0) {
                content.append("\n");
            }
            content.append("地点: ").append(location);
        }
        
        // 格式化时间
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);
        String timeStr = String.format("%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
        
        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(timeStr + " - " + (content.length() > 0 ? content : "事件提醒"))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content.length() > 0 ? content : "事件即将开始"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        // 点击通知打开事件详情
        Intent intent = new Intent(context, com.example.myapplication.calendar.CalendarActivity.class);
        intent.putExtra("event_id", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                (int) eventId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(contentIntent);
        
        notificationManager.notify((int) eventId, builder.build());
    }
}
