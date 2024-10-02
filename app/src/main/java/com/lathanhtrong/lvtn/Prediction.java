package com.lathanhtrong.lvtn;

public class Prediction {
    private int id;
    private String name;
    private float confidence;

    public Prediction(int id, String name, float confidence) {
        this.id = id;
        this.name = name;
        this.confidence = confidence;
    }

    public Prediction() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
