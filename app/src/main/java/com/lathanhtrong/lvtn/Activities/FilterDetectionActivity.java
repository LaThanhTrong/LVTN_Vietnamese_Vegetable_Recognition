package com.lathanhtrong.lvtn.Activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lathanhtrong.lvtn.Adapters.FilterAdapter;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.databinding.ActivityFilterDetectionBinding;
import java.util.ArrayList;
import java.util.List;

public class FilterDetectionActivity extends AppCompatActivity {

    ActivityFilterDetectionBinding binding;
    DBHandler dbHandler;
    private RecyclerView recyclerView;
    List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilterDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHandler = new DBHandler(this);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        loadRecyclerView();
    }

    public void loadRecyclerView() {
        items = dbHandler.getItems();
        String language = Utils.getLanguage(this);
        ArrayList<Item> displayItems = new ArrayList<>();
        for (Item item : items) {
            if (language.equals("vi")) {
                Item displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                        item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                        item.getItem_image(), item.getItem_clipart());
                displayItems.add(displayItem);
            } else {
                displayItems.add(item);
            }
        }

        recyclerView = binding.recyclerView;
        FilterAdapter adapter = new FilterAdapter(displayItems, this);
        recyclerView.setAdapter(adapter);

    }
}