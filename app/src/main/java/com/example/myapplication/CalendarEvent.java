package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import java.util.Date;

/**
 * 日程事件模型类
 */
@Entity(tableName = "calendar_events")
@TypeConverters({DateConverter.class, EventTypeConverter.class})
public class CalendarEvent {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "start_time")
    private Date startTime;
    
    @ColumnInfo(name = "end_time")
    private Date endTime;
    
    @ColumnInfo(name = "location")
    private String location;
    
    @ColumnInfo(name = "color")
    private int color;
    
    @ColumnInfo(name = "type")
    private EventType type;
    
    public enum EventType {
        MEETING("会议", "#2196F3"),
        WORK("工作", "#4CAF50"),
        PERSONAL("个人", "#FF9800"),
        IMPORTANT("重要", "#F44336"),
        OTHER("其他", "#9E9E9E");
        
        private String name;
        private String color;
        
        EventType(String name, String color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    // 无参构造函数（Room 需要）
    public CalendarEvent() {
        this.type = EventType.OTHER;
        this.color = android.graphics.Color.parseColor(EventType.OTHER.getColor());
    }
    
    // 便捷构造函数（使用 @Ignore 避免 Room 警告）
    @androidx.room.Ignore
    public CalendarEvent(String title, Date startTime, Date endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = EventType.OTHER;
        this.color = android.graphics.Color.parseColor(EventType.OTHER.getColor());
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public EventType getType() {
        return type;
    }
    
    public void setType(EventType type) {
        this.type = type;
        this.color = android.graphics.Color.parseColor(type.getColor());
    }
    
    /**
     * 获取事件的时长（分钟）
     */
    public long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (endTime.getTime() - startTime.getTime()) / (1000 * 60);
        }
        return 0;
    }
    
    /**
     * 检查事件是否在指定的日期
     */
    public boolean isOnDate(Date date) {
        if (startTime == null || date == null) {
            return false;
        }
        
        java.util.Calendar eventCal = java.util.Calendar.getInstance();
        eventCal.setTime(startTime);
        
        java.util.Calendar dateCal = java.util.Calendar.getInstance();
        dateCal.setTime(date);
        
        return eventCal.get(java.util.Calendar.YEAR) == dateCal.get(java.util.Calendar.YEAR) &&
               eventCal.get(java.util.Calendar.MONTH) == dateCal.get(java.util.Calendar.MONTH) &&
               eventCal.get(java.util.Calendar.DAY_OF_MONTH) == dateCal.get(java.util.Calendar.DAY_OF_MONTH);
    }
}
