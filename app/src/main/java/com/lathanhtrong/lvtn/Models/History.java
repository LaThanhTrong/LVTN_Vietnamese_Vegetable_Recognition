package com.lathanhtrong.lvtn.Models;

public class History {
    private int history_id;
    private int item_id;
    private String history_datetime;
    private String history_image;
    private String model;
    private String confidence;

    public History(int history_id, int item_id, String history_datetime, String history_image, String model, String confidence) {
        this.history_id = history_id;
        this.item_id = item_id;
        this.history_datetime = history_datetime;
        this.history_image = history_image;
        this.model = model;
        this.confidence = confidence;
    }

    public History(int item_id, String history_datetime, String history_image, String model, String confidence) {
        this.item_id = item_id;
        this.history_datetime = history_datetime;
        this.history_image = history_image;
        this.model = model;
        this.confidence = confidence;
    }

    public int getHistory_id() {
        return history_id;
    }

    public void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getHistory_datetime() {
        return history_datetime;
    }

    public void setHistory_datetime(String history_datetime) {
        this.history_datetime = history_datetime;
    }

    public String getHistory_image() {
        return history_image;
    }

    public void setHistory_image(String history_image) {
        this.history_image = history_image;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
