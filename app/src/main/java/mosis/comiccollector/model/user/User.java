package mosis.comiccollector.model.user;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String userId;

    private String email;
    private String name;

    // this is actually useless
    // it points to firebaseDownloadUri which imageView can't display ...
    private String profilePicUri;

    private int rating;

    private List<String> friendIds;

    // required for serialization ...
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

        this.friendIds = new ArrayList<String>();
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

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }
}
