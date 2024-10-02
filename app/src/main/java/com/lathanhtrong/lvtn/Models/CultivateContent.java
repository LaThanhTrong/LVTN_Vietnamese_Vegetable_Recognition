package com.lathanhtrong.lvtn.Models;

public class CultivateContent {
    private int culcon_id;
    private int cul_id;
    private int item_id;
    private String culcon_name;
    private String culcon_nameVi;
    private String culcon_nameVi2;
    private String culcon_description;
    private String culcon_descriptionVi;
    private String culcon_image;
    private String culcon_html;

    public CultivateContent(int cul_id, int item_id, String culcon_name, String culcon_nameVi, String culcon_nameVi2, String culcon_description, String culcon_descriptionVi,String culcon_image, String culcon_html) {
        this.cul_id = cul_id;
        this.item_id = item_id;
        this.culcon_name = culcon_name;
        this.culcon_nameVi = culcon_nameVi;
        this.culcon_nameVi2 = culcon_nameVi2;
        this.culcon_description = culcon_description;
        this.culcon_descriptionVi = culcon_descriptionVi;
        this.culcon_image = culcon_image;
        this.culcon_html = culcon_html;
    }

    public CultivateContent(int culcon_id, int cul_id, int item_id, String culcon_name, String culcon_nameVi, String culcon_nameVi2, String culcon_description, String culcon_descriptionVi,String culcon_image, String culcon_html) {
        this.culcon_id = culcon_id;
        this.cul_id = cul_id;
        this.item_id = item_id;
        this.culcon_name = culcon_name;
        this.culcon_nameVi = culcon_nameVi;
        this.culcon_nameVi2 = culcon_nameVi2;
        this.culcon_description = culcon_description;
        this.culcon_descriptionVi = culcon_descriptionVi;
        this.culcon_image = culcon_image;
        this.culcon_html = culcon_html;
    }

    public int getCulcon_id() {
        return culcon_id;
    }

    public void setCulcon_id(int culcon_id) {
        this.culcon_id = culcon_id;
    }

    public int getCul_id() {
        return cul_id;
    }

    public void setCul_id(int cul_id) {
        this.cul_id = cul_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getCulcon_name() {
        return culcon_name;
    }

    public void setCulcon_name(String culcon_name) {
        this.culcon_name = culcon_name;
    }

    public String getCulcon_nameVi() {
        return culcon_nameVi;
    }

    public void setCulcon_nameVi(String culcon_nameVi) {
        this.culcon_nameVi = culcon_nameVi;
    }

    public String getCulcon_nameVi2() {
        return culcon_nameVi2;
    }

    public void setCulcon_nameVi2(String culcon_nameVi2) {
        this.culcon_nameVi2 = culcon_nameVi2;
    }

    public String getCulcon_description() {
        return culcon_description;
    }

    public void setCulcon_description(String culcon_description) {
        this.culcon_description = culcon_description;
    }

    public String getCulcon_descriptionVi() {
        return culcon_descriptionVi;
    }

    public void setCulcon_descriptionVi(String culcon_descriptionVi) {
        this.culcon_descriptionVi = culcon_descriptionVi;
    }

    public String getCulcon_image() {
        return culcon_image;
    }

    public void setCulcon_image(String culcon_image) {
        this.culcon_image = culcon_image;
    }

    public String getCulcon_html() {
        return culcon_html;
    }

    public void setCulcon_html(String culcon_html) {
        this.culcon_html = culcon_html;
    }

}
