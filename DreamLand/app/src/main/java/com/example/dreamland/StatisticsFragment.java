package com.example.dreamland;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class StatisticsFragment extends Fragment {

    LineChart lineChart;

    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.lineChart);
        ArrayList<Entry> entries = new ArrayList<>();



        for (int i = 0; i < 7; i++) {
            Calendar c1 = Calendar.getInstance();
            c1.add(Calendar.MINUTE, 20 * i);
            entries.add(new Entry(i, timeToFloat(c1)));
        }


        XAxis xAxis = lineChart.getXAxis(); // X축
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 아래로
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(12.0f);

        YAxis yAxis = lineChart.getAxisLeft(); // Y축
        yAxis.setTextColor(Color.GRAY);
        yAxis.setGranularity(1f);
        yAxis.setTextSize(12.0f);
        yAxis.setXOffset(15.0f);

        final ArrayList<String> xLabels = new ArrayList<>();
        xLabels.add("09-01");
        xLabels.add("09-02");
        xLabels.add("09-03");
        xLabels.add("09-04");
        xLabels.add("09-05");
        xLabels.add("09-06");
        xLabels.add("09-07");

        final String[] yLabels = {
                "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00",
                "22:30", "23:00", "23:30", "00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
                "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00",
                "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00",
                "16:30", "17:00", "17:30"
        };

        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels.get((int) value);
            }
        });

        yAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return yLabels[(int) value];
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setDrawFilled(true); // 아래쪽 채우기
        dataSet.setLineWidth(4.0f);
        dataSet.setCircleRadius(5.0f);
        dataSet.setCircleHoleRadius(3.0f);
        dataSet.setValueTextSize(0.0f);

        LineData lineData = new LineData(dataSet);

        lineChart.getAxisRight().setEnabled(false); // 오른쪽 라인 사용 안함
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh

    }

    // 입력된 시간을 차트의 y좌표 값으로 변환
    private float timeToFloat(Calendar cal) {
        long millis = cal.getTimeInMillis(); // 입력된 날짜의 millis

        // 시간을 당일 18시로 설정
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long diff = millis - cal.getTimeInMillis(); // 입력된 날과 18시의 차

        if (diff < 0) { // 음수이면 하루를 더함
            diff += (1000 * 60 * 60 * 24);
        }
        return (float) diff / (1000 * 60 * 30); // 30분 간격으로 나눈 값
    }
}
