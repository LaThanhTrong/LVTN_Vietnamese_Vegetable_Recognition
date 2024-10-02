package com.lathanhtrong.lvtn.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.databinding.FilterRvBinding;
import com.lathanhtrong.lvtn.Models.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private List<Item> items;
    private Set<String> checkedItems = new HashSet<>();
    private static final String CHECKED_ITEMS_KEY = "checked_items";

    public FilterAdapter(List<Item> items, Context context) {
        this.items = items;
        // Load saved checked items from SharedPreferences
        checkedItems = Utils.getCheckedItems(context);
    }

    @Override
    public FilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return FilterAdapter.ViewHolder.from(parent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(FilterAdapter.ViewHolder holder, int position) {
        Item currentItem = items.get(holder.getAdapterPosition());
        String language = Utils.getLanguage(holder.itemView.getContext());

        if (language.equals("vi")) {
            holder.binding.txtTitle.setText(currentItem.getItem_nameVi());
        } else {
            holder.binding.txtTitle.setText(currentItem.getItem_name());
        }

        // Load the image using Glide
        String baseClipartName = currentItem.getItem_clipart();
        String[] possibleExtensions = {".png", ".jpg", ".jpeg"};
        for (String extension : possibleExtensions) {
            String imagePath = "file:///android_asset/images/" + baseClipartName + extension;
            try {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.binding.imgIcon);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.binding.check.setOnCheckedChangeListener(null);

        // Set the checked state of the checkbox
        holder.binding.check.setChecked(checkedItems.contains(String.valueOf(currentItem.getItem_id())));

        // Set a listener for the checkbox
        holder.binding.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String itemIdString = String.valueOf(currentItem.getItem_id());
            if (isChecked) {
                checkedItems.add(itemIdString);
            } else {
                checkedItems.remove(itemIdString);
            }
            // Save the updated checked items to SharedPreferences
            saveCheckedItems(holder.itemView.getContext());
        });
    }

    private void saveCheckedItems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("checked_items", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CHECKED_ITEMS_KEY, checkedItems);
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private FilterRvBinding binding;

        public ViewHolder(FilterRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            FilterRvBinding binding = FilterRvBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}
