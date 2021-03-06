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
    LineChart lineChart; // ?????? ??????
    LineChart lineChart2; // ?????? ??????
    LineChart lineChart3; // ??????????????? ?????? ??????
    LineChart lineChart4; // ?????? ??????
    LineChart lineChart5; // ?????????, ????????? ??????
    LineChart healthScoreChart;
    BarChart barChart; // ?????? ??????
    BarChart barChart2; // ?????? ?????????
    BarChart barChart3; // ?????? ?????????
    PieChart posPieChart; // ?????? ?????? ??????

    MaterialSpinner spinner; // ?????? ?????? ???????????? ?????????
    Button mapsButton;  // ??? ?????? ??????

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
        spinner.setItems("????????? ????????? ???????????????.","?????? ??????","?????? ??????","??????????????? ????????????","?????? ??????", "????????? ??????","?????? ??????","?????? ?????????","?????? ?????????");
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
        spinner.setItems("????????? ????????? ???????????????.","?????? ??????","?????? ??????","??????????????? ????????????","?????? ??????", "????????? ??????","?????? ??????","?????? ?????????","?????? ?????????");

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() { // ???????????? ????????? ???

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
                    Snackbar.make(view, item + " ?????????.", Snackbar.LENGTH_LONG).show();
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

        // ?????? ?????? ?????????
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
                    if (score <= 40) {  // ?????????
                        color= R.color.colorRed;
                        imgRes = R.drawable.ic_signal_red;
                        evaluation = "?????????????????? ?????? ???????????? ?????????\n" +
                                "???????????? ??????????????? ????????? ???????????? ?????? ????????????";
                        mapsButton.setVisibility(View.VISIBLE);
                    } else if (score <= 70){  // ?????????
                        color = R.color.colorOrange;
                        imgRes = R.drawable.ic_signal_yellow;
                        evaluation = "???????????? ?????????????????? ?????? ???????????? ?????????\n"
                        + "?????? ???????????? ???????????? ?????? ??????????????? ?????? ??????????????? ?????? ?????? ??????, " +
                                "????????? ??????, ????????? ??????, ????????? ?????? ??????, ???????????? ?????? ??? " +
                                "?????? ????????? ????????? ?????? ?????? ????????? ???????????? ?????? ????????????";
                        mapsButton.setVisibility(View.GONE);
                    } else {  // ?????????
                        color = R.color.trafficColorGreen;
                        imgRes = R.drawable.ic_signal_green;
                        evaluation = "????????? ????????????!\n???????????? ???????????? ???????????????";
                        mapsButton.setVisibility(View.GONE);
                    }
                    strTrafficDaily.setText("????????????\n\n"
                            + evaluation);
                    strTrafficScore.setTextColor(getResources().getColor(color)); // ????????? ??? ??????
                    strTrafficScore.setText(score+ "???"); // ?????? ??????
                    imgTrafficImg.setImageResource(imgRes); // ????????? ??????
                }
            }
        });

        // ?????? ?????? ??????
        healthScoreChart = view.findViewById(R.id.health_score_chart);
        final XAxis scoreChartXAxis = healthScoreChart.getXAxis(); // X???

        final YAxis scoreChartYAxis = healthScoreChart.getAxisLeft(); // Y???
        setLineChartOptions(healthScoreChart, scoreChartXAxis, scoreChartYAxis);

        final ArrayList<String> scoreChartXLabels = new ArrayList<>();

        final ArrayList<Entry> scoreEntries = new ArrayList<>();

        // ??????????????? ?????????
        db.sleepDao().getAll().observe(this, new Observer<List<Sleep>>() {
            @Override
            public void onChanged(List<Sleep> sleeps) {
                scoreEntries.clear();
                scoreChartXLabels.clear();
                dateMap.clear();
                avgOfMonthlyDatas.clear();

                // ?????? ?????? ?????? ??????
                posPieChart.animateY(1000, Easing.EaseInOutCubic);
                posPieChart.setEntryLabelColor(getResources().getColor(R.color.colorBlack));
                posPieChart.setEntryLabelTextSize(10f);
                posPieChart.setUsePercentValues(true);

                int postures[] = getPostureCount(sleeps);
                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                pieEntries.add(new PieEntry(postures[0], "?????????"));
                pieEntries.add(new PieEntry(postures[1], "??????"));
                pieEntries.add(new PieEntry(postures[2], "?????????"));

                PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                pieDataSet.setValueTextColor(R.color.colorWhite);
                pieDataSet.setValueTextSize(15f);

                PieData pieData = new PieData(pieDataSet);
                pieData.setValueTextColor(getResources().getColor(R.color.colorBlack));
                posPieChart.setData(pieData);

                if (!sleeps.isEmpty()) {
                    String thisMonth = sdf3.format(Calendar.getInstance().getTime()).substring(0, 6);

                    // ????????? hash map??? ??????
                    for (Sleep sleep : sleeps) {
                        String date = sleep.getSleepDate().substring(0, 6); // sleep??? ????????? ????????? ???
                        if (!date.equals(thisMonth)) { // ?????? ??? ??????
                            if (!dateMap.containsKey(date)) {
                                dateMap.put(date, new ArrayList<Sleep>());
                            }
                            dateMap.get(date).add(sleep);
                        }
                    }
                    if (!dateMap.isEmpty()) { // ?????? ???????????? ??????
                        ArrayList<String> keys = new ArrayList<>(dateMap.keySet());
                        Collections.sort(keys); // ?????? ??????????????? ??????
                        int i = 0;
                        for (String key : keys) {

                            int sumOfScore = 0;
                            int sumOfSpo = 0;
                            int sumOfHeartRate = 0;

                            // x????????? ?????? ?????? ??????
                            String date = key.substring(2, 4) + "/" + key.substring(4, 6);
                            scoreChartXLabels.add(date);
                            AvgOfMonthlyData monthlyData = new AvgOfMonthlyData();
                            monthlyData.setDate(date); // ?????? ?????????

                            ArrayList<Sleep> sleepArrayList = dateMap.get(key); // ?????? ?????? Sleep ?????????
                            for (Sleep sleep : sleepArrayList) {
                                sumOfScore += sleep.getScore();
                                sumOfSpo += sleep.getOxyStr();
                                sumOfHeartRate += sleep.getHeartRate();
                            }

                            // ??? ?????? ?????? ?????????
                            int avgOfScore = sumOfScore / sleepArrayList.size();
                            int avgOfSpo = sumOfSpo / sleepArrayList.size();
                            int avgOfHeartRate = sumOfHeartRate / sleepArrayList.size();

                            monthlyData.setHealthScore(avgOfScore);
                            monthlyData.setSpo(avgOfSpo);
                            monthlyData.setHeartRate(avgOfHeartRate);

                            avgOfMonthlyDatas.add(monthlyData);

                            scoreEntries.add(new Entry(i, avgOfScore)); // ???????????? ??????
                            i++;
                            scores.add(avgOfScore);
                        }
                        int sum = 0;
                        for (int score : scores) {
                            sum += score;
                        }
                        avgOfTotalScore = sum / scores.size(); // ?????? ??????????????? ??????
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
                        return (int) value + "???";
                    }
                });

                // ?????? ?????? ????????? ???????????? ??????
                LineDataSet scoreDataSet = new LineDataSet(scoreEntries, "Label");
                setLineDataSetOptions(scoreDataSet);
                LineData scoreLineData = new LineData(scoreDataSet);
                healthScoreChart.setData(scoreLineData);
                healthScoreChart.invalidate(); // refresh
            }
        });

        // ?????? ?????? ??????


        // ???????????? ??????
        lineChart = view.findViewById(R.id.lineChart);
        final XAxis xAxis = lineChart.getXAxis(); // X???
        final YAxis yAxis = lineChart.getAxisLeft(); // Y???
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

        // ???????????? ??????
        lineChart2 = view.findViewById(R.id.lineChart2);
        final XAxis xAxis2 = lineChart2.getXAxis(); // X???
        final YAxis yAxis2 = lineChart2.getAxisLeft(); // Y???
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

        // ??????????????? ?????? ?????? ??????
        lineChart3 = view.findViewById(R.id.lineChart3);
        final XAxis xAxis3 = lineChart3.getXAxis(); // X???
        final YAxis yAxis3 = lineChart3.getAxisLeft(); // Y???
        setLineChartOptions(lineChart3, xAxis3, yAxis3);

        final String[] yLabels3 = {
                "0???", "5???", "10???", "15???", "20???", "25???", "30???", "35???", "40???", "45???", "50???", "55???",
                "1??????", "1?????? 5???", "1?????? 10???", "1?????? 15???", "1?????? 20???", "1?????? 25???", "1?????? 30???",
                "1?????? 35???", "1?????? 40???", "1?????? 45???", "1?????? 50???", "2??????"
        };
        final ArrayList<Entry> entries3 = new ArrayList<>();

        // ?????? ?????? ??????
        lineChart4 = view.findViewById(R.id.lineChart4);
        final XAxis xAxis4 = lineChart4.getXAxis(); // X???
        final YAxis yAxis4 = lineChart4.getAxisLeft(); // Y???
        setLineChartOptions(lineChart4, xAxis4, yAxis4);

        final String[] yLabels4 = {
                "0???", "30???", "1??????", "1?????? 30???", "2??????", "2?????? 30???", "3??????", "3?????? 30???",
                "4??????", "4?????? 30???", "5??????", "5?????? 30???", "6??????", "6?????? 30???", "7??????", "7?????? 30???",
                "8??????", "8?????? 30???", "9??????", "9?????? 30???", "10??????", "10?????? 30???", "11??????", "11?????? 30???",
                "12??????", "12?????? 30???", "13??????", "13?????? 30???", "14??????", "14?????? 30???", "15??????"
        };
        final ArrayList<Entry> entries4 = new ArrayList<>();

        // ?????????, ????????? ?????? ??????
        lineChart5 = view.findViewById(R.id.lineChart5);
        final XAxis xAxis5 = lineChart5.getXAxis(); // X???
        final YAxis yAxis5 = lineChart5.getAxisLeft(); // Y???
        setLineChartOptions(lineChart5, xAxis5, yAxis5);
        final ArrayList<Entry> entries5 = new ArrayList<>();

        // ?????? ?????? ??????
        barChart = view.findViewById(R.id.barChart);
        final XAxis xAxis6 = barChart.getXAxis(); // X???
        final YAxis yAxis6 = barChart.getAxisLeft(); // Y???
        setBarChartOptions(barChart, xAxis6, yAxis6);
        final ArrayList<BarEntry> entries6 = new ArrayList<>();

        // ?????? ????????? ??????
        barChart2 = view.findViewById(R.id.barChart2);
        final XAxis xAxis7 = barChart2.getXAxis(); // X???
        final YAxis yAxis7 = barChart2.getAxisLeft(); // Y???
        setBarChartOptions(barChart2, xAxis7, yAxis7);
        final ArrayList<BarEntry> entries7 = new ArrayList<>();

        // ?????? ????????? ??????
        barChart3 = view.findViewById(R.id.barChart3);
        final XAxis xAxis8 = barChart3.getXAxis(); // X???
        final YAxis yAxis8 = barChart3.getAxisLeft(); // Y???
        setBarChartOptions(barChart3, xAxis8, yAxis8);
        final ArrayList<BarEntry> entries8 = new ArrayList<>();

        // ?????? ????????? ?????????
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

                            // ??????????????? ??????
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

                // x, y ?????? ??? ??????
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
                        return (int) value + "???";
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
                        return (int) value + "???";
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

                // ????????? ??? ??????
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

    // ?????? ????????? ?????? ?????? ??????
    private void setLineChartOptions(LineChart lineChart, XAxis xAxis, YAxis yAxis) {
        lineChart.getAxisRight().setEnabled(false); // ????????? ?????? ?????? ??????
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X??? ?????????
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

    // ??? ?????? ?????? ?????? ??????
    private void setBarChartOptions(BarChart barChart, XAxis xAxis, YAxis yAxis) {
        barChart.getAxisRight().setEnabled(false); // ????????? ?????? ?????? ??????
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.setHighlightPerDragEnabled(false);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X??? ?????????
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

    // line data set ?????? ??????
    private void setLineDataSetOptions(LineDataSet dataSet) {
        dataSet.setDrawFilled(true); // ????????? ?????????
        dataSet.setLineWidth(5.0f);
        dataSet.setCircleRadius(7.0f);
        dataSet.setCircleHoleRadius(5.0f);
        dataSet.setValueTextSize(0.0f);
    }

    // bar data set ?????? ??????
    private void setBarDataSetOptions(BarDataSet dataSet) {
        dataSet.setValueTextSize(0.0f);
        dataSet.setBarBorderWidth(2f);
    }

    // ????????? ????????? ????????? y?????? ????????? ??????
    private float timeToFloat(String time, int startHour) {
        try {
            Date date = sdf1.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            long millis = cal.getTimeInMillis(); // ????????? ????????? millis

            // ????????? ?????? 18?????? ??????
            cal.set(Calendar.HOUR_OF_DAY, startHour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long diff = millis - cal.getTimeInMillis(); // ????????? ?????? ???????????? ???

            if (diff < 0) { // ???????????? ????????? ??????
                diff += (1000 * 60 * 60 * 24);
            }
            return (float) diff / (1000 * 60 * 30); // 30??? ???????????? ?????? ???
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    // ????????? y????????? ??????
    private float timeToFloatForMinute(String time, int minute) {
        try {
            Date date = sdf1.parse(time);

            long millis = date.getTime();
            if (millis < 0) {
                millis += (1000 * 60 * 60 * 9);
            }
            return (float) millis / (1000 * 60 * minute); // ???????????? ???????????? ?????? ???
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    // ?????? ????????? ????????? ????????? ?????????????????? ???????????? ??????
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

    // ????????? ?????? ?????? ?????????
    private int[] getPostureCount(List<Sleep> sleeps) {
        int posCount[] = new int[3];
        if (sleeps != null) {
            for (Sleep sleep : sleeps) {
                String pos = sleep.getBestPosture();
                switch (pos) {
                    case "?????????":
                        posCount[0]++;
                        break;
                    case "??????":
                        posCount[1]++;
                        break;
                    case "?????????":
                        posCount[2]++;
                        break;
                }
            }
        }
        return posCount;
    }
}
