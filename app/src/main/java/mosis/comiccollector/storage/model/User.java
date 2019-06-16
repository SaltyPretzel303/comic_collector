package mosis.comiccollector.storage.model;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.List;

public class User {

    private String username;
    private String password;

    private String prof_picture_ref;
    @Exclude
    private Bitmap prof_pic;

    private List<String> my_commics;
    private List<String> collected_commics;


    // constructors

    public User() {

    }

    public User(String username, String password) {

        this.username = username;
        this.password = password;

    }

    // methods

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getMy_commics() {
        return my_commics;
    }

    public List<String> getCollected_commics() {
        return collected_commics;
    }

    public String getProf_picture_ref() {
        return prof_picture_ref;
    }

    public void setProf_picture_ref(String prof_picture_ref) {
        this.prof_picture_ref = prof_picture_ref;
    }
}
