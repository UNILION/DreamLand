package com.example.dreamland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dreamland.asynctask.GetAdjsBySleepDateAsyncTask;
import com.example.dreamland.asynctask.GetConsBySleepDateAsyncTask;
import com.example.dreamland.asynctask.GetSleepByDateAsyncTask;
import com.example.dreamland.database.Adjustment;
import com.example.dreamland.database.AppDatabase;
import com.example.dreamland.database.Condition;
import com.example.dreamland.database.Sleep;
import com.willy.ratingbar.ScaleRatingBar;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import at.grabner.circleprogress.CircleProgressView;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarItemStyle;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarPredicate;

import static com.example.dreamland.MySimpleDateFormat.sdf3;

public class ManagementFragment extends Fragment {

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
    private TextView tvHeartRate;
    private TextView tvHumidity;
    private TextView tvTemperature;
    private TextView tvMoved;
    private TextView tvHealthScore;
    private ImageView ivCondition;
    private LinearLayout infoLayout;
    private LinearLayout sleepDataLayout;
    private LinearLayout conLayout;
    private LinearLayout posLayout;
    private LinearLayout adjustLayout;
    private ScaleRatingBar ratingBar;
    private CircleProgressView circleProgressView;

    private Sleep firstSleep;
    private Sleep lastSleep;
    private Sleep selectedSleep;
    Calendar startDate;
    Calendar endDate;
    HorizontalCalendar horizontalCalendar;
    List<Sleep> sleepList;
    int[] posImages;
    List<Adjustment> adjItems;
    List<Condition> conItems;
    PosDetailAdapter posAdapter;
    ConDetailAdapter conAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_management, container, false);

        context = getContext();

        posImages = new int[]{R.drawable.pos1, R.drawable.pos2, R.drawable.pos3,
                R.drawable.pos4, R.drawable.pos5};

        // 캘린더
        /* start before 1 month from now */
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* end after 1 month from now */
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        HorizontalCalendarPredicate horizontalCalendarPredicate = new HorizontalCalendarPredicate() {
            @Override
            public boolean test(Calendar date) {
                Sleep testSleep = getSleepByDate(date.getTime());
                return testSleep == null;
            }

            @Override
            public CalendarItemStyle style() {
                return new CalendarItemStyle().setColorTopText(getResources().getColor(R.color.colorGray))
                        .setColorMiddleText(getResources().getColor(R.color.colorGray))
                        .setColorBottomText(getResources().getColor(R.color.colorGray));
            }
        };

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
                .disableDates(horizontalCalendarPredicate)
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
        tvHeartRate = (TextView) view.findViewById(R.id.tvHeartRate);
        tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
        tvTemperature = (TextView) view.findViewById(R.id.tvTemperature);
        tvMoved = (TextView) view.findViewById(R.id.tvMoved);
        tvHealthScore = (TextView) view.findViewById(R.id.tv_health_score);
        infoLayout = (LinearLayout) view.findViewById(R.id.infoLayout);
        conLayout = (LinearLayout) view.findViewById(R.id.con_layout);
        posLayout = (LinearLayout) view.findViewById(R.id.posLayout);
        sleepDataLayout = (LinearLayout) view.findViewById(R.id.sleepDataLayout);
        adjustLayout = (LinearLayout) view.findViewById(R.id.adjust_layout);
        ivCondition = (ImageView) view.findViewById(R.id.iv_condition);
        tvCondition = (TextView) view.findViewById(R.id.tv_condition);
        ratingBar = (ScaleRatingBar) view.findViewById(R.id.ratingBar);
        circleProgressView = (CircleProgressView) view.findViewById(R.id.circle_progressview);

        sf = getContext().getSharedPreferences("bed", Context.MODE_PRIVATE);

        if (((MainActivity) getActivity()).mode == InitActivity.DISEASE_ALLEVIATION_MODE) {
            hideAdjustLayout();
        }

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
                        endDate.setTime(sdf3.parse(lastSleep.getSleepDate()));
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
                        startDate.setTime(sdf3.parse(firstSleep.getSleepDate()));
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
                        adjItems = new GetAdjsBySleepDateAsyncTask(db.adjustmentDao(),
                                selectedSleep.getSleepDate()).execute().get();
                        conItems = new GetConsBySleepDateAsyncTask(db.conditionDao(),
                                selectedSleep.getSleepDate()).execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "수면 정보가 없어요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 자세교정 클릭시
        posLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tvPos.getText().equals("0")) {
                    View dlgView = getLayoutInflater().from(getContext()).inflate(
                            R.layout.dialog_details, null);

                    RecyclerView recyclerview = dlgView.findViewById(R.id.recyclerview);
                    recyclerview.setLayoutManager(
                            new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    posAdapter = new PosDetailAdapter(adjItems);

                    recyclerview.setAdapter(posAdapter);

                    Button okButton = dlgView.findViewById(R.id.okButton);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final AlertDialog dialog = builder.setView(dlgView).create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    // 확인 버튼
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        // 상태 확인
        conLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tvConTime.equals("00:00")) {
                    View dlgView = getLayoutInflater().from(getContext()).inflate(
                            R.layout.dialog_details, null);

                    RecyclerView recyclerview = dlgView.findViewById(R.id.recyclerview);
                    recyclerview.setLayoutManager(
                            new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    conAdapter = new ConDetailAdapter(conItems);
                    recyclerview.setAdapter(conAdapter);

                    Button okButton = dlgView.findViewById(R.id.okButton);
                    TextView tvTitle = dlgView.findViewById(R.id.tv_title);
                    if (((MainActivity) getActivity()).mode == 1) {
                        tvTitle.setText("코골이");
                    } else {
                        tvTitle.setText("무호흡");
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    final AlertDialog dialog = builder.setView(dlgView).create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    // 확인 버튼
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        updateUI();
    }

    // 지정 날짜의 Sleep 가져오기
    private Sleep getSleepByDate(Date date) {
        try {
            String strDate = sdf3.format(date);
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
                Date date = sdf3.parse(sleep.getSleepDate());
                if (date != null) {
                    // TextView들의 text 변경
                    tvWhenWake.setText(sleep.getWhenWake());
                    tvConTime.setText(sleep.getConTime());
                    tvWaitTime.setText(sleep.getAsleepAfter());
                    tvWhenSleep.setText(sleep.getWhenSleep());
                    tvSleepTime.setText(sleep.getSleepTime());
                    tvOxy.setText(Integer.toString(sleep.getOxyStr()));
                    tvPos.setText(Integer.toString(sleep.getAdjCount()));
                    ratingBar.setRating(sleep.getSatLevel());
                    tvHeartRate.setText(Integer.toString(sleep.getHeartRate()));
                    tvHumidity.setText(Integer.toString(sleep.getHumidity()));
                    tvTemperature.setText(Integer.toString(sleep.getTemperature()));
                    tvMoved.setText(Integer.toString(sleep.getMoved()));

                    int score = sleep.getScore();
                    circleProgressView.setValue(score);
                    tvHealthScore.setText(Integer.toString(score));

                    int color;
                    if (score <= 40) {
                        color = R.color.colorSignalRed;
                    } else if (score <= 70) {
                        color = R.color.colorOrange;
                    } else {
                        color = R.color.trafficColorGreen;
                    }
                    circleProgressView.setBarColor(getResources().getColor(color));
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

    public void hideAdjustLayout() {
        adjustLayout.setVisibility(View.GONE);
    }

    // 수면 정보가 하나도 없다면 추가해달라는 화면으로 바꿈
    void switchScreen() {
        if (sleepList != null) {
            if (sleepList.size() == 0) {
                infoLayout.setVisibility(View.VISIBLE);
                sleepDataLayout.setVisibility(View.GONE);
            } else {
                infoLayout.setVisibility(View.GONE);
                sleepDataLayout.setVisibility(View.VISIBLE);
            }
        }
    }
}