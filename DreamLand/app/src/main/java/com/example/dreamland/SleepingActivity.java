package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class SleepingActivity extends AppCompatActivity {

    public final static int STATE_SLEEP = 0;
    public final static int STATE_SNORING = 1;
    public final static int STATE_APNEA = 2;
    public final static int STATE_ALARM = 3;

    static Context mContext;
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // 화면 켜짐 유지

        mContext = this;

        stopButton = findViewById(R.id.stopButton);
        ivSleepSate = findViewById(R.id.ivSleepState);
        tvSleepState = findViewById(R.id.tvSleepState);
        tvTime = findViewById(R.id.tv_time);

        ((MainActivity) MainActivity.context).isStarted = true;

        // 이미지 애니메이션
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pongpong);
        ivSleepSate.startAnimation(animation);

        // 설정한 시간을 가져옴
        int hour = getIntent().getIntExtra("hour", -1);
        int minute = getIntent().getIntExtra("minute", -1);

        //알람 시간 텍스트뷰
        if (minute < 10) {
            tvTime.setText(hour + ":0" + minute);
        } else {
            tvTime.setText(hour + ":" + minute);
        }

        // 알람 설정
        setAlarm(hour, minute);

        // 즉시 교정을 선택하면 교정
        if (((MainActivity) MainActivity.context).adjMode == 3) {
            ((MainActivity) MainActivity.context).adjustPostureImmediately();
        }

        // 가습기 사용 여부 메시지 전송
        ((MainActivity) MainActivity.context).sendHumidifierMode();

        // 질환 완화 모드이면 측정 시작시 교정
        if (((MainActivity) MainActivity.context).mode == InitActivity.DISEASE_ALLEVIATION_MODE) {
            ((MainActivity) MainActivity.context).sendAct();
        }

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
                changeState(3);  // 리소스 변경

                // 수면 중지
                ((MainActivity) MainActivity.context).stopSleep();

                // 침대에 알람 메시지 전송
                ((MainActivity) MainActivity.context).bluetoothService.writeBLT1("alarm");
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

    // 상태 이미지와 문구를 변경하는 함수
    public void changeState(int state) {
        int res;
        String text;
        switch (state) {
            case STATE_SLEEP:  // 잠에 든 상태
                res = R.drawable.ic_sleep_256dp;
                text = "수면 중이에요";
                break;
            case STATE_SNORING:  // 코골이
                res = R.drawable.ic_blue_zzz_256dp;
                text = "코골이 중이에요";
                break;
            case STATE_APNEA:
                res = R.drawable.ic_crying_256dp;
                text = "무호흡상태에요";
                break;
            case STATE_ALARM:
                res = R.drawable.ic_alarm_256dp;
                text = "일어나세요!";
                break;
            default:
                res = R.drawable.ic_sleep_256dp;
                text = "default";
        }
        ivSleepSate.setImageResource(res);
        tvSleepState.setText(text);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stop, R.anim.up_out);
    }
}