package com.lathanhtrong.lvtn.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.slider.Slider;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.FragmentModelConfigBinding;

public class ModelConfigFragment extends Fragment {

    private FragmentModelConfigBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentModelConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadConfig(requireContext());
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.frame_layout, new SettingsFragment()).commit();
            }
        });

        binding.confSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.confScore.setText((int) value + "%");
            }
        });

        binding.detectSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.detectNum.setText(String.valueOf((int) value));
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfig(requireContext(), binding.confScore.getText().toString().replace("%", ""), binding.detectNum.getText().toString());
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void loadConfig(Context context) {
        SharedPreferences conf_preferences = context.getSharedPreferences("CONFIDENCE_SETTINGS", context.MODE_PRIVATE);
        SharedPreferences numClass_preferences = context.getSharedPreferences("NUMCLASS_SETTINGS", context.MODE_PRIVATE);
        String confidence = conf_preferences.getString("confidence", Values.Confidence);
        String numClass = numClass_preferences.getString("numClass", Values.NumClass);
        setConfig(context,confidence, numClass);
        binding.confSlider.setValue(Float.parseFloat(confidence));
        binding.detectSlider.setValue(Float.parseFloat(numClass));
        binding.confScore.setText(confidence + "%");
        binding.detectNum.setText(numClass);
    }

    public void setConfig(Context context, String confidence, String numClass) {
        SharedPreferences conf_preferences = context.getSharedPreferences("CONFIDENCE_SETTINGS", context.MODE_PRIVATE);
        SharedPreferences numClass_preferences = context.getSharedPreferences("NUMCLASS_SETTINGS", context.MODE_PRIVATE);
        SharedPreferences.Editor conf_editor = conf_preferences.edit();
        SharedPreferences.Editor numClass_editor = numClass_preferences.edit();
        conf_editor.putString("confidence", confidence);
        numClass_editor.putString("numClass", numClass);
        conf_editor.apply();
        numClass_editor.apply();
    }
}