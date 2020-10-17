package com.example.dreamland.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ConditionDao {
    @Query("SELECT * FROM condition")
    List<Condition> getAll();

    @Insert
    void insert(Condition condition);

    @Query("SELECT * FROM condition WHERE conDate=:date")
    List<Condition> getConsBySleepDate(String date);

    @Query("DELETE FROM condition")
    void deleteAll();
}
