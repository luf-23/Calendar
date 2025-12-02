package com.example.myapplication.calendar.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ReminderReceiver - 提醒广播接收器
 * 接收 AlarmManager 触发的提醒
 */
public class ReminderReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        
        // 获取事件信息
        long eventId = intent.getLongExtra("event_id", -1);
        String eventTitle = intent.getStringExtra("event_title");
        String eventDesc = intent.getStringExtra("event_desc");
        String eventLocation = intent.getStringExtra("event_location");
        long eventStartTime = intent.getLongExtra("event_start_time", 0);
        
        // 显示提醒通知
        ReminderManager.showReminderNotification(
                context,
                eventId,
                eventTitle,
                eventDesc,
                eventLocation,
                eventStartTime
        );
    }
}
