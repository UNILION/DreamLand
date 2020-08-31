package com.example.dreamland;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamland.database.Adjustment;

public class PosDetailAdapter extends RecyclerView.Adapter<PosDetailAdapter.ViewHolder> {

    Adjustment[] items;

    public PosDetailAdapter(Adjustment[] items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.pos_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Adjustment item = items[position];
        holder.tvId.setText(String.valueOf(item.getAdjId()));
        holder.tvTime.setText(item.getAdjTime());
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvTime;

        public ViewHolder(View view) {
            super(view);
            tvId = view.findViewById(R.id.tvId);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }
}

