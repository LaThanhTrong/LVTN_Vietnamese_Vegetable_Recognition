package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Adapters.CultivateApdater;
import com.lathanhtrong.lvtn.Adapters.CultivateContentApdater;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Cultivate;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.ActivityCultivateBinding;
import java.util.ArrayList;
import java.util.List;

public class CultivateActivity extends AppCompatActivity implements CultivateApdater.OnCultivateClickListener {
    private ActivityCultivateBinding binding;
    ArrayList<Cultivate> cultivate = new ArrayList<>();
    private Item items;
    private DBHandler dbHandler;
    ArrayList<CultivateContent> cultivateContents = new ArrayList<>();
    int item_id = -1; int cul_id = -1;

    public void setItems(Item items) {
        this.items = items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCultivateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHandler = new DBHandler(this);
        Intent intent = getIntent();
        item_id = intent.getIntExtra("item_id", -1);
        cul_id = intent.getIntExtra("cul_id", -1);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CultivateActivity.this, AddContentActivity.class);
                startActivity(intent);
            }
        });

        loadItem();
        loadCultivation();
        loadCultivationContent();
    }

    private void loadCultivation() {
        cultivate.add(new Cultivate(1, getResources().getString(R.string.cultivar)));
        cultivate.add(new Cultivate(2, getResources().getString(R.string.landscape)));
        cultivate.add(new Cultivate(3, getResources().getString(R.string.plant_propagate)));
        cultivate.add(new Cultivate(4, getResources().getString(R.string.planting)));
        cultivate.add(new Cultivate(5, getResources().getString(R.string.plant_caring)));
        cultivate.add(new Cultivate(6, getResources().getString(R.string.harvest)));
        RecyclerView recyclerViewCultivate = binding.rvCultivate;
        CultivateApdater adapterCultivate = new CultivateApdater(cultivate, this);
        recyclerViewCultivate.setAdapter(adapterCultivate);
        if (cul_id != -1) {
            int selectedPosition = getPositionByCulId(cul_id, cultivate);
            if (selectedPosition != -1) {
                recyclerViewCultivate.scrollToPosition(selectedPosition);
                adapterCultivate.setSelectedPosition(selectedPosition);
            }
        }
    }

    private int getPositionByCulId(int culId, List<Cultivate> cul) {
        for (int i = 0; i < cul.size(); i++) {
            if (cul.get(i).getCul_id() == culId) {
                return i;
            }
        }
        return -1;
    }

    private void loadCultivationContent() {
        cultivateContents = dbHandler.getCultivateContentsByItem(cul_id, item_id);
        RecyclerView recyclerViewCultivateContent = binding.rvCultivateContent;
        if (cultivateContents != null) {
            CultivateContentApdater adapterCultivateContent = new CultivateContentApdater(cultivateContents);
            recyclerViewCultivateContent.setAdapter(adapterCultivateContent);
        }
        else {
            Log.d("CultivateActivity", "loadCultivationContent: cultivateContents is null");
        }
    }

    private void loadItem() {
        SharedPreferences preferences = this.getSharedPreferences("LANGUAGE_SETTINGS", this.MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        Item i = dbHandler.getItembyId(item_id);
        Item displayItem = null;
        if (language.equals("vi")) {
            displayItem = new Item(i.getItem_id(), i.getItem_nameVi(), i.getItem_nameVi(), i.getItem_nameVi2(),
                    i.getItem_sname(), i.getItem_descriptionVi(), i.getItem_descriptionVi(),
                    i.getItem_image(), i.getItem_clipart());
        } else {
            displayItem = i;
        }
        if (displayItem != null) {
            setItems(displayItem);
            String baseImageName = displayItem.getItem_clipart();
            String[] possibleExtensions = {".png", ".jpg", ".jpeg"};

            for (String extension : possibleExtensions) {
                String imagePath = "file:///android_asset/images/" + baseImageName + extension;

                try {
                    Glide.with(this)
                            .load(imagePath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.itemIv);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCultivateClick(int culId) {
        cul_id = culId;
        loadCultivationContent();
    }
}