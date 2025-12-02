package com.example.myapplication.calendar.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * CalendarEvent - 日历事件模型
 * 基于 RFC 5545 (iCalendar) 标准设计
 */
public class CalendarEvent implements Serializable {
    
    // 基本属性
    private long id;
    private String uid;  // RFC 5545: Unique Identifier
    private String title;  // RFC 5545: SUMMARY
    private String description;  // RFC 5545: DESCRIPTION
    private String location;  // RFC 5545: LOCATION
    
    // 时间属性
    private Date startTime;  // RFC 5545: DTSTART
    private Date endTime;  // RFC 5545: DTEND
    private Date createdTime;  // RFC 5545: CREATED
    private Date lastModified;  // RFC 5545: LAST-MODIFIED
    private String timezone;  // RFC 5545: TZID
    
    // 事件属性
    private EventStatus status;  // RFC 5545: STATUS
    private EventClass eventClass;  // RFC 5545: CLASS
    private int priority;  // RFC 5545: PRIORITY (0-9, 0最高)
    private String color;
    private String category;  // RFC 5545: CATEGORIES
    
    // 重复规则 (RFC 5545: RRULE)
    private RecurrenceRule recurrenceRule;
    private List<Date> exceptionDates;  // RFC 5545: EXDATE
    private List<Date> recurrenceDates;  // RFC 5545: RDATE
    
    // 提醒 (RFC 5545: VALARM)
    private List<EventAlarm> alarms;
    
    // 全天事件
    private boolean isAllDay;
    
    // 参与者 (RFC 5545: ATTENDEE)
    private List<Attendee> attendees;
    
    // 组织者 (RFC 5545: ORGANIZER)
    private String organizer;
    
    // 附件 (RFC 5545: ATTACH)
    private List<String> attachments;
    
    // 日历所属
    private long calendarId;  // 所属日历ID（支持多日历）
    
    /**
     * 事件状态
     */
    public enum EventStatus {
        TENTATIVE,  // 待定
        CONFIRMED,  // 已确认
        CANCELLED   // 已取消
    }
    
    /**
     * 事件分类
     */
    public enum EventClass {
        PUBLIC,      // 公开
        PRIVATE,     // 私密
        CONFIDENTIAL // 机密
    }
    
    public CalendarEvent() {
        this.id = -1;
        this.uid = generateUID();
        this.status = EventStatus.CONFIRMED;
        this.eventClass = EventClass.PUBLIC;
        this.priority = 5;  // 默认中等优先级
        this.color = "#4CAF50";
        this.timezone = "Asia/Shanghai";
        this.createdTime = new Date();
        this.lastModified = new Date();
        this.alarms = new ArrayList<>();
        this.attendees = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.exceptionDates = new ArrayList<>();
        this.recurrenceDates = new ArrayList<>();
        this.isAllDay = false;
        this.calendarId = 1;  // 默认日历
    }
    
    public CalendarEvent(String title, Date startTime, Date endTime) {
        this();
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    /**
     * 生成符合RFC 5545的UID
     */
    private String generateUID() {
        return UUID.randomUUID().toString() + "@myapplication.calendar";
    }
    
    /**
     * 判断事件是否在指定日期发生
     */
    public boolean occursOnDate(Date date) {
        if (startTime == null || endTime == null) {
            return false;
        }
        
        // 计算日期范围
        long dateStart = getStartOfDay(date);
        long dateEnd = getEndOfDay(date);
        long eventStart = startTime.getTime();
        long eventEnd = endTime.getTime();
        
        // 检查事件是否与该日期有交集
        return eventStart <= dateEnd && eventEnd >= dateStart;
    }
    
    private long getStartOfDay(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private long getEndOfDay(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
    
    /**
     * 获取事件持续时长（分钟）
     */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return (endTime.getTime() - startTime.getTime()) / (1000 * 60);
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.lastModified = new Date();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.lastModified = new Date();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
        this.lastModified = new Date();
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        this.lastModified = new Date();
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        this.lastModified = new Date();
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public EventStatus getStatus() {
        return status;
    }
    
    public void setStatus(EventStatus status) {
        this.status = status;
        this.lastModified = new Date();
    }
    
    public EventClass getEventClass() {
        return eventClass;
    }
    
    public void setEventClass(EventClass eventClass) {
        this.eventClass = eventClass;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = Math.max(0, Math.min(9, priority));
        this.lastModified = new Date();
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public RecurrenceRule getRecurrenceRule() {
        return recurrenceRule;
    }
    
    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
        this.lastModified = new Date();
    }
    
    public List<Date> getExceptionDates() {
        return exceptionDates;
    }
    
    public void setExceptionDates(List<Date> exceptionDates) {
        this.exceptionDates = exceptionDates;
    }
    
    public List<Date> getRecurrenceDates() {
        return recurrenceDates;
    }
    
    public void setRecurrenceDates(List<Date> recurrenceDates) {
        this.recurrenceDates = recurrenceDates;
    }
    
    public List<EventAlarm> getAlarms() {
        return alarms;
    }
    
    public void setAlarms(List<EventAlarm> alarms) {
        this.alarms = alarms;
    }
    
    public void addAlarm(EventAlarm alarm) {
        if (this.alarms == null) {
            this.alarms = new ArrayList<>();
        }
        this.alarms.add(alarm);
    }
    
    public boolean isAllDay() {
        return isAllDay;
    }
    
    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
        this.lastModified = new Date();
    }
    
    public List<Attendee> getAttendees() {
        return attendees;
    }
    
    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }
    
    public String getOrganizer() {
        return organizer;
    }
    
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
    
    public List<String> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
    
    public long getCalendarId() {
        return calendarId;
    }
    
    public void setCalendarId(long calendarId) {
        this.calendarId = calendarId;
    }
    
    public boolean isRecurring() {
        return recurrenceRule != null;
    }
    
    public boolean hasAlarms() {
        return alarms != null && !alarms.isEmpty();
    }
    
    @NonNull
    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isRecurring=" + isRecurring() +
                '}';
    }
}
