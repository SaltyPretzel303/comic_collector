package mosis.comiccollector.model.user;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.comic.Comic;

public class User implements Serializable {

    public static final String USER_ID_FIELD = "userId";
    public static final String PROFILE_PIC_FIELD = "profilePicUri";

    private String userId;

    private String email;
    private String name;

    private String profilePicUri;

    private int rating;

    // required for serialization
    public User() {

    }

    public User(String userId, String email, String name,
                Uri profilePicUri,
                int rating) {

        this.userId = userId;
        this.email = email;
        this.name = name;
        if (profilePicUri != null) {
            this.profilePicUri = profilePicUri.toString();
        } else {
            this.profilePicUri = null;
        }

        this.rating = rating;

    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public String getProfilePicUri() {
        return profilePicUri;
    }

    public void setProfilePicUri(String newUri) {
        this.profilePicUri = newUri;
    }

}
