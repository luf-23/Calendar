package com.example.myapplication.calendar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RecurrenceRule - 重复规则
 * 基于 RFC 5545 RRULE 规范
 */
public class RecurrenceRule implements Serializable {
    
    private Frequency frequency;  // FREQ
    private Integer interval;  // INTERVAL
    private Integer count;  // COUNT (重复次数)
    private Long until;  // UNTIL (结束日期，毫秒时间戳)
    private List<Integer> byDay;  // BYDAY (星期几: 1=周日, 2=周一, ..., 7=周六)
    private List<Integer> byMonthDay;  // BYMONTHDAY
    private List<Integer> byMonth;  // BYMONTH
    private List<Integer> bySetPos;  // BYSETPOS
    private Weekday weekStart;  // WKST
    
    /**
     * 重复频率
     */
    public enum Frequency {
        DAILY,    // 每天
        WEEKLY,   // 每周
        MONTHLY,  // 每月
        YEARLY    // 每年
    }
    
    /**
     * 周几定义（用于WKST）
     */
    public enum Weekday {
        SUNDAY(1),
        MONDAY(2),
        TUESDAY(3),
        WEDNESDAY(4),
        THURSDAY(5),
        FRIDAY(6),
        SATURDAY(7);
        
        private final int value;
        
        Weekday(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public RecurrenceRule() {
        this.interval = 1;
        this.weekStart = Weekday.MONDAY;
        this.byDay = new ArrayList<>();
        this.byMonthDay = new ArrayList<>();
        this.byMonth = new ArrayList<>();
        this.bySetPos = new ArrayList<>();
    }
    
    /**
     * 创建每日重复规则
     */
    public static RecurrenceRule daily(int interval) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.DAILY);
        rule.setInterval(interval);
        return rule;
    }
    
    /**
     * 创建每周重复规则
     */
    public static RecurrenceRule weekly(int interval, List<Integer> daysOfWeek) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.WEEKLY);
        rule.setInterval(interval);
        rule.setByDay(daysOfWeek);
        return rule;
    }
    
    /**
     * 创建每月重复规则
     */
    public static RecurrenceRule monthly(int interval, List<Integer> daysOfMonth) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.MONTHLY);
        rule.setInterval(interval);
        rule.setByMonthDay(daysOfMonth);
        return rule;
    }
    
    /**
     * 创建每年重复规则
     */
    public static RecurrenceRule yearly(int interval) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(Frequency.YEARLY);
        rule.setInterval(interval);
        return rule;
    }
    
    /**
     * 转换为 RFC 5545 RRULE 字符串
     */
    public String toRRuleString() {
        StringBuilder rrule = new StringBuilder("RRULE:FREQ=");
        rrule.append(frequency.name());
        
        if (interval != null && interval > 1) {
            rrule.append(";INTERVAL=").append(interval);
        }
        
        if (count != null) {
            rrule.append(";COUNT=").append(count);
        }
        
        if (until != null) {
            rrule.append(";UNTIL=").append(formatDate(until));
        }
        
        if (byDay != null && !byDay.isEmpty()) {
            rrule.append(";BYDAY=");
            for (int i = 0; i < byDay.size(); i++) {
                if (i > 0) rrule.append(",");
                rrule.append(getDayAbbreviation(byDay.get(i)));
            }
        }
        
        if (byMonthDay != null && !byMonthDay.isEmpty()) {
            rrule.append(";BYMONTHDAY=");
            for (int i = 0; i < byMonthDay.size(); i++) {
                if (i > 0) rrule.append(",");
                rrule.append(byMonthDay.get(i));
            }
        }
        
        if (byMonth != null && !byMonth.isEmpty()) {
            rrule.append(";BYMONTH=");
            for (int i = 0; i < byMonth.size(); i++) {
                if (i > 0) rrule.append(",");
                rrule.append(byMonth.get(i));
            }
        }
        
        if (weekStart != null && weekStart != Weekday.MONDAY) {
            rrule.append(";WKST=").append(getDayAbbreviation(weekStart.getValue()));
        }
        
        return rrule.toString();
    }
    
    /**
     * 从 RFC 5545 RRULE 字符串解析
     */
    public static RecurrenceRule fromRRuleString(String rruleString) {
        if (rruleString == null || !rruleString.startsWith("RRULE:")) {
            return null;
        }
        
        RecurrenceRule rule = new RecurrenceRule();
        String[] parts = rruleString.substring(6).split(";");
        
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length != 2) continue;
            
            String key = keyValue[0];
            String value = keyValue[1];
            
            switch (key) {
                case "FREQ":
                    rule.setFrequency(Frequency.valueOf(value));
                    break;
                case "INTERVAL":
                    rule.setInterval(Integer.parseInt(value));
                    break;
                case "COUNT":
                    rule.setCount(Integer.parseInt(value));
                    break;
                case "UNTIL":
                    // 解析日期（简化版）
                    break;
                case "BYDAY":
                    String[] days = value.split(",");
                    List<Integer> byDay = new ArrayList<>();
                    for (String day : days) {
                        byDay.add(getDayNumber(day));
                    }
                    rule.setByDay(byDay);
                    break;
                case "BYMONTHDAY":
                    String[] monthDays = value.split(",");
                    List<Integer> byMonthDay = new ArrayList<>();
                    for (String day : monthDays) {
                        byMonthDay.add(Integer.parseInt(day));
                    }
                    rule.setByMonthDay(byMonthDay);
                    break;
            }
        }
        
        return rule;
    }
    
    private String formatDate(long timestamp) {
        // 简化版：实际应该格式化为 YYYYMMDDTHHMMSSZ
        return String.valueOf(timestamp);
    }
    
    private String getDayAbbreviation(int day) {
        switch (day) {
            case 1: return "SU";
            case 2: return "MO";
            case 3: return "TU";
            case 4: return "WE";
            case 5: return "TH";
            case 6: return "FR";
            case 7: return "SA";
            default: return "MO";
        }
    }
    
    private static int getDayNumber(String day) {
        switch (day) {
            case "SU": return 1;
            case "MO": return 2;
            case "TU": return 3;
            case "WE": return 4;
            case "TH": return 5;
            case "FR": return 6;
            case "SA": return 7;
            default: return 2;
        }
    }
    
    /**
     * 获取友好的描述文本
     */
    public String getDescription() {
        if (frequency == null) {
            return "不重复";
        }
        
        StringBuilder desc = new StringBuilder();
        
        if (interval != null && interval > 1) {
            desc.append("每").append(interval);
        } else {
            desc.append("每");
        }
        
        switch (frequency) {
            case DAILY:
                desc.append("天");
                break;
            case WEEKLY:
                desc.append("周");
                if (byDay != null && !byDay.isEmpty()) {
                    desc.append("的");
                    for (int i = 0; i < byDay.size(); i++) {
                        if (i > 0) desc.append("、");
                        desc.append(getChineseDayName(byDay.get(i)));
                    }
                }
                break;
            case MONTHLY:
                desc.append("月");
                if (byMonthDay != null && !byMonthDay.isEmpty()) {
                    desc.append("的").append(byMonthDay.get(0)).append("号");
                }
                break;
            case YEARLY:
                desc.append("年");
                break;
        }
        
        if (count != null) {
            desc.append("，共").append(count).append("次");
        }
        
        return desc.toString();
    }
    
    private String getChineseDayName(int day) {
        switch (day) {
            case 1: return "周日";
            case 2: return "周一";
            case 3: return "周二";
            case 4: return "周三";
            case 5: return "周四";
            case 6: return "周五";
            case 7: return "周六";
            default: return "";
        }
    }
    
    // Getters and Setters
    public Frequency getFrequency() {
        return frequency;
    }
    
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
    
    public Integer getInterval() {
        return interval;
    }
    
    public void setInterval(Integer interval) {
        this.interval = interval;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public Long getUntil() {
        return until;
    }
    
    public void setUntil(Long until) {
        this.until = until;
    }
    
    public List<Integer> getByDay() {
        return byDay;
    }
    
    public void setByDay(List<Integer> byDay) {
        this.byDay = byDay;
    }
    
    public List<Integer> getByMonthDay() {
        return byMonthDay;
    }
    
    public void setByMonthDay(List<Integer> byMonthDay) {
        this.byMonthDay = byMonthDay;
    }
    
    public List<Integer> getByMonth() {
        return byMonth;
    }
    
    public void setByMonth(List<Integer> byMonth) {
        this.byMonth = byMonth;
    }
    
    public List<Integer> getBySetPos() {
        return bySetPos;
    }
    
    public void setBySetPos(List<Integer> bySetPos) {
        this.bySetPos = bySetPos;
    }
    
    public Weekday getWeekStart() {
        return weekStart;
    }
    
    public void setWeekStart(Weekday weekStart) {
        this.weekStart = weekStart;
    }
}
