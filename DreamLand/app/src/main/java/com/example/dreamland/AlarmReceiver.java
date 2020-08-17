package com.example.dreamland;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmTest", "AlarmReceiver - onReceive");
        Toast.makeText(context, "알람 울림", Toast.LENGTH_SHORT).show();
    }
}
