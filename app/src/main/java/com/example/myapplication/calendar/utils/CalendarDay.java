package com.example.myapplication.calendar.utils;

import com.example.myapplication.calendar.model.CalendarEvent;
import com.example.myapplication.calendar.model.LunarCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CalendarDay - 日历日期模型
 * 增强版,支持农历显示
 */
public class CalendarDay {
    private Calendar calendar;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean isSelected;
    private boolean isWeekend;
    private List<CalendarEvent> events;
    private LunarCalendar lunarInfo;
    
    public CalendarDay(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
        this.events = new ArrayList<>();
        this.isCurrentMonth = true;
        this.isToday = false;
        this.isSelected = false;
        
        // 判断是否周末
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        this.isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
        
        // 计算农历信息
        this.lunarInfo = new LunarCalendar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
    
    public Date getDate() {
        return calendar.getTime();
    }
    
    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }
    
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }
    
    public Calendar getCalendar() {
        return (Calendar) calendar.clone();
    }
    
    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }
    
    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }
    
    public boolean isToday() {
        return isToday;
    }
    
    public void setToday(boolean today) {
        isToday = today;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public boolean isWeekend() {
        return isWeekend;
    }
    
    public List<CalendarEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
    }
    
    public void addEvent(CalendarEvent event) {
        if (this.events == null) {
            this.events = new ArrayList<>();
        }
        this.events.add(event);
    }
    
    public boolean hasEvents() {
        return events != null && !events.isEmpty();
    }
    
    public int getEventCount() {
        return events != null ? events.size() : 0;
    }
    
    public LunarCalendar getLunarInfo() {
        return lunarInfo;
    }
    
    /**
     * 获取农历日期字符串
     */
    public String getLunarDateString() {
        if (lunarInfo == null) {
            return "";
        }
        
        // 优先显示节日
        String festival = lunarInfo.getFestival();
        if (festival != null) {
            return festival;
        }
        
        // 显示农历日期
        return lunarInfo.getLunarDayString();
    }
    
    /**
     * 获取格式化的日期字符串
     */
    public String getFormattedDate(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
    
    /**
     * 获取星期几的名称
     */
    public String getDayOfWeekName() {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "周日";
            case Calendar.MONDAY: return "周一";
            case Calendar.TUESDAY: return "周二";
            case Calendar.WEDNESDAY: return "周三";
            case Calendar.THURSDAY: return "周四";
            case Calendar.FRIDAY: return "周五";
            case Calendar.SATURDAY: return "周六";
            default: return "";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        CalendarDay that = (CalendarDay) o;
        
        return getYear() == that.getYear() &&
               getMonth() == that.getMonth() &&
               getDay() == that.getDay();
    }
    
    @Override
    public int hashCode() {
        int result = getYear();
        result = 31 * result + getMonth();
        result = 31 * result + getDay();
        return result;
    }
}
