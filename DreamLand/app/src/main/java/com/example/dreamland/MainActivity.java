package com.example.dreamland;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.dreamland.asynctask.DeleteAdjAsyncTask;
import com.example.dreamland.asynctask.DeleteConAsyncTask;
import com.example.dreamland.asynctask.DeleteSleepAsyncTask;
import com.example.dreamland.asynctask.InsertAdjAsyncTask;
import com.example.dreamland.asynctask.InsertConAsyncTask;
import com.example.dreamland.asynctask.InsertSleepAsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AppDatabase;
import com.example.dreamland.database.Condition;
import com.example.dreamland.database.Sleep;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.willy.ratingbar.ScaleRatingBar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import iammert.com.library.StatusView;

import static com.example.dreamland.MySimpleDateFormat.sdf1;
import static com.example.dreamland.MySimpleDateFormat.sdf3;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_ENABLE_BT = 111;
    final int RC_INIT_ACTIVITY = 1000;
    public static final int RC_SLEEPING_ACTIVITY = 2000;
    private final int DOWN_WAIT_TIME = 1000 * 30;  // 엑추에이터 내림 대기시간
    public static final String COMMAND_TAG = "BT-CMD";  // 블루투스 메시지
    public static final String STATE_TAG = "BT-STATE";  // 수면 상태 메시지
    public static final String ACT_LEFT = "0,1,0,1,0,1,0,1,0,0";  // 자세를 왼쪽으로 교정
    public static final String ACT_RIGHT = "1,0,1,0,1,0,1,0,0,0";  // 자세를 오른쪽으로 교정
    public static final String ACT_DISC = "0,0,0,0,0,0,0,0,1,1";  // 허리디스크 교정 자세


    private StartFragment startFragment;
    private ManagementFragment managementFragment;
    public SettingFragment settingFragment;
    private HealthFragment healthFragment;
    private Fragment curFragment;
    private StatusView statusView;
    public static Context context;

    private AppDatabase db;
    private ActionBar actionBar;
    private BottomNavigationView bottomNavigation;
    public SharedPreferences sf;
    public List<Sleep> sleepList;
    BluetoothAdapter bluetoothAdapter;
    BluetoothService bluetoothService;
    ArrayList<BluetoothSocket> bluetoothSocketArrayList = null;
    public BluetoothMessageHandler bluetoothMessageHandler;
    PostureInfo postureInfo;  // 현제 자세 정보

    boolean isSleep = false; // 잠에 들었는지 여부
    boolean isAdjust = false; // 교정 중인지 여부
    boolean isSense = false; // 이산화탄소 감지 여부
    boolean isCon = false;  // 상태 지속 여부
    boolean isStarted = false;  // 수면 측정 여부
    boolean adjEnd = false;  // 교정 횟수를 제한하기 위한 변수
    boolean isAlarm = false;  // 알람이 울렸는지 여부
    boolean isLEDOnL = false;  // 왼쪽 이산화탄소 LED 켜짐 여부
    boolean isLEDOnR = false;  // 오른쪽 이산화탄소 LED 켜짐 여부
    boolean isLEDOnM = false;  // 중앙 이산화탄소 LED 켜짐 여부

    boolean isVisible = true;
    boolean isGetStart = false;

    ArrayList<Integer> heartRates;
    int currentHeartRate;
    ArrayList<Integer> oxygenSaturations; // 산소포화도 리스트
    int currentOxy;
    ArrayList<Integer> humidities; // 습도 리스트
    int currentHumidity;
    ArrayList<Integer> temps; // 온도 리스트

    int currentTemp;
    ArrayList<Integer> problems; // 코골이, 무호흡 리스트
    Sleep sleep;
    int adjCount;  // 자세 교정 횟수
    int mode;  // 모드
    int adjMode = 0;  // 교정 모드
    boolean customAct = false;  // 사용자 설정 여부
    boolean autoHumidifier = true;  // 가습기 사용 여부
    boolean useHumidifier = false;
    boolean useO2 = false;
    long conMilliTime = 0L;  // 상태 지속 총 시간
    long conStartTime = 0L;  // 상태 시작 시간
    long conEndTime = 0L;  // 상태 종료 시간
    int noConditionCount = 0;  // 정상 상태 감지 카운트
    int lowDecibelCount = 0;
    int moved = 0;
    int initCO2M = -1;
    int initCO2R = -1;
    int initCO2L = -1;

    String act;
    String beforePos = null;  // 교정 전 자세
    String afterPos = null;  // 교정 후 자세

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        heartRates = new ArrayList<>();
        oxygenSaturations = new ArrayList<>();
        humidities = new ArrayList<>();
        temps = new ArrayList<>();
        problems = new ArrayList<>();
        sleep = new Sleep();
        adjCount = 0;
        postureInfo = new PostureInfo();

        statusView = (StatusView) findViewById(R.id.status);

        bluetoothSocketArrayList = new ArrayList<>();
        bluetoothMessageHandler = new BluetoothMessageHandler();


        bluetoothService = BluetoothService.getInstance();
        bluetoothService.setContext(this);
        bluetoothService.setHandler(bluetoothMessageHandler);

        sf = getSharedPreferences("bed", MODE_PRIVATE);


        mode = sf.getInt("mode", 0);
        customAct = sf.getBoolean("customAct", false);

        // 모드 설정값이 없으면 모드 선택 액티비티로 이동
        if (mode == 0) {
            Intent initIntent = new Intent(this, InitActivity.class);
            startActivityForResult(initIntent, 1000);
        }

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);

        startFragment = new StartFragment();
        managementFragment = new ManagementFragment();
        settingFragment = new SettingFragment();
        healthFragment = new HealthFragment();
        actionBar = getSupportActionBar();

        // 화면에 프래그먼트 추가
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, startFragment)
                .add(R.id.container, managementFragment)
                .hide(managementFragment)
                .add(R.id.container, settingFragment)
                .hide(settingFragment)
                .add(R.id.container, healthFragment)
                .hide(healthFragment).commit();

        curFragment = startFragment;
        actionBar.setTitle("수면시작");

        // 하단 탭 클릭시 화면 전환
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // 수면시작 버튼
                    case R.id.tab_start:
                        if (curFragment != startFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(startFragment)
                                    .hide(curFragment).commit();
                            curFragment = startFragment;
                            actionBar.setTitle("수면시작");
                        }
                        return true;

                    // 수면일지 버튼
                    case R.id.tab_management:
                        if (curFragment != managementFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(managementFragment)
                                    .hide(curFragment).commit();
                            curFragment = managementFragment;
                            actionBar.setTitle("수면일지");
                        }
                        return true;

                    // 건강상태 버튼
                    case R.id.tab_health:
                        if (curFragment != healthFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(healthFragment)
                                    .hide(curFragment).commit();
                            curFragment = healthFragment;
                            actionBar.setTitle("건강상태");
                        }
                        return true;

                    // 설정 버튼
                    case R.id.tab_setting:
                        if (curFragment != settingFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(settingFragment)
                                    .hide(curFragment).commit();
                            curFragment = settingFragment;
                            actionBar.setTitle("설정");
                        }
                        return true;

                    default:
                        return false;

                }
            }
        });

        db = AppDatabase.getDatabase(this); // db 생성

        // sleep 테이블의 모든 레코드 관찰
        db.sleepDao().getAll().observe(this, new Observer<List<Sleep>>() {
            @Override
            public void onChanged(List<Sleep> sleeps) {
                sleepList = sleeps;
                managementFragment.sleepList = sleeps;
                managementFragment.switchScreen();
                healthFragment.switchScreen();
                managementFragment.updateUI();
            }
        });

        // 샘플 데이터 삽입
        insertSampleData();
    }

    // 초기화 함수
    public void resetData() {
        sf.edit().putInt("mode", 0).apply();
        sf.edit().putInt("disease", 0).apply();
        new DeleteSleepAsyncTask(db.sleepDao()).execute();
        new DeleteAdjAsyncTask(db.adjustmentDao()).execute();
        new DeleteConAsyncTask(db.conditionDao()).execute();
        finish();
    }

    public void enableBluetooth() { // 블루투스 활성화 함수
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않는 기기", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectDevices();
        }
    }

    // 기기 연결 함수
    public void connectDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.d(STATE_TAG, "페어링된 기기");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(STATE_TAG, deviceName + " " + deviceHardwareAddress);
                // 기기 이름이 BLT1, BLT2, JCNET-JARDUINO-7826인 경우 연결
                if (deviceName.equals("BLT1") || deviceName.equals("BLT2")
                        || deviceName.equals("JCNET-JARDUINO-7826")) {
                    bluetoothService.connect(device);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    // 수면 시작 시간 랜덤 생성 함수
    private String createRandomStartTime() {
        Calendar calendar = Calendar.getInstance();

        int hour = (int) (Math.random() * 4) + 22; // 시간 랜덤 생성 22 ~ 01시 사이
        if (hour >= 24) {
            hour -= 24;
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        int minute = (int) (Math.random() * 59); // 분 랜덤 생성 0 ~ 59분 사이
        calendar.set(Calendar.MINUTE, minute);
        return sdf1.format(calendar.getTime());
    }

    // 잠에 든 시간 랜덤 생성 함수
    private String createRandomWhenSleep(String startTime) {
        Date date;
        Calendar calendar = null;
        try {
            date = sdf1.parse(startTime);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
            int additionalTime = (int) (Math.random() * 30) + 3;
            calendar.add(Calendar.MINUTE, additionalTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf1.format(calendar.getTime());
    }

    // 기상 시각 랜덤 생성 함수
    private String createRandomWhenWake() {
        Calendar calendar = Calendar.getInstance();

        int hour = (int) (Math.random() * 4) + 6; // 시간 랜덤 생성 22 ~ 01시 사이
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        int minute = (int) (Math.random() * 59); // 분 랜덤 생성 0 ~ 59분 사이
        calendar.set(Calendar.MINUTE, minute);
        return sdf1.format(calendar.getTime());
    }

    // 잠에 든 시간 랜덤 생성 함수
    private String createRandomConTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        int minute = (int) (Math.random() * 40);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.MINUTE, minute);
        return sdf1.format(calendar.getTime());
    }

    // 잠들기까지 걸린 시간을 반환하는 함수
    public String getAsleepAfter(String whenSleep, String whenStart) {
        long diffTime = 0L;
        String asleepAfter = "";
        try {
            diffTime = sdf1.parse(whenSleep).getTime() - sdf1.parse(whenStart).getTime();
            diffTime -= (1000 * 60 * 60 * 9); // 기본 9시간을 뺌
            asleepAfter = sdf1.format(diffTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return asleepAfter;
    }

    // 수면 시간을 반환하는 함수
    private String getSleepTime(String whenSleep, String whenWake) {
        String sleepTime = "";
        try {
            Date startTime = sdf1.parse(whenSleep);
            long diff = sdf1.parse(whenWake).getTime() - startTime.getTime()
                    - 1000 * 60 * 60 * 9;
            sleepTime = sdf1.format(diff);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sleepTime;
    }

    private void insertSampleData() {
        Calendar c = Calendar.getInstance();
        String sleepDate;
        c.add(Calendar.DAY_OF_MONTH, -190);
        for (int i = 0; i < 190; i++) {
            sleepDate = sdf3.format(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, 1);

            String whenStart = createRandomStartTime();
            String whenSleep = createRandomWhenSleep(whenStart);
            String whenWake = createRandomWhenWake();
            int heartRate = (int) (Math.random() * 50) + 40;
            int spo = (int) (Math.random() * 14) + 88;
            // 샘플 데이터 생성
            new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                    sleepDate, whenSleep, whenStart, getAsleepAfter(whenSleep, whenStart),
                    whenWake, getSleepTime(whenSleep, whenWake), createRandomConTime(),
                    (int) (Math.random() * 7), (int) (Math.random() * 5) + 1,
                    spo, heartRate, (int) (Math.random() * 50) + 10,
                    (int) (Math.random() * 5) + 20, (int) (Math.random() * 7),
                    getScore(spo, heartRate), postureInfo.postures[(((int) (Math.random() * 5)) % 3 + 1) % 3])
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // 샘플 데이터 삽입
            case R.id.insert_sleeps:
                insertSampleData();
                return true;

            // 수면 데이터 모두 삭제
            case R.id.delete_sleeps:
                new DeleteSleepAsyncTask(db.sleepDao()).execute();
                new DeleteAdjAsyncTask(db.adjustmentDao()).execute();
                new DeleteConAsyncTask(db.conditionDao()).execute();
                return true;

            // 오늘 수면 데이터 삽입
            case R.id.insert_today_sleep:
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        sdf3.format(new Date()), "00:11", "00:41", "00:31",
                        "09:02", "08:20", "00:06", 3, 1,
                        88, 47, 49, 20, 6, 27, "정자세")
                );
                return true;

            // 엑추에이터 제어
            case R.id.test_act:
                final View dlgView = getLayoutInflater().from(this).inflate(
                        R.layout.dialog_bed_act_test, null);

                final ToggleButton[] actButtons = new ToggleButton[9];
                int[] buttonIds = {R.id.actButton0, R.id.actButton1,
                        R.id.actButton2, R.id.actButton3, R.id.actButton4, R.id.actButton5,
                        R.id.actButton6, R.id.actButton7, R.id.actButton8};

                for (int i = 0; i < 9; i++) {
                    actButtons[i] = (ToggleButton) dlgView.findViewById(buttonIds[i]);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog dialog = builder.setView(dlgView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                // 완료 버튼
                Button completeButton = dlgView.findViewById(R.id.yesButton);
                completeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StringBuilder actStr = new StringBuilder();
                        for (int i = 0; i < 9; i++) {
                            if (actButtons[i].isChecked()) {
                                actStr.append("1,");
                                if (i == 8) {
                                    actStr.append("1,");
                                }
                            } else {
                                actStr.append("0,");
                                if (i == 8) {
                                    actStr.append("0,");
                                }
                            }
                        }
                        actStr.deleteCharAt(actStr.length() - 1);
                        bluetoothService.writeBLT1("act:" + actStr);
                    }
                });

                Button downButton = dlgView.findViewById(R.id.downButton);
                downButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bluetoothService.writeBLT1("down");
                    }
                });
                return true;

            case R.id.test_hum_on:
                bluetoothService.writeBLT2("H2O_ON");
                return true;
            case R.id.test_hum_off:
                bluetoothService.writeBLT2("H2O_OFF");
                return true;
            case R.id.test_O2_on:
                bluetoothService.writeBLT2("O2_ON");
                return true;
            case R.id.test_O2_off:
                bluetoothService.writeBLT2("O2_OFF");
                return true;
            case R.id.test_m_led_on:
                bluetoothService.writeBLT2("M_ON");
                return true;
            case R.id.test_m_led_off:
                bluetoothService.writeBLT2("M_OFF");
                return true;
            case R.id.test_l_led_on:
                bluetoothService.writeBLT2("L_ON");
                return true;
            case R.id.test_l_led_off:
                bluetoothService.writeBLT2("L_OFF");
                return true;
            case R.id.test_r_led_on:
                bluetoothService.writeBLT2("R_ON");
                return true;
            case R.id.test_r_led_off:
                bluetoothService.writeBLT2("R_OFF");
                return true;
            case R.id.test_lamp_off:
                bluetoothService.writeBLT2("Lamp_OFF");
                return true;
            case R.id.test_alarmout:
                bluetoothService.writeBLT1("alarmout");
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_INIT_ACTIVITY:
                if (resultCode == 1001) {  // 코골이 모드 선택
                    mode = InitActivity.SNORING_PREVENTION_MODE;
                    settingFragment.hideDiseaseView();
                } else if (resultCode == 1002) {  // 무호흡 모드 선택
                    mode = InitActivity.APNEA_PREVENTION_MODE;
                    settingFragment.hideDiseaseView();
                    managementFragment.changeConditionView();
                    healthFragment.changeView();
                } else if (resultCode == 1003) {  // 질환 모드 선택
                    mode = InitActivity.DISEASE_ALLEVIATION_MODE;
                }
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) { // 블루투스 활성화 성공
                    Log.d(STATE_TAG, "블루투스 활성화 성공");
                    connectDevices();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 실패
                    Log.d(STATE_TAG, "블루투스 활성화 실패");
                }
                break;

            case RC_SLEEPING_ACTIVITY: // 수면 중지
                if (resultCode == 99) {
                    stopSleep();  // 측정 중지
                }
                break;
        }
    }

    void stopSleep() { // 측정 중지
        if (isSleep) { // 잠에 들었다가 중지했을 경우

            Calendar calendar = Calendar.getInstance();
            String whenWake = sdf1.format(calendar.getTime());

            String sleepTime = getSleepTime(sleep.getWhenSleep(), whenWake);
            sleep.setSleepTime(sleepTime);
            sleep.setWhenWake(whenWake);
            int heartRate = getAverage(heartRates);  // 심박수 평균
            sleep.setHeartRate(heartRate);
            int spo = getAverage(oxygenSaturations);  // 산소포화도 평균
            sleep.setOxyStr(spo);
            sleep.setHumidity(getAverage(humidities));  // 습도 평균
            sleep.setTemperature(getAverage(temps));  // 온도 평균
            sleep.setAdjCount(adjCount);  // 교정 횟수
            sleep.setScore(getScore(spo, heartRate)); // 건강 점수
            Date date = new Date(conMilliTime - (1000 * 60 * 60 * 9));
            String conTime = sdf1.format(date);
            sleep.setConTime(conTime);
            sleep.setBestPosture(postureInfo.getBestPosture());
            Toast.makeText(context, "수면 종료", Toast.LENGTH_SHORT).show();
            Log.d(STATE_TAG,
                    "일자: " + sleep.getSleepDate()
                            + "  시작 시간: " + sleep.getWhenStart()
                            + "  잠에 든 시각: " + sleep.getWhenSleep()
                            + "  잠들기까지 걸린 시간: " + sleep.getAsleepAfter()
                            + "  기상 시각: " + sleep.getWhenWake()
                            + "  수면 시간: " + sleep.getSleepTime()
                            + "  상태 시간: " + sleep.getConTime()
                            + " \n심박수: " + sleep.getHeartRate()
                            + "  산소포화도: " + sleep.getOxyStr()
                            + "  습도: " + sleep.getHumidity()
                            + "  교정 횟수: " + sleep.getAdjCount()
                            + "  건강 점수: " + sleep.getScore()
                            + "  최적 자세: " + sleep.getBestPosture());

            // 만족도 평가 다이얼로그
            View dlgView = getLayoutInflater().from(this).inflate(
                    R.layout.dialog_sat_level, null);
            Button satConfirmButton = dlgView.findViewById(R.id.sat_confirm_button);
            final ScaleRatingBar scaleRatingBar = dlgView.findViewById(R.id.dialog_rating_bar);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder.setView(dlgView).create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();


            // 평가 버튼
            satConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sleep.setSatLevel((int) scaleRatingBar.getRating());
                    new InsertSleepAsyncTask(db.sleepDao()).execute(sleep);
                    sleep = new Sleep();
                    dialog.dismiss();
                }
            });

            isSleep = false;
        }
        isStarted = false;
        clearData();

        // 알람이 울렸으면 알람 정지 메시지 보냄
        if (isAlarm) {
            bluetoothService.writeBLT1("alarmout");
            Log.d(STATE_TAG, "alarmout 전송");
        }
        ((SleepingActivity) SleepingActivity.mContext).finish();
    }

    // 건강 점수
    int getScore(int spo, int heartRate) {
        int spoScore;
        int heartRateScore;
        if (spo >= 95) {  // 산소포화도 95이상, 정상
            spoScore = 0;
        } else if (spo >= 91) { // 산소포화도 91이상 95미만, 정상 이하
            spoScore = 10 * (95 - spo);
        } else {  // 진단 필요
            spoScore = 60;
        }

        if (heartRate >= 50 && heartRate <= 100) { // 심박수 정상 수치
            heartRateScore = 0;
        } else if (heartRate > 100) {  // 정상 수치보다 높음
            heartRateScore = heartRate - 100;
        } else {  // 정상 수치보다 낮음
            heartRateScore = 50 - heartRate;  // 정상 수치의 최소인 50에서 1이 떨어지면 1점 증가
        }

        int warningCount;
        // 감점이 있는 경우 경고
        if (heartRate > 0 || spoScore > 0) {
            warningCount = sf.getInt("warningCount", 0);
            warningCount++;
            if (warningCount > 3) {
                warningCount = 3;
            }
        } else {
            warningCount = 0;
        }
        sf.edit().putInt("warningCount", warningCount).apply();

        return Math.max(100 - (warningCount * (heartRateScore + spoScore)), 0);
    }

    // 입력값들의 평균을 구하는 함수
    int getAverage(ArrayList<Integer> arr) {
        if (arr.size() == 0) {
            return 0;
        }
        int sum = 0;
        for (Integer num : arr) {
            sum += num;
        }
        return sum / arr.size();
    }

    // 잠에서 깬 후 데이터 삭제
    void clearData() {
        heartRates.clear();
        humidities.clear();
        oxygenSaturations.clear();
        problems.clear();
        adjCount = 0;
        currentHeartRate = 0;
        currentHumidity = 0;
        currentOxy = 0;
        currentTemp = 0;
        moved = 0;
        adjMode = 0;
        postureInfo = new PostureInfo();
        lowDecibelCount = 0;
        noConditionCount = 0;
        adjEnd = false;
        isCon = false;
        isAdjust = false;
        isStarted = false;
        isSleep = false;
        isSense = false;
        initCO2L = -1;
        initCO2R = -1;
        initCO2M = -1;
        isLEDOnL = false;
        isLEDOnR = false;
        isLEDOnM = false;
    }

    void maintainPosture() {
        adjEnd = true;
        sendAct();
    }

    void sendAct() {
        Log.d(STATE_TAG, "자세 교정 -> act:" + act + " 전송");
        bluetoothService.writeBLT1("act:" + act); // 교정 정보 전송
        switch (act) {
            case ACT_LEFT:
                Log.d(STATE_TAG, "자세 교정 -> 왼쪽으로 교정");
                Toast.makeText(context, "왼쪽 방향으로 교정", Toast.LENGTH_SHORT).show();
                break;
            case ACT_RIGHT:
                Log.d(STATE_TAG, "자세 교정 -> 오른쪽으로 교정");
                Toast.makeText(context, "오른쪽 방향으로 교정", Toast.LENGTH_SHORT).show();
                break;
            case ACT_DISC:
                Log.d(STATE_TAG, "자세 교정 -> 허리디스크 교정");
                Toast.makeText(context, "허리디스크 완화 자세로 교정", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d(STATE_TAG, "자세 교정 -> 사용자 설정 자세로 교정");
                Toast.makeText(context, "사용자 설정 자세로 교정", Toast.LENGTH_SHORT).show();
        }
    }

    void sendHumidifierMode() {  // 가습기 사용 메시지 전송
        if (autoHumidifier) {
            bluetoothService.writeBLT2("H2O_AUTO");
            Log.d(STATE_TAG, "가습기 Auto");
            Toast.makeText(context, "가습기 자동 사용", Toast.LENGTH_SHORT).show();
        } else {
            if (useHumidifier) {
                bluetoothService.writeBLT2("H2O_ON");
                Log.d(STATE_TAG, "가습기 On");
                Toast.makeText(context, "가습기 On", Toast.LENGTH_SHORT).show();
            } else {
                bluetoothService.writeBLT2("H2O_OFF");
                Log.d(STATE_TAG, "가습기 Off");
                Toast.makeText(context, "가습기 Off", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void adjustPosture() {
        // 이미지 리소스 변경
        if (mode == InitActivity.SNORING_PREVENTION_MODE) {
            ((SleepingActivity) SleepingActivity.mContext).changeState(
                    SleepingActivity.STATE_SNORING);
        } else if (mode == InitActivity.APNEA_PREVENTION_MODE) {
            ((SleepingActivity) SleepingActivity.mContext).changeState(
                    SleepingActivity.STATE_APNEA);
        }
        if (!isAdjust) {  // 교정중이 아닐때
            if (!adjEnd) {
                if (adjMode == 2) {  // 수면 중 한 번 교정을 선택하면 1회 교정 후 교정 불가
                    adjEnd = true;
                }
                noConditionCount = 0;
                if (!isCon || mode == InitActivity.DISEASE_ALLEVIATION_MODE) {
                    isAdjust = true; // 교정중으로 상태 변경
                    isCon = true;

                    postureInfo.start();
                    conStartTime = System.currentTimeMillis();
                    beforePos = postureInfo.getCurrentPos();  // 교정 전 자세
                    sendAct();

                    Calendar calendar = Calendar.getInstance();
                    final String adjTime = sdf1.format(calendar.getTime()); // 교정 시간

                    new Thread() { // 2분 후 down 메시지를 전송 후 자세정보 삽입
                        @Override
                        public synchronized void run() {
                            try {
                                sleep(DOWN_WAIT_TIME); // 2분 대기
                                bluetoothService.writeBLT1("down"); // 교정 해제
                                Log.d(STATE_TAG, "자세 교정 -> down 전송");
                                adjCount++; // 교정 횟수 증가
                                afterPos = postureInfo.getCurrentPos();  // 교정 후 자세
                                new InsertAdjAsyncTask(db.adjustmentDao())
                                        .execute(new Adjustment(sleep.getSleepDate(), adjTime, beforePos, afterPos));
                                Log.d(STATE_TAG, "교정 정보 삽입 -> Date: " + sleep.getSleepDate() + "  교정 시각: "
                                        + adjTime + "  교정 전 자세: " + beforePos + "  교정 후 자세: " + afterPos);
                                bluetoothService.mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "교정 해제", Toast.LENGTH_SHORT).show();
                                        beforePos = null;  // 자세정보 삽입 후 교정 전, 후 자세 정보 초기화
                                        afterPos = null;
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            } else {  // 교정을 더이상 하지 않을 때
                isCon = true;
            }
        }
    }


    // 블루투스 메시지 핸들러
    class BluetoothMessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            byte[] readBuf = (byte[]) msg.obj;
            if (msg.arg1 > 0) {
                String readMessage = new String(readBuf, 0, msg.arg1);
                readMessage = readMessage.trim();

                Log.d(COMMAND_TAG, "message -> " + readMessage);

                if (readMessage.contains("/")) {  // /가 포함되어있으면 /기준으로 나눠서 명령 처리
                    String[] msgArray = readMessage.split("/");

                    for (String message : msgArray) {
                        processCommand(message);
                    }
                } else {  // /가 포함되어 있지 않은 메시지
                    processCommand(readMessage);
                }
            }
        }

        public void processCommand(String message) {
            if (isStarted) {  // 측정 중
                Log.d(COMMAND_TAG, "명령 -> " + message);
                if (message.contains(":")) {
                    String[] msgArray = message.split(":");
                    if (isSleep) {  // 잠에 들었을 때
                        switch (msgArray[0]) {
                            case "heartrate": // 심박수
                                currentHeartRate = (int) Double.parseDouble(msgArray[1]);
                                heartRates.add(currentHeartRate);
                                break;
                            case "spo": // 산소포화도
                                ((SleepingActivity) SleepingActivity.mContext).isReceivedBandMsg = true;
                                currentOxy = (int) Double.parseDouble(msgArray[1]);
                                oxygenSaturations.add(currentOxy);
                                if (currentOxy >= 95) {  // 산소포화도가 정상
                                    if (useO2) {  // 산소 발생기를 사용중이면 전원 off
                                        useO2 = false;
                                        bluetoothService.writeBLT2("O2_OFF");  // 산소발생기 off
                                        Log.d(STATE_TAG, "산소발생기 Off");
                                        Toast.makeText(MainActivity.this,
                                                "산소발생기 Off", Toast.LENGTH_SHORT).show();
                                    }

                                    // 무호흡 모드에서 적용
                                    if (mode == InitActivity.APNEA_PREVENTION_MODE && isCon) {
                                        noConditionCount++;
                                        Log.d(STATE_TAG, "noConditionCount  -> " + noConditionCount);
                                        if (noConditionCount == 5) {  // 무호흡에서 정상 상태로 돌아옴
                                            lowDecibelCount = 0;
                                            conEndTime = System.currentTimeMillis();
                                            postureInfo.stop();
                                            Log.d(STATE_TAG, "무호흡 종료");
                                            Toast.makeText(MainActivity.this,
                                                    "무호흡 상태 종료", Toast.LENGTH_SHORT).show();

                                            // 리소스 변경
                                            ((SleepingActivity) SleepingActivity.mContext).changeState(
                                                    SleepingActivity.STATE_SLEEP);
                                            isCon = false;
                                            noConditionCount = 0;
                                            conMilliTime += conEndTime - conStartTime;
                                            insertCondition(conStartTime, conEndTime);  // 무호흡 데이터 삽입
                                        }
                                    }
                                } else {  // 산소포화도가 정상수치보다 낮음
                                    if (!useO2) {  // 산소 발생기가 off일때 on
                                        useO2 = true;
                                        bluetoothService.writeBLT2("O2_ON");  // 산소발생기 on
                                        Log.d(STATE_TAG, "산소발생기 On");
                                        Toast.makeText(MainActivity.this,
                                                "산소발생기 On", Toast.LENGTH_SHORT).show();
                                    }

                                    // 무호흡 모드에서 적용
                                    if (mode == InitActivity.APNEA_PREVENTION_MODE) {
                                        if (isCon) {
                                            noConditionCount = 0;
                                        }
                                        if (lowDecibelCount > 5) {  // 데시벨이 낮게 유지되고 산소포화도가 낮으면 무호흡이라고 판단
                                            lowDecibelCount = 0;
                                            noConditionCount = 0;
                                            Toast.makeText(MainActivity.this,
                                                    "무호흡 상태", Toast.LENGTH_SHORT).show();
                                            adjustPosture();// 자세 교정
                                        }
                                    }
                                }
                                break;
                            case "HUM": // 습도
                                currentHumidity = (int) Double.parseDouble(msgArray[1]);
                                humidities.add(currentHumidity);

                                // 수면 기록 밴드 고장나서 심박수 값을 임의로 값을 주는 부분.
                                double random = Math.random();
                                currentHeartRate = (int)(random*40)+70;
                                Log.d("BT-", "currentHeartRate: "+currentHeartRate);

                                // ... 산소포화도 값을 임의로 주는 부분
                                double random2 = Math.random();

                                ((SleepingActivity) SleepingActivity.mContext).isReceivedBandMsg = true;
                                currentOxy = (int)(random2*4)+96;
                                Log.d("BT-", "currentOxy: "+currentOxy);

                                oxygenSaturations.add(currentOxy);
                                if (currentOxy >= 95) {  // 산소포화도가 정상
                                    if (useO2) {  // 산소 발생기를 사용중이면 전원 off
                                        useO2 = false;
                                        bluetoothService.writeBLT2("O2_OFF");  // 산소발생기 off
                                        Log.d(STATE_TAG, "산소발생기 Off");
                                        Toast.makeText(MainActivity.this,
                                                "산소발생기 Off", Toast.LENGTH_SHORT).show();
                                    }

                                    // 무호흡 모드에서 적용
                                    if (mode == InitActivity.APNEA_PREVENTION_MODE && isCon) {
                                        noConditionCount++;
                                        Log.d(STATE_TAG, "noConditionCount  -> " + noConditionCount);
                                        if (noConditionCount == 5) {  // 무호흡에서 정상 상태로 돌아옴
                                            lowDecibelCount = 0;
                                            conEndTime = System.currentTimeMillis();
                                            postureInfo.stop();
                                            Log.d(STATE_TAG, "무호흡 종료");
                                            Toast.makeText(MainActivity.this,
                                                    "무호흡 상태 종료", Toast.LENGTH_SHORT).show();

                                            // 리소스 변경
                                            ((SleepingActivity) SleepingActivity.mContext).changeState(
                                                    SleepingActivity.STATE_SLEEP);
                                            isCon = false;
                                            noConditionCount = 0;
                                            conMilliTime += conEndTime - conStartTime;
                                            insertCondition(conStartTime, conEndTime);  // 무호흡 데이터 삽입
                                        }
                                    }
                                } else {  // 산소포화도가 정상수치보다 낮음
                                    if (!useO2) {  // 산소 발생기가 off일때 on
                                        useO2 = true;
                                        bluetoothService.writeBLT2("O2_ON");  // 산소발생기 on
                                        Log.d(STATE_TAG, "산소발생기 On");
                                        Toast.makeText(MainActivity.this,
                                                "산소발생기 On", Toast.LENGTH_SHORT).show();
                                    }

                                    // 무호흡 모드에서 적용
                                    if (mode == InitActivity.APNEA_PREVENTION_MODE) {
                                        if (isCon) {
                                            noConditionCount = 0;
                                        }
                                        if (lowDecibelCount > 5) {  // 데시벨이 낮게 유지되고 산소포화도가 낮으면 무호흡이라고 판단
                                            lowDecibelCount = 0;
                                            noConditionCount = 0;
                                            Toast.makeText(MainActivity.this,
                                                    "무호흡 상태", Toast.LENGTH_SHORT).show();
                                            adjustPosture();// 자세 교정
                                        }
                                    }
                                }




                                Log.d("BT-", "currentHeartRate: "+currentHeartRate);
                                heartRates.add(currentHeartRate);
                                break;
                            case "TEM": // 온도
                                currentTemp = (int) Double.parseDouble(msgArray[1]);
                                temps.add(currentTemp);
                                break;
                            case "SOU": // 소리 센서
                                if (mode == InitActivity.DISEASE_ALLEVIATION_MODE) {  // 질환 완화 모드는 무시
                                    break;
                                }
                                if (!isAdjust) {  // 교정 중이 아닐 때 소릿값 처리
                                    int decibel = (int) Double.parseDouble(msgArray[1]);
                                    problems.add(decibel); // 데시벨 저장

                                    if (mode == InitActivity.SNORING_PREVENTION_MODE) { // 코골이 방지 모드
                                        if (decibel > 60) {  // 60데시벨이 넘으면 자세 교정
                                            Toast.makeText(MainActivity.this,
                                                    "코골이 중", Toast.LENGTH_SHORT).show();
                                            adjustPosture();
                                        } else {  // 코골이 데시벨 이하일때
                                            if (isCon) {
                                                if (noConditionCount == 5) {  // 카운트가 5이 되면 코골이 끝
                                                    conEndTime = System.currentTimeMillis();
                                                    postureInfo.stop();
                                                    Log.d(STATE_TAG, "코골이 종료");
                                                    Toast.makeText(MainActivity.this,
                                                            "코골이 종료", Toast.LENGTH_SHORT).show();
                                                    ((SleepingActivity) SleepingActivity.mContext).changeState(  // 리소스 변경
                                                            SleepingActivity.STATE_SLEEP);
                                                    isCon = false;
                                                    noConditionCount = 0;
                                                    conMilliTime += conEndTime - conStartTime;
                                                    insertCondition(conStartTime, conEndTime);  // 코골이 데이터 삽입
                                                } else {
                                                    Log.d(STATE_TAG, "noConditionCount  -> " + noConditionCount);
                                                    noConditionCount++;
                                                }
                                            }
                                        }
                                    } else if (mode == InitActivity.APNEA_PREVENTION_MODE) { // 무호흡 모드
                                        if (decibel < 50) {  // 데시벨이 50이하이고 카운트가 5 미만이면 카운트 증가
                                            lowDecibelCount++;
                                            Log.d(STATE_TAG, "lowDecibelCount -> " + lowDecibelCount);
                                        } else {
                                            lowDecibelCount = 0;
                                        }
                                    }
                                }
                                break;
                            case "position": // 무게 센서
                                String position = msgArray[1];
                                Log.d(COMMAND_TAG, "position: " + position);
                                String pos = postureInfo.setCurrentPos(position, isSense);  // 자세 정보 입력

                                if (mode == InitActivity.DISEASE_ALLEVIATION_MODE) {
                                    switch (settingFragment.diseaseIndex) {
                                        case 1:  // 강직성척추염
                                            // 옆으로 누운 자세이면 정자세로
                                            switch (pos) {
                                                case PostureInfo.rightPos:
                                                    // 왼쪽 교정
                                                    if (!customAct) {
                                                        act = ACT_LEFT;
                                                    }
                                                    adjustPosture();
                                                    break;
                                                case PostureInfo.leftPos:
                                                    // 오른쪽 교정
                                                    if (!customAct) {
                                                        act = ACT_RIGHT;
                                                    }
                                                    adjustPosture();
                                                    break;
                                                default:
                                            }
                                            break;
                                        case 2:  // 척추관협착증
                                        case 3:  //척추전방전위증
                                            // 정자세일때 옆으로 누운 자세로 교정
                                            if (pos.equals(PostureInfo.upPos)) {
                                                if ((int) (Math.random() * 2) == 0) {
                                                    if (!customAct) {
                                                        act = ACT_LEFT;
                                                    }
                                                } else {
                                                    if (!customAct) {
                                                        act = ACT_RIGHT;
                                                    }
                                                }
                                                adjustPosture();
                                            }
                                            break;
                                        default:
                                    }
                                }

                                break;
                            case "CO2_L": // 이산화탄소 센서 왼쪽
                                if (initCO2L == -1) {  // 초기값이 없음
                                    initCO2L = (int) Double.parseDouble(msgArray[1]);
                                } else {  // 초기값이 있음
                                    int co2 = (int) Double.parseDouble(msgArray[1]);
                                    if (co2 > initCO2L + 1) {  // 초기값보다 1초과 측정
                                        if (!isLEDOnL) {
                                            isLEDOnL = true;
                                            bluetoothService.writeBLT2("L_ON");
                                            Log.d(STATE_TAG, "L_ON 전송");
                                        }
                                    } else {
                                        if (isLEDOnL) {
                                            isLEDOnL = false;
                                            bluetoothService.writeBLT2("L_OFF");
                                            Log.d(STATE_TAG, "L_OFF 전송");
                                        }
                                    }
                                }
                                break;
                            case "CO2_R": // 이산화탄소 센서 오른쪽
                                if (initCO2R == -1) {  // 초기값이 없음
                                    initCO2R = (int) Double.parseDouble(msgArray[1]);
                                } else {  // 초기값이 있음
                                    int co2 = (int) Double.parseDouble(msgArray[1]);
                                    if (co2 > initCO2R + 1) {  // 초기값보다 1초과 측정
                                        if (!isLEDOnR) {
                                            isLEDOnR = true;
                                            bluetoothService.writeBLT2("R_ON");
                                            Log.d(STATE_TAG, "R_ON 전송");
                                        }
                                    } else {
                                        if (isLEDOnR) {
                                            isLEDOnR = false;
                                            bluetoothService.writeBLT2("R_OFF");
                                            Log.d(STATE_TAG, "R_OFF 전송");
                                        }
                                    }
                                }
                                break;
                            case "CO2_M": // 이산화탄소 센서 중앙
                                if (initCO2M == -1) {  // 초기값이 없음
                                    initCO2M = (int) Double.parseDouble(msgArray[1]);
                                } else {  // 초기값이 있음
                                    int co2 = (int) Double.parseDouble(msgArray[1]);
                                    isSense = true;
                                    if (co2 > initCO2M + 1) {  // 초기값보다 1초과 측정
                                        if (!isLEDOnM) {
                                            isLEDOnM = true;
                                            bluetoothService.writeBLT2("M_ON");
                                            Log.d(STATE_TAG, "M_ON 전송");
                                        }
                                    } else {
                                        isSense = false;
                                        if (isLEDOnM) {
                                            isLEDOnM = false;
                                            bluetoothService.writeBLT2("M_OFF");
                                            Log.d(STATE_TAG, "M_OFF 전송");
                                        }
                                    }
                                }
                                break;
                            case "moved": // 뒤척임
                                moved = Integer.parseInt(msgArray[1]);
                                break;
                            default:
                                Log.d(COMMAND_TAG, "동작 없음1");
                        }
                    } else { // 잠들기 전 입력
                        switch (msgArray[0]) {
                            case "position": // 무게 센서
                                String position = msgArray[1];
                                postureInfo.setCurrentPos(position, isSense);  // 자세 정보 입력
                                break;
                            default:
                                Log.d(COMMAND_TAG, "동작 없음2");
                        }
                    }
                } else {
                    switch (message) {
                        case "start": // 잠에 듦
                            if (!isSleep && !isGetStart) {
                                isGetStart = true;
                                String whenSleep = sdf1.format(Calendar.getInstance().getTime());
                                sleep.setWhenSleep(whenSleep); // 잠에 든 시각
                                isSleep = true;
                                Log.d(STATE_TAG, "사용자가 잠에 들었습니다 / " + sleep.getWhenSleep());
                                ((SleepingActivity) SleepingActivity.mContext).changeState(
                                        SleepingActivity.STATE_SLEEP);
                                Toast.makeText(MainActivity.this,
                                        "사용자가 잠에 들었습니다", Toast.LENGTH_SHORT).show();

                                // 잠들기까지 걸린 시간
                                String asleepAfter = getAsleepAfter(whenSleep, sleep.getWhenStart());
                                sleep.setAsleepAfter(asleepAfter);
                                Log.d(STATE_TAG, "잠들기까지 걸린 시간 / " + sleep.getAsleepAfter());
                            }
                            break;
                        case "stop": // 밴드에서 수면 종료
                            stopSleep();
                            break;
                        case "down-1":
                        case "own-1":
                            isAdjust = false;
                            break;
                        default:
                            Log.d(COMMAND_TAG, "동작 없음3");
                    }
                }
            }
        }

        public void insertCondition(long startTime, long endTime) {
            String strStart = sdf1.format(startTime);
            String strEnd = sdf1.format(endTime);

            Condition condition = new Condition(sleep.getSleepDate(), strStart, strEnd);

            new InsertConAsyncTask(db.conditionDao()).execute(condition);
            Log.d(STATE_TAG, "코골이, 무호흡 정보 삽입 -> 시작시간: " + strStart
                    + "  종료시간: " + strEnd);
        }
    }
}