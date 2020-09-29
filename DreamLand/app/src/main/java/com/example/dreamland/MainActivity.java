package com.example.dreamland;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.app.Activity;
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

import com.example.dreamland.asynctask.DeleteAdjAsyncTask;
import com.example.dreamland.asynctask.DeleteSleepAsyncTask;
import com.example.dreamland.asynctask.InsertAdjAsyncTask;
import com.example.dreamland.asynctask.InsertSleepAsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AppDatabase;
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
    final int RC_SLEEPING_ACTIVITY = 2000;
    final int DOWN_WAIT_TIME = 1000 * 5;  // 엑추에이터 내림 대기시간

    private HomeFragment homeFragment;
    private ManagementFragment managementFragment;
    public SettingFragment settingFragment;
    private HealthFragment healthFragment;
    private Fragment curFragment;
    private StatusView statusView;
    public static Context context;

    private AppDatabase db;
    private ActionBar actionBar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sf;
    private List<Sleep> sleepList;
    BluetoothAdapter bluetoothAdapter;
    BluetoothService bluetoothService;
    ArrayList<BluetoothSocket> bluetoothSocketArrayList = null;
    Handler bluetoothMessageHandler;
    PostureInfo postureInfo;  // 현제 자세 정보
    RequestThread requestThread;  // 밴드로 데이터를 요청하는 스레드

    boolean isConnected = false; // 블루투스 연결 여부
    boolean isSleep = false; // 잠에 들었는지 여부
    boolean isAdjust = false; // 교정 중인지 여부
    boolean isSense = false; // 이산화탄소 감지 여부
    boolean isCon = false;  // 상태 지속 여부
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
    boolean customAct = false;  // 사용자 설정 여부
    boolean autoHumidifier = true;  // 가습기 사용 여부
    boolean useO2 = false;
    long conMilliTime = 0L;  // 상태 지속 총 시간
    long conStartTime = 0L;  // 상태 시작 시간
    long conEndTime = 0L;  // 상태 종료 시간
    int noConditionCount = 0;  // 정상 상태 감지 카운트

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

        bluetoothService = new BluetoothService(this, bluetoothMessageHandler);

        sf = getSharedPreferences("bed", MODE_PRIVATE);

        mode = sf.getInt("mode", 0);

        // 모드 설정값이 없으면 모드 선택 액티비티로 이동
        if (mode == 0) {
            Intent initIntent = new Intent(this, InitActivity.class);
            startActivityForResult(initIntent, 1000);
        }

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);

        homeFragment = new HomeFragment();
        managementFragment = new ManagementFragment();
        settingFragment = new SettingFragment();
        healthFragment = new HealthFragment();
        actionBar = getSupportActionBar();

        // 화면에 프래그먼트 추가
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, homeFragment)
                .add(R.id.container, managementFragment)
                .hide(managementFragment)
                .add(R.id.container, settingFragment)
                .hide(settingFragment)
                .add(R.id.container, healthFragment)
                .hide(healthFragment).commit();

        curFragment = homeFragment;
        actionBar.setTitle("홈");

        // 하단 탭 클릭시 화면 전환
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // 홈 버튼
                    case R.id.tab_home:
                        if (curFragment != homeFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(homeFragment)
                                    .hide(curFragment).commit();
                            curFragment = homeFragment;
                            actionBar.setTitle("홈");
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
                managementFragment.updateUI();
            }
        });
    }

    @Override
    protected void onDestroy() {
        bluetoothService.cancel(); // 소켓 close
        super.onDestroy();
    }

    // 초기화 함수
    public void resetData() {
        sf.edit().putInt("mode", 0).apply();
        sf.edit().putInt("disease", 0).apply();
        new DeleteSleepAsyncTask(db.sleepDao()).execute();
        new DeleteAdjAsyncTask(db.adjustmentDao()).execute();
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
        Log.d("BLT", "페어링된 기기");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("BLT", deviceName + " " + deviceHardwareAddress);
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
    private String getAsleepAfter(String whenSleep, String whenStart) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // 테스트 데이터 삽입
            case R.id.insert_sleeps:
                Calendar c = Calendar.getInstance();
                String sleepDate;
                c.set(2020, 2, 1);
                for (int i = 0; i < 190; i++) {
                    sleepDate = sdf3.format(c.getTime());
                    c.add(Calendar.DAY_OF_MONTH, 1);

                    String whenStart = createRandomStartTime();
                    String whenSleep = createRandomWhenSleep(whenStart);
                    String whenWake = createRandomWhenWake();
                    int heartRate = (int) (Math.random() * 70) + 40;
                    int spo = (int) (Math.random() * 14) + 88;
                    // 샘플 데이터 생성
                    new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                            sleepDate, whenSleep, whenStart, getAsleepAfter(whenSleep, whenStart),
                            whenWake, getSleepTime(whenSleep, whenWake), createRandomConTime(),
                            (int) (Math.random() * 7), (int) (Math.random() * 5) + 1,
                            spo, heartRate, (int) (Math.random() * 50) + 10,
                            (int) (Math.random() * 5) + 20,
                            getScore(spo, heartRate))
                    );
                }
                return true;

            // 수면 데이터 모두 삭제
            case R.id.delete_sleeps:
                new DeleteSleepAsyncTask(db.sleepDao()).execute();
                new DeleteAdjAsyncTask(db.adjustmentDao()).execute();
                return true;

            // 오늘 수면 데이터 삽입
            case R.id.insert_today_sleep:
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        sdf3.format(new Date()), "00:11", "00:41", "00:31",
                        "09:02", "08:20", "00:06", 3, 1,
                        88, 47, 49, 20, 27)
                );
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
                    settingFragment.hideDiseaseView();
                } else if (resultCode == 1002) {  // 무호흡 모드 선택
                    settingFragment.hideDiseaseView();
                    managementFragment.changeConditionView();
                    healthFragment.changeView();
                } else if (resultCode == 1003) {  // 질환 모드 선택
                    // TODO: 질환 모드에 맞는 UI 출력
                }
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) { // 블루투스 활성화 성공
                    Log.d("BLT", "블루투스 활성화 성공");
                    connectDevices();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 실패
                    Log.d("BLT", "블루투스 활성화 실패");
                }
                break;

            case RC_SLEEPING_ACTIVITY: // 수면 중지
                Log.d("BLT", "RC_SLEEPING_ACTIVITY");
                stopSleep();  // 측정 중지
                break;

        }
    }

    void stopSleep() { // 측정 중지
        if (isSleep) { // 잠에 들었다가 중지했을 경우

            requestThread.setStop(true);  // 쓰레드 종료

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

            Log.d("BLT",
                    "일자: " + sleep.getSleepDate()
                            + "  시작 시간: " + sleep.getWhenStart()
                            + "  잠에 든 시각: " + sleep.getWhenSleep()
                            + "  기상 시각: " + sleep.getWhenWake()
                            + "  수면 시간: " + sleep.getSleepTime()
                            + "  상태 시간: " + sleep.getConTime()
                            + "  심박수: " + sleep.getHeartRate()
                            + "  산소포화도: " + sleep.getOxyStr()
                            + "  습도: " + sleep.getHumidity()
                            + "  교정 횟수: " + sleep.getAdjCount()
                            + "  건강 점수: " + sleep.getScore());

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
                    dialog.dismiss();
                }
            });
            // TODO: sleep 삽입
            isSleep = false;
            clearData();
        }
    }

    // 건강 점수
    int getScore(int spo, int heartRate) { // TODO: 수정 필요
        int spoScore;
        int heartRateScore;
        if (spo >= 95) {  // 산소포화도 95이상, 정상
            spoScore = 0;
        } else if (spo >= 91) { // 산소포화도 91이상 95미만, 정상 이하
            spoScore = 10 * (95 - spo);
        } else {  // 진단 필요
            spoScore = 60;
        }

        if (heartRate >= 60 && heartRate <= 100) { // 심박수 정상 수치
            heartRateScore = 0;
        } else if (heartRate > 100) {  // 정상 수치보다 높음
            heartRateScore = heartRate - 100;
        } else {  // 정상 수치보다 낮음
            heartRateScore = 60 - heartRate;  // 정상 수치의 최소인 60에서 1이 떨어지면 1점 증가
        }

        return 100 - heartRateScore - spoScore;
    }

    // 입력값들의 평균을 구하는 함수
    int getAverage(ArrayList<Integer> arr) {
        if (arr.size() == 0) {
            return -1;
        }
        int sum = 0;
        for (Integer num : arr) {
            sum += num;
        }
        return sum / arr.size();
    }

    // 잠에서 깬 후 데이터 삭제
    void clearData() {
        sleep = new Sleep();
        heartRates.clear();
        humidities.clear();
        oxygenSaturations.clear();
        problems.clear();
        adjCount = 0;
        currentHeartRate = 0;
        currentHumidity = 0;
        currentOxy = 0;
        currentTemp = 0;
        postureInfo = new PostureInfo();
    }

    // 블루투스 메시지 핸들러
    class BluetoothMessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            byte[] readBuf = (byte[]) msg.obj;
            if (msg.arg1 > 0) {
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d("BLT", "message -> " + readMessage);

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

        private void processCommand(String message) {
            Log.d("BLT", "명령 -> " + message);
            if (message.contains(":")) {
                String[] msgArray = message.split(":");
                if (isSleep) {
                    switch (msgArray[0]) {
                        case "heartrate": // 심박수
                            currentHeartRate = Integer.parseInt(msgArray[1]);
                            heartRates.add(currentHeartRate);
                            break;
                        case "spo": // 산소포화도
                            currentOxy = Integer.parseInt(msgArray[1]);
                            oxygenSaturations.add(currentOxy);
                            if (currentOxy >= 95 && useO2) {  // 산소포화도가 정상
                                useO2 = false;
                                bluetoothService.writeBLT2("O2_OFF");  // 산소발생기 off
                            }
                            if (currentOxy < 95 && !useO2) {  // 산소포화도가 정상수치보다 낮음
                                useO2 = true;
                                bluetoothService.writeBLT2("O2_ON");  // 산소발생기 on
                            }
                            break;
                        case "HUM": // 습도
                            currentHumidity = (int) Double.parseDouble(msgArray[1]);
                            humidities.add(currentHumidity);
                            break;
                        case "TEM": // 온도
                            currentTemp = (int) Double.parseDouble(msgArray[1]);
                            temps.add(currentTemp);
                            break;
                        case "SOU": // 소리 센서
                            int decibel = Integer.parseInt(msgArray[1]);
                            problems.add(decibel); // 데시벨 저장
                            if (postureInfo.getCurrentPos() != null) { // 교정을 하기 위해 자세 정보가 필요함
                                if (mode == 1) { // 코골이 방지 모드
                                    if (decibel > 50) {
                                        noConditionCount = 0;
                                        if (!isCon) {
                                            isCon = true;
                                            conStartTime = System.currentTimeMillis();
                                            beforePos = postureInfo.getCurrentPos();  // 교정 전 자세
                                            bluetoothService.writeBLT1("Act:" + act); // 교정 정보 전송
                                            Log.d("BLT", "Act:" + act + " 전송");
                                            ((SleepingActivity) SleepingActivity.mContext).changeState(  // 리소스 변경
                                                    SleepingActivity.STATE_SNORING);

                                            Calendar calendar = Calendar.getInstance();
                                            final String adjTime = sdf1.format(calendar.getTime()); // 교정 시간
                                            isAdjust = true; // 교정중으로 상태 변경

                                            new Thread() { // 2분 후 down 메시지 전송
                                                @Override
                                                public synchronized void run() {
                                                    try {
                                                        sleep(DOWN_WAIT_TIME); // 2분 대기
                                                        bluetoothService.writeBLT1("down"); // 교정 해제
                                                        isAdjust = false; // 교정중 아님
                                                        Log.d("BLT", "down 전송");
                                                        adjCount++; // 교정 횟수 증가
                                                        afterPos = postureInfo.getCurrentPos();  // 교정 후 자세
                                                        new InsertAdjAsyncTask(db.adjustmentDao())
                                                                .execute(new Adjustment(sleep.getSleepDate(), adjTime, beforePos, afterPos));
                                                        Log.d("BLT", "교정 정보 삽입 -> Date: " + sleep.getSleepDate() + "  교정 시각: "
                                                                + adjTime + "  교정 전 자세: " + beforePos + "  교정 후 자세: " + afterPos);
                                                        beforePos = null;  // 자세정보 삽입 후 교정 전, 후 자세 정보 초기화
                                                        afterPos = null;
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }.start();
                                        }
                                    } else {  // 코골이 데시벨 이하일때
                                        if (noConditionCount == 10) {  // 카운트가 10이 되면 코골이 끝
                                            conEndTime = System.currentTimeMillis();
                                            Log.d("BLT", "코골이 종료");
                                            ((SleepingActivity) SleepingActivity.mContext).changeState(  // 리소스 변경
                                                    SleepingActivity.STATE_SLEEP);

                                            isCon = false;
                                            noConditionCount = 0;
                                            conMilliTime += conEndTime - conStartTime;
                                        } else {
                                            Log.d("BLT", "noConditionCount -> " + noConditionCount);
                                            noConditionCount++;
                                        }
                                    }
                                } else if (mode == 2) { // 무호흡 모드

                                } else { // 질환 모드

                                }
                            }
                            break;
                        case "position": // 무게 센서
                            String position = msgArray[1];
                            Log.d("BLT", "position: " + position);
                            postureInfo.setCurrentPos(position, isSense);  // 자세 정보 입력
                            break;
                        case "CO2_L": // 이산화탄소 센서 왼쪽
                            break;
                        case "CO2_R": // 이산화탄소 센서 오른쪽
                            break;
                        case "CO2_M": // 이산화탄소 센서 중앙
                            int co2 = (int) Double.parseDouble(msgArray[1]);
                            isSense = co2 >= 6;
                            break;
                        case "moved": // 뒤척임
                            break;
                        default:
                            Log.d("BLT", "동작 없음");
                    }
                } else { // 잠들기 전 입력
                    switch (msgArray[0]) {
                        case "position": // 무게 센서
                            String position = msgArray[1];
                            postureInfo.setCurrentPos(position, isSense);  // 자세 정보 입력
                            break;
                        default:
                            Log.d("BLT", "동작 없음");
                    }
                }
            } else {
                switch (message) {
                    case "start": // 잠에 듦
                        if (!isSleep) {
                            String whenSleep = sdf1.format(Calendar.getInstance().getTime());
                            sleep.setWhenSleep(whenSleep); // 잠에 든 시각
                            isSleep = true;
                            Log.d("BLT", "사용자가 잠에 들었습니다 / " + sleep.getWhenSleep());
                            ((SleepingActivity) SleepingActivity.mContext).changeState(
                                    SleepingActivity.STATE_SLEEP);

                            // 잠들기까지 걸린 시간
                            String asleepAfter = getAsleepAfter(whenSleep, sleep.getWhenStart());
                            sleep.setAsleepAfter(asleepAfter);
                            Log.d("BLT", "잠들기까지 걸린 시간 / " + sleep.getAsleepAfter());

                            // 사용자 교정자세 정보
                            if (customAct) {  // 사용
                                act = sf.getString("act", "0,0,0,0,0,0,0,0,0");
                            } else {  // 사용 안함
                                act = "1,0,1,0,1,0,1,0,0";  // 왼쪽  // TODO: 왼쪽 오른쪽 지정
                            }

                            requestThread = new RequestThread(bluetoothService);
                            requestThread.start();  // 밴드로 주기적으로 데이터 요청
                        }
                        break;
                    case "end": // 밴드에서 수면 종료
                        ((SleepingActivity) SleepingActivity.mContext).finish();
                        stopSleep();
                        break;
                    default:
                        Log.d("BLT", "동작 없음");
                }
            }
        }
    }
}