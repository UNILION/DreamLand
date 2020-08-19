package com.example.dreamland.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SleepDao {
    @Query("SELECT * FROM sleep")
    LiveData<List<Sleep>> getAll();

    @Query("SELECT * FROM sleep ORDER BY sleepId DESC LIMIT 1")
    LiveData<Sleep> getLastSleep();

    @Query("SELECT * FROM sleep ORDER BY sleepId LIMIT 1")
    LiveData<Sleep> getFirstSleep();

    @Query("SELECT * FROM sleep WHERE sleepDate=:date")
    Sleep getSleepByDate(String date);

    @Query("SELECT * FROM sleep WHERE sleepId=:id")
    Sleep getSleepById(int id);

    @Query("DELETE FROM sleep")
    void deleteAll();

    @Query("SELECT * FROM sleep ORDER BY sleepId DESC LIMIT 7")
    LiveData<List<Sleep>> getRecentSleeps();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sleep sleep);
}
