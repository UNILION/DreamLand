package com.example.dreamland.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Sleep.class, Adjustment.class, Condition.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SleepDao sleepDao();
    public abstract AdjustmentDao adjustmentDao();
    public abstract ConditionDao conditionDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,"dreamland-database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
