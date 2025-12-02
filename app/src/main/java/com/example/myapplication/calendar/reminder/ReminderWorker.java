package com.example.myapplication.calendar.reminder;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * ReminderWorker - 提醒工作器
 * 使用 WorkManager 执行后台提醒任务
 */
public class ReminderWorker extends Worker {
    
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            // 获取事件信息
            long eventId = getInputData().getLong("event_id", -1);
            String eventTitle = getInputData().getString("event_title");
            String eventDesc = getInputData().getString("event_desc");
            String eventLocation = getInputData().getString("event_location");
            long eventStartTime = getInputData().getLong("event_start_time", 0);
            
            // 显示提醒通知
            ReminderManager.showReminderNotification(
                    getApplicationContext(),
                    eventId,
                    eventTitle,
                    eventDesc,
                    eventLocation,
                    eventStartTime
            );
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
