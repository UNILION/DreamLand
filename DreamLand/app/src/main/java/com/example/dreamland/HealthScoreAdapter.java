package com.example.dreamland;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamland.database.Adjustment;

import java.util.ArrayList;
import java.util.List;

public class HealthScoreAdapter extends RecyclerView.Adapter<HealthScoreAdapter.ViewHolder> {

    ArrayList<AvgOfMonthlyData> items;

    public HealthScoreAdapter(ArrayList<AvgOfMonthlyData> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.health_score_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int index = items.size() - position -1;
        AvgOfMonthlyData item = items.get(index);

        holder.tvDate.setText(item.getDate());
        holder.tvSpo.setText(String.valueOf(item.getSpo()));
        holder.tvHeartRate.setText(String.valueOf(item.getHeartRate()));

        int score = item.getHealthScore();
        holder.tvHealthScore.setText(String.valueOf(score));

        int img;
        if (score <= 40) {
            img = R.drawable.ic_circle_red_24;
        } else if (score <= 70){
            img = R.drawable.ic_circle_yellow_24;
        } else {
            img = R.drawable.ic_circle_green_24;
        }
        holder.ivSignal.setImageResource(img);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvSpo;
        TextView tvHeartRate;
        TextView tvHealthScore;
        ImageView ivSignal;

        public ViewHolder(View view) {
            super(view);
            tvDate = (TextView) view.findViewById(R.id.tv_item_date);
            tvSpo = (TextView) view.findViewById(R.id.tv_item_spo);
            tvHeartRate = (TextView) view.findViewById(R.id.tv_item_heart_rate);
            tvHealthScore = (TextView) view.findViewById(R.id.tv_item_health_score);
            ivSignal = (ImageView) view.findViewById(R.id.iv_item_signal);
        }
    }
}

