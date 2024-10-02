package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.databinding.ActivityItemDescriptionBinding;

public class ItemDescriptionActivity extends AppCompatActivity {
    private ActivityItemDescriptionBinding binding;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        int id = intent.getIntExtra("item_id", -1);
        dbHandler = new DBHandler(this);
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        if (id == -1) {
            return;
        } else {
            loadItem(id);
        }
    }

    private void loadItem(int item_id) {
        Item item = dbHandler.getItembyId(item_id);
        Item displayItem = null;
        if (Utils.getLanguage(this).equals("vi")) {
            displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                    item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                    item.getItem_image(), item.getItem_clipart());
        } else {
            displayItem = item;
        }
        binding.title.setText(displayItem.getItem_name());
        binding.itemName.setText(displayItem.getItem_name());
        binding.itemSname.setText(displayItem.getItem_sname());
        binding.description.setText(displayItem.getItem_description());

        String baseImageName = displayItem.getItem_image();
        String[] possibleExtensions = {".png", ".jpg", ".jpeg"};

        for (String extension : possibleExtensions) {
            String imagePath = "file:///android_asset/images/" + baseImageName + extension;

            try {
                Glide.with(this)
                        .load(imagePath)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.image);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}