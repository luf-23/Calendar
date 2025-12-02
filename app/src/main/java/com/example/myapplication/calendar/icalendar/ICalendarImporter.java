package com.example.myapplication.calendar.icalendar;

import com.example.myapplication.calendar.model.CalendarEvent;
import com.example.myapplication.calendar.model.EventAlarm;
import com.example.myapplication.calendar.model.RecurrenceRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ICalendarImporter - iCalendar (ICS) 导入工具
 * 符合 RFC 5545 标准
 */
public class ICalendarImporter {
    
    private static final SimpleDateFormat DATE_FORMAT;
    private static final SimpleDateFormat DATETIME_FORMAT;
    
    static {
        DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        DATETIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * 从ICS字符串导入事件
     */
    public static List<CalendarEvent> importFromICS(String icsContent) {
        List<CalendarEvent> events = new ArrayList<>();
        
        // 查找所有VEVENT组件
        Pattern veventPattern = Pattern.compile(
                "BEGIN:VEVENT.*?END:VEVENT",
                Pattern.DOTALL
        );
        Matcher matcher = veventPattern.matcher(icsContent);
        
        while (matcher.find()) {
            String veventContent = matcher.group();
            CalendarEvent event = parseVEvent(veventContent);
            if (event != null) {
                events.add(event);
            }
        }
        
        return events;
    }
    
    /**
     * 解析VEVENT组件
     */
    private static CalendarEvent parseVEvent(String veventContent) {
        CalendarEvent event = new CalendarEvent();
        List<EventAlarm> alarms = new ArrayList<>();
        
        // 按行分割（处理折叠行）
        String[] lines = unfoldLines(veventContent).split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("BEGIN:") || line.startsWith("END:")) {
                continue;
            }
            
            // 查找VALARM
            if (line.contains("BEGIN:VALARM")) {
                Pattern valarmPattern = Pattern.compile(
                        "BEGIN:VALARM.*?END:VALARM",
                        Pattern.DOTALL
                );
                Matcher alarmMatcher = valarmPattern.matcher(veventContent);
                while (alarmMatcher.find()) {
                    EventAlarm alarm = parseVAlarm(alarmMatcher.group());
                    if (alarm != null) {
                        alarms.add(alarm);
                    }
                }
                continue;
            }
            
            // 解析属性
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                continue;
            }
            
            String property = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1);
            
            // 处理属性参数（如DTSTART;VALUE=DATE）
            String[] propertyParts = property.split(";");
            String propertyName = propertyParts[0];
            
            switch (propertyName) {
                case "UID":
                    event.setUid(value);
                    break;
                    
                case "SUMMARY":
                    event.setTitle(unescapeText(value));
                    break;
                    
                case "DESCRIPTION":
                    event.setDescription(unescapeText(value));
                    break;
                    
                case "LOCATION":
                    event.setLocation(unescapeText(value));
                    break;
                    
                case "DTSTART":
                    Date startTime = parseDateTime(value, property.contains("VALUE=DATE"));
                    if (startTime != null) {
                        event.setStartTime(startTime);
                        event.setAllDay(property.contains("VALUE=DATE"));
                    }
                    break;
                    
                case "DTEND":
                    Date endTime = parseDateTime(value, property.contains("VALUE=DATE"));
                    if (endTime != null) {
                        event.setEndTime(endTime);
                    }
                    break;
                    
                case "CREATED":
                    Date created = parseDateTime(value, false);
                    if (created != null) {
                        event.setCreatedTime(created);
                    }
                    break;
                    
                case "LAST-MODIFIED":
                    Date modified = parseDateTime(value, false);
                    if (modified != null) {
                        event.setLastModified(modified);
                    }
                    break;
                    
                case "STATUS":
                    try {
                        event.setStatus(CalendarEvent.EventStatus.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        // 忽略无效状态
                    }
                    break;
                    
                case "CLASS":
                    try {
                        event.setEventClass(CalendarEvent.EventClass.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        // 忽略无效分类
                    }
                    break;
                    
                case "PRIORITY":
                    try {
                        event.setPriority(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        // 忽略无效优先级
                    }
                    break;
                    
                case "CATEGORIES":
                    event.setCategory(value);
                    break;
                    
                case "ORGANIZER":
                    event.setOrganizer(value);
                    break;
                    
                case "RRULE":
                    RecurrenceRule rrule = RecurrenceRule.fromRRuleString("RRULE:" + value);
                    event.setRecurrenceRule(rrule);
                    break;
                    
                case "EXDATE":
                    List<Date> exDates = parseMultipleDates(value);
                    event.setExceptionDates(exDates);
                    break;
                    
                case "X-APPLE-CALENDAR-COLOR":
                case "COLOR":
                    event.setColor(value);
                    break;
            }
        }
        
        // 设置提醒
        if (!alarms.isEmpty()) {
            event.setAlarms(alarms);
        }
        
        // 验证必需字段
        if (event.getTitle() == null || event.getStartTime() == null || event.getEndTime() == null) {
            return null;
        }
        
        return event;
    }
    
    /**
     * 解析VALARM组件
     */
    private static EventAlarm parseVAlarm(String valarmContent) {
        EventAlarm alarm = new EventAlarm();
        
        String[] lines = valarmContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("BEGIN:") || line.startsWith("END:")) {
                continue;
            }
            
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                continue;
            }
            
            String property = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1);
            
            switch (property) {
                case "ACTION":
                    try {
                        alarm.setAction(EventAlarm.AlarmAction.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        // 默认DISPLAY
                    }
                    break;
                    
                case "TRIGGER":
                    int minutes = parseTrigger(value);
                    alarm.setMinutesBefore(minutes);
                    break;
                    
                case "DESCRIPTION":
                    alarm.setDescription(unescapeText(value));
                    break;
                    
                case "REPEAT":
                    try {
                        alarm.setRepeat(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                    break;
                    
                case "DURATION":
                    int duration = parseDuration(value);
                    alarm.setDuration(duration);
                    break;
            }
        }
        
        return alarm;
    }
    
    /**
     * 解析TRIGGER值（如：-PT15M）
     */
    private static int parseTrigger(String trigger) {
        if (trigger.startsWith("-PT") || trigger.startsWith("PT")) {
            trigger = trigger.replace("-", "").replace("PT", "");
            
            // 解析天、小时、分钟
            Pattern pattern = Pattern.compile("(\\d+)([DHMS])");
            Matcher matcher = pattern.matcher(trigger);
            
            int totalMinutes = 0;
            while (matcher.find()) {
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                
                switch (unit) {
                    case "D":
                        totalMinutes += value * 24 * 60;
                        break;
                    case "H":
                        totalMinutes += value * 60;
                        break;
                    case "M":
                        totalMinutes += value;
                        break;
                    case "S":
                        totalMinutes += value / 60;
                        break;
                }
            }
            
            return totalMinutes;
        }
        
        return 15; // 默认15分钟
    }
    
    /**
     * 解析DURATION值
     */
    private static int parseDuration(String duration) {
        if (duration.startsWith("PT")) {
            duration = duration.replace("PT", "");
            
            Pattern pattern = Pattern.compile("(\\d+)([HMS])");
            Matcher matcher = pattern.matcher(duration);
            
            int totalSeconds = 0;
            while (matcher.find()) {
                int value = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                
                switch (unit) {
                    case "H":
                        totalSeconds += value * 3600;
                        break;
                    case "M":
                        totalSeconds += value * 60;
                        break;
                    case "S":
                        totalSeconds += value;
                        break;
                }
            }
            
            return totalSeconds;
        }
        
        return 0;
    }
    
    /**
     * 解析日期时间
     */
    private static Date parseDateTime(String dateTimeStr, boolean isDateOnly) {
        try {
            if (isDateOnly) {
                return DATE_FORMAT.parse(dateTimeStr);
            } else {
                return DATETIME_FORMAT.parse(dateTimeStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 解析多个日期（用逗号分隔）
     */
    private static List<Date> parseMultipleDates(String datesStr) {
        List<Date> dates = new ArrayList<>();
        String[] dateStrs = datesStr.split(",");
        
        for (String dateStr : dateStrs) {
            Date date = parseDateTime(dateStr.trim(), false);
            if (date != null) {
                dates.add(date);
            }
        }
        
        return dates;
    }
    
    /**
     * 展开折叠行
     */
    private static String unfoldLines(String content) {
        // RFC 5545: 续行以空格或制表符开头
        return content.replaceAll("\r?\n[ \t]", "");
    }
    
    /**
     * 反转义文本
     */
    private static String unescapeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\n", "\n")
                   .replace("\\,", ",")
                   .replace("\\;", ";")
                   .replace("\\\\", "\\");
    }
}
