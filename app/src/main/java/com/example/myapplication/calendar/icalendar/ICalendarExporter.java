package com.example.myapplication.calendar.icalendar;

import com.example.myapplication.calendar.model.CalendarEvent;
import com.example.myapplication.calendar.model.EventAlarm;
import com.example.myapplication.calendar.model.RecurrenceRule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * ICalendarExporter - iCalendar (ICS) 导出工具
 * 符合 RFC 5545 标准
 */
public class ICalendarExporter {
    
    private static final String CRLF = "\r\n";
    private static final SimpleDateFormat DATE_FORMAT;
    private static final SimpleDateFormat DATETIME_FORMAT;
    
    static {
        DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        DATETIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * 导出单个事件为iCalendar格式
     */
    public static String exportEvent(CalendarEvent event) {
        List<CalendarEvent> events = new ArrayList<>();
        events.add(event);
        return exportEvents(events);
    }
    
    /**
     * 导出多个事件为iCalendar格式
     */
    public static String exportEvents(List<CalendarEvent> events) {
        StringBuilder ics = new StringBuilder();
        
        // 开始VCALENDAR
        ics.append("BEGIN:VCALENDAR").append(CRLF);
        ics.append("VERSION:2.0").append(CRLF);
        ics.append("PRODID:-//MyCalendarApp//Calendar//CN").append(CRLF);
        ics.append("CALSCALE:GREGORIAN").append(CRLF);
        ics.append("METHOD:PUBLISH").append(CRLF);
        
        // 添加每个事件
        for (CalendarEvent event : events) {
            ics.append(exportEventComponent(event));
        }
        
        // 结束VCALENDAR
        ics.append("END:VCALENDAR").append(CRLF);
        
        return ics.toString();
    }
    
    /**
     * 导出VEVENT组件
     */
    private static String exportEventComponent(CalendarEvent event) {
        StringBuilder vevent = new StringBuilder();
        
        vevent.append("BEGIN:VEVENT").append(CRLF);
        
        // UID - 唯一标识符
        vevent.append("UID:").append(event.getUid()).append(CRLF);
        
        // DTSTAMP - 时间戳
        vevent.append("DTSTAMP:").append(formatDateTime(new Date())).append(CRLF);
        
        // CREATED - 创建时间
        if (event.getCreatedTime() != null) {
            vevent.append("CREATED:").append(formatDateTime(event.getCreatedTime())).append(CRLF);
        }
        
        // LAST-MODIFIED - 最后修改时间
        if (event.getLastModified() != null) {
            vevent.append("LAST-MODIFIED:").append(formatDateTime(event.getLastModified())).append(CRLF);
        }
        
        // SUMMARY - 标题
        vevent.append("SUMMARY:").append(escapeText(event.getTitle())).append(CRLF);
        
        // DESCRIPTION - 描述
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            vevent.append("DESCRIPTION:").append(escapeText(event.getDescription())).append(CRLF);
        }
        
        // LOCATION - 地点
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            vevent.append("LOCATION:").append(escapeText(event.getLocation())).append(CRLF);
        }
        
        // DTSTART - 开始时间
        if (event.isAllDay()) {
            vevent.append("DTSTART;VALUE=DATE:").append(formatDate(event.getStartTime())).append(CRLF);
        } else {
            vevent.append("DTSTART:").append(formatDateTime(event.getStartTime())).append(CRLF);
        }
        
        // DTEND - 结束时间
        if (event.isAllDay()) {
            vevent.append("DTEND;VALUE=DATE:").append(formatDate(event.getEndTime())).append(CRLF);
        } else {
            vevent.append("DTEND:").append(formatDateTime(event.getEndTime())).append(CRLF);
        }
        
        // STATUS - 状态
        vevent.append("STATUS:").append(event.getStatus().name()).append(CRLF);
        
        // CLASS - 分类
        vevent.append("CLASS:").append(event.getEventClass().name()).append(CRLF);
        
        // PRIORITY - 优先级
        vevent.append("PRIORITY:").append(event.getPriority()).append(CRLF);
        
        // CATEGORIES - 类别
        if (event.getCategory() != null && !event.getCategory().isEmpty()) {
            vevent.append("CATEGORIES:").append(event.getCategory()).append(CRLF);
        }
        
        // ORGANIZER - 组织者
        if (event.getOrganizer() != null && !event.getOrganizer().isEmpty()) {
            vevent.append("ORGANIZER:").append(event.getOrganizer()).append(CRLF);
        }
        
        // RRULE - 重复规则
        if (event.getRecurrenceRule() != null) {
            vevent.append(event.getRecurrenceRule().toRRuleString()).append(CRLF);
        }
        
        // EXDATE - 例外日期
        if (event.getExceptionDates() != null && !event.getExceptionDates().isEmpty()) {
            vevent.append("EXDATE:");
            for (int i = 0; i < event.getExceptionDates().size(); i++) {
                if (i > 0) vevent.append(",");
                vevent.append(formatDateTime(event.getExceptionDates().get(i)));
            }
            vevent.append(CRLF);
        }
        
        // VALARM - 提醒
        if (event.getAlarms() != null) {
            for (EventAlarm alarm : event.getAlarms()) {
                vevent.append(exportAlarmComponent(alarm));
            }
        }
        
        // COLOR (非标准，但常用)
        if (event.getColor() != null) {
            vevent.append("X-APPLE-CALENDAR-COLOR:").append(event.getColor()).append(CRLF);
        }
        
        vevent.append("END:VEVENT").append(CRLF);
        
        return vevent.toString();
    }
    
    /**
     * 导出VALARM组件
     */
    private static String exportAlarmComponent(EventAlarm alarm) {
        StringBuilder valarm = new StringBuilder();
        
        valarm.append("BEGIN:VALARM").append(CRLF);
        valarm.append("ACTION:").append(alarm.getAction().name()).append(CRLF);
        
        // TRIGGER - 触发时间
        if (alarm.getTriggerType() == EventAlarm.TriggerType.RELATIVE) {
            int minutes = alarm.getMinutesBefore();
            valarm.append("TRIGGER:-PT");
            
            if (minutes >= 1440) {
                // 天
                valarm.append(minutes / 1440).append("D");
            } else if (minutes >= 60) {
                // 小时
                valarm.append(minutes / 60).append("H");
            } else {
                // 分钟
                valarm.append(minutes).append("M");
            }
            valarm.append(CRLF);
        }
        
        // DESCRIPTION
        if (alarm.getDescription() != null && !alarm.getDescription().isEmpty()) {
            valarm.append("DESCRIPTION:").append(escapeText(alarm.getDescription())).append(CRLF);
        }
        
        // REPEAT
        if (alarm.getRepeat() > 0) {
            valarm.append("REPEAT:").append(alarm.getRepeat()).append(CRLF);
            valarm.append("DURATION:PT").append(alarm.getDuration()).append("S").append(CRLF);
        }
        
        valarm.append("END:VALARM").append(CRLF);
        
        return valarm.toString();
    }
    
    /**
     * 格式化日期（DATE格式）
     */
    private static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
    
    /**
     * 格式化日期时间（DATETIME格式）
     */
    private static String formatDateTime(Date date) {
        return DATETIME_FORMAT.format(date);
    }
    
    /**
     * 转义文本（处理特殊字符）
     */
    private static String escapeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace(",", "\\,")
                   .replace(";", "\\;");
    }
    
    /**
     * 折叠长行（RFC 5545要求每行最多75个字符）
     */
    private static String foldLine(String line) {
        if (line.length() <= 75) {
            return line + CRLF;
        }
        
        StringBuilder folded = new StringBuilder();
        int start = 0;
        while (start < line.length()) {
            int end = Math.min(start + 75, line.length());
            folded.append(line, start, end).append(CRLF);
            if (end < line.length()) {
                folded.append(" "); // 续行以空格开头
            }
            start = end;
        }
        return folded.toString();
    }
}
