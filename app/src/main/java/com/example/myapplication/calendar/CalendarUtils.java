package com.example.myapplication.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日历工具类
 * 提供日期计算、格式化和日历视图数据生成功能
 */
public class CalendarUtils {

    /**
     * 获取月份名称
     * @param month 月份 (0-11, Calendar.JANUARY 到 Calendar.DECEMBER)
     * @return 月份名称，如"1月"、"2月"等
     */
    public static String getMonthName(int month) {
        String[] monthNames = {
                "1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"
        };

        if (month >= 0 && month < 12) {
            return monthNames[month];
        }
        return "";
    }

    /**
     * 格式化日期
     * @param date 要格式化的日期
     * @param format 格式字符串，如"yyyy年MM月dd日"、"dd日"等
     * @return 格式化后的日期字符串
     */
    public static String formatDate(Date date, String format) {
        if (date == null || format == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取年视图的日期数据
     * 返回12个月的代表日期，用于年视图展示
     * @param year 年份
     * @return 包含12个月的日期列表
     */
    public static List<CalendarDay> getYearDays(int year) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        for (int month = 0; month < 12; month++) {
            calendar.set(year, month, 1);
            CalendarDay day = new CalendarDay(calendar);
            day.setCurrentMonth(true);

            // 检查是否是当前月
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                day.setToday(true);
            }

            days.add(day);
        }

        return days;
    }

    /**
     * 获取月视图的日期数据
     * 返回一个月的所有日期，包括前后月份的填充日期
     * @param year 年份
     * @param month 月份 (0-11)
     * @return 月视图的日期列表（通常为42天，6周）
     */
    public static List<CalendarDay> getMonthDays(int year, int month) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        // 设置为当月第一天
        calendar.set(year, month, 1);

        // 获取当月第一天是星期几（0=周日, 1=周一, ..., 6=周六）
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // 获取当月天数
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 获取上月天数
        calendar.add(Calendar.MONTH, -1);
        int daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.MONTH, 1);

        // 添加上月的日期（填充）
        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            Calendar prevMonthCal = (Calendar) calendar.clone();
            prevMonthCal.add(Calendar.MONTH, -1);
            prevMonthCal.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i);

            CalendarDay day = new CalendarDay(prevMonthCal);
            day.setCurrentMonth(false);
            days.add(day);
        }

        // 添加当月的日期
        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            CalendarDay day = new CalendarDay(calendar);
            day.setCurrentMonth(true);

            // 检查是否是今天
            if (isSameDay(calendar, today)) {
                day.setToday(true);
            }

            days.add(day);
        }

        // 计算需要多少行（5行或6行）
        // 如果当前已有的天数大于35，则需要6行（42天），否则5行（35天）就够了
        int totalDays = (days.size() + daysInMonth > 35) ? 42 : 35;
        
        // 添加下月的日期（填充到总天数）
        int remainingDays = totalDays - days.size();
        for (int i = 1; i <= remainingDays; i++) {
            Calendar nextMonthCal = (Calendar) calendar.clone();
            nextMonthCal.add(Calendar.MONTH, 1);
            nextMonthCal.set(Calendar.DAY_OF_MONTH, i);

            CalendarDay day = new CalendarDay(nextMonthCal);
            day.setCurrentMonth(false);
            days.add(day);
        }

        return days;
    }

    /**
     * 获取周视图的日期数据
     * 返回包含指定日期的一周的所有日期
     * @param year 年份
     * @param month 月份 (0-11)
     * @param day 日期
     * @return 周视图的日期列表（7天）
     */
    public static List<CalendarDay> getWeekDays(int year, int month, int day) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        calendar.set(year, month, day);

        // 获取当前日期是星期几
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // 移动到本周的第一天（周日）
        calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - 1));

        // 添加本周的7天
        for (int i = 0; i < 7; i++) {
            CalendarDay calendarDay = new CalendarDay(calendar);
            calendarDay.setCurrentMonth(calendar.get(Calendar.MONTH) == month);

            // 检查是否是今天
            if (isSameDay(calendar, today)) {
                calendarDay.setToday(true);
            }

            days.add(calendarDay);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return days;
    }

    /**
     * 判断两个Calendar是否是同一天
     * @param cal1 第一个Calendar
     * @param cal2 第二个Calendar
     * @return 如果是同一天返回true，否则返回false
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 重置Calendar的时间部分（时、分、秒、毫秒）
     * @param calendar 要重置的Calendar
     */
    private static void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 获取两个日期之间的天数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static int getDaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(startDate);
        end.setTime(endDate);

        resetTime(start);
        resetTime(end);

        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * 判断是否是今天
     * @param date 要检查的日期
     * @return 如果是今天返回true，否则返回false
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }

        Calendar today = Calendar.getInstance();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTime(date);

        return isSameDay(today, checkDate);
    }

    /**
     * 判断日期是否在指定月份
     * @param date 要检查的日期
     * @param year 年份
     * @param month 月份 (0-11)
     * @return 如果日期在指定月份返回true，否则返回false
     */
    public static boolean isInMonth(Date date, int year, int month) {
        if (date == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month;
    }
}
