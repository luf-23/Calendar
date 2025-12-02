package com.example.myapplication.calendar;

import java.io.Serializable;
import java.util.Date;

public class CalendarEvent implements Serializable {
    private long id;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private String location;
    private boolean hasReminder;
    private int reminderMinutes; // 提前多少分钟提醒
    private String color; // 事件颜色标记
    private int priority; // 优先级：1-低，2-中，3-高

    public CalendarEvent() {
        this.id = -1;
        this.hasReminder = false;
        this.reminderMinutes = 15;
        this.color = "#4CAF50";
        this.priority = 2;
    }

    public CalendarEvent(String title, Date startTime, Date endTime) {
        this();
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
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

    public boolean isHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
