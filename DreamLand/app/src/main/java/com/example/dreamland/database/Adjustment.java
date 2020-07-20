package com.example.dreamland.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "adjustment")
public class Adjustment {
    @PrimaryKey(autoGenerate = true)
    int adjId;  // ID
    int sleepNo;  // sleep 테이블의 ID
    String adjTime;  // 교정 시각

    public Adjustment(int sleepNo, String adjTime) {
        this.sleepNo = sleepNo;
        this.adjTime = adjTime;
    }
}
