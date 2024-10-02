package com.lathanhtrong.lvtn.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.ItemRvBinding;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }

    private List<Item> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private OnItemClickListener listener;

    public ItemAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String baseClipartName = items.get(holder.getAdapterPosition()).getItem_clipart();
        String[] possibleExtensions = {".png", ".jpg", ".jpeg"};

        for (String extension : possibleExtensions) {
            String imagePath = "file:///android_asset/images/" + baseClipartName + extension;

            try {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.binding.itemIv);

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onItemClick(items.get(holder.getAdapterPosition()).getItem_id());
                }
            }
        });

        if (selectedPosition == holder.getAdapterPosition()) {
            holder.binding.item.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.corner));
            holder.binding.item.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.light_green));
        }
        else{
            holder.binding.item.setBackground(holder.itemView.getContext().getResources().getDrawable(R.drawable.corner));
            holder.binding.item.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.white));
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

    public void setData(List<Item> newData) {
        items = newData;
        notifyDataSetChanged();
    }

    public void clear() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemRvBinding binding;

        private ViewHolder(ItemRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemRvBinding binding = ItemRvBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}
