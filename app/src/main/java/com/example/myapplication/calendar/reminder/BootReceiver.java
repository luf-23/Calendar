package com.example.myapplication.calendar.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.calendar.database.CalendarDatabase;
import com.example.myapplication.calendar.model.CalendarEvent;

import java.util.List;

/**
 * BootReceiver - 系统启动接收器
 * 系统重启后重新安排所有提醒
 */
public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 重新安排所有提醒
            rescheduleReminders(context);
        }
    }
    
    private void rescheduleReminders(Context context) {
        try {
            CalendarDatabase database = new CalendarDatabase(context);
            List<CalendarEvent> events = database.getAllEvents();
            
            ReminderManager reminderManager = new ReminderManager(context);
            reminderManager.rescheduleAllReminders(events);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
