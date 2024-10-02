package com.lathanhtrong.lvtn.Models;

public class Item {
    private int item_id;
    private String item_name;
    private String item_nameVi;
    private String item_nameVi2;
    private String item_sname;
    private String item_description;
    private String item_descriptionVi;
    private String item_image;
    private String item_clipart;

    public Item(int item_id, String item_name, String item_nameVi, String item_nameVi2, String item_sname, String item_description, String item_descriptionVi,String item_image, String item_clipart) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_nameVi = item_nameVi;
        this.item_nameVi2 = item_nameVi2;
        this.item_sname = item_sname;
        this.item_description = item_description;
        this.item_descriptionVi = item_descriptionVi;
        this.item_image = item_image;
        this.item_clipart = item_clipart;
    }

    public Item(int item_id, String item_name, String item_nameVi2, String item_image) {
        this.item_name = item_name;
        this.item_image = item_image;
        this.item_id = item_id;
        this.item_nameVi2 = item_nameVi2;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_nameVi() {
        return item_nameVi;
    }

    public void setItem_nameVi(String item_nameVi) {
        this.item_nameVi = item_nameVi;
    }

    public String getItem_nameVi2() {
        return item_nameVi2;
    }

    public void setItem_nameVi2(String item_nameVi2) {
        this.item_nameVi2 = item_nameVi2;
    }

    public String getItem_sname() {
        return item_sname;
    }

    public void setItem_sname(String item_sname) {
        this.item_sname = item_sname;
    }

    public String getItem_description() {
        return item_description;
    }

    public void setItem_description(String item_description) {
        this.item_description = item_description;
    }

    public String getItem_descriptionVi() {
        return item_descriptionVi;
    }

    public void setItem_descriptionVi(String item_descriptionVi) {
        this.item_descriptionVi = item_descriptionVi;
    }

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }

    public String getItem_clipart() {
        return item_clipart;
    }

    public void setItem_clipart(String item_clipart) {
        this.item_clipart = item_clipart;
    }

    @Override
    public String toString() {
        return "Item{" +
                "item_id=" + item_id +
                ", item_name='" + item_name + '\'' +
                ", item_nameVi='" + item_nameVi + '\'' +
                ", item_sname='" + item_sname + '\'' +
                ", item_description='" + item_description + '\'' +
                ", item_image='" + item_image + '\'' +
                ", item_clipart='" + item_clipart + '\'' +
                '}';
    }
}
