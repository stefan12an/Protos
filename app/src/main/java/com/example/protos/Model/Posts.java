package com.example.protos.Model;

import java.util.Date;

public class Posts {
    String user_id;
    String username;
    String post_pic;
    String creation_date;
    String caption;

    public Posts() {
    }

    public Posts(String user_id, String username, String post_pic, String creation_date, String caption) {
        this.user_id = user_id;
        this.username = username;
        this.post_pic = post_pic;
        this.creation_date = creation_date;
        this.caption = caption;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPost_pic() {
        return post_pic;
    }

    public void setPost_pic(String post_pic) {
        this.post_pic = post_pic;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
