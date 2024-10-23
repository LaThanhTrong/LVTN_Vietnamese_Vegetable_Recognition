package com.lathanhtrong.lvtn.Fragments;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Preview;

import com.google.android.material.slider.Slider;
import com.google.common.util.concurrent.ListenableFuture;
import com.lathanhtrong.lvtn.Activities.MainActivity;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.Values;
import com.lathanhtrong.lvtn.databinding.FragmentCameraBinding;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Camera;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.lathanhtrong.lvtn.Others.Detector;
import java.util.List;
import java.util.concurrent.ExecutorService;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import java.util.concurrent.Executors;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.lathanhtrong.lvtn.Models.BoundingBox;
import com.lathanhtrong.lvtn.Others.Animation;

public class CameraFragment extends Fragment implements Detector.DetectorListener {
    private FragmentCameraBinding binding;
    private boolean isFrontCamera = false;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private Detector detector;
    private ExecutorService cameraExecutor;
    BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String confidence = Utils.getConfidence(requireContext());
        String numClass = Utils.getNumClass(requireContext());
        String model = Utils.getModel(requireContext());
        binding.confSlider.setValue(Float.parseFloat(confidence));
        binding.confScore.setText(confidence + "%");
        binding.detectSlider.setValue(Float.parseFloat(numClass));
        binding.detectNum.setText(numClass);
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraExecutor.execute(() -> {
            detector = new Detector(requireContext(), model, Values.LABELS_PATH, this, this::toast);
            detector.setConfidenceThreshold(Float.parseFloat(confidence) / 100);
            detector.setNumDetections(Integer.parseInt(numClass));
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        setBottomSheet();
        bindListeners();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        binding.confSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.confScore.setText((int) value + "%");
                cameraExecutor.submit(() -> {
                    if (detector != null) {
                        detector.setConfidenceThreshold(value / 100);
                    }
                });
            }
        });

        binding.detectSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.detectNum.setText(String.valueOf((int) value));
                cameraExecutor.submit(() -> {
                    if (detector != null) {
                        detector.setNumDetections((int) value);
                    }
                });
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);
    }

    private void bindListeners() {
        binding.cbGPU.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cameraExecutor.submit(() -> {
                if (detector != null) {
                    detector.restart(isChecked);
                }
            });
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions like InterruptedException, ExecutionException, etc.
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            throw new IllegalStateException("Camera initialization failed.");
        }

        int rotation = binding.viewFinder.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(rotation)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
            try {
                Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
                bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

                Matrix matrix = new Matrix();
                matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

                if (isFrontCamera) {
                    matrix.postScale(-1f, 1f, imageProxy.getWidth(), imageProxy.getHeight());
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);

                if (detector != null) {
                    detector.detect(rotatedBitmap);
                }
            } finally {
                imageProxy.close();
            }
        });

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                if (permissions.get(Manifest.permission.CAMERA) != null && Boolean.TRUE.equals(permissions.get(Manifest.permission.CAMERA))) {
                    startCamera();
                }
            }
    );

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
        cameraExecutor.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onEmptyDetect() {
        requireActivity().runOnUiThread(() -> binding.overlay.clear());
    }

    @Override
    public void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime) {
        requireActivity().runOnUiThread(() -> {
            binding.inferenceTime.setText(inferenceTime + "ms");
            binding.overlay.setResults(boundingBoxes);
            binding.overlay.invalidate();
        });
    }

    private void toast(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        );
    }

    private void setBottomSheet() {
        View bottomSheet = binding.llSheet;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        WindowManager windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            int screenHeight = windowManager.getDefaultDisplay().getHeight();
            int peekHeight = (int) (screenHeight * 0.08); // 30% of the screen height
            bottomSheetBehavior.setPeekHeight(peekHeight);
        }

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Animation.animateArrow(binding.ivArrow, 180f, 0f);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Animation.animateArrow(binding.ivArrow, 0f, 180f);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Handle slide events if needed
            }
        });

        binding.llSheet.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private static final String TAG = "Camera";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
}
