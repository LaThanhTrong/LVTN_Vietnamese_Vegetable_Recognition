package com.lathanhtrong.lvtn.Others;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.util.Log;
import androidx.exifinterface.media.ExifInterface;

import com.lathanhtrong.lvtn.Values;

import java.io.File;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static Bitmap rotatedBitmap(Bitmap bitmap, int rotation) {
        int exifOrientation = computeExifOrientation(rotation);
        Matrix matrix = decodeExifOrientation(exifOrientation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap fileToBitmap(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    public static File createImageFile(Context context) {
        File uupDir = new File(context.getFilesDir(), "lathanhtrong.com");
        if (!uupDir.exists()) {
            uupDir.mkdir();
        }
        try {
            return File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", uupDir);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e("BitmapError", "Failed to load bitmap from URI", e);
            return null;
        }
    }

    public static String getCameraId(CameraManager cameraManager) {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (Exception e) {
            Log.e("CameraError", "Failed to get camera ID", e);
        }
        return "";
    }

    private static int computeExifOrientation(int rotationDegrees) {
        switch (rotationDegrees) {
            case 0:
                return ExifInterface.ORIENTATION_NORMAL;
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    private static Matrix decodeExifOrientation(int exifOrientation) {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90F);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180F);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270F);
                break;
            default:
                Log.e("Camera", "Invalid orientation: " + exifOrientation);
                break;
        }
        return matrix;
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("LANGUAGE_SETTINGS", context.MODE_PRIVATE);
        return preferences.getString("language", "en");
    }

    public static String getModel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MODEL_SETTINGS", context.MODE_PRIVATE);
        return preferences.getString("model", Values.YOLOCustom);
    }

    public static String formatModelName(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            return modelName;
        }
        String nameWithoutExtension = modelName.substring(0, modelName.lastIndexOf('.'));
        return nameWithoutExtension.substring(0, 1).toUpperCase() + nameWithoutExtension.substring(1);
    }

    public static String getConfidence(Context context) {
        SharedPreferences conf_preferences = context.getSharedPreferences("CONFIDENCE_SETTINGS", Context.MODE_PRIVATE);
        return conf_preferences.getString("confidence", Values.Confidence);
    }

    public static String getNumClass(Context context) {
        SharedPreferences numClass_preferences = context.getSharedPreferences("NUMCLASS_SETTINGS", Context.MODE_PRIVATE);
        return numClass_preferences.getString("numClass", Values.NumClass);
    }

    public static Set<String> getCheckedItems(Context context) {
        String CHECKED_ITEMS_KEY = "checked_items";
        Set<String> checkedItems = new HashSet<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(CHECKED_ITEMS_KEY, Context.MODE_PRIVATE);
        checkedItems = new HashSet<String>(sharedPreferences.getStringSet(CHECKED_ITEMS_KEY, new HashSet<String>()));
        return checkedItems;
    }

    public static String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
