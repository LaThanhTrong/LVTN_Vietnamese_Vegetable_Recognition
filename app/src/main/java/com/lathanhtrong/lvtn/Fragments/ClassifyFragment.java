package com.lathanhtrong.lvtn.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.lathanhtrong.lvtn.Adapters.PredicationAdapter;
import com.lathanhtrong.lvtn.BuildConfig;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.History;
import com.lathanhtrong.lvtn.Prediction;
import com.lathanhtrong.lvtn.Others.Classification;
import com.lathanhtrong.lvtn.Others.OrientationLiveData;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.FragmentClassifyBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassifyFragment extends Fragment implements Classification.ClassificationListener {

    private FragmentClassifyBinding binding;
    private Classification imageClassification;
    private PredicationAdapter predicationAdapter;
    private OrientationLiveData orientationLiveData;
    private ExecutorService backgroundExecutor;

    private File currentPhotoFile;
    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;

    private String currentImageName;
    private DBHandler dbHandler;

    private final ActivityResultLauncher<android.net.Uri> photoCapture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && currentPhotoFile != null) {
                            Bitmap bitmap = Utils.fileToBitmap(currentPhotoFile.getAbsolutePath());
                            if (bitmap != null) {
                                int rotation = orientationLiveData.getValue() != null ? orientationLiveData.getValue() : 90;
                                Bitmap rotated = Utils.rotatedBitmap(bitmap, rotation);
                                runClassification(rotated);
                            }
                        }
                    });

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            Bitmap rotated = Utils.getBitmapFromUri(requireContext(), uri);
                            if (rotated != null) {
                                runClassification(rotated);
                            }
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentClassifyBinding.inflate(inflater, container, false);
        predicationAdapter = new PredicationAdapter();
        binding.rvPredications.setAdapter(predicationAdapter);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHandler = new DBHandler(requireContext());

        String model = Utils.getModel(requireContext());
        backgroundExecutor = Executors.newSingleThreadExecutor();

        Context context = requireContext().getApplicationContext();
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        if (cameraManager == null) {
            throw new RuntimeException("CameraManager is not available.");
        }

        String cameraId = Utils.getCameraId(cameraManager);
        if (cameraId == null) {
            throw new RuntimeException("Camera ID is not available.");
        }

        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
            if (characteristics == null) {
                throw new RuntimeException("CameraCharacteristics is not available.");
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to access camera characteristics.", e);
        }

        backgroundExecutor.execute(() -> {
            imageClassification = new Classification(requireContext(), model, Values.LABELS_PATH, this, this::toast);
        });

        orientationLiveData = new OrientationLiveData(requireContext(), characteristics);
        orientationLiveData.observe(getViewLifecycleOwner(), orientation -> {
            if (orientation != null) {
                Log.d(ClassifyFragment.class.getSimpleName(), "Orientation changed: " + orientation);
            } else {
                Log.d(ClassifyFragment.class.getSimpleName(), "Orientation is null.");
            }
        });

        bindListeners();
    }

    private void bindListeners() {
        binding.btnCamera.setOnClickListener(v -> {
            File photoFile = Utils.createImageFile(requireContext());
            Uri photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
            );
            currentPhotoFile = photoFile;
            photoCapture.launch(photoUri);
        });

        binding.btnGallery.setOnClickListener(v -> {
            PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();
            photoPicker.launch(request);
        });
    }

    private void runClassification(Bitmap bitmap) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvResClass.setText("");
        binding.tvResProb.setText("");
        binding.tvLatency.setText("");
        predicationAdapter.clear();
        binding.ivMain.setImageBitmap(bitmap);

        backgroundExecutor.submit(() -> {
            currentImageName = saveImageToInternalStorage(bitmap);
            imageClassification.invoke(bitmap);
        });
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        String directoryName = "historyImages";
        File directory = new File(requireContext().getFilesDir(), directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String imageName = generateUniqueImageName(directory);
        File imageFile = new File(directory, imageName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the image name without extension
        return imageName.substring(0, imageName.lastIndexOf('.'));
    }

    private String generateUniqueImageName(File directory) {
        String baseName = "image";
        String extension = ".png";
        int counter = 0;
        String imageName;

        do {
            imageName = baseName + (counter == 0 ? "" : "_" + counter) + extension;
            counter++;
        } while (new File(directory, imageName).exists());

        return imageName;
    }

    private void toast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onResult(List<Prediction> data, long inferenceTime) {
        //Log.d("onResult", data.toString());
        new Handler(Looper.getMainLooper()).post(() -> {
            predicationAdapter.setData(data);

            if (data != null && !data.isEmpty()) {
                binding.tvResClass.setText(data.get(0).getName());
                binding.tvResProb.setText(String.format("%.2f%%", data.get(0).getConfidence() * 100));

                int id = data.get(0).getId() + 1;
                String datetime = Utils.getCurrentDateTime();
                String model = Utils.getModel(requireContext());
                String formatModel = Utils.formatModelName(model);
                String confidence = String.format("%.2f%%", data.get(0).getConfidence() * 100);

                // Save to database
                History history = new History(id, datetime, currentImageName, formatModel, confidence);
                dbHandler.addHistory(history);

            } else {
                binding.tvResClass.setText("");
                binding.tvResProb.setText("");
                Toast.makeText(requireContext(), "No Detection found.", Toast.LENGTH_SHORT).show();
            }

            binding.tvLatency.setText(Long.toString(inferenceTime)+"ms");
            binding.progressBar.setVisibility(View.INVISIBLE);
        });
    }
}