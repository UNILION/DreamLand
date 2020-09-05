package com.example.dreamland;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingFragment extends Fragment {

    SharedPreferences sf;
    LinearLayout resetButton;
    LinearLayout myDiseaseButton;
    LinearLayout bedPositionButton;
    TextView tvSleepSetting;
    TextView tvDisease;
    View line1;
    View line2;
    View line3;
    RadioGroup diseaseRadioGroup;
    int diseaseIndex;
    final String[] diseaseNames = { "허리디스크", "강직성척추염", "척추관협착증", "척추전방전위증" };
    String position;

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
        tvSleepSetting = (TextView) view.findViewById(R.id.tvSleepSetting);
        tvDisease = view.findViewById(R.id.tvDisease);
        line1 = (View) view.findViewById(R.id.view1);
        line2 = (View) view.findViewById(R.id.view2);
        line3 = (View) view.findViewById(R.id.view3);

        sf = getContext().getSharedPreferences("bed", getContext().MODE_PRIVATE);
        int mode = sf.getInt("mode", 0); // 사용자가 설정한 모드를 불러옴

        // 코골이, 무호흡모드이면 질병 관련 뷰들을 숨김
        if (mode == 1 || mode == 2) {
            hideDiseaseView();
        } else { // 질환 완화 모드
            diseaseIndex = sf.getInt("disease", 0); // 설정한 질환을 불러옴
             position = sf.getString("position", "0000000000");
            tvDisease.setText(diseaseNames[diseaseIndex]);
        }

        // 초기화 버튼
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
                builder.setTitle("초기화")
                        .setMessage("수면 정보가 모두 삭제됩니다")
                        .setPositiveButton("초기화", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Main Activity 재시작
                                startActivity(new Intent(getContext(), MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                ((MainActivity) getActivity()).resetData();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 아무 동작 없음
                            }
                        });
                builder.create().show();
            }
        });

        // 사용자 조정 버튼
        bedPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.dialog_bed_position, null);
                Button completeButton = dlgView.findViewById(R.id.yesButton);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog dialog = builder.setView(dlgView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                // 완료 버튼
                completeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        });

        // 나의 질환 버튼
        myDiseaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.select_disease_layout, null);
                diseaseRadioGroup = dlgView.findViewById(R.id.diseaseRadioGroup);
                RadioButton[] radioButtons = new RadioButton[4];
                radioButtons[0] = dlgView.findViewById(R.id.radioButton);
                radioButtons[1] = dlgView.findViewById(R.id.radioButton2);
                radioButtons[2] = dlgView.findViewById(R.id.radioButton3);
                radioButtons[3] = dlgView.findViewById(R.id.radioButton4);
                radioButtons[diseaseIndex].setChecked(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
                builder.setTitle("나의 질환")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 아무 동작 없음
                            }
                        });
                builder.setView(dlgView).create().show();
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
