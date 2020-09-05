package com.example.dreamland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

public class HomeFragment extends Fragment {

    Button startButton;
    TimePicker timePicker;
    Context context;
    Button selButton1;
    Button selButton2;
    Button selButton3;
    AlertDialog selDiaglog;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startButton = (Button) view.findViewById(R.id.startButton);
        timePicker = view.findViewById(R.id.timePicker);
        context = getContext();


        // 수면 시작 버튼
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    // 수면 시작 함수
    private void startSleep(int selectedMenu) {
        Intent intent = new Intent(getContext(), SleepingActivity.class);
        intent.putExtra("hour", timePicker.getHour());
        intent.putExtra("minute", timePicker.getMinute());
        intent.putExtra("selectedMenu", selectedMenu);
        startActivity(intent);
    }

    // 교정방식 선택 화면
    private void showDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        final View dlgView = getLayoutInflater().from(getContext()).inflate(
                R.layout.dialog_before_sleep, null);
        selButton1 = dlgView.findViewById(R.id.selButton1);
        selButton2 = dlgView.findViewById(R.id.selButton2);
        selButton3 = dlgView.findViewById(R.id.selButton3);

        selDiaglog = builder.setView(dlgView).create();
        selDiaglog.show();

        // 수면 중 교정
        selButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDiaglog.dismiss();
                startSleep(1);
            }
        });

        // 수면 중 한 번 교정
        selButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDiaglog.dismiss();
                startSleep(2);
            }
        });

        // 즉시 교정
        selButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selDiaglog.dismiss();
                startSleep(3);
            }
        });
    }
}
