package com.example.dreamland;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamland.database.Adjustment;

import java.util.List;

public class PosDetailAdapter extends RecyclerView.Adapter<PosDetailAdapter.ViewHolder> {

    List<Adjustment> items;

    public PosDetailAdapter(List<Adjustment> items) {
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
        Adjustment item = items.get(position);
        holder.tvId.setText(String.valueOf(position + 1));
        holder.tvTime.setText(item.getAdjTime());
        holder.tvBeforePos.setText(item.getBeforePos());
        holder.tvAfterPos.setText(item.getAfterPos());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvTime;
        TextView tvBeforePos;
        TextView tvAfterPos;

        public ViewHolder(View view) {
            super(view);
            tvId = view.findViewById(R.id.tv_id);
            tvTime = view.findViewById(R.id.tv_time);
            tvBeforePos = view.findViewById(R.id.tv_before_pos);
            tvAfterPos = view.findViewById(R.id.tv_after_pos);
        }
    }
}

