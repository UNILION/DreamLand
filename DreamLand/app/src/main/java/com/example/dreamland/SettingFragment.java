package com.example.dreamland;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import iammert.com.library.Status;
import iammert.com.library.StatusView;

public class SettingFragment extends Fragment {

    SharedPreferences sf;
    LinearLayout resetButton;
    LinearLayout myDiseaseButton;
    LinearLayout bedActButton;
    LinearLayout bltSettingLayout;
    LinearLayout sleepSettingLayout;
    TextView tvSleepSetting;
    TextView tvDisease;
    Switch autoSwitch;
    Switch manualSwitch;
    Switch posSwitch;
    // TEST
    Button button2;
    Button button3;

    View line1;
    RadioGroup diseaseRadioGroup;
    ToggleButton[] actButtons;

    int diseaseIndex;
    final String[] diseaseNames = {"허리디스크", "강직성척추염", "척추관협착증", "척추전방전위증"};
    int[] buttonIds = {R.id.actButton0, R.id.actButton1,
            R.id.actButton2, R.id.actButton3, R.id.actButton4, R.id.actButton5,
            R.id.actButton6, R.id.actButton7, R.id.actButton8};
    String act;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resetButton = (LinearLayout) view.findViewById(R.id.resetButtonLayout);
        myDiseaseButton = (LinearLayout) view.findViewById(R.id.myDiseaseLayout);
        bedActButton = (LinearLayout) view.findViewById(R.id.bedActButtonLayout);
        bltSettingLayout = (LinearLayout) view.findViewById(R.id.bltSettingLayout);
        sleepSettingLayout = (LinearLayout) view.findViewById(R.id.sleep_setting_layout);
        tvSleepSetting = (TextView) view.findViewById(R.id.tvSleepSetting);
        autoSwitch = (Switch) view.findViewById(R.id.switch_auto);
        manualSwitch = (Switch) view.findViewById(R.id.switch_manual);
        posSwitch = (Switch) view.findViewById(R.id.switch_pos);
        tvDisease = (TextView) view.findViewById(R.id.tvDisease);
        line1 = (View) view.findViewById(R.id.view1);
        //TEST
        button2 = (Button) view.findViewById(R.id.button2);
        button3 = (Button) view.findViewById(R.id.button3);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getContext()).bluetoothService.writeBLT1("act:0,1,0,1,0,1,0,1,0"); // 교정 정보 전송
                Log.d(MainActivity.STATE_TAG, "act:0,1,0,1,0,1,0,1,0");
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getContext()).bluetoothService.writeBLT1("down");
                Log.d(MainActivity.STATE_TAG, "down 전송");
            }
        });


        actButtons = new ToggleButton[10];
        sf = getContext().getSharedPreferences("bed", getContext().MODE_PRIVATE);

        ((MainActivity) getActivity()).autoHumidifier = sf.getBoolean("autoHumidifier", true);
        ((MainActivity) getActivity()).useHumidifier = sf.getBoolean("useHumidifier", false);

        // 가습기 뷰를 저장된 상태로 변경
        if (((MainActivity) getActivity()).autoHumidifier) {
            autoSwitch.setChecked(true);
        } else {
            autoSwitch.setChecked(false);
            manualSwitch.setVisibility(View.VISIBLE);
        }

        if (((MainActivity) getActivity()).useHumidifier) {
            manualSwitch.setChecked(true);
        } else {
            manualSwitch.setChecked(false);
        }

        // 가습기 자동 사용 스위치
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {  // 자동 사용
                    manualSwitch.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).autoHumidifier = true;
                    sf.edit().putBoolean("autoHumidifier", true).apply();
                } else {  // 수동 사용
                    manualSwitch.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).autoHumidifier = false;
                    sf.edit().putBoolean("autoHumidifier", false).apply();
                    if (manualSwitch.isChecked()) {
                        ((MainActivity) getActivity()).useHumidifier = true;
                        sf.edit().putBoolean("useHumidifier", true).apply();
                    } else {
                        ((MainActivity) getActivity()).useHumidifier = false;
                        sf.edit().putBoolean("useHumidifier", false).apply();
                    }
                }
            }
        });

        // 가습기 수동 사용 스위치
        manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {  // 사용
                    ((MainActivity) getActivity()).useHumidifier = true;
                    sf.edit().putBoolean("useHumidifier", true).apply();
                } else {  // 사용 안함
                    ((MainActivity) getActivity()).useHumidifier = false;
                    sf.edit().putBoolean("useHumidifier", false).apply();
                }
            }
        });


        // 사용자 설정 자세 스위치
        boolean customAct = sf.getBoolean("customAct", false);
        if (customAct) {
            bedActButton.setVisibility(View.VISIBLE);
        }
        posSwitch.setChecked(customAct);
        posSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bedActButton.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).customAct = true;
                } else {
                    bedActButton.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).customAct = false;
                }
                sf.edit().putBoolean("customAct", ((MainActivity) getActivity()).customAct).apply();
            }
        });

        int mode = sf.getInt("mode", 0); // 사용자가 설정한 모드를 불러옴

        // 코골이, 무호흡모드이면 질병 관련 뷰들을 숨김
        if (mode == InitActivity.SNORING_PREVENTION_MODE || mode == InitActivity.APNEA_PREVENTION_MODE) {
            hideDiseaseView();
        } else { // 질환 완화 모드
            diseaseIndex = sf.getInt("disease", 0); // 설정한 질환을 불러옴
            tvDisease.setText(diseaseNames[diseaseIndex]);
        }

        // 초기화 버튼
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.dialog_reset, null);
                Button resetConfirmButton = dlgView.findViewById(R.id.resetConfirmButton);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog dialog = builder.setView(dlgView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                // 초기화 확인 버튼
                resetConfirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Main Activity 재시작
                        startActivity(new Intent(getContext(), MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        ((MainActivity) getActivity()).resetData();
                    }
                });
            }
        });

        // 사용자 조정 버튼
        bedActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.dialog_bed_act, null);
                act = sf.getString("act", "0,0,0,0,0,0,0,0,0");
                String[] actArray = act.split(",");

                for (int i = 0; i < 9; i++) {
                    actButtons[i] = (ToggleButton) dlgView.findViewById(buttonIds[i]);
                    if (actArray[i].equals("1")) {
                        actButtons[i].setChecked(true);
                    } else {
                        actButtons[i].setChecked(false);
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                            } else {
                                actStr.append("0,");
                            }
                        }
                        actStr.deleteCharAt(actStr.length() - 1);
                        sf.edit().putString("act", actStr.toString()).apply();
                        dialog.dismiss();
                    }
                });
            }
        });

        // 나의 질환 버튼
        myDiseaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.dialog_disease, null);
                Button diseaseSelectButton = dlgView.findViewById(R.id.diseaseSelectButton);

                diseaseRadioGroup = dlgView.findViewById(R.id.disease_radio_group);
                RadioButton[] radioButtons = new RadioButton[4];
                radioButtons[0] = dlgView.findViewById(R.id.radio_button);
                radioButtons[1] = dlgView.findViewById(R.id.radio_button2);
                radioButtons[2] = dlgView.findViewById(R.id.radio_button3);
                radioButtons[3] = dlgView.findViewById(R.id.radio_button4);
                radioButtons[diseaseIndex].setChecked(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog dialog = builder.setView(dlgView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                // 질환 선택 버튼
                diseaseSelectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (diseaseRadioGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_button:
                                diseaseIndex = 0;
                                break;
                            case R.id.radio_button2:
                                diseaseIndex = 1;
                                break;
                            case R.id.radio_button3:
                                diseaseIndex = 2;
                                break;
                            case R.id.radio_button4:
                                diseaseIndex = 3;
                                break;
                        }
                        sf.edit().putInt("disease", diseaseIndex).apply();
                        tvDisease.setText(diseaseNames[diseaseIndex]);
                        dialog.dismiss();
                    }
                });
            }
        });

        // 블루투스 연결 버튼
        bltSettingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).enableBluetooth();
                StatusView statusView = (StatusView) ((MainActivity) getContext()).findViewById(R.id.status);
                statusView.setStatus(Status.LOADING);
            }
        });
    }

    // 질환 관련 뷰를 숨김
    public void hideDiseaseView() {
        myDiseaseButton.setVisibility(View.GONE);
        tvSleepSetting.setVisibility(View.GONE);
        line1.setVisibility(View.GONE);
        sleepSettingLayout.setVisibility(View.GONE);
    }
}
