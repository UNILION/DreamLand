package com.example.dreamland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dreamland.asynctask.GetAdjsByIdAsyncTask;
import com.example.dreamland.asynctask.GetAllAdjsAsyncTask;
import com.example.dreamland.asynctask.GetSleepByDateAsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AppDatabase;
import com.example.dreamland.database.Sleep;
import com.willy.ratingbar.ScaleRatingBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class ManagementFragment extends Fragment {

    private SimpleDateFormat format1 = new SimpleDateFormat("M월 d일 E요일");
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private Context context;
    private AppDatabase db;
    SharedPreferences sf;

    private TextView tvWhenWake;
    private TextView tvWhenSleep;
    private TextView tvConTime;
    private TextView tvWaitTime;
    private TextView tvSleepTime;
    private TextView tvOxy;
    private TextView tvPos;
    private TextView tvCondition;
    private ImageView ivCondition;
    private ScrollView scrollView;
    private LinearLayout infoLayout;
    private LinearLayout posLayout;
    private ScaleRatingBar ratingBar;

    private Sleep firstSleep;
    private Sleep lastSleep;
    private Sleep selectedSleep;
    Calendar startDate;
    Calendar endDate;
    HorizontalCalendar horizontalCalendar;
    List<Sleep> sleepList;
    int[] posImages;
    List<Adjustment> items;
    PosDetailAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_management, container, false);

        posImages = new int[]{R.drawable.pos1, R.drawable.pos2, R.drawable.pos3,
                R.drawable.pos4, R.drawable.pos5};

        // 캘린더
        /* start before 1 month from now */
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* end after 1 month from now */
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        horizontalCalendar = new HorizontalCalendar.Builder(root, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .configure()
                .formatTopText("MMM")
                .formatMiddleText("dd")
                .formatBottomText("EEE")
                .textSize(14f, 24f, 14f)
                .showTopText(true)
                .showBottomText(true)
                .textColor(Color.LTGRAY, Color.WHITE)
                .end()
                .build();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvWhenWake = (TextView) view.findViewById(R.id.tvWhenWake);
        tvWhenSleep = (TextView) view.findViewById(R.id.tvWhenSleep);
        tvConTime = (TextView) view.findViewById(R.id.tvConTime);
        tvWaitTime = (TextView) view.findViewById(R.id.tvWaitTime);
        tvSleepTime = (TextView) view.findViewById(R.id.tvSleepTime);
        tvOxy = (TextView) view.findViewById(R.id.tvOxy);
        tvPos = (TextView) view.findViewById(R.id.tvPos);
        infoLayout = (LinearLayout) view.findViewById(R.id.infoLayout);
        posLayout = (LinearLayout) view.findViewById(R.id.posLayout);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        ivCondition = (ImageView) view.findViewById(R.id.iv_condition);
        tvCondition = (TextView) view.findViewById(R.id.tv_condition);
        ratingBar = (ScaleRatingBar) view.findViewById(R.id.ratingBar);

        sf = getContext().getSharedPreferences("bed", Context.MODE_PRIVATE);

        // 초기 화면 세팅
        switchScreen();
        db = AppDatabase.getDatabase(context);

        // 마지막 수면 정보 관찰
        db.sleepDao().getLastSleep().observe(this, new Observer<Sleep>() {
            @Override
            public void onChanged(Sleep sleep) {
                lastSleep = sleep;
                if (lastSleep != null) {
                    try {
                        endDate.setTime(format2.parse(lastSleep.getSleepDate()));
                        horizontalCalendar.setRange(startDate, endDate);
                        updateUI();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 첫 번째 수면 정보 관찰
        db.sleepDao().getFirstSleep().observe(this, new Observer<Sleep>() {
            @Override
            public void onChanged(Sleep sleep) {
                firstSleep = sleep;
                if (firstSleep != null) {
                    try {
                        startDate.setTime(format2.parse(firstSleep.getSleepDate()));
                        horizontalCalendar.setRange(startDate, endDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 캘린더 뷰 날짜 선택시
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                selectedSleep = getSleepByDate(date.getTime());
                if (selectedSleep != null) {
                    setUI(selectedSleep);
                    try {
                        items = new GetAdjsByIdAsyncTask(db.adjustmentDao(),
                                String.valueOf(selectedSleep.getSleepId())).execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 자세교정 클릭시
        posLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dlgView = getLayoutInflater().from(getContext()).inflate(
                        R.layout.pos_detail_layout, null);
                RecyclerView recyclerview = dlgView.findViewById(R.id.posRecyclerview);
                recyclerview.setLayoutManager(
                        new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                adapter = new PosDetailAdapter(items);
                recyclerview.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
                builder.setTitle("자세 교정 시간")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 동작 없음
                            }
                        });
                builder.setView(dlgView).create().show();
            }
        });

        updateUI();
    }

    // 지정 날짜의 Sleep 가져오기
    private Sleep getSleepByDate(Date date) {
        try {
            String strDate = format2.format(date);
            return new GetSleepByDateAsyncTask(db.sleepDao(), strDate).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    // 인자에 들어간 sleep으로 UI 세팅
    private void setUI(Sleep sleep) {
        if (sleep != null) {
            try {
                // 날짜 변환
                Date date = format2.parse(sleep.getSleepDate());
                if (date != null) {
                    // TextView들의 text 변경
                    tvWhenWake.setText(sleep.getWhenWake());
                    tvConTime.setText(sleep.getConTime());
                    // 시작 시간과 잠든 시간의 차
                    long diffTime = sdf.parse(sleep.getWhenSleep()).getTime() - sdf.parse(sleep.getWhenStart()).getTime();
                    diffTime -= (1000 * 60 * 60 * 9); // 기본 9시간을 뺌
                    tvWaitTime.setText(sdf.format(diffTime));
                    tvWhenSleep.setText(sleep.getWhenSleep());
                    tvSleepTime.setText(sleep.getSleepTime());
                    tvOxy.setText(Integer.toString(sleep.getOxyStr()));
                    tvPos.setText(Integer.toString(sleep.getAdjCount()));
                    ratingBar.setRating(sleep.getSatLevel());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // 최신 수면 정보로 업데이트
    void updateUI() {
        if (lastSleep != null) {
            setUI(lastSleep);
            selectedSleep = lastSleep;
            horizontalCalendar.selectDate(endDate, true);
        }
    }

    // 무호흡 모드일때 뷰를 변경
    public void changeConditionView() {
        ivCondition.setImageResource(R.drawable.ic_cry_24dp);
        tvCondition.setText("무호흡");
    }

    // 수면 정보가 하나도 없다면 추가해달라는 화면으로 바꿈
    void switchScreen() {
        if (sleepList != null) {
            if (sleepList.size() == 0) {
                infoLayout.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
            } else {
                infoLayout.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            }
        }
    }
}