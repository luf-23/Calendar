package com.example.myapplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarDay {
    private Calendar calendar;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean isSelected;
    private int eventCount;

    public CalendarDay(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
        this.isCurrentMonth = true;
        this.isToday = false;
        this.isSelected = false;
        this.eventCount = 0;
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

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public boolean hasEvents() {
        return eventCount > 0;
    }

}
