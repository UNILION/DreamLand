package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

public class SleepingActivity extends AppCompatActivity {

    Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeping);
        stopButton = findViewById(R.id.stopButton);

        // 설정한 시간을 가져옴
        int hour = getIntent().getIntExtra("hour", -1);
        int minute = getIntent().getIntExtra("minute", -1);

        // 알람 설정
        setAlarm(hour, minute);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 알람 시작 함수
    public void setAlarm(int hour, int minute) {
        Intent alarmIntent = new Intent("ALARM_START");
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        // 설정한 시간으로 날짜를 설정
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        Toast.makeText(this, hour + "시 " + minute + "분에 깨워드릴게요", Toast.LENGTH_SHORT).show();

        // 알람매니저 설정
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}