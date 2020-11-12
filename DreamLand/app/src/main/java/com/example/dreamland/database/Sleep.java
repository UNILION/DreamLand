package com.example.dreamland.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep")
public class Sleep {
    @PrimaryKey(autoGenerate = true)
    private int sleepId;  // ID
    private String sleepDate;  // 날짜
    private String whenStart;  // 측정 시작 시간
    private String whenSleep;  // 수면 시작 시간
    private String asleepAfter;  // 잠들기까지 걸린 시간
    private String whenWake;  // 기상 시간
    private String sleepTime;  // 수면 시간
    private String conTime;  // 상태 지속 시간
    private int adjCount; // 교정 횟수
    private int satLevel = 0;  // 수면 만족도
    private int oxyStr;  // 산소 포화도
    private int heartRate; // 심박수
    private int humidity; // 습도
    private int temperature; // 온도
    private int moved;  // 뒤척임 수
    private int score; // 건강 점수
    private String bestPosture = "정자세";  // 수면 최적 자세

    public Sleep(
            String sleepDate, String whenStart, String whenSleep, String asleepAfter,
            String whenWake, String sleepTime, String conTime, int adjCount, int satLevel,
            int oxyStr, int heartRate, int humidity, int temperature, int moved,
            int score, String bestPosture) {
        this.sleepDate = sleepDate;
        this.whenStart = whenStart;
        this.whenSleep = whenSleep;
        this.asleepAfter = asleepAfter;
        this.whenWake = whenWake;
        this.sleepTime = sleepTime;
        this.conTime = conTime;
        this.adjCount = adjCount;
        this.satLevel = satLevel;
        this.oxyStr = oxyStr;
        this.heartRate = heartRate;
        this.humidity = humidity;
        this.temperature = temperature;
        this.moved = moved;
        this.score = score;
        this.bestPosture = bestPosture;
    }

    public Sleep() {
    }

    @NonNull
    @Override
    public String toString() {
        return "sleepDate: " + sleepDate + " whenStart: " + whenStart + " whenSleep: " + whenSleep
                + "asleepAfter: " + asleepAfter + " whenWake: " + whenWake + " sleepTime: "
                + sleepTime + " conTime: " + conTime + " adjCount: " + adjCount + " satLevel: "
                + satLevel + " oxyStr: " + oxyStr + " heartRate: " + heartRate + "humidity: "
                + humidity + " temperature: " + temperature + " moved: " + moved + " score: " + score
                + " bestPosture: " + bestPosture;
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

    public String getAsleepAfter() { return asleepAfter; }

    public void setAsleepAfter(String asleepAfter) { this.asleepAfter = asleepAfter; }

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

    public int getHeartRate() { return heartRate; }

    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getHumidity() { return humidity; }

    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getTemperature() { return temperature; }

    public void setTemperature(int temperature) { this.temperature = temperature; }

    public int getMoved() { return moved; }

    public void setMoved(int moved) { this.moved = moved; }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }

    public String getBestPosture() { return bestPosture; }

    public void setBestPosture(String bestPosture) { this.bestPosture = bestPosture; }
}