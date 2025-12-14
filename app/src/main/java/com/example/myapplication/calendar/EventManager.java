package com.example.myapplication.calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 日程管理器 - 负责日程的增删改查和持久化
 */
public class EventManager {
    private static final String PREFS_NAME = "calendar_events";
    private static final String KEY_EVENTS = "events";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private List<CalendarEvent> events;
    
    public EventManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadEvents();
    }
    
    /**
     * 从本地存储加载所有日程
     */
    private void loadEvents() {
        String json = prefs.getString(KEY_EVENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<CalendarEvent>>() {}.getType();
            events = gson.fromJson(json, type);
        } else {
            events = new ArrayList<>();
        }
    }
    
    /**
     * 保存所有日程到本地存储
     */
    private void saveEvents() {
        String json = gson.toJson(events);
        prefs.edit().putString(KEY_EVENTS, json).apply();
    }
    
    /**
     * 添加新日程
     */
    public CalendarEvent addEvent(CalendarEvent event) {
        if (event.getId() == null || event.getId().isEmpty()) {
            event.setId(UUID.randomUUID().toString());
        }
        events.add(event);
        saveEvents();
        
        // 调试输出
        android.util.Log.d("EventManager", "添加日程: " + event.getTitle());
        android.util.Log.d("EventManager", "开始时间: " + event.getStartTime());
        android.util.Log.d("EventManager", "总日程数: " + events.size());
        
        return event;
    }
    
    /**
     * 更新日程
     */
    public boolean updateEvent(CalendarEvent event) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(event.getId())) {
                events.set(i, event);
                saveEvents();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 删除日程
     */
    public boolean deleteEvent(String eventId) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(eventId)) {
                events.remove(i);
                saveEvents();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据ID获取日程
     */
    public CalendarEvent getEvent(String eventId) {
        for (CalendarEvent event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }
    
    /**
     * 获取所有日程
     */
    public List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    /**
     * 获取指定日期的所有日程
     */
    public List<CalendarEvent> getEventsForDate(java.util.Date date) {
        List<CalendarEvent> result = new ArrayList<>();
        android.util.Log.d("EventManager", "查询日期: " + date);
        android.util.Log.d("EventManager", "总事件数: " + events.size());
        
        for (CalendarEvent event : events) {
            android.util.Log.d("EventManager", "检查事件: " + event.getTitle() + ", 开始时间: " + event.getStartTime());
            boolean matches = event.isOnDate(date);
            android.util.Log.d("EventManager", "是否匹配: " + matches);
            if (matches) {
                result.add(event);
            }
        }
        // 按开始时间排序
        result.sort((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()));
        android.util.Log.d("EventManager", "匹配的事件数: " + result.size());
        return result;
    }
    
    /**
     * 清空所有日程（仅用于测试）
     */
    public void clearAllEvents() {
        events.clear();
        saveEvents();
    }
}
