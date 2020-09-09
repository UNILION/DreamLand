package com.example.dreamland.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep")
public class Sleep {
    @PrimaryKey(autoGenerate = true)
    private int sleepId;  // ID
    private String sleepDate;  // 날짜
    private String whenStart;  // 측정 시작 시간
    private String whenSleep;  // 수면 시작 시간
    private String whenWake;  // 기상 시간
    private String sleepTime;  // 수면 시간
    private String conStartTime;  // 상태 시작 시간
    private String conStopTime;  //  상태 정지 시간
    private String conTime;  // 상태 지속 시간
    private int adjCount; // 교정 횟수
    private int satLevel;  // 수면 만족도
    private int oxyStr;  // 산소 포화도
    private int sysAdjPos; // 시스템 교정 자세(전날 교정 후 자세)
    private int heartRate; // 심박수
    private int humidity; // 습도

    public Sleep(
            String sleepDate, String whenStart, String whenSleep, String whenWake,
            String sleepTime, String conStartTime, String conStopTime, String conTime,
            int adjCount, int satLevel, int oxyStr, int sysAdjPos, int heartRate, int humidity) {
        this.sleepDate = sleepDate;
        this.whenStart = whenStart;
        this.whenSleep = whenSleep;
        this.whenWake = whenWake;
        this.sleepTime = sleepTime;
        this.conStartTime = conStartTime;
        this.conStopTime = conStopTime;
        this.conTime = conTime;
        this.adjCount = adjCount;
        this.satLevel = satLevel;
        this.oxyStr = oxyStr;
        this.sysAdjPos = sysAdjPos;
        this.heartRate = heartRate;
        this.humidity = humidity;
    }

    public Sleep() {
    }

    public int getSleepId() {
        return sleepId;
    }

    public void setSleepId(int sleepId) {
        this.sleepId = sleepId;
    }

    public String getSleepDate() {
        return sleepDate;
    }

    public void setSleepDate(String sleepDate) {
        this.sleepDate = sleepDate;
    }

    public String getWhenStart() {
        return whenStart;
    }

    public void setWhenStart(String whenStart) {
        this.whenStart = whenStart;
    }

    public String getWhenSleep() {
        return whenSleep;
    }

    public void setWhenSleep(String whenSleep) {
        this.whenSleep = whenSleep;
    }

    public String getWhenWake() {
        return whenWake;
    }

    public void setWhenWake(String whenWake) {
        this.whenWake = whenWake;
    }

    public String getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }

    public String getConStartTime() {
        return conStartTime;
    }

    public void setConStartTime(String conStartTime) {
        this.conStartTime = conStartTime;
    }

    public String getConStopTime() {
        return conStopTime;
    }

    public void setConStopTime(String conStopTime) {
        this.conStopTime = conStopTime;
    }

    public String getConTime() {
        return conTime;
    }

    public void setConTime(String conTime) {
        this.conTime = conTime;
    }

    public int getAdjCount() {
        return adjCount;
    }

    public void setAdjCount(int adjCount) {
        this.adjCount = adjCount;
    }

    public int getSatLevel() {
        return satLevel;
    }

    public void setSatLevel(int satLevel) {
        this.satLevel = satLevel;
    }

    public int getOxyStr() {
        return oxyStr;
    }

    public void setOxyStr(int oxyStr) {
        this.oxyStr = oxyStr;
    }

    public int getSysAdjPos() {
        return sysAdjPos;
    }

    public void setSysAdjPos(int sysAdjPos) {
        this.sysAdjPos = sysAdjPos;
    }

    public int getHeartRate() { return heartRate; }

    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getHumidity() { return humidity; }

    public void setHumidity(int humidity) { this.humidity = humidity; }
}