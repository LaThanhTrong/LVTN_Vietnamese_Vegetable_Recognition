package com.lathanhtrong.lvtn.Others;

import android.content.Context;
import android.graphics.Bitmap;
import com.lathanhtrong.lvtn.Prediction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lathanhtrong.lvtn.Models.BoundingBox;

public class Classification {

    private final Context context;
    private final String modelPath;
    private final String labelPath;
    private final ClassificationListener classificationListener;
    private final MessageCallback messageCallback;

    private Detector detector;

    public Classification(Context context, String modelPath, String labelPath,
                          ClassificationListener classificationListener, MessageCallback messageCallback) {
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.classificationListener = classificationListener;
        this.messageCallback = messageCallback;

        this.detector = new Detector(context, modelPath, labelPath, new Detector.DetectorListener() {
            @Override
            public void onEmptyDetect() {
                classificationListener.onResult(new ArrayList<>(), 0);
            }

            @Override
            public void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime) {
                List<Prediction> predictions = new ArrayList<>();
                for (BoundingBox box : boundingBoxes) {
                    predictions.add(new Prediction(box.getCls(), box.getClsName(), box.getCnf()));
                }
                filterAndSortPredictions(predictions);
                classificationListener.onResult(predictions, inferenceTime);
            }
        }, messageCallback::onMessage);
    }

    public void close() {
        if (detector != null) {
            detector.close();
        }
    }

    public void invoke(Bitmap frame) {
        String confidence = Utils.getConfidence(context);
        String numClass = Utils.getNumClass(context);
        detector.setConfidenceThreshold(Float.parseFloat(confidence) / 100);
        detector.setNumDetections(Integer.parseInt(numClass));
        if (detector != null) {
            detector.detect(frame);
        }
    }

    private void filterAndSortPredictions(List<Prediction> predictions) {
        Map<Integer, Prediction> predictionMap = new HashMap<>();

        for (Prediction prediction : predictions) {
            int id = prediction.getId();
            if (predictionMap.containsKey(id)) {
                Prediction existingPrediction = predictionMap.get(id);
                if (prediction.getConfidence() > existingPrediction.getConfidence()) {
                    predictionMap.put(id, prediction);
                }
            } else {
                predictionMap.put(id, prediction);
            }
        }
        predictions.clear();
        predictions.addAll(predictionMap.values());
        predictions.sort((p1, p2) -> Float.compare(p2.getConfidence(), p1.getConfidence()));
    }

    public interface ClassificationListener {
        void onResult(List<Prediction> data, long inferenceTime);
    }

    public interface MessageCallback {
        void onMessage(String message);
    }

}
