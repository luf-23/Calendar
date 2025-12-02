package com.example.myapplication.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class EventReminderManager {

    /**
     * 设置事件提醒
     */
    public static void setReminder(Context context, CalendarEvent event) {
        if (!event.isHasReminder()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // 计算提醒时间
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTime(event.getStartTime());
        reminderTime.add(Calendar.MINUTE, -event.getReminderMinutes());

        // 只有未来的时间才设置提醒
        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_description", event.getDescription());
        intent.putExtra("event_start_time", event.getStartTime().getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) event.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 设置闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    /**
     * 取消事件提醒
     */
    public static void cancelReminder(Context context, CalendarEvent event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, EventReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) event.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
