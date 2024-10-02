package com.lathanhtrong.lvtn.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationBarView;
import com.lathanhtrong.lvtn.Fragments.ClassifyFragment;
import com.lathanhtrong.lvtn.Fragments.HomeFragment;
import com.lathanhtrong.lvtn.Fragments.ItemsFragment;
import com.lathanhtrong.lvtn.Fragments.SettingsFragment;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    public MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsFragment.loadLanguage(MainActivity.this);
        SettingsFragment.loadModel(MainActivity.this);
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean loadSettings = getIntent().getBooleanExtra("LOAD_SETTINGS_FRAGMENT", false);
        if (loadSettings) {
            replaceFragment(new SettingsFragment(), "SettingsFragment");
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        } else {
            replaceFragment(new HomeFragment(), "HomeFragment");
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment = null;
                String tag = "";

                if (id == R.id.nav_home) {
                    fragment = new HomeFragment();
                    tag = "HomeFragment";
                } else if (id == R.id.nav_image) {
                    fragment = new ClassifyFragment();
                    tag = "ClassifyFragment";
                } else if (id == R.id.nav_items) {
                    fragment = new ItemsFragment();
                    tag = "ItemsFragment";
                } else if (id == R.id.nav_settings) {
                    fragment = new SettingsFragment();
                    tag = "SettingsFragment";
                }

                if (fragment != null) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    if (currentFragment == null || !tag.equals(currentFragment.getTag())) {
                        replaceFragment(fragment, tag);
                    }
                }

                return true;
            }
        });

        binding.detectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetectionActivity.class);
                startActivity(intent);
            }
        });
    }

    public void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}