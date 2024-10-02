package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.DetectionActivityBinding;

public class DetectionActivity extends AppCompatActivity {

    DetectionActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DetectionActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}