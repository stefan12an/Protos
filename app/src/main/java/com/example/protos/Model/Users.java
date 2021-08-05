package com.example.protos.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {
    String id;
    String username;
    String email;
    String gender;
    String date_of_birth;
    String creation_date;
    String profile_pic;

    public Users() {
    }

    public Users(Parcel in) {
        readFromParcel(in);
    }


    public Users(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public Users(String username, String email, String gender, String date_of_birth, String creation_date, String id) {
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.date_of_birth = date_of_birth;
        this.creation_date = creation_date;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        // We just need to write each field into the
        // parcel. When we read from parcel, they
        // will come back in the same order
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(gender);
        dest.writeString(date_of_birth);
        dest.writeString(creation_date);
        dest.writeString(profile_pic);
        dest.writeString(id);

    }

    private void readFromParcel(Parcel in) {

        // We just need to read back each
        // field in the order that it was
        // written to the parcel
        username = in.readString();
        email = in.readString();
        gender = in.readString();
        date_of_birth = in.readString();
        creation_date = in.readString();
        profile_pic = in.readString();
        id = in.readString();

    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Users createFromParcel(Parcel in) {
                    return new Users(in);
                }

                public Users[] newArray(int size) {
                    return new Users[size];
                }
            };
}
