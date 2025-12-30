package com.example.myapplication.manager;

import android.content.Context;
import android.net.Uri;

import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.data.model.CalendarDay;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.database.EventDao;
import com.example.myapplication.util.IcsExportImportHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//日程管理器 - 负责日程的增删改查和持久化（使用 Room 数据库）
public class EventManager {
    
    private Context context;
    private EventDao eventDao;
    
    public EventManager(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.context);
        this.eventDao = database.eventDao();
    }

    public CalendarEvent addEvent(CalendarEvent event) {
        long id = eventDao.insert(event);
        event.setId(id);
        return event;
    }
    

    public boolean updateEvent(CalendarEvent event) {
        try {
            eventDao.update(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    

    public boolean deleteEvent(long eventId) {
        try {
            eventDao.deleteById(eventId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    

    public boolean deleteEvent(CalendarEvent event) {
        return deleteEvent(event.getId());
    }
    

    public CalendarEvent getEvent(long eventId) {
        return eventDao.getEventById(eventId);
    }
    

    public CalendarEvent getEvent(String eventId) {
        try {
            long id = Long.parseLong(eventId);
            return getEvent(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    

    public List<CalendarEvent> getAllEvents() {
        return eventDao.getAllEvents();
    }
    

    public List<CalendarEvent> getEventsForDate(java.util.Date date) {
        // 计算当天的开始和结束时间戳
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();
        
        return eventDao.getEventsByDate(startOfDay, endOfDay);
    }
    

    public List<CalendarEvent> getEventsForMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long monthStart = calendar.getTimeInMillis();
        
        calendar.add(Calendar.MONTH, 1);
        long monthEnd = calendar.getTimeInMillis();
        
        return eventDao.getEventsByMonth(monthStart, monthEnd);
    }
    

    public List<CalendarEvent> searchEvents(String keyword) {
        return eventDao.searchEvents(keyword);
    }
    

    public int getEventCount() {
        return eventDao.getEventCount();
    }
    

    

    public void loadEventCounts(List<CalendarDay> calendarDays, EventCountCallback callback) {
        new Thread(() -> {
            java.util.Map<java.util.Date, Integer> counts = new java.util.HashMap<>();
            for (CalendarDay day : calendarDays) {
                List<CalendarEvent> events = getEventsForDate(day.getDate());
                if (!events.isEmpty()) {
                    counts.put(day.getDate(), events.size());
                }
            }
            callback.onCountsLoaded(counts);
        }).start();
    }
    

    public void loadDayEvents(java.util.Date date, DayEventsCallback callback) {
        new Thread(() -> {
            List<CalendarEvent> events = getEventsForDate(date);
            callback.onEventsLoaded(events);
        }).start();
    }
    

    public interface EventCountCallback {
        void onCountsLoaded(java.util.Map<java.util.Date, Integer> counts);
    }
    

    public interface DayEventsCallback {
        void onEventsLoaded(List<CalendarEvent> events);
    }
    
    /**
     * 导出所有事件到 ICS 文件
     * 
     * @param uri 目标文件 Uri
     * @param callback 回调接口
     */
    public void exportEventsToIcs(Uri uri, ExportCallback callback) {
        new Thread(() -> {
            try {
                List<CalendarEvent> events = getAllEvents();
                boolean success = IcsExportImportHelper.exportToIcs(context, events, uri);
                callback.onExportComplete(success, events.size());
            } catch (Exception e) {
                e.printStackTrace();
                callback.onExportComplete(false, 0);
            }
        }).start();
    }
    
    /**
     * 从 ICS 文件导入事件
     * 
     * @param uri 源文件 Uri
     * @param callback 回调接口
     */
    public void importEventsFromIcs(Uri uri, ImportCallback callback) {
        new Thread(() -> {
            try {
                List<CalendarEvent> events = IcsExportImportHelper.importFromIcs(context, uri);
                
                if (events.isEmpty()) {
                    callback.onImportComplete(false, 0);
                    return;
                }
                
                // 批量插入事件
                int successCount = 0;
                for (CalendarEvent event : events) {
                    try {
                        addEvent(event);
                        successCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 继续导入其他事件
                    }
                }
                
                callback.onImportComplete(successCount > 0, successCount);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onImportComplete(false, 0);
            }
        }).start();
    }
    
    /**
     * 导出回调接口
     */
    public interface ExportCallback {
        void onExportComplete(boolean success, int eventCount);
    }
    
    /**
     * 导入回调接口
     */
    public interface ImportCallback {
        void onImportComplete(boolean success, int eventCount);
    }
}
