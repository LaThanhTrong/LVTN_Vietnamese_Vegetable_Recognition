package com.lathanhtrong.lvtn.Models;

public class Cultivate {
    private int cul_id;
    private String cul_name;

    public Cultivate(int cul_id, String cul_name) {
        this.cul_id = cul_id;
        this.cul_name = cul_name;
    }

    public int getCul_id() {
        return cul_id;
    }

    public void setCul_id(int cul_id) {
        this.cul_id = cul_id;
    }

    public String getCul_name() {
        return cul_name;
    }

    public void setCul_name(String cul_name) {
        this.cul_name = cul_name;
    }
    
}
