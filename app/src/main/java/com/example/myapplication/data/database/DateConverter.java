package com.example.myapplication.data.database;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Date 类型转换器，用于 Room 数据库
 */
public class DateConverter {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
