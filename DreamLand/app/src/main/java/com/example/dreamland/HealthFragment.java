package com.example.dreamland;

import android.content.Intent;
import android.content.res.Resources;
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

import com.example.dreamland.database.AppDatabase;
import com.example.dreamland.database.Sleep;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import static com.example.dreamland.MySimpleDateFormat.sdf1;
import static com.example.dreamland.MySimpleDateFormat.sdf3;
import static com.example.dreamland.MySimpleDateFormat.sdf4;


public class HealthFragment extends Fragment {

    private AppDatabase db;
    private HashMap<String, ArrayList<Sleep>> dateMap;
    private ArrayList<Integer> scores;
    int avgOfTotalScore;
    LinearLayout healthInfoLayout;
    ScrollView healthScrollView;
    LineChart lineChart; // 취침 시간
    LineChart lineChart2; // 기상 시간
    LineChart lineChart3; // 잠들기까지 걸린 시간
    LineChart lineChart4; // 수면 시간
    LineChart lineChart5; // 코골이, 무호흡 시간
    LineChart healthScoreChart;
    BarChart barChart; // 자세 교정
    BarChart barChart2; // 수면 만족도
    BarChart barChart3; // 산소 포화도
    PieChart posPieChart; // 최적 자세 통계

    MaterialSpinner spinner; // 통계 선택 드랍다운 스피너
    Button mapsButton;  // 맵 보기 버튼

    TextView strTrafficTitle;
    TextView strTrafficScore;
    TextView strTrafficDaily;
    ImageView imgTrafficImg;

    ArrayList<AvgOfMonthlyData> avgOfMonthlyDatas;
    HealthScoreAdapter adapter;

    public HealthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_health, container, false);

    }

    public void changeView() {
        spinner.setItems("원하는 통계를 선택하세요.","취침 시간","기상 시간","잠들기까지 걸린시간","수면 시간", "무호흡 시간","자세 교정","수면 만족도","산소 포화도");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getDatabase(getContext());

        dateMap = new HashMap<>();
        scores = new ArrayList<>();
        avgOfMonthlyDatas = new ArrayList<>();

        healthInfoLayout = view.findViewById(R.id.health_info_layout);
        healthScrollView = view.findViewById(R.id.health_scrollview);
        strTrafficTitle = view.findViewById(R.id.strTrafficTitle);
        strTrafficScore = view.findViewById(R.id.strTrafficScore);
        strTrafficDaily = view.findViewById(R.id.strTrafficDaily);
        imgTrafficImg = view.findViewById(R.id.imgTrafficImg);
        mapsButton = view.findViewById(R.id.maps_button);
        posPieChart = view.findViewById(R.id.best_pos_chart);


        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapsIntent = new Intent(getContext(), MapsActivity.class);
                startActivity(mapsIntent);
            }
        });


        spinner = view.findViewById(R.id.spinner);
        spinner.setItems("원하는 통계를 선택하세요.","취침 시간","기상 시간","잠들기까지 걸린시간","수면 시간", "코골이 시간","자세 교정","수면 만족도","산소 포화도");

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() { // 스피너를 눌렀을 때

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

                if(position == 0) {

                    strTrafficTitle.setVisibility(View.VISIBLE);
                    strTrafficScore.setVisibility(View.VISIBLE);
                    strTrafficDaily.setVisibility(View.VISIBLE);
                    imgTrafficImg.setVisibility(View.VISIBLE);


                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else
                {
                    Snackbar.make(view, item + " 선택됨.", Snackbar.LENGTH_LONG).show();
                }

                if (position == 1)
                {
                    lineChart.setVisibility(View.VISIBLE);

                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 2)
                {
                    lineChart2.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }

                else  if (position == 3)
                {
                    lineChart3.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 4)
                {
                    lineChart4.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 5)
                {
                    lineChart5.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 6)
                {
                    barChart.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 7)
                {
                    barChart2.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart3.setVisibility(View.GONE);
                }
                else  if (position == 8)
                {
                    barChart3.setVisibility(View.VISIBLE);

                    lineChart.setVisibility(View.GONE);
                    lineChart2.setVisibility(View.GONE);
                    lineChart3.setVisibility(View.GONE);
                    lineChart4.setVisibility(View.GONE);
                    lineChart5.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    barChart2.setVisibility(View.GONE);
                }

            }
        });

        // 건강 점수 리스트
        RecyclerView recyclerview = (RecyclerView) view.findViewById(R.id.health_score_recyclerview);

        recyclerview.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        adapter = new HealthScoreAdapter(avgOfMonthlyDatas);
        recyclerview.setAdapter(adapter);

        db.sleepDao().getLastSleep().observe(this, new Observer<Sleep>() {
            @Override
            public void onChanged(Sleep sleep) {
                if (sleep != null) {
                    int score = sleep.getScore();
                    int color;
                    int imgRes;
                    String evaluation = "";
                    if (score <= 40) {  // 적색등
                        color= R.color.colorRed;
                        imgRes = R.drawable.ic_signal_red;
                        evaluation = "산소포화도가 위험 수치보다 낮아요\n" +
                                "적색등이 지속된다면 병원에 방문하는 것을 추천해요";
                        mapsButton.setVisibility(View.VISIBLE);
                    } else if (score <= 70){  // 황색등
                        color = R.color.colorOrange;
                        imgRes = R.drawable.ic_signal_yellow;
                        evaluation = "심박수와 산소포화도가 정상 수치보다 낮아요\n"
                        + "산소 포화도를 높이려면 고른 영양섭취를 위한 규칙적이고 균형 있는 식사, " +
                                "꾸준한 운동, 충분한 수면, 충분한 수분 섭취, 스트레스 관리 등 " +
                                "체력 상승에 도움이 되는 생활 습관을 유지하는 것이 중요해요";
                        mapsButton.setVisibility(View.GONE);
                    } else {  // 녹색등
                        color = R.color.trafficColorGreen;
                        imgRes = R.drawable.ic_signal_green;
                        evaluation = "건강한 상태에요!\n계속해서 녹색등을 유지하세요";
                        mapsButton.setVisibility(View.GONE);
                    }
                    strTrafficDaily.setText("일일평가\n\n"
                            + evaluation);
                    strTrafficScore.setTextColor(getResources().getColor(color)); // 텍스트 색 변경
                    strTrafficScore.setText(score+ "점"); // 점수 변경
                    imgTrafficImg.setImageResource(imgRes); // 이미지 변경
                }
            }
        });

        // 건강 점수 차트
        healthScoreChart = view.findViewById(R.id.health_score_chart);
        final XAxis scoreChartXAxis = healthScoreChart.getXAxis(); // X축

        final YAxis scoreChartYAxis = healthScoreChart.getAxisLeft(); // Y축
        setLineChartOptions(healthScoreChart, scoreChartXAxis, scoreChartYAxis);

        final ArrayList<String> scoreChartXLabels = new ArrayList<>();

        final ArrayList<Entry> scoreEntries = new ArrayList<>();

        // 수면데이터 변경시
        db.sleepDao().getAll().observe(this, new Observer<List<Sleep>>() {
            @Override
            public void onChanged(List<Sleep> sleeps) {
                scoreEntries.clear();
                scoreChartXLabels.clear();
                dateMap.clear();
                avgOfMonthlyDatas.clear();

                // 최적 수면 자세 차트
                posPieChart.animateY(1000, Easing.EaseInOutCubic);
                posPieChart.setEntryLabelColor(getResources().getColor(R.color.colorBlack));
                posPieChart.setEntryLabelTextSize(10f);
                posPieChart.setUsePercentValues(true);

                int postures[] = getPostureCount(sleeps);
                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                pieEntries.add(new PieEntry(postures[0], "정자세"));
                pieEntries.add(new PieEntry(postures[1], "왼쪽"));
                pieEntries.add(new PieEntry(postures[2], "오른쪽"));

                PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                pieDataSet.setValueTextColor(R.color.colorWhite);
                pieDataSet.setValueTextSize(15f);

                PieData pieData = new PieData(pieDataSet);
                pieData.setValueTextColor(getResources().getColor(R.color.colorBlack));
                posPieChart.setData(pieData);

                if (!sleeps.isEmpty()) {
                    String thisMonth = sdf3.format(Calendar.getInstance().getTime()).substring(0, 6);

                    // 달별로 hash map에 삽입
                    for (Sleep sleep : sleeps) {
                        String date = sleep.getSleepDate().substring(0, 6); // sleep에 저장된 날짜의 달
                        if (!date.equals(thisMonth)) { // 현재 달 제외
                            if (!dateMap.containsKey(date)) {
                                dateMap.put(date, new ArrayList<Sleep>());
                            }
                            dateMap.get(date).add(sleep);
                        }
                    }
                    if (!dateMap.isEmpty()) { // 수면 데이터가 존재
                        ArrayList<String> keys = new ArrayList<>(dateMap.keySet());
                        Collections.sort(keys); // 키를 시간순으로 정렬
                        int i = 0;
                        for (String key : keys) {

                            int sumOfScore = 0;
                            int sumOfSpo = 0;
                            int sumOfHeartRate = 0;

                            // x라벨에 해당 달을 추가
                            String date = key.substring(2, 4) + "/" + key.substring(4, 6);
                            scoreChartXLabels.add(date);
                            AvgOfMonthlyData monthlyData = new AvgOfMonthlyData();
                            monthlyData.setDate(date); // 날짜 데이터

                            ArrayList<Sleep> sleepArrayList = dateMap.get(key); // 해당 달의 Sleep 리스트
                            for (Sleep sleep : sleepArrayList) {
                                sumOfScore += sleep.getScore();
                                sumOfSpo += sleep.getOxyStr();
                                sumOfHeartRate += sleep.getHeartRate();
                            }

                            // 한 달의 평균 데이터
                            int avgOfScore = sumOfScore / sleepArrayList.size();
                            int avgOfSpo = sumOfSpo / sleepArrayList.size();
                            int avgOfHeartRate = sumOfHeartRate / sleepArrayList.size();

                            monthlyData.setHealthScore(avgOfScore);
                            monthlyData.setSpo(avgOfSpo);
                            monthlyData.setHeartRate(avgOfHeartRate);

                            avgOfMonthlyDatas.add(monthlyData);

                            scoreEntries.add(new Entry(i, avgOfScore)); // 엔트리에 추가
                            i++;
                            scores.add(avgOfScore);
                        }
                        int sum = 0;
                        for (int score : scores) {
                            sum += score;
                        }
                        avgOfTotalScore = sum / scores.size(); // 전체 수면점수의 평균
                    }
                }

                adapter.notifyDataSetChanged();

                scoreChartXAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return scoreChartXLabels.get((int) value);
                    }
                });

                scoreChartYAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return (int) value + "점";
                    }
                });

                // 건강 점수 차트의 데이터셋 설정
                LineDataSet scoreDataSet = new LineDataSet(scoreEntries, "Label");
                setLineDataSetOptions(scoreDataSet);
                LineData scoreLineData = new LineData(scoreDataSet);
                healthScoreChart.setData(scoreLineData);
                healthScoreChart.invalidate(); // refresh
            }
        });

        // 최적 자세 차트


        // 취침시간 차트
        lineChart = view.findViewById(R.id.lineChart);
        final XAxis xAxis = lineChart.getXAxis(); // X축
        final YAxis yAxis = lineChart.getAxisLeft(); // Y축
        setLineChartOptions(lineChart, xAxis, yAxis);

        final ArrayList<String> xLabels = new ArrayList<>();
        final String[] yLabels = {
                "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00",
                "22:30", "23:00", "23:30", "00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
                "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00",
                "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00",
                "16:30", "17:00", "17:30"
        };
        final ArrayList<Entry> entries = new ArrayList<>();

        // 기상시간 차트
        lineChart2 = view.findViewById(R.id.lineChart2);
        final XAxis xAxis2 = lineChart2.getXAxis(); // X축
        final YAxis yAxis2 = lineChart2.getAxisLeft(); // Y축
        setLineChartOptions(lineChart2, xAxis2, yAxis2);

        final String[] yLabels2 = {
                "00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
                "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00",
                "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00",
                "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30",
                "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"
        };
        final ArrayList<Entry> entries2 = new ArrayList<>();

        // 잠들끼까지 걸린 시간 차트
        lineChart3 = view.findViewById(R.id.lineChart3);
        final XAxis xAxis3 = lineChart3.getXAxis(); // X축
        final YAxis yAxis3 = lineChart3.getAxisLeft(); // Y축
        setLineChartOptions(lineChart3, xAxis3, yAxis3);

        final String[] yLabels3 = {
                "0분", "5분", "10분", "15분", "20분", "25분", "30분", "35분", "40분", "45분", "50분", "55분",
                "1시간", "1시간 5분", "1시간 10분", "1시간 15분", "1시간 20분", "1시간 25분", "1시간 30분",
                "1시간 35분", "1시간 40분", "1시간 45분", "1시간 50분", "2시간"
        };
        final ArrayList<Entry> entries3 = new ArrayList<>();

        // 수면 시간 차트
        lineChart4 = view.findViewById(R.id.lineChart4);
        final XAxis xAxis4 = lineChart4.getXAxis(); // X축
        final YAxis yAxis4 = lineChart4.getAxisLeft(); // Y축
        setLineChartOptions(lineChart4, xAxis4, yAxis4);

        final String[] yLabels4 = {
                "0분", "30분", "1시간", "1시간 30분", "2시간", "2시간 30분", "3시간", "3시간 30분",
                "4시간", "4시간 30분", "5시간", "5시간 30분", "6시간", "6시간 30분", "7시간", "7시간 30분",
                "8시간", "8시간 30분", "9시간", "9시간 30분", "10시간", "10시간 30분", "11시간", "11시간 30분",
                "12시간", "12시간 30분", "13시간", "13시간 30분", "14시간", "14시간 30분", "15시간"
        };
        final ArrayList<Entry> entries4 = new ArrayList<>();

        // 코골이, 무호흡 시간 차트
        lineChart5 = view.findViewById(R.id.lineChart5);
        final XAxis xAxis5 = lineChart5.getXAxis(); // X축
        final YAxis yAxis5 = lineChart5.getAxisLeft(); // Y축
        setLineChartOptions(lineChart5, xAxis5, yAxis5);
        final ArrayList<Entry> entries5 = new ArrayList<>();

        // 자세 교정 차트
        barChart = view.findViewById(R.id.barChart);
        final XAxis xAxis6 = barChart.getXAxis(); // X축
        final YAxis yAxis6 = barChart.getAxisLeft(); // Y축
        setBarChartOptions(barChart, xAxis6, yAxis6);
        final ArrayList<BarEntry> entries6 = new ArrayList<>();

        // 수면 만족도 차트
        barChart2 = view.findViewById(R.id.barChart2);
        final XAxis xAxis7 = barChart2.getXAxis(); // X축
        final YAxis yAxis7 = barChart2.getAxisLeft(); // Y축
        setBarChartOptions(barChart2, xAxis7, yAxis7);
        final ArrayList<BarEntry> entries7 = new ArrayList<>();

        // 산소 포화도 차트
        barChart3 = view.findViewById(R.id.barChart3);
        final XAxis xAxis8 = barChart3.getXAxis(); // X축
        final YAxis yAxis8 = barChart3.getAxisLeft(); // Y축
        setBarChartOptions(barChart3, xAxis8, yAxis8);
        final ArrayList<BarEntry> entries8 = new ArrayList<>();

        // 수면 데이터 변경시
        db.sleepDao().getRecentSleeps().observe(this, new Observer<List<Sleep>>() {
            @Override
            public void onChanged(List<Sleep> sleeps) {
                entries.clear();
                entries2.clear();
                entries3.clear();
                entries4.clear();
                entries5.clear();
                entries6.clear();
                entries7.clear();
                entries8.clear();
                if (!sleeps.isEmpty()) {
                    for (int i = 0; i < sleeps.size(); i++) {
                        int index = (sleeps.size() - 1) - i;
                        Sleep sleep = sleeps.get(index);
                        try {
                            Date date = sdf3.parse(sleep.getSleepDate());

                            xLabels.add(sdf4.format(date));


                            entries.add(new Entry(i, timeToFloat(
                                    sleep.getWhenSleep(), 18)));
                            entries2.add(new Entry(i, timeToFloat(
                                    sleep.getWhenWake(), 0)));

                            // 잠들기까지 시간
                            Entry entry = new Entry(i, timeToFloatForMinute(
                                    sleep.getAsleepAfter(), 5));
                            entries3.add(entry);
                            entries4.add(new Entry(i, timeToFloatForMinute(
                                    sleep.getSleepTime(), 30)));
                            entries5.add(new Entry(i, timeToFloatForMinute(
                                    sleep.getConTime(), 5)));
                            entries6.add(new BarEntry(i, sleep.getAdjCount()));
                            entries7.add(new BarEntry(i, sleep.getSatLevel()));
                            entries8.add(new BarEntry(i, sleep.getOxyStr()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // x, y 라벨 값 반환
                xAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value < 0) {
                            value = 0;
                        }
                        return yLabels[(int) value % yLabels.length];
                    }
                });

                xAxis2.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis2.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value < 0) {
                            value = 0;
                        }
                        return yLabels2[(int) value % yLabels2.length];
                    }
                });

                xAxis3.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis3.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value < 0) {
                            value = 0;
                        }
                        return yLabels3[(int) value];
                    }
                });

                xAxis4.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis4.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value < 0) {
                            value = 0;
                        }
                        return yLabels4[(int) value];
                    }
                });

                xAxis5.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis5.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value < 0) {
                            value = 0;
                        }
                        return yLabels3[(int) value];
                    }
                });

                xAxis6.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis6.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return (int) value + "번";
                    }
                });

                xAxis7.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis7.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return (int) value + "점";
                    }
                });

                xAxis8.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return xLabels.get((int) value);
                    }
                });

                yAxis8.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return (((int) value) / 10) * 10 + "%";
                    }
                });

                // 데이터 셋 설정
                LineDataSet dataSet = new LineDataSet(entries, "Label");
                setLineDataSetOptions(dataSet);
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.invalidate(); // refresh

                LineDataSet dataSet2 = new LineDataSet(entries2, "Label");
                setLineDataSetOptions(dataSet2);
                LineData lineData2 = new LineData(dataSet2);
                lineChart2.setData(lineData2);
                lineChart2.invalidate(); // refresh

                LineDataSet dataSet3 = new LineDataSet(entries3, "Label");
                setLineDataSetOptions(dataSet3);
                LineData lineData3 = new LineData(dataSet3);
                lineChart3.setData(lineData3);
                lineChart3.invalidate(); // refresh

                LineDataSet dataSet4 = new LineDataSet(entries4, "Label");
                setLineDataSetOptions(dataSet4);
                LineData lineData4 = new LineData(dataSet4);
                lineChart4.setData(lineData4);
                lineChart4.invalidate(); // refresh

                LineDataSet dataSet5 = new LineDataSet(entries5, "Label");
                setLineDataSetOptions(dataSet5);
                LineData lineData5 = new LineData(dataSet5);
                lineChart5.setData(lineData5);
                lineChart5.invalidate(); // refresh

                BarDataSet barDataSet = new BarDataSet(entries6, "Label");
                setBarDataSetOptions(barDataSet);
                BarData barData = new BarData(barDataSet);
                barChart.setData(barData);
                barChart.invalidate();

                BarDataSet barDataSet2 = new BarDataSet(entries7, "Label");
                setBarDataSetOptions(barDataSet2);
                BarData barData2 = new BarData(barDataSet2);
                barChart2.setData(barData2);
                barChart2.invalidate();

                BarDataSet barDataSet3 = new BarDataSet(entries8, "Label");
                setBarDataSetOptions(barDataSet3);
                BarData barData3 = new BarData(barDataSet3);
                barChart3.setData(barData3);
                barChart3.invalidate();
            }
        });

    }

    // 라인 차트의 기본 옵션 세팅
    private void setLineChartOptions(LineChart lineChart, XAxis xAxis, YAxis yAxis) {
        lineChart.getAxisRight().setEnabled(false); // 오른쪽 라인 사용 안함
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 아래로
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(12.0f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        yAxis.setTextColor(Color.GRAY);
        yAxis.setGranularity(1f);
        yAxis.setTextSize(12.0f);
        yAxis.setXOffset(15.0f);
    }

    // 바 차트 기본 옵션 세팅
    private void setBarChartOptions(BarChart barChart, XAxis xAxis, YAxis yAxis) {
        barChart.getAxisRight().setEnabled(false); // 오른쪽 라인 사용 안함
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.setHighlightPerDragEnabled(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 아래로
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(12.0f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        yAxis.setTextColor(Color.GRAY);
        yAxis.setGranularity(1f);
        yAxis.setTextSize(12.0f);
        yAxis.setXOffset(15.0f);
        yAxis.setAxisMinimum(0.0f);
    }

    // line data set 옵션 세팅
    private void setLineDataSetOptions(LineDataSet dataSet) {
        dataSet.setDrawFilled(true); // 아래쪽 채우기
        dataSet.setLineWidth(5.0f);
        dataSet.setCircleRadius(7.0f);
        dataSet.setCircleHoleRadius(5.0f);
        dataSet.setValueTextSize(0.0f);
    }

    // bar data set 옵션 세팅
    private void setBarDataSetOptions(BarDataSet dataSet) {
        dataSet.setValueTextSize(0.0f);
        dataSet.setBarBorderWidth(2f);
    }

    // 입력된 시간을 차트의 y좌표 값으로 변환
    private float timeToFloat(String time, int startHour) {
        try {
            Date date = sdf1.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            long millis = cal.getTimeInMillis(); // 입력된 날짜의 millis

            // 시간을 당일 18시로 설정
            cal.set(Calendar.HOUR_OF_DAY, startHour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long diff = millis - cal.getTimeInMillis(); // 입력된 날과 시작시의 차

            if (diff < 0) { // 음수이면 하루를 더함
                diff += (1000 * 60 * 60 * 24);
            }
            return (float) diff / (1000 * 60 * 30); // 30분 간격으로 나눈 값
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    // 시간을 y좌표로 변환
    private float timeToFloatForMinute(String time, int minute) {
        try {
            Date date = sdf1.parse(time);

            long millis = date.getTime();
            if (millis < 0) {
                millis += (1000 * 60 * 60 * 9);
            }
            return (float) millis / (1000 * 60 * minute); // 입력시간 간격으로 나눈 값
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    // 수면 정보가 하나도 없다면 추가해달라는 화면으로 바꿈
    void switchScreen() {
        if (((MainActivity) getActivity()).sleepList != null) {
            if (((MainActivity) getActivity()).sleepList.size() > 0) {
                healthScrollView.setVisibility(View.VISIBLE);
                healthInfoLayout.setVisibility(View.GONE);
            } else {
                healthScrollView.setVisibility(View.GONE);
                healthInfoLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    // 최적의 자세 수를 카운트
    private int[] getPostureCount(List<Sleep> sleeps) {
        int posCount[] = new int[3];
        if (sleeps != null) {
            for (Sleep sleep : sleeps) {
                String pos = sleep.getBestPosture();
                switch (pos) {
                    case "정자세":
                        posCount[0]++;
                        break;
                    case "왼쪽":
                        posCount[1]++;
                        break;
                    case "오른쪽":
                        posCount[2]++;
                        break;
                }
            }
        }
        return posCount;
    }
}
