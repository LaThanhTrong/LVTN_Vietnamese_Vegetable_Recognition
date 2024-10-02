package com.lathanhtrong.lvtn.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.lathanhtrong.lvtn.Models.Cultivate;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.CultivateRvBinding;
import java.util.ArrayList;
import java.util.List;

public class CultivateApdater extends RecyclerView.Adapter<CultivateApdater.ViewHolder> {

    public interface OnCultivateClickListener {
        void onCultivateClick(int culId);
    }

    private List<Cultivate> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private OnCultivateClickListener listener;

    public CultivateApdater(List<Cultivate> items, OnCultivateClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.tvLabel.setText(items.get(position).getCul_name());

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onCultivateClick(items.get(holder.getAdapterPosition()).getCul_id());
                }
            }
        });

        if (selectedPosition == holder.getAdapterPosition()) {
            holder.binding.cul.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.corner));
            holder.binding.cul.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.emerald));
            holder.binding.tvLabel.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.binding.tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        else{
            holder.binding.cul.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.white_corner_bg));
            holder.binding.cul.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.white));
            holder.binding.tvLabel.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.binding.tvLabel.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setData(List<Cultivate> newData) {
        items = newData;
        notifyDataSetChanged();
    }

    public void clear() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CultivateRvBinding binding;

        private ViewHolder(CultivateRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            CultivateRvBinding binding = CultivateRvBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}
