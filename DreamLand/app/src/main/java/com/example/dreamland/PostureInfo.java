package com.example.dreamland;

import android.util.Log;
import android.widget.Toast;

public class PostureInfo {
    private String currentPos;

    public static final String upPos = "정자세";
    public static final String downPos = "엎드림";
    public static final String leftPos = "왼쪽";
    public static final String rightPos = "오른쪽";


    public PostureInfo() {
        currentPos = upPos;
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
}
