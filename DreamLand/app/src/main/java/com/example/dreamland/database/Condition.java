package com.example.dreamland.database;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "condition")
public class Condition {
    @PrimaryKey(autoGenerate = true)
    private int conId;         // ID
    private String conDate;      // 날짜
    private String startTime;  // 시작 시간
    private String endTime;    // 종료 시간

    public Condition(String conDate, String startTime, String endTime) {
        this.conDate = conDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getConId() {
        return conId;
    }

    public void setConId(int conId) {
        this.conId = conId;
    }

    public String getConDate() {
        return conDate;
    }

    public void setConDate(String conDate) {
        this.conDate = conDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
