package com.example.myapplication.util;

import android.content.Context;
import android.net.Uri;

import com.example.myapplication.data.model.CalendarEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * iCalendar (.ics) 格式的导入导出工具类
 * 支持将日历事件导出为标准 ICS 文件，以及从 ICS 文件导入事件
 */
public class IcsExportImportHelper {
    
    private static final String ICAL_VERSION = "2.0";
    private static final String PRODUCT_ID = "-//MyCalendar//Calendar Events//CN";
    
    // iCalendar 日期时间格式：yyyyMMdd'T'HHmmss'Z'
    private static final SimpleDateFormat ICS_DATE_FORMAT = 
        new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
    
    static {
        ICS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * 导出事件列表到 ICS 文件
     * 
     * @param context 上下文
     * @param events 要导出的事件列表
     * @param uri 输出文件的 Uri
     * @return 是否成功
     */
    public static boolean exportToIcs(Context context, List<CalendarEvent> events, Uri uri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            
            // 写入 iCalendar 头部
            writer.write("BEGIN:VCALENDAR\r\n");
            writer.write("VERSION:" + ICAL_VERSION + "\r\n");
            writer.write("PRODID:" + PRODUCT_ID + "\r\n");
            writer.write("CALSCALE:GREGORIAN\r\n");
            
            // 写入每个事件
            for (CalendarEvent event : events) {
                writeEvent(writer, event);
            }
            
            // 写入 iCalendar 结尾
            writer.write("END:VCALENDAR\r\n");
            writer.flush();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 写入单个事件到 ICS 文件
     */
    private static void writeEvent(BufferedWriter writer, CalendarEvent event) throws Exception {
        writer.write("BEGIN:VEVENT\r\n");
        
        // UID: 唯一标识符
        writer.write("UID:event-" + event.getId() + "@mycalendar.app\r\n");
        
        // DTSTAMP: 创建时间戳 (使用当前时间)
        writer.write("DTSTAMP:" + ICS_DATE_FORMAT.format(new Date()) + "\r\n");
        
        // DTSTART: 开始时间
        if (event.getStartTime() != null) {
            writer.write("DTSTART:" + ICS_DATE_FORMAT.format(event.getStartTime()) + "\r\n");
        }
        
        // DTEND: 结束时间
        if (event.getEndTime() != null) {
            writer.write("DTEND:" + ICS_DATE_FORMAT.format(event.getEndTime()) + "\r\n");
        }
        
        // SUMMARY: 标题
        if (event.getTitle() != null && !event.getTitle().isEmpty()) {
            writer.write("SUMMARY:" + escapeText(event.getTitle()) + "\r\n");
        }
        
        // DESCRIPTION: 描述
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            writer.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\r\n");
        }
        
        // LOCATION: 地点
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            writer.write("LOCATION:" + escapeText(event.getLocation()) + "\r\n");
        }
        
        // CATEGORIES: 类别（使用事件类型）
        if (event.getType() != null) {
            writer.write("CATEGORIES:" + event.getType().getName() + "\r\n");
        }
        
        // COLOR: 颜色（使用 X- 扩展属性）
        writer.write("X-APPLE-CALENDAR-COLOR:" + String.format("#%06X", (0xFFFFFF & event.getColor())) + "\r\n");
        
        // 提醒设置
        if (event.isReminderEnabled() && event.getReminderMinutesBefore() > 0) {
            writer.write("BEGIN:VALARM\r\n");
            writer.write("ACTION:DISPLAY\r\n");
            writer.write("TRIGGER:-PT" + event.getReminderMinutesBefore() + "M\r\n");
            writer.write("DESCRIPTION:Event reminder\r\n");
            
            // 如果开启响铃
            if (event.isSoundEnabled()) {
                writer.write("X-SOUND-ENABLED:TRUE\r\n");
            }
            
            writer.write("END:VALARM\r\n");
        }
        
        writer.write("END:VEVENT\r\n");
    }
    
    /**
     * 从 ICS 文件导入事件
     * 
     * @param context 上下文
     * @param uri 输入文件的 Uri
     * @return 导入的事件列表
     */
    public static List<CalendarEvent> importFromIcs(Context context, Uri uri) {
        List<CalendarEvent> events = new ArrayList<>();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            
            String line;
            CalendarEvent currentEvent = null;
            boolean inEvent = false;
            boolean inAlarm = false;
            ReminderInfo reminderInfo = new ReminderInfo();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // 开始解析事件
                if (line.equals("BEGIN:VEVENT")) {
                    currentEvent = new CalendarEvent();
                    inEvent = true;
                    reminderInfo.reset();
                    continue;
                }
                
                // 结束解析事件
                if (line.equals("END:VEVENT")) {
                    if (currentEvent != null) {
                        // 应用提醒设置
                        if (reminderInfo.reminderMinutes > 0) {
                            currentEvent.setReminderEnabled(true);
                            currentEvent.setReminderMinutesBefore(reminderInfo.reminderMinutes);
                            currentEvent.setSoundEnabled(reminderInfo.soundEnabled);
                        }
                        events.add(currentEvent);
                    }
                    currentEvent = null;
                    inEvent = false;
                    continue;
                }
                
                // 解析提醒信息
                if (line.equals("BEGIN:VALARM")) {
                    inAlarm = true;
                    continue;
                }
                
                if (line.equals("END:VALARM")) {
                    inAlarm = false;
                    continue;
                }
                
                // 解析事件属性
                if (inEvent && currentEvent != null) {
                    parseEventProperty(line, currentEvent, inAlarm, reminderInfo);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return events;
    }
    
    /**
     * 提醒信息辅助类
     */
    private static class ReminderInfo {
        int reminderMinutes = 0;
        boolean soundEnabled = false;
        
        void reset() {
            reminderMinutes = 0;
            soundEnabled = false;
        }
    }
    
    /**
     * 解析事件属性
     */
    private static void parseEventProperty(String line, CalendarEvent event, 
                                          boolean inAlarm, ReminderInfo reminderInfo) {
        try {
            // 分割属性名和值
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                return;
            }
            
            String property = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1);
            
            // 处理带参数的属性（如 DTSTART;TZID=xxx:20230101T120000）
            int semicolonIndex = property.indexOf(';');
            if (semicolonIndex != -1) {
                property = property.substring(0, semicolonIndex);
            }
            
            switch (property) {
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
                    Date startDate = parseIcsDate(value);
                    if (startDate != null) {
                        event.setStartTime(startDate);
                    }
                    break;
                    
                case "DTEND":
                    Date endDate = parseIcsDate(value);
                    if (endDate != null) {
                        event.setEndTime(endDate);
                    }
                    break;
                    
                case "CATEGORIES":
                    // 根据类别名称设置事件类型
                    for (CalendarEvent.EventType type : CalendarEvent.EventType.values()) {
                        if (type.getName().equals(value)) {
                            event.setType(type);
                            break;
                        }
                    }
                    break;
                    
                case "X-APPLE-CALENDAR-COLOR":
                    // 解析颜色
                    try {
                        int color = android.graphics.Color.parseColor(value);
                        event.setColor(color);
                    } catch (Exception e) {
                        // 颜色解析失败，使用默认颜色
                    }
                    break;
                    
                case "TRIGGER":
                    // 解析提醒时间（格式如：-PT15M 表示提前15分钟）
                    if (inAlarm && value.startsWith("-PT") && value.endsWith("M")) {
                        try {
                            String minutesStr = value.substring(3, value.length() - 1);
                            reminderInfo.reminderMinutes = Integer.parseInt(minutesStr);
                        } catch (NumberFormatException e) {
                            // 解析失败
                        }
                    }
                    break;
                    
                case "X-SOUND-ENABLED":
                    if (inAlarm && "TRUE".equalsIgnoreCase(value)) {
                        reminderInfo.soundEnabled = true;
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 解析 ICS 日期时间格式
     */
    private static Date parseIcsDate(String dateStr) {
        try {
            // 移除可能的时区标识符
            dateStr = dateStr.replace("Z", "");
            dateStr = dateStr.replace("z", "");
            
            // 尝试标准格式：yyyyMMddTHHmmss
            if (dateStr.contains("T")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
                return format.parse(dateStr);
            } else {
                // 全天事件格式：yyyyMMdd
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                return format.parse(dateStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 转义特殊字符（iCalendar 规范）
     */
    private static String escapeText(String text) {
        if (text == null) return "";
        
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
    
    /**
     * 反转义特殊字符
     */
    private static String unescapeText(String text) {
        if (text == null) return "";
        
        return text.replace("\\n", "\n")
                   .replace("\\,", ",")
                   .replace("\\;", ";")
                   .replace("\\\\", "\\");
    }
}
