package com.lathanhtrong.lvtn.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Adapters.HistoryAdapter;
import com.lathanhtrong.lvtn.Models.History;
import com.lathanhtrong.lvtn.databinding.ActivityHistoryRecognitionBinding;

import java.util.ArrayList;

public class HistoryRecognitionActivity extends AppCompatActivity {

    ActivityHistoryRecognitionBinding binding;
    private DBHandler dbHandler;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryRecognitionBinding.inflate(getLayoutInflater());
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
        ArrayList<History> history = new ArrayList<>();
        history = dbHandler.getHistories();

        if (history != null) {
            recyclerView = binding.recyclerView;
            HistoryAdapter historyAdapter = new HistoryAdapter(history, this);
            recyclerView.setAdapter(historyAdapter);
        }
    }
}