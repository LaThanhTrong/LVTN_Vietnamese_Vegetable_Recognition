package com.lathanhtrong.lvtn.Others;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.view.OrientationEventListener;
import androidx.lifecycle.LiveData;

public class OrientationLiveData extends LiveData<Integer> {
    private final OrientationEventListener listener;
    private final CameraCharacteristics characteristics;

    public OrientationLiveData(Context context, CameraCharacteristics characteristics) {
        this.characteristics = characteristics;
        listener = new OrientationEventListener(context.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                // Compute rotation based on the orientation value
                int rotation;
                if (orientation <= 45 || orientation > 315) {
                    rotation = 270;
                } else if (orientation <= 135) {
                    rotation = 270;
                } else if (orientation <= 225) {
                    rotation = 270;
                } else {
                    rotation = 270;
                }

                // Handle possible null characteristics
                if (characteristics != null) {
                    Integer sensorOrientationDegrees = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    if (sensorOrientationDegrees != null) {
                        int relative = (sensorOrientationDegrees + rotation + 360) % 360;
                        if (getValue() == null || relative != getValue()) {
                            postValue(relative);
                        }
                    } else {
                        // Handle case where SENSOR_ORIENTATION is null
                        postValue(rotation);
                    }
                } else {
                    // Handle case where characteristics is null
                    postValue(rotation);
                }
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        listener.enable();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        listener.disable();
    }
}
