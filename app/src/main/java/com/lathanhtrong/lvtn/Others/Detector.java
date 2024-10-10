package com.lathanhtrong.lvtn.Others;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.lathanhtrong.lvtn.Models.BoundingBox;
import com.lathanhtrong.lvtn.Values;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.gpu.CompatibilityList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Detector {
    private Context context;
    private String modelPath;
    private String labelPath;
    private DetectorListener detectorListener;
    private java.util.function.Consumer<String> message;

    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();

    private int tensorWidth = 0;
    private int tensorHeight = 0;
    private int numChannel = 0;
    private int numElements = 0;
    private static final float IOU_THRESHOLD = 0.5F;

    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private float CONFIDENCE_THRESHOLD = 0.3F;
    private int NUM_DETECTIONS = 100;

    private Set<String> checkedItems = new HashSet<>();

    private ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
            .add(new CastOp(INPUT_IMAGE_TYPE))
            .build();

    public Detector(Context context, String modelPath, String labelPath, DetectorListener detectorListener, java.util.function.Consumer<String> message) {
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.detectorListener = detectorListener;
        this.message = message;
        this.checkedItems = Utils.getCheckedItems(context);

        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);

        try {
            MappedByteBuffer model = FileUtil.loadMappedFile(context, modelPath);
            this.interpreter = new Interpreter(model, options);

            String language = Utils.getLanguage(context);

            if (language.equals("en")) {
                labels.addAll(MetaData.extractNamesFromMetadata(model));
            } else if (language.equals("vi")) {
                labels.addAll(MetaData.extractNamesFromLabelFile(context, labelPath));
            }

            if (labels.isEmpty()) {
                if (labelPath == null) {
                    message.accept("Model does not contain metadata, provide LABELS_PATH in Constants.java");
                    labels.addAll(MetaData.TEMP_CLASSES);
                } else {
                    labels.addAll(MetaData.extractNamesFromLabelFile(context, labelPath));
                }
            }

            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();

            if (inputShape != null) {
                tensorWidth = inputShape[1];
                tensorHeight = inputShape[2];

                if (inputShape[1] == 3) {
                    tensorWidth = inputShape[2];
                    tensorHeight = inputShape[3];
                }
            }

            if (outputShape != null) {
                numElements = outputShape[1];
                numChannel = outputShape[2];
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.accept("Failed to load model: " + e.getMessage());
        }
    }

    public void restart(boolean isGpu) {
        interpreter.close();

        Interpreter.Options options;
        if (isGpu) {
            CompatibilityList compatList = new CompatibilityList();
            options = new Interpreter.Options();
            if (compatList.isDelegateSupportedOnThisDevice()) {
                GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
                options.addDelegate(new GpuDelegate(delegateOptions));
            } else {
                options.setNumThreads(4);
            }
        } else {
            options = new Interpreter.Options();
            options.setNumThreads(4);
        }

        try {
            MappedByteBuffer model = FileUtil.loadMappedFile(context, modelPath);
            interpreter = new Interpreter(model, options);
        } catch (IOException e) {
            e.printStackTrace();
            message.accept("Failed to reload model: " + e.getMessage());
        }
    }

    public void close() {
        interpreter.close();
    }

    public void detect(Bitmap frame) {
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            return;
        }

        long inferenceTime = SystemClock.uptimeMillis();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);

        TensorImage tensorImage = new TensorImage(INPUT_IMAGE_TYPE);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        ByteBuffer imageBuffer = processedImage.getBuffer();

        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);
        interpreter.run(imageBuffer, output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        List<BoundingBox> limitedBestBoxes = bestBoxes.subList(0, Math.min(bestBoxes.size(), NUM_DETECTIONS));
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if (limitedBestBoxes.isEmpty()) {
            detectorListener.onEmptyDetect();
            return;
        }

        detectorListener.onDetect(limitedBestBoxes, inferenceTime);
    }

    private List<BoundingBox> bestBox(float[] array) {
        String model = Utils.getModel(context);
        if (model.equals(Values.YOLOv8n) || model.equals(Values.YOLOCustom)) {
            return YOLOv8BBox(array);
        }
        else if (model.equals(Values.YOLOv10n)) {
            return YOLOv10BBox(array);
        }
        return new ArrayList<>();
    }

    public List<BoundingBox> YOLOv8BBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        int LABEL_SIZE = numElements - 4;
        for (int c = 0; c < numChannel; c++) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 4; i <= 3 + LABEL_SIZE; i++) {
                indices.add(c + numChannel * i);
            }

            float[] cnfArray = new float[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                cnfArray[i] = array[indices.get(i)];
            }

            float cnf = Float.NEGATIVE_INFINITY;
            int cls = -1;
            for (int i = 0; i < cnfArray.length; i++) {
                if (cnfArray[i] > cnf) {
                    cnf = cnfArray[i];
                    cls = i;
                }
            }

            if (cnf <= CONFIDENCE_THRESHOLD || !checkedItems.contains(String.valueOf(cls+1))) continue;

            float cx = array[c];
            float cy = array[c + numChannel];
            float w = array[c + numChannel * 2];
            float h = array[c + numChannel * 3];
            float x1 = cx - (w / 2f);
            float y1 = cy - (h / 2f);
            float x2 = cx + (w / 2f);
            float y2 = cy + (h / 2f);
            String clsName = labels.get(cls);

            if (x1 <= 0f || x1 >= tensorWidth) continue;
            if (y1 <= 0f || y1 >= tensorHeight) continue;
            if (x2 <= 0f || x2 >= tensorWidth) continue;
            if (y2 <= 0f || y2 >= tensorHeight) continue;

            boundingBoxes.add(new BoundingBox(x1, y1, x2, y2, cnf, cls, clsName));

        }
        if (boundingBoxes.isEmpty()) {
            return new ArrayList<>();
        }
        return applyNMS(boundingBoxes);
    }

    public List<BoundingBox> YOLOv10BBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        for (int r = 0; r < numElements; r++) {
            float cnf = array[r * numChannel + 4];
            if (cnf > CONFIDENCE_THRESHOLD) {
                int cls = (int) array[r * numChannel + 5];
                if (!checkedItems.contains(String.valueOf(cls+1))) continue;
                float x1 = array[r * numChannel];
                float y1 = array[r * numChannel + 1];
                float x2 = array[r * numChannel + 2];
                float y2 = array[r * numChannel + 3];
                String clsName = labels.get(cls);
                boundingBoxes.add(new BoundingBox(x1, y1, x2, y2, cnf, cls, clsName));
            }
        }
        return boundingBoxes;
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.getX1(), box2.getX1());
        float y1 = Math.max(box1.getY1(), box2.getY1());
        float x2 = Math.min(box1.getX2(), box2.getX2());
        float y2 = Math.min(box1.getY2(), box2.getY2());

        float intersectionArea = Math.max(0f, x2 - x1) * Math.max(0f, y2 - y1);
        float box1Area = (box1.getX2() - box1.getX1()) * (box1.getY2() - box1.getY1());
        float box2Area = (box2.getX2() - box2.getX1()) * (box2.getY2() - box2.getY1());

        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        sortedBoxes.sort((b1, b2) -> Float.compare((b2.getX2() - b2.getX1()) * (b2.getY2() - b2.getY1()),
                (b1.getX2() - b1.getX1()) * (b1.getY2() - b1.getY1())));
        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while (!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.remove(0);
            selectedBoxes.add(first);

            Iterator<BoundingBox> iterator = sortedBoxes.iterator();
            while (iterator.hasNext()) {
                BoundingBox nextBox = iterator.next();
                float iou = calculateIoU(first, nextBox);
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove();
                }
            }
        }

        return selectedBoxes;
    }

    public void setConfidenceThreshold(float v) {
        this.CONFIDENCE_THRESHOLD = v;
    }

    public void setNumDetections(int v) {
        this.NUM_DETECTIONS = v;
    }

    public interface DetectorListener {
        void onEmptyDetect();
        void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime);
    }
}
