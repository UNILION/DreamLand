package com.example.dreamland;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.dreamland.asynctask.NonRecommImageAsyncTask;

import java.lang.reflect.Field;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    Button startButton;
    TimePicker timePicker;
    Context context;

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

    private void startSleep() {
        Intent intent = new Intent(getContext(), SleepingActivity.class);
        intent.putExtra("hour", timePicker.getHour());
        intent.putExtra("minute", timePicker.getMinute());
        startActivity(intent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        View dlgView = getLayoutInflater().from(getContext()).inflate(
                R.layout.diaglog_before_sleep, null);
        builder.setView(dlgView).create().show();
    }
}
