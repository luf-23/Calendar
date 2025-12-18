package com.example.myapplication.manager;

import android.content.Context;

import com.example.myapplication.data.model.CalendarEvent;
import com.example.myapplication.data.model.CalendarDay;
import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.data.database.EventDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 日程管理器 - 负责日程的增删改查和持久化（使用 Room 数据库）
 */
public class EventManager {
    
    private Context context;
    private EventDao eventDao;
    
    public EventManager(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.context);
        this.eventDao = database.eventDao();
    }
    
    /**
     * 添加新日程
     */
    public CalendarEvent addEvent(CalendarEvent event) {
        long id = eventDao.insert(event);
        event.setId(id);
        return event;
    }
    
    /**
     * 更新日程
     */
    public boolean updateEvent(CalendarEvent event) {
        try {
            eventDao.update(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 删除日程
     */
    public boolean deleteEvent(long eventId) {
        try {
            eventDao.deleteById(eventId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 删除日程（通过对象）
     */
    public boolean deleteEvent(CalendarEvent event) {
        return deleteEvent(event.getId());
    }
    
    /**
     * 根据ID获取日程
     */
    public CalendarEvent getEvent(long eventId) {
        return eventDao.getEventById(eventId);
    }
    
    /**
     * 根据ID字符串获取日程（兼容旧代码）
     */
    public CalendarEvent getEvent(String eventId) {
        try {
            long id = Long.parseLong(eventId);
            return getEvent(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 获取所有日程
     */
    public List<CalendarEvent> getAllEvents() {
        return eventDao.getAllEvents();
    }
    
    /**
     * 获取指定日期的所有日程
     */
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
    
    /**
     * 获取指定月份的所有日程
     */
    public List<CalendarEvent> getEventsForMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long monthStart = calendar.getTimeInMillis();
        
        calendar.add(Calendar.MONTH, 1);
        long monthEnd = calendar.getTimeInMillis();
        
        return eventDao.getEventsByMonth(monthStart, monthEnd);
    }
    
    /**
     * 搜索日程
     */
    public List<CalendarEvent> searchEvents(String keyword) {
        return eventDao.searchEvents(keyword);
    }
    
    /**
     * 获取日程总数
     */
    public int getEventCount() {
        return eventDao.getEventCount();
    }
    

    
    /**
     * 加载日历天数的事件数量
     * @param calendarDays 日历天数列表
     * @param callback 回调接口，返回日期和事件数量的映射
     */
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
    
    /**
     * 加载指定日期的事件
     * @param date 日期
     * @param callback 回调接口，返回事件列表
     */
    public void loadDayEvents(java.util.Date date, DayEventsCallback callback) {
        new Thread(() -> {
            List<CalendarEvent> events = getEventsForDate(date);
            callback.onEventsLoaded(events);
        }).start();
    }
    
    /**
     * 事件数量回调接口
     */
    public interface EventCountCallback {
        void onCountsLoaded(java.util.Map<java.util.Date, Integer> counts);
    }
    
    /**
     * 日事件回调接口
     */
    public interface DayEventsCallback {
        void onEventsLoaded(List<CalendarEvent> events);
    }
}
