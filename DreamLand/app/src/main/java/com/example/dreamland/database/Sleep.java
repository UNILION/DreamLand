package com.example.dreamland.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep")
public class Sleep {
    @PrimaryKey(autoGenerate = true)
    int sleepId;  // ID

    String sleepDate;  // 날짜 *
    String whenStart;  // 측정 시작 시간
    String whenSleep;  // 수면 시작 시간 *
    String sleepWaitTime;  // 잠들기까지 걸린 시간 *
    String whenWake;  // 기상 시간 *
    String sleepTime;  // 수면 시간
    String conStartTime;  // 상태 시작 시간
    String conStopTime;  //  상태 정지 시간
    String conTime;  // 상태 지속 시간 *
    int conStartPos;  // 상태 시작 자세
    int conStopPos;  // 상태 정지 자세
    int adjCount; // 교정 횟수
    int satLevel;  // 수면 만족도 *
    int oxyStr;  // 산소 포화도 *

    public Sleep(
            String sleepDate, String whenStart, String whenSleep, String sleepWaitTime,
            String whenWake, String sleepTime, String conStartTime, String conStopTime,
            String conTime, int conStartPos, int conStopPos, int adjCount,
            int satLevel, int oxyStr) {
        this.sleepDate = sleepDate;
        this.whenStart = whenStart;
        this.whenSleep = whenSleep;
        this.sleepWaitTime = sleepWaitTime;
        this.whenWake = whenWake;
        this.sleepTime = sleepTime;
        this.conStartTime = conStartTime;
        this.conStopTime = conStopTime;
        this.conTime = conTime;
        this.conStartPos = conStartPos;
        this.conStopPos = conStopPos;
        this.adjCount = adjCount;
        this.satLevel = satLevel;
        this.oxyStr = oxyStr;
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

    public String getSleepWaitTime() {
        return sleepWaitTime;
    }

    public void setSleepWaitTime(String sleepWaitTime) {
        this.sleepWaitTime = sleepWaitTime;
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

    public int getConStartPos() {
        return conStartPos;
    }

    public void setConStartPos(int conStartPos) {
        this.conStartPos = conStartPos;
    }

    public int getConStopPos() {
        return conStopPos;
    }

    public void setConStopPos(int conStopPos) {
        this.conStopPos = conStopPos;
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
}