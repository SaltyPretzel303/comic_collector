package mosis.comiccollector.model.user;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.comic.Comic;

public class User implements Serializable {

    public static final String USER_ID_FIELD = "userId";
    public static final String PROFILE_PIC_FIELD = "profilePicUri";
    public static final String RATING_FIELD = "rating";

    private String userId;

    private String email;
    private String name;

    private String profilePicUri;

    private float rating;

    public User() {

    }

    public User(String userId,
                String email,
                String name,
                Uri profilePicUri,
                float rating) {

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

    public String getName() {
        return name;
    }

    public String getProfilePicUri() {
        return profilePicUri;
    }

    public float getRating() {
        return rating;
    }
}
