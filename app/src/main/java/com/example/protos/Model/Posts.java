package com.example.protos.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import static android.os.UserHandle.readFromParcel;

public class Posts implements Parcelable {
    String user_id;
    String post_id;
    String username;
    String post_pic;
    String creation_date;
    String caption;

    public Posts() {
    }

    public Posts(Parcel in) {
        readFromParcel(in);
    }

    public Posts(String user_id, String post_id, String username, String post_pic, String creation_date, String caption) {
        this.user_id = user_id;
        this.username = username;
        this.post_pic = post_pic;
        this.creation_date = creation_date;
        this.caption = caption;
        this.post_id = post_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        // We just need to write each field into the
        // parcel. When we read from parcel, they
        // will come back in the same order
        dest.writeString(user_id);
        dest.writeString(post_id);
        dest.writeString(username);
        dest.writeString(post_pic);
        dest.writeString(creation_date);
        dest.writeString(caption);
    }

    private void readFromParcel(Parcel in) {

        // We just need to read back each
        // field in the order that it was
        // written to the parcel
        user_id = in.readString();
        post_id = in.readString();
        username = in.readString();
        post_pic = in.readString();
        creation_date = in.readString();
        caption = in.readString();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Posts createFromParcel(Parcel in) {
                    return new Posts(in);
                }

                public Posts[] newArray(int size) {
                    return new Posts[size];
                }
            };
}
