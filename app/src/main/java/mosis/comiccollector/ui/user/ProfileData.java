package mosis.comiccollector.ui.user;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ProfileData implements Serializable {
    public String userId;

    public String email;
    public String name;

    public int rating;

    public Bitmap profilePic;

    public ProfileData() {

    }

    public ProfileData(String id,
                       String email,
                       String name,
                       int rating,
                       Bitmap picUri) {

        this.userId = id;
        this.email = email;
        this.name = name;
        this.rating = rating;
        this.profilePic = picUri;
    }

}
