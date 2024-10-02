package com.lathanhtrong.lvtn.Adapters;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Activities.MainActivity;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Fragments.ItemsFragment;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Prediction;
import java.util.ArrayList;
import java.util.List;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.ItemPredicationBinding;

@SuppressLint("NotifyDataSetChanged")
public class PredicationAdapter extends RecyclerView.Adapter<PredicationAdapter.ViewHolder> {

    private List<Prediction> predictions = new ArrayList<>();
    private DBHandler db;

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(predictions.get(position), position);

        db = new DBHandler(holder.binding.getRoot().getContext());
        String name = predictions.get(position).getName();
        Item item = db.getItembyName(name);
        if (item != null && item.getItem_image() != null && !item.getItem_image().isEmpty()) {
            Glide.with(holder.binding.ivImage.getContext())
                    .load("file:///android_asset/images/" + item.getItem_image() + ".png")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.binding.ivImage);
        }

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), predictions.get(position).getName(), Toast.LENGTH_SHORT).show();
                if (item == null) {
                    return;
                }
                int item_id = item.getItem_id();
                Bundle bundle = new Bundle();
                bundle.putInt("item_id", item_id);
                ItemsFragment itemsFragment = new ItemsFragment();
                itemsFragment.setArguments(bundle);

                MainActivity mainActivity = (MainActivity) v.getContext();
                mainActivity.binding.bottomNavigationView.setSelectedItemId(R.id.nav_items);

                FragmentManager fragmentManager = ((MainActivity) v.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_layout, itemsFragment).commit();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    public void setData(List<Prediction> newData) {
        predictions = newData;
        notifyDataSetChanged();
    }

    public void clear() {
        predictions = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemPredicationBinding binding;

        private ViewHolder(ItemPredicationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Prediction item, int position) {
            binding.setPrediction(item);
            binding.setPosition(position);
            binding.executePendingBindings();
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemPredicationBinding binding = ItemPredicationBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}
