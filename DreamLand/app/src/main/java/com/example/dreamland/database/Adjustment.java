package com.example.dreamland.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "adjustment")
public class Adjustment {
    @PrimaryKey(autoGenerate = true)
    private int adjId;  // ID
    private String sleepNo;  // sleep 테이블의 ID
    private String adjTime;  // 교정 시각

    public Adjustment(String sleepNo, String adjTime) {
        this.sleepNo = sleepNo;
        this.adjTime = adjTime;
    }

    public int getAdjId() {
        return adjId;
    }

    public void setAdjId(int adjId) {
        this.adjId = adjId;
    }

    public String getSleepNo() {
        return sleepNo;
    }

    public void setSleepNo(String sleepNo) {
        this.sleepNo = sleepNo;
    }

    public String getAdjTime() {
        return adjTime;
    }

    public void setAdjTime(String adjTime) {
        this.adjTime = adjTime;
    }
}
