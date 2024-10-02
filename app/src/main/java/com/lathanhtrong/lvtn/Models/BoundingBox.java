package com.lathanhtrong.lvtn.Models;

public class BoundingBox {
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float cnf;
    private int cls;
    private String clsName;

    public BoundingBox(float x1, float y1, float x2, float y2, float cnf, int cls, String clsName) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cnf = cnf;
        this.cls = cls;
        this.clsName = clsName;
    }

    public BoundingBox() {
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public float getCnf() {
        return cnf;
    }

    public void setCnf(float cnf) {
        this.cnf = cnf;
    }

    public int getCls() {
        return cls;
    }

    public void setCls(int cls) {
        this.cls = cls;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "x1=" + x1 +
                "y1=" + y1 +
                "x2=" + x2 +
                "y2=" + y2 +
                "cnf=" + cnf +
                "cls=" + cls +
                "clsName='" + clsName + '\'' +
                '}';
    }
}
