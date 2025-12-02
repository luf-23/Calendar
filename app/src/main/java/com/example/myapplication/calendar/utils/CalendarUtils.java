package com.example.myapplication.calendar.utils;

import com.example.myapplication.calendar.model.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CalendarUtils - 日历工具类
 * 增强版,支持更多功能
 */
public class CalendarUtils {
    
    /**
     * 获取指定月份的所有日期（包括上月末尾和下月开头的日期，用于填充日历网格）
     */
    public static List<CalendarDay> getMonthDays(int year, int month) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        
        // 获取本月第一天是星期几（1=周日, 2=周一, ...）
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // 计算需要显示的上月天数（让周一作为一周的第一天）
        int daysFromPrevMonth = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;
        
        // 添加上月的日期
        Calendar prevMonthCal = (Calendar) calendar.clone();
        prevMonthCal.add(Calendar.DAY_OF_MONTH, -daysFromPrevMonth);
        
        for (int i = 0; i < daysFromPrevMonth; i++) {
            CalendarDay day = new CalendarDay((Calendar) prevMonthCal.clone());
            day.setCurrentMonth(false);
            days.add(day);
            prevMonthCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 添加本月的日期
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        
        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            CalendarDay day = new CalendarDay((Calendar) calendar.clone());
            day.setCurrentMonth(true);
            
            // 判断是否是今天
            if (isSameDay(calendar, today)) {
                day.setToday(true);
            }
            
            days.add(day);
        }
        
        // 添加下月的日期以填满6行（42个格子）
        int remainingDays = 42 - days.size();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        for (int i = 0; i < remainingDays; i++) {
            CalendarDay day = new CalendarDay((Calendar) calendar.clone());
            day.setCurrentMonth(false);
            days.add(day);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return days;
    }
    
    /**
     * 获取指定周的所有日期（周一到周日）
     */
    public static List<CalendarDay> getWeekDays(int year, int month, int day) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        // 获取本周的周一
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
        calendar.add(Calendar.DAY_OF_MONTH, offset);
        
        Calendar today = Calendar.getInstance();
        
        // 添加周一到周日
        for (int i = 0; i < 7; i++) {
            CalendarDay calDay = new CalendarDay((Calendar) calendar.clone());
            calDay.setCurrentMonth(calendar.get(Calendar.MONTH) == month);
            
            if (isSameDay(calendar, today)) {
                calDay.setToday(true);
            }
            
            days.add(calDay);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return days;
    }
    
    /**
     * 获取指定日期的单天（用于日视图）
     */
    public static CalendarDay getSingleDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        CalendarDay calDay = new CalendarDay(calendar);
        calDay.setCurrentMonth(true);
        
        Calendar today = Calendar.getInstance();
        if (isSameDay(calendar, today)) {
            calDay.setToday(true);
        }
        
        return calDay;
    }
    
    /**
     * 获取时间段列表（用于日/周视图的时间轴）
     */
    public static List<String> getHourSlots() {
        List<String> slots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            slots.add(String.format(Locale.getDefault(), "%02d:00", hour));
        }
        return slots;
    }
    
    /**
     * 判断两个日期是否是同一天
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 判断日期是否在同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }
    
    /**
     * 判断事件是否在指定日期发生
     */
    public static boolean isEventOnDay(CalendarEvent event, Date date) {
        if (event == null || event.getStartTime() == null || event.getEndTime() == null) {
            return false;
        }
        return event.occursOnDate(date);
    }
    
    /**
     * 格式化日期
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 格式化时间
     */
    public static String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 格式化日期时间
     */
    public static String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 获取月份名称
     */
    public static String getMonthName(int month) {
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月",
                          "7月", "8月", "9月", "10月", "11月", "12月"};
        return months[month];
    }
    
    /**
     * 获取星期几的缩写
     */
    public static String[] getWeekdayAbbreviations() {
        return new String[]{"一", "二", "三", "四", "五", "六", "日"};
    }
    
    /**
     * 获取今天的开始时间（00:00:00）
     */
    public static Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * 获取今天的结束时间（23:59:59）
     */
    public static Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    /**
     * 获取两个时间之间的天数
     */
    public static int getDaysBetween(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    /**
     * 添加天数
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
    
    /**
     * 添加月份
     */
    public static Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }
    
    /**
     * 计算事件在日视图中的位置（百分比）
     */
    public static float getEventPositionInDay(CalendarEvent event) {
        if (event == null || event.getStartTime() == null) {
            return 0;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(event.getStartTime());
        
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        return (hour * 60f + minute) / (24f * 60f);
    }
    
    /**
     * 计算事件在日视图中的高度（百分比）
     */
    public static float getEventHeightInDay(CalendarEvent event) {
        if (event == null || event.getStartTime() == null || event.getEndTime() == null) {
            return 0.04f; // 最小高度
        }
        
        long duration = event.getEndTime().getTime() - event.getStartTime().getTime();
        float durationMinutes = duration / (1000f * 60f);
        
        return Math.max(0.04f, durationMinutes / (24f * 60f));
    }
}
