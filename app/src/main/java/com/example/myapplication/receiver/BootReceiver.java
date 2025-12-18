package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.manager.EventManager;
import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.util.ReminderScheduler;

import java.util.List;

/**
 * 开机广播接收器
 * 在设备重启后重新安排所有有效的提醒
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            
            // 在后台线程中重新安排所有提醒
            new Thread(() -> {
                EventManager eventManager = new EventManager(context);
                ReminderScheduler reminderScheduler = new ReminderScheduler(context);
                
                // 获取所有启用提醒的事件
                List<CalendarEvent> allEvents = eventManager.getAllEvents();
                for (CalendarEvent event : allEvents) {
                    if (event.isReminderEnabled()) {
                        // 只安排未来的提醒
                        long reminderTime = event.getStartTime().getTime() - 
                                           (event.getReminderMinutesBefore() * 60 * 1000L);
                        if (reminderTime > System.currentTimeMillis()) {
                            reminderScheduler.scheduleReminder(event);
                        }
                    }
                }
            }).start();
        }
    }
}
