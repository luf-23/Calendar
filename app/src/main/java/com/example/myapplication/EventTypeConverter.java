package com.example.myapplication;

import androidx.room.TypeConverter;

/**
 * EventType 枚举类型转换器，用于 Room 数据库
 */
public class EventTypeConverter {
    
    @TypeConverter
    public static String fromEventType(CalendarEvent.EventType type) {
        return type == null ? null : type.name();
    }
    
    @TypeConverter
    public static CalendarEvent.EventType toEventType(String value) {
        try {
            return value == null ? null : CalendarEvent.EventType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CalendarEvent.EventType.OTHER;
        }
    }
}
