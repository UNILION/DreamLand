package com.example.dreamland;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import java.util.ArrayList;


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
        entries.add(new Entry(1.0f, 1.0f));
        entries.add(new Entry(2.0f, 2.0f));
        entries.add(new Entry(3.0f, 3.0f));
        entries.add(new Entry(4.0f, 4.0f));
        entries.add(new Entry(5.0f, 5.0f));

        XAxis xAxis = lineChart.getXAxis(); // X축
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 아래로
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(15.0f);

        YAxis yAxis = lineChart.getAxisLeft(); // Y축
        yAxis.setTextColor(Color.GRAY);
        yAxis.setGranularity(1f);
        yAxis.setTextSize(15.0f);
        yAxis.setXOffset(15.0f);

        final ArrayList<String> xLabels = new ArrayList<>();
        xLabels.add("09-01");
        xLabels.add("09-02");
        xLabels.add("09-03");
        xLabels.add("09-04");
        xLabels.add("09-05");
        xLabels.add("09-06");
        xLabels.add("09-07");
        final ArrayList<String> yLabels = new ArrayList<>();
        yLabels.add("00:00");
        yLabels.add("00:30");
        yLabels.add("01:00");
        yLabels.add("01:30");
        yLabels.add("02:00");
        yLabels.add("02:30");

        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels.get((int) value);
            }
        });

        yAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return yLabels.get((int) value);
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
}
