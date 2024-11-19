package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.ActivityOpenPictureBinding;

public class OpenPictureActivity extends AppCompatActivity {

    ActivityOpenPictureBinding binding;

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image");
        if (imageUrl == null || imageUrl.isEmpty()) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpenPictureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image");

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Glide.with(this).load(imageUrl).into(binding.zoomImageView);
    }
}