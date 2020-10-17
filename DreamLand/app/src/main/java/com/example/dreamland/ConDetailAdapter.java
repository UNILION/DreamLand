package com.example.dreamland;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamland.database.Condition;

import java.util.List;

public class ConDetailAdapter extends RecyclerView.Adapter<ConDetailAdapter.ViewHolder> {

    List<Condition> items;

    public ConDetailAdapter(List<Condition> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.con_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Condition item = items.get(position);
        holder.tvId.setText(String.valueOf(position + 1));
        holder.tvBeforePos.setText(item.getStartTime());
        holder.tvAfterPos.setText(item.getEndTime());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvBeforePos;
        TextView tvAfterPos;

        public ViewHolder(View view) {
            super(view);
            tvId = view.findViewById(R.id.tv_id);
            tvBeforePos = view.findViewById(R.id.tv_before_pos);
            tvAfterPos = view.findViewById(R.id.tv_after_pos);
        }
    }
}

