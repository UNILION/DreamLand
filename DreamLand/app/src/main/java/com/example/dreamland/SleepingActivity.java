package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class SleepingActivity extends AppCompatActivity {

    Button stopButton;
    ImageView ivSleepSate;
    TextView tvSleepState;
    TextView tvTime;
    AlarmManager alarmManager;
    AlarmManager.OnAlarmListener onAlarmListener;
    boolean isSleep = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeping);
        stopButton = findViewById(R.id.stopButton);
        ivSleepSate = findViewById(R.id.ivSleepState);
        tvSleepState = findViewById(R.id.tvSleepState);
        tvTime = findViewById(R.id.tvTime);

        // 설정한 시간을 가져옴
        int hour = getIntent().getIntExtra("hour", -1);
        int minute = getIntent().getIntExtra("minute", -1);

        // 선택한 교정 방식
        int selectedMenu = getIntent().getIntExtra("selectedMenu", -1);

        //알람 시간 텍스트뷰
        if (minute < 10) {
            tvTime.setText(hour + ":0" + minute);
        } else {
            tvTime.setText(hour + ":" + minute);
        }

        // 알람 설정
        setAlarm(hour, minute);

        // 중지 버튼
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSleep) {
                    // 수면 데이터 저장
                } else {
                    alarmManager.cancel(onAlarmListener); // 알람 취소
                }
                finish();
            }
        });
    }

    // 알람 시작 함수
    public void setAlarm(int hour, int minute) {
        // 설정한 시간으로 날짜를 설정
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        Toast.makeText(this, hour + "시 " + minute + "분에 깨워드릴게요", Toast.LENGTH_SHORT).show();

        // 알람매니저 설정
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        onAlarmListener = new AlarmManager.OnAlarmListener() {
            @Override
            public void onAlarm() { // 알람 시간이 되었을때
                Log.d("AlarmTest", "onAlarm");
                ivSleepSate.setImageResource(R.drawable.ic_alarm_256dp);
                tvSleepState.setText("일어나세요!");
            }
        };

        // 알람매니저에 리스너와 시간을 설정
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                "Alarm",
                onAlarmListener,
                null
        );
    }
}