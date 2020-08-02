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

import com.xw.repo.BubbleSeekBar;

public class SettingFragment extends Fragment {

    SharedPreferences sf;
    LinearLayout resetButton;
    LinearLayout myDiseaseButton;
    LinearLayout comfortableDirectionButton;
    TextView tvDesease;
    BubbleSeekBar bubbleSeekBar4;
    View line1;
    View line2;


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
        comfortableDirectionButton = (LinearLayout) view.findViewById(R.id.comfortableDirectionLayout);
        tvDesease = (TextView) view.findViewById(R.id.textView16);
        bubbleSeekBar4 = (BubbleSeekBar) view.findViewById(R.id.bubbleSeekBar4);
        line1 = (View) view.findViewById(R.id.view1);
        line2 = (View) view.findViewById(R.id.view2);

        sf = getContext().getSharedPreferences("bed", getContext().MODE_PRIVATE);

        // 무호흡모드이면 질병 관련 뷰들을 숨김
        if (sf.getInt("mode", 0) == 2) {
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

        // 편한 수면 방향 버튼
        comfortableDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.select_direction_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
                builder.setTitle("편한 수면 방향")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO 수면 방향 저장하고 라디오 버튼과 프래그먼트의 상태를
                                // 해당 수면 방향으로 변경
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

    public void hideDeseaseView() {
        myDiseaseButton.setVisibility(View.GONE);
        line1.setVisibility(View.GONE);
        tvDesease.setVisibility(View.GONE);
        bubbleSeekBar4.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
    }
}
