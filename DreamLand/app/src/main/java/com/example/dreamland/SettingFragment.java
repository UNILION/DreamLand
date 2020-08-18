package com.example.dreamland;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.LineRadarDataSet;
import com.xw.repo.BubbleSeekBar;

public class SettingFragment extends Fragment {

    SharedPreferences sf;
    LinearLayout resetButton;
    LinearLayout myDiseaseButton;
    LinearLayout bedPositionButton;
    TextView tvSleepSetting;
    View line1;
    View line2;
    View line3;


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
        line1 = (View) view.findViewById(R.id.view1);
        line2 = (View) view.findViewById(R.id.view2);
        line3 = (View) view.findViewById(R.id.view3);

        sf = getContext().getSharedPreferences("bed", getContext().MODE_PRIVATE);
        int mode = sf.getInt("mode", 0); // 사용자가 설정한 모드를 불러옴

        // 코골이, 무호흡모드이면 질병 관련 뷰들을 숨김
        if (mode == 1 || mode == 2) {
            hideDeseaseView();
        }

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


        // 나의 질환 버튼
        myDiseaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.select_desease_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
                builder.setTitle("나의 질환")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO 질환을 저장하고 라디오 버튼과 프래그먼트의 상태를 해당 질환으로 변경
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
    public void hideDeseaseView() {
        myDiseaseButton.setVisibility(View.GONE);
        bedPositionButton.setVisibility(View.GONE);
        tvSleepSetting.setVisibility(View.GONE);
        line1.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        line3.setVisibility(View.GONE);
    }
}
