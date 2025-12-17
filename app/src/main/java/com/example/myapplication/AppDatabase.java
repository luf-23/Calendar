package com.example.myapplication;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * 应用数据库
 */
@Database(entities = {CalendarEvent.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, EventTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "calendar_database";
    private static volatile AppDatabase INSTANCE;
    
    /**
     * 获取数据库单例
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .allowMainThreadQueries() // 注意：生产环境应该在后台线程操作
                    .fallbackToDestructiveMigration() // 数据库升级时清空数据（开发阶段）
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 获取事件 DAO
     */
    public abstract EventDao eventDao();
}
