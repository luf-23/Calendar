package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;


@Dao
public interface EventDao {
    

    @Insert
    long insert(CalendarEvent event);
    

    @Insert
    void insertAll(List<CalendarEvent> events);
    

    @Update
    void update(CalendarEvent event);
    

    @Delete
    void delete(CalendarEvent event);
    

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    void deleteById(long eventId);
    

    @Query("SELECT * FROM calendar_events WHERE id = :eventId")
    CalendarEvent getEventById(long eventId);

    @Query("SELECT * FROM calendar_events ORDER BY start_time ASC")
    List<CalendarEvent> getAllEvents();

    @Query("SELECT * FROM calendar_events WHERE start_time >= :startOfDay AND start_time < :endOfDay ORDER BY start_time ASC")
    List<CalendarEvent> getEventsByDate(long startOfDay, long endOfDay);
    

    @Query("SELECT * FROM calendar_events WHERE start_time >= :startTime AND start_time <= :endTime ORDER BY start_time ASC")
    List<CalendarEvent> getEventsByTimeRange(long startTime, long endTime);
    

    @Query("SELECT * FROM calendar_events WHERE type = :type ORDER BY start_time ASC")
    List<CalendarEvent> getEventsByType(CalendarEvent.EventType type);
    

    @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' ORDER BY start_time ASC")
    List<CalendarEvent> searchEvents(String keyword);
    

    @Query("SELECT * FROM calendar_events WHERE start_time >= :monthStart AND start_time < :monthEnd ORDER BY start_time ASC")
    List<CalendarEvent> getEventsByMonth(long monthStart, long monthEnd);
    

    @Query("DELETE FROM calendar_events")
    void deleteAll();
    

    @Query("SELECT COUNT(*) FROM calendar_events")
    int getEventCount();
}
