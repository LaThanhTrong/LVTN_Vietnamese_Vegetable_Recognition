package com.lathanhtrong.lvtn.Models;

import androidx.annotation.NonNull;

public class Post {
    private int post_id;
    private String user_id;
    private int item_id;
    private String post_date;
    private String post_desc;
    private String post_image;

    public Post() {

    }

    public Post (Post post) {
        this.post_id = post.getPost_id();
        this.user_id = post.getUser_id();
        this.item_id = post.getItem_id();
        this.post_date = post.getPost_date();
        this.post_desc = post.getPost_desc();
        this.post_image = post.getPost_image();
    }

    public Post(int post_id, String user_id, int item_id, String post_date, String post_desc, String post_image) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.item_id = item_id;
        this.post_date = post_date;
        this.post_desc = post_desc;
        this.post_image = post_image;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    @Override
    public String toString() {
        return "Post{" +
                "post_id=" + post_id +
                ", user_id='" + user_id + '\'' +
                ", item_id=" + item_id +
                ", post_date='" + post_date + '\'' +
                ", post_desc='" + post_desc + '\'' +
                ", post_image='" + post_image + '\'' +
                '}';
    }
}
