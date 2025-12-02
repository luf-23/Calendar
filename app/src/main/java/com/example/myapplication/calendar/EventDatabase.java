package com.example.myapplication.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calendar.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    private static final String TABLE_EVENTS = "events";

    // 列名
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_HAS_REMINDER = "has_reminder";
    private static final String COLUMN_REMINDER_MINUTES = "reminder_minutes";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_PRIORITY = "priority";

    public EventDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_START_TIME + " INTEGER NOT NULL,"
                + COLUMN_END_TIME + " INTEGER NOT NULL,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_HAS_REMINDER + " INTEGER DEFAULT 0,"
                + COLUMN_REMINDER_MINUTES + " INTEGER DEFAULT 15,"
                + COLUMN_COLOR + " TEXT DEFAULT '#4CAF50',"
                + COLUMN_PRIORITY + " INTEGER DEFAULT 2"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    /**
     * 添加事件
     */
    public long addEvent(CalendarEvent event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_START_TIME, event.getStartTime().getTime());
        values.put(COLUMN_END_TIME, event.getEndTime().getTime());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_HAS_REMINDER, event.isHasReminder() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(COLUMN_COLOR, event.getColor());
        values.put(COLUMN_PRIORITY, event.getPriority());

        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    /**
     * 获取所有事件
     */
    public List<CalendarEvent> getAllEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_START_TIME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                CalendarEvent event = cursorToEvent(cursor);
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
        Cursor cursor = db.query(TABLE_EVENTS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        CalendarEvent event = null;
        if (cursor != null && cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
            cursor.close();
        }

        db.close();
        return event;
    }

    /**
     * 获取指定日期范围的事件
     */
    public List<CalendarEvent> getEventsBetween(Date startDate, Date endDate) {
        List<CalendarEvent> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_START_TIME + " >= ? AND " + COLUMN_START_TIME + " <= ?";
        String[] selectionArgs = {
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = db.query(TABLE_EVENTS, null, selection, selectionArgs,
                null, null, COLUMN_START_TIME);

        if (cursor.moveToFirst()) {
            do {
                CalendarEvent event = cursorToEvent(cursor);
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return events;
    }

    /**
     * 更新事件
     */
    public int updateEvent(CalendarEvent event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_START_TIME, event.getStartTime().getTime());
        values.put(COLUMN_END_TIME, event.getEndTime().getTime());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_HAS_REMINDER, event.isHasReminder() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(COLUMN_COLOR, event.getColor());
        values.put(COLUMN_PRIORITY, event.getPriority());

        int result = db.update(TABLE_EVENTS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(event.getId())});
        db.close();
        return result;
    }

    /**
     * 删除事件
     */
    public void deleteEvent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 将Cursor转换为CalendarEvent对象
     */
    private CalendarEvent cursorToEvent(Cursor cursor) {
        CalendarEvent event = new CalendarEvent();
        
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        event.setStartTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME))));
        event.setEndTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME))));
        event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
        event.setHasReminder(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_REMINDER)) == 1);
        event.setReminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_MINUTES)));
        event.setColor(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)));
        event.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));

        return event;
    }

    /**
     * 获取事件数量
     */
    public int getEventCount() {
        String countQuery = "SELECT * FROM " + TABLE_EVENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
