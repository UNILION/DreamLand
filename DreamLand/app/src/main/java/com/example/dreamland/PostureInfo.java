package com.example.dreamland;

import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;

public class PostureInfo {
    private String currentPos;
    private long[] durations;
    private long startTime;
    private int posIndex;

    public String[] postures = {"정자세", "왼쪽", "오른쪽"};

    public static final String upPos = "정자세";
    public static final String downPos = "엎드림";
    public static final String leftPos = "왼쪽";
    public static final String rightPos = "오른쪽";


    public PostureInfo() {
        currentPos = upPos;
        durations = new long[3];
    }

    public String getCurrentPos() {
        return currentPos;
    }

    public String setCurrentPos(String position, boolean isSense) {
        String[] posArr = position.split(",");
        if (posArr[2].equals("1") && posArr[4].equals("1") && posArr[3].equals("1")
                && posArr[5].equals("1")) {
            if (isSense) {  // 이산화탄소가 감지되면 엎드림
                currentPos = downPos;
            } else {
                currentPos = upPos;
            }
        } else if (posArr[2].equals("1") && posArr[4].equals("1")) {
            currentPos = leftPos;
        } else if (posArr[3].equals("1") && posArr[5].equals("1")) {
            currentPos = rightPos;
        } else {  // 자세 판별이 되지 않을 경우 정자세
            currentPos = upPos;
        }
        Log.d(MainActivity.STATE_TAG, "현제 자세 -> " + currentPos);
        return currentPos;
    }

    // 시간 측정 시작
    public void start() {
        startTime = new Date().getTime();
        switch (currentPos) {
            case "정자세":
                posIndex = 0;
                break;
            case "왼쪽":
                posIndex = 1;
                break;
            case "오른쪽":
                posIndex = 2;
                break;
            default:
        }
    }

    // 시간 측정 종료
    public void stop() {
        long duration = new Date().getTime() - startTime;
        durations[posIndex] += duration;
        startTime = 0L;
    }

    // 코골이나 무호흡 시간이 가장 작은 자세를 반환
    public String getBestPosture() {
        long min = durations[0];
        int index = 0;
        for (int i = 1; i < durations.length; i++) {
            if (min > durations[i]) {
                min = durations[i];
                index = i;
            }
        }
        durations = new long[3];
        return postures[index];
    }
}
