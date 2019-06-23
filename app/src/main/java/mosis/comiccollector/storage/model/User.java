package mosis.comiccollector.storage.model;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mosis.comiccollector.comic.Comic;

public class User {

    static public final String data_id_prefs_path = "userId";
    static public final String username_prefs_path = "user_username";
    static public final String password_prefs_path = "user_password";
    static public final String prof_pic_prefs_path = "user_prof_pic";

    private String userId;

    private String username;
    private String password;

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

    public User(String dataId, String username, String password) {

        this.userId = dataId;
        this.username = username;
        this.password = password;

        this.myComics = new ArrayList<String>();
        this.collectedComics = new ArrayList<String>();

        Random gen = new Random();
        this.rating = gen.nextInt(100);

    }

    // methods

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getMyCommics() {
        return myComics;
    }

    public List<String> getCollectedCommics() {
        return collectedComics;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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
        new_user.setUsername(map_sample.get("username"));
        new_user.setPassword(map_sample.get("password"));

        return new_user;

    }

    @Override
    public String toString() {
        return "\nData_id: " + this.userId + "\nUsername: " + this.username + "\nPassword: " + this.password;
    }

    @Exclude
    public String getLocalProfilePicName() {

        // every picture is 'translated' to jpeg format after load from gallery
        return this.getProfilePicName() + ".jpeg";

    }

    @Exclude
    public String getProfilePicName() {
        return this.username + "-profile_pic";
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
