package com.example.dreamland;

import android.util.Log;
import android.widget.Toast;

public class PostureInfo {
    private String currentPos = null;

    public static final String upPos = "정자세";
    public static final String downPos = "엎드림";
    public static final String leftPos = "왼쪽";
    public static final String rightPos = "오른쪽";


    public PostureInfo() {

    }

    public String getCurrentPos() {
        return currentPos;
    }

    public String setCurrentPos(String position, boolean isSense) {
        String[] posArr = position.split(",");
        if (posArr[0].equals("1") && posArr[2].equals("1") && posArr[4].equals("1")
                && posArr[1].equals("1") && posArr[3].equals("1") && posArr[5].equals("1")) {
            if (isSense) {
                currentPos = downPos;
            } else {
                currentPos = upPos;
            }
        } else if (posArr[0].equals("1") && posArr[2].equals("1") && posArr[4].equals("1")) {
            currentPos = leftPos;
        } else if (posArr[1].equals("1") && posArr[3].equals("1") && posArr[5].equals("1")) {
            currentPos = rightPos;
        } else {
            currentPos = "자세 정보 없음";
        }
        Log.d(MainActivity.STATE_TAG, "현제 자세 -> " + currentPos);
        return currentPos;
    }
}
