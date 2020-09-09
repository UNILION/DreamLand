package com.example.dreamland;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dreamland.asynctask.DeleteAdjAsyncTask;
import com.example.dreamland.asynctask.DeleteSleepAsyncTask;
import com.example.dreamland.asynctask.InsertAdjAsyncTask;
import com.example.dreamland.asynctask.InsertSleepAsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AppDatabase;
import com.example.dreamland.database.Sleep;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import iammert.com.library.StatusView;

import static com.example.dreamland.MySimpleDateFormat.sdf1;
import static com.example.dreamland.MySimpleDateFormat.sdf3;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_ENABLE_BT = 111;
    final int RC_INIT_ACTIVITY = 1000;
    final int RC_SLEEPING_ACTIVITY = 2000;

    private HomeFragment homeFragment;
    private ManagementFragment managementFragment;
    private SettingFragment settingFragment;
    private StatisticsFragment statisticsFragment;
    private Fragment curFragment;
    private StatusView statusView;

    private AppDatabase db;
    private ActionBar actionBar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sf;
    private List<Sleep> sleepList;
    BluetoothAdapter bluetoothAdapter;
    BluetoothService bluetoothService;
    ArrayList<BluetoothSocket> bluetoothSocketArrayList = null;
    Handler bluetoothMessageHandler;

    boolean isConnected = false;
    boolean isSleep = false;
    ArrayList<Integer> heartRates;
    ArrayList<Integer> oxygenSaturations;
    ArrayList<Integer> humidities;
    Sleep sleep;

    private int mode;  // 모드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartRates = new ArrayList<>();
        oxygenSaturations = new ArrayList<>();
        sleep = new Sleep();

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
        statisticsFragment = new StatisticsFragment();
        actionBar = getSupportActionBar();

        // 화면에 프래그먼트 추가
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, homeFragment)
                .add(R.id.container, managementFragment)
                .hide(managementFragment)
                .add(R.id.container, settingFragment)
                .hide(settingFragment)
                .add(R.id.container, statisticsFragment)
                .hide(statisticsFragment).commit();

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

                    // 통계 버튼
                    case R.id.tab_statistics:
                        if (curFragment != statisticsFragment) {
                            getSupportFragmentManager().beginTransaction()
                                    .show(statisticsFragment)
                                    .hide(curFragment).commit();
                            curFragment = statisticsFragment;
                            actionBar.setTitle("통계");
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
                // 기기 이름이 BLT1, BLT2, BLT3인 경우 연결
                if (deviceName.equals("BLT1") || deviceName.equals("BLT2")
                        || deviceName.equals("BLT3")) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // 테스트 데이터 삽입
            case R.id.insertSleep:
                Calendar c = Calendar.getInstance();

                c.set(2020, 4, 1);
                String s1 = sdf3.format(c.getTime());
                c.set(2020, 4, 2);
                String s2 = sdf3.format(c.getTime());
                c.set(2020, 4, 3);
                String s3 = sdf3.format(c.getTime());
                c.set(2020, 4, 4);
                String s4 = sdf3.format(c.getTime());
                c.set(2020, 4, 5);
                String s5 = sdf3.format(c.getTime());
                c.set(2020, 4, 6);
                String s6 = sdf3.format(c.getTime());
                c.set(2020, 4, 7);
                String s7 = sdf3.format(c.getTime());

                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s1, "01:00", "01:10",
                        "07:10", "08:00", "05:00", "05:50",
                        "00:50", 1, 2, 1, 3, 40,
                        1, 70, 40)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s2, "02:12", "02:24",
                        "07:24", "05:00", "04:15", "04:50",
                        "00:35", 3, 4, 2, 5, 30,
                        1, 77, 60)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s3, "01:40", "01:50",
                        "08:10", "06:20", "07:30", "07:40",
                        "00:10", 3, 5, 3, 5, 20,
                        1, 80, 50)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s4, "23:55", "00:20",
                        "05:20", "05:00", "03:30", "03:40",
                        "00:10", 3, 5, 3, 1, 10,
                        1, 72, 46)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s5, "01:10", "01:30",
                        "08:15", "06:45", "06:30", "06:45",
                        "00:15", 3, 5, 2, 2, 60,
                        1, 83, 59)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s6, "00:51", "01:00",
                        "06:40", "05:40", "", "",
                        "00:00", 3, 5, 0, 4, 60,
                        1, 81, 43)
                );
                new InsertSleepAsyncTask(db.sleepDao()).execute(new Sleep(
                        s7, "03:20", "03:25",
                        "08:55", "05:30", "", "",
                        "00:00", 3, 5, 0, 0, 80,
                        1, 75, 39)
                );
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "1", "00:32"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "2", "00:35"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "2", "00:51"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "3", "01:13"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "3", "03:11"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "3", "04:14"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "4", "05:23"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "4", "05:41"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "4", "06:52"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "5", "06:42"
                ));
                new InsertAdjAsyncTask(db.adjustmentDao()).execute(new Adjustment(
                        "5", "06:55"
                ));
                return true;

            // 수면 데이터 모두 삭제
            case R.id.deleteSleep:
                new DeleteSleepAsyncTask(db.sleepDao()).execute();
                new DeleteAdjAsyncTask(db.adjustmentDao()).execute();
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
                if (resultCode == 1001) {
                    settingFragment.hideDiseaseView();
                    managementFragment.changeConditionView();
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

            case RC_SLEEPING_ACTIVITY:
                Log.d("BLT", "RC_SLEEPING_ACTIVITY");
                if (isSleep) { // 잠에 들었다가 중지했을 경우
                    sleep.setHeartRate(getAverage(heartRates)); // 심박수 평균
                    sleep.setOxyStr(getAverage(oxygenSaturations)); // 산소포화도 평균
                    isSleep = false;
                } else { // 잠에 들지 않고 중지했을 경우

                }
                break;

        }
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

    // 블루투스 메시지 핸들러
    class BluetoothMessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            byte[] readBuf = (byte[]) msg.obj;
            if (msg.arg1 > 0) {
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d("BLT", "message -> " + readMessage);

                if (readMessage.contains(":")) {
                    String[] msgArray = readMessage.split(":");
                    switch (msgArray[0]) {
                        case "heartrate": // 심박수 메시지
                            heartRates.add(Integer.parseInt(msgArray[1]));
                            break;
                        case "oxygensaturation": // 산소포화도 메시지
                            oxygenSaturations.add(Integer.parseInt(msgArray[1]));
                            break;
                        default:
                            Log.d("BLT", "잘못된 메시지");
                    }
                } else {
                    switch (readMessage) {
                        case "sleepstart":
                            String whenSleep = sdf1.format(Calendar.getInstance().getTime());
                            sleep.setWhenSleep(whenSleep);
                            isSleep = true;
                            Log.d("BLT", "사용자가 잠에 들었습니다 / " + whenSleep);
                            break;
                        default:
                            Log.d("BLT", "잘못된 메시지");
                    }
                }
            }
        }
    }
}