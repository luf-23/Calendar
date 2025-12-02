package com.example.myapplication.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarUtils {

    /**
     * 获取指定月份的所有日期（包括上月末尾和下月开头的日期，用于填充日历网格）
     */
    public static List<CalendarDay> getMonthDays(int year, int month) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        
        // 获取当月第一天是星期几（1=周日，2=周一...7=周六）
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // 获取当月天数
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // 添加上个月的日期（填充第一行）
        Calendar prevMonth = (Calendar) calendar.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int prevMonthDays = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = firstDayOfWeek - 1; i > 0; i--) {
            prevMonth.set(Calendar.DAY_OF_MONTH, prevMonthDays - i + 1);
            CalendarDay day = new CalendarDay(prevMonth);
            day.setCurrentMonth(false);
            days.add(day);
        }
        
        // 添加当月日期
        Calendar today = Calendar.getInstance();
        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            CalendarDay day = new CalendarDay(calendar);
            day.setCurrentMonth(true);
            
            // 判断是否是今天
            if (isSameDay(calendar, today)) {
                day.setToday(true);
            }
            
            days.add(day);
        }
        
        // 添加下个月的日期（填充最后一行）
        Calendar nextMonth = (Calendar) calendar.clone();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        
        int remainingDays = 42 - days.size(); // 6行 * 7天 = 42格
        for (int i = 1; i <= remainingDays; i++) {
            nextMonth.set(Calendar.DAY_OF_MONTH, i);
            CalendarDay day = new CalendarDay(nextMonth);
            day.setCurrentMonth(false);
            days.add(day);
        }
        
        return days;
    }

    /**
     * 获取指定周的所有日期
     */
    public static List<CalendarDay> getWeekDays(int year, int month, int day) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        // 设置为本周第一天（周日）
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        
        Calendar today = Calendar.getInstance();
        
        // 获取一周的7天
        for (int i = 0; i < 7; i++) {
            CalendarDay calendarDay = new CalendarDay(calendar);
            
            if (isSameDay(calendar, today)) {
                calendarDay.setToday(true);
            }
            
            days.add(calendarDay);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return days;
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
     * 格式化日期
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取月份名称
     */
    public static String getMonthName(int month) {
        String[] months = {"一月", "二月", "三月", "四月", "五月", "六月",
                          "七月", "八月", "九月", "十月", "十一月", "十二月"};
        return months[month];
    }

    /**
     * 获取星期名称
     */
    public static String[] getWeekDayNames() {
        return new String[]{"日", "一", "二", "三", "四", "五", "六"};
    }

    /**
     * 创建指定时间的Date对象
     */
    public static Date createDate(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 检查事件是否在指定日期
     */
    public static boolean isEventOnDay(CalendarEvent event, Date day) {
        Calendar eventStart = Calendar.getInstance();
        eventStart.setTime(event.getStartTime());
        
        Calendar checkDay = Calendar.getInstance();
        checkDay.setTime(day);
        
        return isSameDay(eventStart, checkDay);
    }
}
