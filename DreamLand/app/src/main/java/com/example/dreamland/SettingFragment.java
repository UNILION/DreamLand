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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import iammert.com.library.Status;
import iammert.com.library.StatusView;

public class SettingFragment extends Fragment {

    SharedPreferences sf;
    LinearLayout resetButton;
    LinearLayout myDiseaseButton;
    LinearLayout bedPositionButton;
    LinearLayout bltSettingLayout;
    TextView tvSleepSetting;
    TextView tvDisease;
    View line1;
    View line2;
    View line3;
    RadioGroup diseaseRadioGroup;
    ToggleButton[] positionButtons;

    int diseaseIndex;
    final String[] diseaseNames = {"허리디스크", "강직성척추염", "척추관협착증", "척추전방전위증"};
    int[] buttonIds = {R.id.positionButton0, R.id.positionButton1,
            R.id.positionButton2, R.id.positionButton3, R.id.positionButton4, R.id.positionButton5,
            R.id.positionButton6, R.id.positionButton7, R.id.positionButton8, R.id.positionButton9};
    String position;

    // Test
    Button button1;
    Button button2;
    EditText editText1;
    EditText editText2;

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
        bedPositionButton = (LinearLayout) view.findViewById(R.id.bedPositionButtonLayout);
        bltSettingLayout = (LinearLayout) view.findViewById(R.id.bltSettingLayout);
        tvSleepSetting = (TextView) view.findViewById(R.id.tvSleepSetting);
        tvDisease = view.findViewById(R.id.tvDisease);
        line1 = (View) view.findViewById(R.id.view1);
        line2 = (View) view.findViewById(R.id.view2);
        line3 = (View) view.findViewById(R.id.view3);

        // Test
        button1 = (Button) view.findViewById(R.id.button);
        button2 = (Button) view.findViewById(R.id.button2);
        editText1 = (EditText) view.findViewById(R.id.editTextTextPersonName);
        editText2 = (EditText) view.findViewById(R.id.editTextTextPersonName2);

        positionButtons = new ToggleButton[10];

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getContext()).bluetoothService.writeBLT1(editText1.getText().toString());
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getContext()).bluetoothService.writeBLT2(editText2.getText().toString());
            }
        });

        sf = getContext().getSharedPreferences("bed", getContext().MODE_PRIVATE);
        int mode = sf.getInt("mode", 0); // 사용자가 설정한 모드를 불러옴

        // 코골이, 무호흡모드이면 질병 관련 뷰들을 숨김
        if (mode == 1 || mode == 2) {
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

                // 초기화 버튼
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
        bedPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.dialog_bed_position, null);
                position = sf.getString("position", "0,0,0,0,0,0,0,0,0,0");
                String[] positionArray = position.split(",");

                for (int i = 0; i < 10; i++) {
                    positionButtons[i] = (ToggleButton) dlgView.findViewById(buttonIds[i]);
                    if (positionArray[i].equals("1")) {
                        positionButtons[i].setChecked(true);
                    } else {
                        positionButtons[i].setChecked(false);
                    }
                }

                Button completeButton = dlgView.findViewById(R.id.yesButton);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog dialog = builder.setView(dlgView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                // 완료 버튼
                completeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StringBuilder positionStr = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            if (positionButtons[i].isChecked()) {
                                positionStr.append("1,");
                            } else {
                                positionStr.append("0,");
                            }
                        }
                        positionStr.deleteCharAt(positionStr.length() - 1);
                        String savedPosition = positionStr.toString();
                        sf.edit().putString("position", savedPosition).apply();
                        String msg = "position:" + savedPosition;
                        Log.d("BLT", msg);
                        // TODO : 침대에 포지션 정보 전송
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

                diseaseRadioGroup = dlgView.findViewById(R.id.diseaseRadioGroup);
                RadioButton[] radioButtons = new RadioButton[4];
                radioButtons[0] = dlgView.findViewById(R.id.radioButton);
                radioButtons[1] = dlgView.findViewById(R.id.radioButton2);
                radioButtons[2] = dlgView.findViewById(R.id.radioButton3);
                radioButtons[3] = dlgView.findViewById(R.id.radioButton4);
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
                            case R.id.radioButton:
                                diseaseIndex = 0;
                                break;
                            case R.id.radioButton2:
                                diseaseIndex = 1;
                                break;
                            case R.id.radioButton3:
                                diseaseIndex = 2;
                                break;
                            case R.id.radioButton4:
                                diseaseIndex = 3;
                                break;
                        }
                        sf.edit().putInt("disease", diseaseIndex).commit();
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
        bedPositionButton.setVisibility(View.GONE);
        tvSleepSetting.setVisibility(View.GONE);
        line1.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        line3.setVisibility(View.GONE);
    }
}
