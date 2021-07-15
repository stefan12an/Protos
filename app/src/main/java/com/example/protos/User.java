package com.example.protos;

import java.util.Date;

public class User {
    String username;
    String email;
    String gender;
    String date_of_birth;
    String creation_date;
    String profile_pic;

    public User() {
    }

    public User(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public User(String username, String email, String gender, String date_of_birth, String creation_date) {
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.date_of_birth = date_of_birth;
        this.creation_date = creation_date;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }
}
