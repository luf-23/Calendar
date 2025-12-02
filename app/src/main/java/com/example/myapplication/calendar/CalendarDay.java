package com.example.myapplication.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarDay {
    private Calendar calendar;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean isSelected;
    private List<CalendarEvent> events;

    public CalendarDay(Calendar calendar) {
        this.calendar = (Calendar) calendar.clone();
        this.events = new ArrayList<>();
        this.isCurrentMonth = true;
        this.isToday = false;
        this.isSelected = false;
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

    public List<CalendarEvent> getEvents() {
        return events;
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
    }

    public void addEvent(CalendarEvent event) {
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    public boolean hasEvents() {
        return events != null && !events.isEmpty();
    }

    public int getEventCount() {
        return events != null ? events.size() : 0;
    }
}
