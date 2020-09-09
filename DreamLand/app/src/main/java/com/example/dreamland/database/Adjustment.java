package com.example.dreamland.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "adjustment")
public class Adjustment {
    @PrimaryKey(autoGenerate = true)
    private int adjId;  // ID
    private String sleepDate;  // sleep 테이블의 날짜
    private String adjTime;  // 교정 시각

    public Adjustment(String sleepDate, String adjTime) {
        this.sleepDate = sleepDate;
        this.adjTime = adjTime;
    }

    public int getAdjId() {
        return adjId;
    }

    public void setAdjId(int adjId) {
        this.adjId = adjId;
    }

    public String getSleepDate() { return sleepDate; }

    public void setSleepDate(String sleepDate) { this.sleepDate = sleepDate; }

    public String getAdjTime() {
        return adjTime;
    }

    public void setAdjTime(String adjTime) {
        this.adjTime = adjTime;
    }
}
