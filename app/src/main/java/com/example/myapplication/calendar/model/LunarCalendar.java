package com.example.myapplication.calendar.model;

/**
 * LunarCalendar - 农历日历
 * 支持农历日期计算和节气、节日显示
 */
public class LunarCalendar {
    
    // 农历数据表（1900-2100年）
    // 每个数值表示一年的农历信息：前12/13位表示每月大小（1=30天，0=29天），最后4位表示闰月月份
    private static final int[] LUNAR_INFO = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252
    };
    
    // 节气数据
    private static final String[] SOLAR_TERMS = {
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };
    
    // 天干
    private static final String[] TIAN_GAN = {
        "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };
    
    // 地支
    private static final String[] DI_ZHI = {
        "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };
    
    // 生肖
    private static final String[] ZODIAC = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };
    
    // 农历月份名称
    private static final String[] LUNAR_MONTHS = {
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    };
    
    // 农历日期名称
    private static final String[] LUNAR_DAYS = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    
    // 传统节日
    private static final String[][] LUNAR_FESTIVALS = {
        {"正月初一", "春节"},
        {"正月十五", "元宵节"},
        {"二月初二", "龙抬头"},
        {"五月初五", "端午节"},
        {"七月初七", "七夕节"},
        {"八月十五", "中秋节"},
        {"九月初九", "重阳节"},
        {"腊月初八", "腊八节"},
        {"腊月廿三", "小年"},
        {"腊月三十", "除夕"}
    };
    
    private int lunarYear;
    private int lunarMonth;
    private int lunarDay;
    private boolean isLeapMonth;
    
    /**
     * 根据公历日期计算农历
     */
    public LunarCalendar(int year, int month, int day) {
        calculateLunar(year, month, day);
    }
    
    private void calculateLunar(int year, int month, int day) {
        // 简化的农历计算（实际应用中需要更精确的算法）
        // 这里提供基本框架
        
        // 计算从1900年1月31日（农历1900年正月初一）到指定日期的天数
        int offset = getDaysBetween(1900, 1, 31, year, month, day);
        
        // 计算农历年份
        int lunarY = 1900;
        int daysInYear = 0;
        while (lunarY < 2100 && offset > 0) {
            daysInYear = getLunarYearDays(lunarY);
            if (offset < daysInYear) {
                break;
            }
            offset -= daysInYear;
            lunarY++;
        }
        
        this.lunarYear = lunarY;
        
        // 计算农历月份和日期
        int leapMonth = getLeapMonth(lunarY);
        this.isLeapMonth = false;
        
        int lunarM = 1;
        int daysInMonth = 0;
        while (lunarM < 13 && offset > 0) {
            if (leapMonth > 0 && lunarM == (leapMonth + 1) && !this.isLeapMonth) {
                --lunarM;
                this.isLeapMonth = true;
                daysInMonth = getLeapMonthDays(lunarY);
            } else {
                daysInMonth = getLunarMonthDays(lunarY, lunarM);
            }
            
            if (offset < daysInMonth) {
                break;
            }
            offset -= daysInMonth;
            lunarM++;
            
            if (this.isLeapMonth && lunarM == (leapMonth + 1)) {
                this.isLeapMonth = false;
            }
        }
        
        this.lunarMonth = lunarM;
        this.lunarDay = offset + 1;
    }
    
    /**
     * 获取农历年的总天数
     */
    private int getLunarYearDays(int year) {
        int sum = 348;
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0) {
                sum += 1;
            }
        }
        return sum + getLeapMonthDays(year);
    }
    
    /**
     * 获取农历月的天数
     */
    private int getLunarMonthDays(int year, int month) {
        if ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) == 0) {
            return 29;
        } else {
            return 30;
        }
    }
    
    /**
     * 获取闰月月份（0表示无闰月）
     */
    private int getLeapMonth(int year) {
        return LUNAR_INFO[year - 1900] & 0xf;
    }
    
    /**
     * 获取闰月天数
     */
    private int getLeapMonthDays(int year) {
        if (getLeapMonth(year) == 0) {
            return 0;
        }
        if ((LUNAR_INFO[year - 1900] & 0x10000) != 0) {
            return 30;
        } else {
            return 29;
        }
    }
    
    /**
     * 计算两个日期之间的天数
     */
    private int getDaysBetween(int y1, int m1, int d1, int y2, int m2, int d2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.set(y1, m1 - 1, d1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.set(y2, m2 - 1, d2);
        
        long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    /**
     * 获取农历日期字符串（如：正月初一）
     */
    public String getLunarDateString() {
        String prefix = isLeapMonth ? "闰" : "";
        return prefix + LUNAR_MONTHS[lunarMonth - 1] + LUNAR_DAYS[lunarDay - 1];
    }
    
    /**
     * 获取农历月份字符串
     */
    public String getLunarMonthString() {
        String prefix = isLeapMonth ? "闰" : "";
        return prefix + LUNAR_MONTHS[lunarMonth - 1];
    }
    
    /**
     * 获取农历日字符串
     */
    public String getLunarDayString() {
        return LUNAR_DAYS[lunarDay - 1];
    }
    
    /**
     * 获取干支年（如：甲子年）
     */
    public String getGanZhiYear() {
        int ganIndex = (lunarYear - 4) % 10;
        int zhiIndex = (lunarYear - 4) % 12;
        return TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex] + "年";
    }
    
    /**
     * 获取生肖
     */
    public String getZodiac() {
        return ZODIAC[(lunarYear - 4) % 12];
    }
    
    /**
     * 获取节日（如果有）
     */
    public String getFestival() {
        String lunarDate = LUNAR_MONTHS[lunarMonth - 1] + LUNAR_DAYS[lunarDay - 1];
        for (String[] festival : LUNAR_FESTIVALS) {
            if (festival[0].equals(lunarDate)) {
                return festival[1];
            }
        }
        return null;
    }
    
    /**
     * 获取完整描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(getGanZhiYear());
        sb.append(" ");
        sb.append(getZodiac()).append("年");
        sb.append(" ");
        sb.append(getLunarDateString());
        
        String festival = getFestival();
        if (festival != null) {
            sb.append(" [").append(festival).append("]");
        }
        
        return sb.toString();
    }
    
    // Getters
    public int getLunarYear() {
        return lunarYear;
    }
    
    public int getLunarMonth() {
        return lunarMonth;
    }
    
    public int getLunarDay() {
        return lunarDay;
    }
    
    public boolean isLeapMonth() {
        return isLeapMonth;
    }
}
