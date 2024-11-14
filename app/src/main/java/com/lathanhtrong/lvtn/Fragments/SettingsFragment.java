package com.lathanhtrong.lvtn.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.FragmentSettingsBinding;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadLanguage(requireContext());
        loadModel(requireContext());
        binding.languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });
        binding.modelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModelDialog();
            }
        });
        binding.modelConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.frame_layout, new ModelConfigFragment()).commit();
            }
        });
    }

    private void showModelDialog() {
        final String[] models = {"YOLOv11-GhostNet","YOLOv11", "YOLOv10"};
        SharedPreferences preferences = getContext().getSharedPreferences("MODEL_SETTINGS", getContext().MODE_PRIVATE);
        int item = preferences.getInt("item", 0);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.select_model);
        builder.setSingleChoiceItems(models, item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    setModel(requireContext(), Values.YOLOCustom, 0);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                } else if (position == 1) {
                    setModel(requireContext(), Values.YOLOv8n, 1);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                }
                else if (position == 2) {
                    setModel(requireContext(), Values.YOLOv10n, 2);
                    getFragmentManager().beginTransaction().detach(SettingsFragment.this).commit();
                    getFragmentManager().beginTransaction().attach(SettingsFragment.this).commit();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Tiếng Việt"};

        SharedPreferences preferences = getContext().getSharedPreferences("LANGUAGE_SETTINGS", getContext().MODE_PRIVATE);
        int item = preferences.getInt("item", 0);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.select_language);
        builder.setSingleChoiceItems(languages, item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Intent i = getActivity().getIntent();
                if (position == 0) {
                    setLanguage(requireContext(),"en", 0);
                    i.putExtra("LOAD_SETTINGS_FRAGMENT", true);
                    getActivity().finish();
                    startActivity(i);
                } else if (position == 1) {
                    setLanguage(requireContext(),"vi", 1);
                    i.putExtra("LOAD_SETTINGS_FRAGMENT", true);
                    getActivity().finish();
                    startActivity(i);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void setModel(Context context, String model, int item) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MODEL_SETTINGS", context.MODE_PRIVATE).edit();
        editor.putString("model", model);
        editor.putInt("item", item);
        editor.apply();
    }

    public static void setLanguage(Context context, String language, int item) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = context.getSharedPreferences("LANGUAGE_SETTINGS", context.MODE_PRIVATE).edit();
        editor.putString("language", language);
        editor.putInt("item", item);
        editor.apply();
    }

    public static void loadModel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MODEL_SETTINGS", Context.MODE_PRIVATE);
        String model = preferences.getString("model", Values.YOLOCustom);
        int item = preferences.getInt("item", 0);
        setModel(context, model, item);
    }

    public static void loadLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("LANGUAGE_SETTINGS", Context.MODE_PRIVATE);
        String language = preferences.getString("language", "en");
        int item = preferences.getInt("item", 0);
        setLanguage(context ,language, item);
    }

}