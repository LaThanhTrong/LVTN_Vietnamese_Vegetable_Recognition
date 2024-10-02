package com.lathanhtrong.lvtn.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lathanhtrong.lvtn.Activities.DetectionActivity;
import com.lathanhtrong.lvtn.Activities.FilterDetectionActivity;
import com.lathanhtrong.lvtn.Activities.HistoryRecognitionActivity;
import com.lathanhtrong.lvtn.Activities.MainActivity;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.FragmentHomeBinding;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    private double latitude;
    private double longitude;
    private String url;
    TextView tvWeather, tvDateTime, tvTemperature, tvHumidity, tvWindSpeed;
    ImageView ivWeatherIcon;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_WEATHER_DATA = "weather_data";

    private SharedPreferences sharedPreferences;

    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject curr = jsonObject.getJSONObject("current");
                JSONArray weatherArray = curr.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                String main = weather.getString("main");
                String description = weather.getString("description");
                String icon = weather.getString("icon");
                double temp = curr.getDouble("temp");
                int tempInt = (int) temp;
                int humidity = curr.getInt("humidity");
                double windSpeed = curr.getDouble("wind_speed");
                String imageUrl = "https://openweathermap.org/img/wn/"+icon+"@2x.png";

                tvWeather.setText(capitalizeWords(description));
                tvTemperature.setText(tempInt + "°C");
                tvHumidity.setText(humidity + "%");
                tvWindSpeed.setText(windSpeed + "m/s");
                tvDateTime.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
                try {
                    Picasso.get().load(imageUrl).into(ivWeatherIcon, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            //Toast.makeText(requireContext(), "Weather updated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(requireContext(), "Error loading weather", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvWeather = binding.weathertext;
        tvDateTime = binding.datetime;
        tvTemperature = binding.tvTemp;
        tvWindSpeed = binding.tvWind;
        tvHumidity = binding.tvHumid;
        ivWeatherIcon = binding.weather;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (isDataExpired()) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.weather.setVisibility(View.INVISIBLE);
            //Toast.makeText(requireContext(), "Attempt to load weather data", Toast.LENGTH_SHORT).show();
            loadWeatherData();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.weather.setVisibility(View.INVISIBLE);
            String weatherData = sharedPreferences.getString(KEY_WEATHER_DATA, "");
            if (!weatherData.isEmpty()) {
                updateUI(weatherData);
            } else {
                //Toast.makeText(requireContext(), "Attempt to load weather data", Toast.LENGTH_SHORT).show();
                loadWeatherData();
            }
        }

        binding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.weather.setVisibility(View.INVISIBLE);
                //Toast.makeText(requireContext(), "Attempt to load weather data", Toast.LENGTH_SHORT).show();
                loadWeatherData();
            }
        });

        binding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.binding.bottomNavigationView.setSelectedItemId(R.id.nav_items);

                FragmentManager fragmentManager = ((MainActivity) requireActivity()).getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_layout, new ItemsFragment()).commit();

                binding.search.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
                        if (currentFragment instanceof ItemsFragment) {
                            AutoCompleteTextView autoCompleteTextView = currentFragment.getView().findViewById(R.id.search);
                            if (autoCompleteTextView != null) {
                                autoCompleteTextView.requestFocus();
                                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    }
                }, 500);
            }
        });

        binding.btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DetectionActivity.class);
                startActivity(intent);
            }
        });

        binding.btnClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.binding.bottomNavigationView.setSelectedItemId(R.id.nav_image);

                FragmentManager fragmentManager = ((MainActivity) requireActivity()).getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_layout, new ClassifyFragment()).commit();
            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), FilterDetectionActivity.class);
                startActivity(intent);
            }
        });

        binding.btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), HistoryRecognitionActivity.class);
                startActivity(intent);
            }
        });

        binding.parentScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.childScroll.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        binding.childScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    private void loadWeatherData() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            assert addresses != null;
                            latitude = addresses.get(0).getLatitude();
                            longitude = addresses.get(0).getLongitude();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (latitude != 0 && longitude != 0) {
                            try {
                                String language = Utils.getLanguage(requireContext());
                                if (language.equals("en")) {
                                    url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + latitude + "&lon=" + longitude + "&lang=en&units=metric&exclude=hourly,daily,minutely&appid=9df6c7b1561bddde1038b8318ba42972";
                                } else {
                                    url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + latitude + "&lon=" + longitude + "&lang=vi&units=metric&exclude=hourly,daily,minutely&appid=9df6c7b1561bddde1038b8318ba42972";
                                }
                                getWeather task = new getWeather();
                                String weatherData = task.execute(url).get();
                                if (weatherData != null) {
                                    // Save the weather data and update time
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(KEY_WEATHER_DATA, weatherData);
                                    editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
                                    editor.apply();
                                    updateUI(weatherData);
                                } else {
                                    Toast.makeText(requireContext(), "Error loading weather information", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            binding.progressBar.setVisibility(View.INVISIBLE);
                            binding.weather.setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.INVISIBLE);
                            binding.weather.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.weather.setVisibility(View.VISIBLE);
                    }
                }
            }).addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.weather.setVisibility(View.VISIBLE);
            });
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadWeatherData();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isDataExpired() {
        long lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATE, 0);
        long currentTime = System.currentTimeMillis();
        return currentTime - lastUpdate > DateUtils.DAY_IN_MILLIS;
    }

    private void updateUI(String weatherData) {
        try {
            JSONObject jsonObject = new JSONObject(weatherData);
            JSONObject curr = jsonObject.getJSONObject("current");
            JSONArray weatherArray = curr.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String main = weather.getString("main");
            String description = weather.getString("description");
            String icon = weather.getString("icon");
            double temp = curr.getDouble("temp");
            int tempInt = (int) temp;
            int humidity = curr.getInt("humidity");
            double windSpeed = curr.getDouble("wind_speed");
            String imageUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";

            tvWeather.setText(capitalizeWords(description));
            tvTemperature.setText(tempInt + "°C");
            tvHumidity.setText(humidity + "%");
            tvWindSpeed.setText(windSpeed + "m/s");
            tvDateTime.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
            try {
                Picasso.get().load(imageUrl).into(ivWeatherIcon, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.weather.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.weather.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.weather.setVisibility(View.VISIBLE);
    }

    private String capitalizeWords(String description) {
        String[] words = description.split("\\s+");
        StringBuilder capitalizedDescription = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedDescription.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalizedDescription.toString().trim();
    }
}