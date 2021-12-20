package mosis.comiccollector.model.user;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mosis.comiccollector.ui.comic.Comic;

public class User implements Serializable {

    private String userId;
    private String email;
    private String name;

    @Exclude // profile pic cache
    private Bitmap profPicBitmap;

    private int rating;

    // TODO save them to the local storage
    private List<String> myComics;
    private List<String> collectedComics;

    @Exclude
    private List<Comic> myComicsCache;
    @Exclude
    private List<Comic> collectedComicsCache;

    // constructors

    public User() {

    }

    public User(String dataId, String email, String name) {

        this.userId = dataId;
        this.email = email;
        this.name = name;

        this.myComics = new ArrayList<String>();
        this.collectedComics = new ArrayList<String>();

        Random gen = new Random();
        this.rating = gen.nextInt(100);

    }

    // methods

    public String getEmail() {
        return email;
    }

    public List<String> getMyCommics() {
        return myComics;
    }

    public List<String> getCollectedCommics() {
        return collectedComics;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfPicBitmap(Bitmap profPicBitmap) {
        this.profPicBitmap = profPicBitmap;
    }

    public void setMyCommics(List<String> myCommics) {
        this.myComics = myCommics;
    }

    public void setCollectedCommics(List<String> collectedCommics) {
        this.collectedComics = collectedCommics;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    static public User parseMap(Map<String, String> map_sample) {

        User new_user = new User();

        new_user.setUserId(map_sample.get("userId"));
        new_user.setEmail(map_sample.get("username"));

        return new_user;

    }

    @Exclude
    public String getLocalProfilePicName() {

        // every picture is 'translated' to jpeg format after load from gallery
        return this.getProfilePicName() + ".jpeg";

    }

    @Exclude
    public String getProfilePicName() {
        return this.email + "-profile_pic";
    }

    @Exclude
    public boolean hasProfilePic() {
        return this.profPicBitmap != null;
    }

    public Bitmap getProfPicBitmap() {
        return profPicBitmap;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
