package com.example.dreamland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static com.example.dreamland.MySimpleDateFormat.sdf1;
import static com.example.dreamland.MySimpleDateFormat.sdf3;

public class StartFragment extends Fragment {

    Button startButton;
    TimePicker timePicker;
    Context context;
    Button selButton1;
    Button selButton2;
    Button selButton3;
    Button posCheckButton;
    AlertDialog selDialog;
    ImageView ivPredicPos;
    TextView tvTitle;
    TextView tvSel;
    Guideline guideline3;
    private MainActivity mainActivity;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startButton = (Button) view.findViewById(R.id.startButton);
        timePicker = view.findViewById(R.id.timePicker);
        context = getContext();

        mainActivity = (MainActivity) getActivity();

        // 수면 시작 버튼
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity.bluetoothService.isConnected) {
                    showDialog();
                } else {
                    Toast.makeText(context, "블루투스 연결을 해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 수면 시작 함수
    private void startSleep(int selectedMenu) {
        mainActivity.adjMode = selectedMenu;
        Intent intent = new Intent(getContext(), SleepingActivity.class);
        intent.putExtra("hour", timePicker.getHour());
        intent.putExtra("minute", timePicker.getMinute());
        Calendar calendar = Calendar.getInstance();
        String whenStart = sdf1.format(calendar.getTime());
        mainActivity.sleep.setWhenStart(whenStart);
        if (calendar.get(Calendar.HOUR_OF_DAY) < 6) { // 자정이 지나면 전날로 표기
            calendar.add(Calendar.HOUR_OF_DAY, -7);
            Log.d(MainActivity.STATE_TAG, calendar.get(Calendar.HOUR_OF_DAY) + "");
        }
        String sleepDate = sdf3.format(calendar.getTime());
        mainActivity.sleep.setSleepDate(sleepDate);
        Log.d(MainActivity.STATE_TAG, "측정 시작 / " + sleepDate + " " + whenStart);
        mainActivity.startActivityForResult(intent, MainActivity.RC_SLEEPING_ACTIVITY);
        mainActivity.overridePendingTransition(R.anim.down_in, R.anim.stop);
    }

    public void changeDiseaseView() {
        posCheckButton.setVisibility(View.VISIBLE);
        selButton1.setVisibility(View.GONE);
        selButton2.setVisibility(View.GONE);
        selButton3.setVisibility(View.GONE);
        tvSel.setVisibility(View.GONE);
        guideline3.setGuidelinePercent(0.8f);
    }

    private void setRandomAdjDirection() {
        if (!mainActivity.customAct) {  // 사용자 설정 교정 자세 이용 안함
            if ((int) (Math.random() * 2) == 0) {
                ivPredicPos.setImageResource(R.drawable.pos1);  // 왼쪽으로 교정
                mainActivity.act = MainActivity.ACT_LEFT;
            } else {
                ivPredicPos.setImageResource(R.drawable.pos2);  // 오른쪽으로 교정
                mainActivity.act = MainActivity.ACT_RIGHT;
            }
        } else {  // 이용함
            mainActivity.act = mainActivity.sf.getString("act", "0,0,0,0,0,0,0,0,0,0");
            tvTitle.setVisibility(View.GONE);
            ivPredicPos.setVisibility(View.GONE);
            guideline3.setGuidelinePercent(0.2f);
        }

    }

    // 교정방식 선택 화면
    private void showDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        final View dlgView = getLayoutInflater().from(getContext()).inflate(
                R.layout.dialog_before_sleep, null);

        selButton1 = dlgView.findViewById(R.id.selButton1);
        selButton2 = dlgView.findViewById(R.id.selButton2);
        selButton3 = dlgView.findViewById(R.id.selButton3);
        posCheckButton = dlgView.findViewById(R.id.pos_check_button);
        ivPredicPos = dlgView.findViewById(R.id.ivPredicPos);
        tvSel = dlgView.findViewById(R.id.tv_sel);
        guideline3 = (Guideline) dlgView.findViewById(R.id.guideline3);
        tvTitle = (TextView) dlgView.findViewById(R.id.tv_title);

        switch (mainActivity.mode) {
            case 1:
            case 2:  // 코골이, 무호흡 모드이면 자세를 오른쪽 또는 왼쪽 방향으로 교정
                setRandomAdjDirection();
                break;
            case 3:
                changeDiseaseView();
                switch (mainActivity.settingFragment.diseaseIndex) {
                    case 0:
                        ivPredicPos.setImageResource(R.drawable.pos5);
                        changeDiseaseView();
                        break;
                    case 1:
                        ivPredicPos.setImageResource(R.drawable.pos3);
                        break;
                    case 2:
                    case 3:
                        setRandomAdjDirection();
                        break;
                    default:
                }
                break;
            default:
        }

        selDialog = builder.setView(dlgView).create();

        // 수면 중 교정
        selButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDialog.dismiss();
                startSleep(1);
            }
        });

        // 수면 중 한 번 교정
        selButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDialog.dismiss();
                startSleep(2);
            }
        });

        // 즉시 교정
        selButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDialog.dismiss();
                startSleep(3);
            }
        });

        // 질환 교정 자세 확인 버튼
        posCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDialog.dismiss();
                if (mainActivity.settingFragment.diseaseIndex == 0) {  // 허리디스크시 교정 자세 고정
                    startSleep(4);
                } else {
                    startSleep(1);  // 다른 질환은 수면중 교정
                }
            }
        });

        selDialog.show();
    }
}
