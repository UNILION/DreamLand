package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.dreamland.database.Sleep;

import java.util.Calendar;

import static com.example.dreamland.MySimpleDateFormat.sdf1;

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
    boolean isReceivedBandMsg = false;
    MainActivity mainActivity;
    // 테스트를 위한 버튼
    ToggleButton testButton;
    Button testStart;
    Button testStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeping);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // 화면 켜짐 유지

        mContext = this;
        mainActivity = (MainActivity) MainActivity.context;

        stopButton = findViewById(R.id.stopButton);
        ivSleepSate = findViewById(R.id.ivSleepState);
        tvSleepState = findViewById(R.id.tvSleepState);
        tvTime = findViewById(R.id.tv_time);
        
        testButton = findViewById(R.id.btn_test_action);
        testButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switch (mainActivity.mode) {
                    case InitActivity.SNORING_PREVENTION_MODE:
                        if (b) {
                            mainActivity.bluetoothMessageHandler.processCommand("SOU:80");
                        } else {
                            mainActivity.isAdjust = false;
                            mainActivity.noConditionCount = 5;
                            mainActivity.bluetoothMessageHandler.processCommand("SOU:40");
                        }
                        break;
                    case InitActivity.APNEA_PREVENTION_MODE:
                        if (b) {
                            mainActivity.lowDecibelCount = 6;
                            mainActivity.bluetoothMessageHandler.processCommand("spo:90");
                            mainActivity.bluetoothService.writeBLT2("M_ON");
                        } else {
                            mainActivity.isAdjust = false;
                            mainActivity.noConditionCount = 4;
                            mainActivity.bluetoothMessageHandler.processCommand("spo:100");
                        }
                        break;
                }
            }
        });
        testStart = findViewById(R.id.btn_start);
        testStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.isSleep) {
                    String whenSleep = sdf1.format(Calendar.getInstance().getTime());
                    mainActivity.sleep.setWhenSleep(whenSleep); // 잠에 든 시각
                    mainActivity.isSleep = true;
                    Log.d(MainActivity.STATE_TAG, "사용자가 잠에 들었습니다 / " + mainActivity.sleep.getWhenSleep());
                    ((SleepingActivity) SleepingActivity.mContext).changeState(
                            SleepingActivity.STATE_SLEEP);
                    Toast.makeText(SleepingActivity.this,
                            "사용자가 잠에 들었습니다", Toast.LENGTH_SHORT).show();

                    // 잠들기까지 걸린 시간
                    String asleepAfter = mainActivity.getAsleepAfter(whenSleep, mainActivity.sleep.getWhenStart());
                    mainActivity.sleep.setAsleepAfter(asleepAfter);
                    Log.d(MainActivity.STATE_TAG, "잠들기까지 걸린 시간 / " + mainActivity.sleep.getAsleepAfter());
                }
            }
        });
        testStop = findViewById(R.id.btn_stop);
        testStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.stopSleep();
            }
        });
        setButtonsVisibility(mainActivity.isVisible);

        mainActivity.isStarted = true;

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

        // 밴드에 시작 메시지 전송
        new Thread() {
            @Override
            public void run() {
                while (!isReceivedBandMsg) {
                    try {
                        mainActivity.bluetoothService.writeBLT3("alarm");
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(MainActivity.STATE_TAG, "밴드 메시지 응답받음");
            }
        }.start();
        Log.d(mainActivity.STATE_TAG, "밴드에 alarm 전송");

        // 즉시 교정을 선택하면 교정
        if (mainActivity.adjMode == 3) {
            mainActivity.maintainPosture();
        }
        if (mainActivity.adjMode == 4) {  // 허리디스크 자세 교정
            mainActivity.act = MainActivity.ACT_DISC;
            mainActivity.maintainPosture();
        }

        // 가습기 사용 여부 메시지 전송
        mainActivity.sendHumidifierMode();

        // 중지 버튼
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mainActivity.isSleep) {
                    alarmManager.cancel(onAlarmListener); // 알람 취소
                }
                setResult(99);
                finish();
            }
        });
    }

    public void setButtonsVisibility(boolean b) {
        if (!b) {
            testButton.setBackgroundColor(Color.TRANSPARENT);
            testStart.setBackgroundColor(Color.TRANSPARENT);
            testStop.setBackgroundColor(Color.TRANSPARENT);
            testButton.setText("");
            testStart.setText("");
            testStop.setText("");
        }
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
                changeState(3);  // 리소스 변경

                // 침대에 알람 메시지 전송
                mainActivity.bluetoothService.writeBLT1("alarm");
                Log.d(MainActivity.STATE_TAG, "alarm 전송");
                mainActivity.isAlarm = true;
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

    @Override
    protected void onDestroy() {
        if (mainActivity.isAlarm) {
            mainActivity.isAlarm = false;
        } else {
            mainActivity.bluetoothService.writeBLT1("down");
            Log.d(MainActivity.STATE_TAG, "down 전송");
        }
        mainActivity.bluetoothService.writeBLT2("H2O_OFF");  // 가습기 off
        Log.d(MainActivity.STATE_TAG, "가습기 Off");
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(10000);
                    mainActivity.bluetoothService.writeBLT2("O2_OFF");  // 산소발생기 off
                    Log.d(MainActivity.STATE_TAG, "산소발생기 Off");
                    sleep(10000);
                    mainActivity.bluetoothService.writeBLT2("Lamp_OFF");  // LED off
                    Log.d(MainActivity.STATE_TAG, "모든 LED Off");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        mainActivity.isGetStart = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}