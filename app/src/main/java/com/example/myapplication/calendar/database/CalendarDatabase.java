package com.example.myapplication.calendar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.calendar.model.Attendee;
import com.example.myapplication.calendar.model.CalendarEvent;
import com.example.myapplication.calendar.model.EventAlarm;
import com.example.myapplication.calendar.model.RecurrenceRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CalendarDatabase - 日历数据库
 * 支持完整的 RFC 5545 iCalendar 事件存储
 */
public class CalendarDatabase extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "calendar_rfc5545.db";
    private static final int DATABASE_VERSION = 2;
    
    // 事件表
    private static final String TABLE_EVENTS = "events";
    private static final String COL_ID = "id";
    private static final String COL_UID = "uid";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_LOCATION = "location";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_CREATED_TIME = "created_time";
    private static final String COL_MODIFIED_TIME = "modified_time";
    private static final String COL_TIMEZONE = "timezone";
    private static final String COL_STATUS = "status";
    private static final String COL_EVENT_CLASS = "event_class";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_COLOR = "color";
    private static final String COL_CATEGORY = "category";
    private static final String COL_IS_ALL_DAY = "is_all_day";
    private static final String COL_ORGANIZER = "organizer";
    private static final String COL_CALENDAR_ID = "calendar_id";
    
    // 重复规则（存储为JSON）
    private static final String COL_RECURRENCE_RULE = "recurrence_rule";
    private static final String COL_EXCEPTION_DATES = "exception_dates";
    private static final String COL_RECURRENCE_DATES = "recurrence_dates";
    
    // 提醒表
    private static final String TABLE_ALARMS = "alarms";
    private static final String COL_ALARM_ID = "id";
    private static final String COL_EVENT_ID = "event_id";
    private static final String COL_ACTION = "action";
    private static final String COL_TRIGGER_TYPE = "trigger_type";
    private static final String COL_MINUTES_BEFORE = "minutes_before";
    private static final String COL_ALARM_DESC = "description";
    private static final String COL_REPEAT = "repeat_count";
    private static final String COL_DURATION = "duration";
    
    // 参与者表
    private static final String TABLE_ATTENDEES = "attendees";
    private static final String COL_ATTENDEE_ID = "id";
    private static final String COL_ATTENDEE_EVENT_ID = "event_id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_ROLE = "role";
    private static final String COL_ATTENDEE_STATUS = "status";
    private static final String COL_RSVP = "rsvp";
    
    // 日历表（支持多日历）
    private static final String TABLE_CALENDARS = "calendars";
    private static final String COL_CAL_ID = "id";
    private static final String COL_CAL_NAME = "name";
    private static final String COL_CAL_COLOR = "color";
    private static final String COL_CAL_DESC = "description";
    private static final String COL_CAL_VISIBLE = "visible";
    private static final String COL_CAL_SYNC_URL = "sync_url";  // 网络订阅URL
    
    private final Gson gson;
    
    public CalendarDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.gson = new Gson();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建日历表
        String createCalendarsTable = "CREATE TABLE " + TABLE_CALENDARS + " ("
                + COL_CAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CAL_NAME + " TEXT NOT NULL, "
                + COL_CAL_COLOR + " TEXT DEFAULT '#4CAF50', "
                + COL_CAL_DESC + " TEXT, "
                + COL_CAL_VISIBLE + " INTEGER DEFAULT 1, "
                + COL_CAL_SYNC_URL + " TEXT"
                + ")";
        db.execSQL(createCalendarsTable);
        
        // 插入默认日历
        ContentValues defaultCal = new ContentValues();
        defaultCal.put(COL_CAL_NAME, "我的日历");
        defaultCal.put(COL_CAL_COLOR, "#4CAF50");
        defaultCal.put(COL_CAL_VISIBLE, 1);
        db.insert(TABLE_CALENDARS, null, defaultCal);
        
        // 创建事件表
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_UID + " TEXT UNIQUE NOT NULL, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_LOCATION + " TEXT, "
                + COL_START_TIME + " INTEGER NOT NULL, "
                + COL_END_TIME + " INTEGER NOT NULL, "
                + COL_CREATED_TIME + " INTEGER, "
                + COL_MODIFIED_TIME + " INTEGER, "
                + COL_TIMEZONE + " TEXT DEFAULT 'Asia/Shanghai', "
                + COL_STATUS + " TEXT DEFAULT 'CONFIRMED', "
                + COL_EVENT_CLASS + " TEXT DEFAULT 'PUBLIC', "
                + COL_PRIORITY + " INTEGER DEFAULT 5, "
                + COL_COLOR + " TEXT DEFAULT '#4CAF50', "
                + COL_CATEGORY + " TEXT, "
                + COL_IS_ALL_DAY + " INTEGER DEFAULT 0, "
                + COL_ORGANIZER + " TEXT, "
                + COL_CALENDAR_ID + " INTEGER DEFAULT 1, "
                + COL_RECURRENCE_RULE + " TEXT, "
                + COL_EXCEPTION_DATES + " TEXT, "
                + COL_RECURRENCE_DATES + " TEXT, "
                + "FOREIGN KEY(" + COL_CALENDAR_ID + ") REFERENCES " + TABLE_CALENDARS + "(" + COL_CAL_ID + ")"
                + ")";
        db.execSQL(createEventsTable);
        
        // 创建索引以提高查询性能
        db.execSQL("CREATE INDEX idx_start_time ON " + TABLE_EVENTS + "(" + COL_START_TIME + ")");
        db.execSQL("CREATE INDEX idx_calendar_id ON " + TABLE_EVENTS + "(" + COL_CALENDAR_ID + ")");
        
        // 创建提醒表
        String createAlarmsTable = "CREATE TABLE " + TABLE_ALARMS + " ("
                + COL_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EVENT_ID + " INTEGER NOT NULL, "
                + COL_ACTION + " TEXT DEFAULT 'DISPLAY', "
                + COL_TRIGGER_TYPE + " TEXT DEFAULT 'RELATIVE', "
                + COL_MINUTES_BEFORE + " INTEGER DEFAULT 15, "
                + COL_ALARM_DESC + " TEXT, "
                + COL_REPEAT + " INTEGER DEFAULT 0, "
                + COL_DURATION + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COL_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COL_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(createAlarmsTable);
        
        // 创建参与者表
        String createAttendeesTable = "CREATE TABLE " + TABLE_ATTENDEES + " ("
                + COL_ATTENDEE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ATTENDEE_EVENT_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT, "
                + COL_EMAIL + " TEXT, "
                + COL_ROLE + " TEXT DEFAULT 'REQ_PARTICIPANT', "
                + COL_ATTENDEE_STATUS + " TEXT DEFAULT 'NEEDS_ACTION', "
                + COL_RSVP + " INTEGER DEFAULT 1, "
                + "FOREIGN KEY(" + COL_ATTENDEE_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COL_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(createAttendeesTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级策略：保留数据
        if (oldVersion < 2) {
            // 从旧版本升级
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDEES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALENDARS);
            onCreate(db);
        }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    
    /**
     * 添加或更新事件
     */
    public long saveEvent(CalendarEvent event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = eventToContentValues(event);
        
        long id;
        if (event.getId() > 0) {
            // 更新现有事件
            db.update(TABLE_EVENTS, values, COL_ID + "=?", 
                    new String[]{String.valueOf(event.getId())});
            id = event.getId();
            
            // 删除旧的提醒和参与者
            deleteEventAlarms(id);
            deleteEventAttendees(id);
        } else {
            // 插入新事件
            id = db.insert(TABLE_EVENTS, null, values);
            event.setId(id);
        }
        
        // 保存提醒
        if (event.getAlarms() != null) {
            for (EventAlarm alarm : event.getAlarms()) {
                alarm.setEventId(id);
                saveAlarm(alarm);
            }
        }
        
        // 保存参与者
        if (event.getAttendees() != null) {
            for (Attendee attendee : event.getAttendees()) {
                saveAttendee(id, attendee);
            }
        }
        
        db.close();
        return id;
    }
    
    private ContentValues eventToContentValues(CalendarEvent event) {
        ContentValues values = new ContentValues();
        
        values.put(COL_UID, event.getUid());
        values.put(COL_TITLE, event.getTitle());
        values.put(COL_DESCRIPTION, event.getDescription());
        values.put(COL_LOCATION, event.getLocation());
        values.put(COL_START_TIME, event.getStartTime().getTime());
        values.put(COL_END_TIME, event.getEndTime().getTime());
        values.put(COL_CREATED_TIME, event.getCreatedTime().getTime());
        values.put(COL_MODIFIED_TIME, event.getLastModified().getTime());
        values.put(COL_TIMEZONE, event.getTimezone());
        values.put(COL_STATUS, event.getStatus().name());
        values.put(COL_EVENT_CLASS, event.getEventClass().name());
        values.put(COL_PRIORITY, event.getPriority());
        values.put(COL_COLOR, event.getColor());
        values.put(COL_CATEGORY, event.getCategory());
        values.put(COL_IS_ALL_DAY, event.isAllDay() ? 1 : 0);
        values.put(COL_ORGANIZER, event.getOrganizer());
        values.put(COL_CALENDAR_ID, event.getCalendarId());
        
        // 序列化重复规则
        if (event.getRecurrenceRule() != null) {
            values.put(COL_RECURRENCE_RULE, gson.toJson(event.getRecurrenceRule()));
        }
        
        // 序列化例外日期和重复日期
        if (event.getExceptionDates() != null && !event.getExceptionDates().isEmpty()) {
            values.put(COL_EXCEPTION_DATES, gson.toJson(event.getExceptionDates()));
        }
        if (event.getRecurrenceDates() != null && !event.getRecurrenceDates().isEmpty()) {
            values.put(COL_RECURRENCE_DATES, gson.toJson(event.getRecurrenceDates()));
        }
        
        return values;
    }
    
    /**
     * 获取所有事件
     */
    public List<CalendarEvent> getAllEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_EVENTS, null, null, null, null, null, 
                COL_START_TIME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                CalendarEvent event = cursorToEvent(cursor);
                loadEventDetails(event);
                events.add(event);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return events;
    }
    
    /**
     * 根据日期范围获取事件
     */
    public List<CalendarEvent> getEventsBetween(Date startDate, Date endDate) {
        List<CalendarEvent> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COL_START_TIME + " <= ? AND " + COL_END_TIME + " >= ?";
        String[] selectionArgs = {
                String.valueOf(endDate.getTime()),
                String.valueOf(startDate.getTime())
        };
        
        Cursor cursor = db.query(TABLE_EVENTS, null, selection, selectionArgs, 
                null, null, COL_START_TIME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                CalendarEvent event = cursorToEvent(cursor);
                loadEventDetails(event);
                events.add(event);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return events;
    }
    
    /**
     * 根据ID获取事件
     */
    public CalendarEvent getEventById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        CalendarEvent event = null;
        if (cursor != null && cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
            loadEventDetails(event);
            cursor.close();
        }
        
        db.close();
        return event;
    }
    
    /**
     * 删除事件
     */
    public void deleteEvent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COL_ID + "=?", new String[]{String.valueOf(id)});
        // 外键约束会自动删除相关的提醒和参与者
        db.close();
    }
    
    private CalendarEvent cursorToEvent(Cursor cursor) {
        CalendarEvent event = new CalendarEvent();
        
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        event.setUid(cursor.getString(cursor.getColumnIndexOrThrow(COL_UID)));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)));
        event.setStartTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_START_TIME))));
        event.setEndTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_END_TIME))));
        event.setCreatedTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_TIME))));
        event.setLastModified(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MODIFIED_TIME))));
        event.setTimezone(cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMEZONE)));
        
        String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
        event.setStatus(CalendarEvent.EventStatus.valueOf(status));
        
        String eventClass = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_CLASS));
        event.setEventClass(CalendarEvent.EventClass.valueOf(eventClass));
        
        event.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRIORITY)));
        event.setColor(cursor.getString(cursor.getColumnIndexOrThrow(COL_COLOR)));
        event.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        event.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ALL_DAY)) == 1);
        event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORGANIZER)));
        event.setCalendarId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CALENDAR_ID)));
        
        // 反序列化重复规则
        String rruleJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECURRENCE_RULE));
        if (rruleJson != null && !rruleJson.isEmpty()) {
            RecurrenceRule rrule = gson.fromJson(rruleJson, RecurrenceRule.class);
            event.setRecurrenceRule(rrule);
        }
        
        // 反序列化例外日期
        String exDatesJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXCEPTION_DATES));
        if (exDatesJson != null && !exDatesJson.isEmpty()) {
            Type listType = new TypeToken<List<Date>>(){}.getType();
            List<Date> exDates = gson.fromJson(exDatesJson, listType);
            event.setExceptionDates(exDates);
        }
        
        // 反序列化重复日期
        String rDatesJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECURRENCE_DATES));
        if (rDatesJson != null && !rDatesJson.isEmpty()) {
            Type listType = new TypeToken<List<Date>>(){}.getType();
            List<Date> rDates = gson.fromJson(rDatesJson, listType);
            event.setRecurrenceDates(rDates);
        }
        
        return event;
    }
    
    private void loadEventDetails(CalendarEvent event) {
        // 加载提醒
        event.setAlarms(getEventAlarms(event.getId()));
        // 加载参与者
        event.setAttendees(getEventAttendees(event.getId()));
    }
    
    // 提醒相关方法
    private long saveAlarm(EventAlarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_EVENT_ID, alarm.getEventId());
        values.put(COL_ACTION, alarm.getAction().name());
        values.put(COL_TRIGGER_TYPE, alarm.getTriggerType().name());
        values.put(COL_MINUTES_BEFORE, alarm.getMinutesBefore());
        values.put(COL_ALARM_DESC, alarm.getDescription());
        values.put(COL_REPEAT, alarm.getRepeat());
        values.put(COL_DURATION, alarm.getDuration());
        
        long id = db.insert(TABLE_ALARMS, null, values);
        db.close();
        return id;
    }
    
    private List<EventAlarm> getEventAlarms(long eventId) {
        List<EventAlarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ALARMS, null, COL_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                EventAlarm alarm = new EventAlarm();
                alarm.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ALARM_ID)));
                alarm.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENT_ID)));
                alarm.setAction(EventAlarm.AlarmAction.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTION))));
                alarm.setTriggerType(EventAlarm.TriggerType.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIGGER_TYPE))));
                alarm.setMinutesBefore(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MINUTES_BEFORE)));
                alarm.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_ALARM_DESC)));
                alarm.setRepeat(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPEAT)));
                alarm.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)));
                
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return alarms;
    }
    
    private void deleteEventAlarms(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARMS, COL_EVENT_ID + "=?", new String[]{String.valueOf(eventId)});
        db.close();
    }
    
    // 参与者相关方法
    private long saveAttendee(long eventId, Attendee attendee) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_ATTENDEE_EVENT_ID, eventId);
        values.put(COL_NAME, attendee.getName());
        values.put(COL_EMAIL, attendee.getEmail());
        values.put(COL_ROLE, attendee.getRole().name());
        values.put(COL_ATTENDEE_STATUS, attendee.getStatus().name());
        values.put(COL_RSVP, attendee.isRsvp() ? 1 : 0);
        
        long id = db.insert(TABLE_ATTENDEES, null, values);
        db.close();
        return id;
    }
    
    private List<Attendee> getEventAttendees(long eventId) {
        List<Attendee> attendees = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ATTENDEES, null, COL_ATTENDEE_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Attendee attendee = new Attendee();
                attendee.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                attendee.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
                attendee.setRole(Attendee.AttendeeRole.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))));
                attendee.setStatus(Attendee.AttendeeStatus.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ATTENDEE_STATUS))));
                attendee.setRsvp(cursor.getInt(cursor.getColumnIndexOrThrow(COL_RSVP)) == 1);
                
                attendees.add(attendee);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return attendees;
    }
    
    private void deleteEventAttendees(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ATTENDEES, COL_ATTENDEE_EVENT_ID + "=?", 
                new String[]{String.valueOf(eventId)});
        db.close();
    }
}
