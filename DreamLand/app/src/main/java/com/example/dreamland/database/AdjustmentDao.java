package com.example.dreamland.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AdjustmentDao {
    @Query("SELECT * FROM adjustment")
    List<Adjustment> getAll();

    @Insert
    void insert(Adjustment adjustment);

    @Query("SELECT * FROM adjustment WHERE sleepDate=:date")
    List<Adjustment> getAdjsBySleepDate(String date);

    @Query("DELETE FROM adjustment")
    void deleteAll();
}
