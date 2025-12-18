package com.example.myapplication.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.receiver.ReminderReceiver;

import java.util.Calendar;

/**
 * 提醒调度器
 * 使用 AlarmManager 来安排定时提醒
 */
public class ReminderScheduler {
    
    private static final String TAG = "ReminderScheduler";
    private Context context;
    private AlarmManager alarmManager;
    
    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    /**
     * 检查是否有精确闹钟权限
     */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
    
    /**
     * 请求精确闹钟权限
     */
    public void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    // 失败静默处理
                }
            }
        }
    }
    
    /**
     * 为事件设置提醒
     */
    public void scheduleReminder(CalendarEvent event) {
        if (!event.isReminderEnabled()) {
            return;
        }
        
        // 计算提醒时间
        long reminderTimeMillis = event.getStartTime().getTime() - 
                                  (event.getReminderMinutesBefore() * 60 * 1000L);
        
        // 如果提醒时间已经过去，则不设置
        if (reminderTimeMillis < System.currentTimeMillis()) {
            return;
        }
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_description", event.getDescription());
        intent.putExtra("event_location", event.getLocation());
        intent.putExtra("event_start_time", event.getStartTime().getTime());
        intent.putExtra("sound_enabled", event.isSoundEnabled());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.getAlarmRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 检查精确闹钟权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!canScheduleExactAlarms()) {
                return;
            }
        }
        
        // 使用精确闹钟 - 确保准时触发
        if (alarmManager != null) {
            try {
                // Android 12 (API 31) 及以上使用 setAlarmClock，这是最高优先级的闹钟
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // 创建显示信息（在锁屏界面显示）
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                            reminderTimeMillis,
                            pendingIntent
                    );
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Android 6.0+ 使用 setExactAndAllowWhileIdle
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    // Android 4.4+ 使用 setExact
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                } else {
                    // 旧版本使用 set
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                }
            } catch (SecurityException e) {
                // 失败静默处理
            }
        }
    }
    
    /**
     * 取消事件的提醒
     */
    public void cancelReminder(CalendarEvent event) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.getAlarmRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
    
    /**
     * 取消提醒（通过请求码）
     */
    public void cancelReminder(int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
