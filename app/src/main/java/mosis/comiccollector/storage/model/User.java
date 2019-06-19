package mosis.comiccollector.storage.model;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    static public String data_id_prefs_path = "userId";
    static public String username_prefs_path = "user_username";
    static public String password_prefs_path = "user_password";
    static public String prof_pic_prefs_path = "user_prof_pic";

    private String userId;

    private String username;
    private String password;

    // TODO can be formed using usernamem maybe remove this field
    private String profPictureRef;
    @Exclude // profile pic cache
    private Bitmap profPicBitmap;

    private List<String> myCommics;
    private List<String> collectedCommics;


    // constructors

    public User() {

    }

    public User(String dataId, String username, String password) {

        this.userId = dataId;
        this.username = username;
        this.password = password;

        this.profPictureRef = "undefined";
        this.myCommics = new ArrayList<String>();
        this.collectedCommics = new ArrayList<String>();

    }

    // methods

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getMyCommics() {
        return myCommics;
    }

    public List<String> getCollectedCommics() {
        return collectedCommics;
    }

    public String getProfPictureRef() {
        return profPictureRef;
    }

    public void setProfPictureRef(String profPictureRef) {
        this.profPictureRef = profPictureRef;
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
        this.myCommics = myCommics;
    }

    public void setCollectedCommics(List<String> collectedCommics) {
        this.collectedCommics = collectedCommics;
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
        new_user.setProfPictureRef(map_sample.get("profPictureRef"));

        return new_user;

    }

    @Override
    public String toString() {
        return "\nData_id: " + this.userId + "\nUsername: " + this.username + "\nPassword: " + this.password;
    }

    public String getLocalProfilePicName() {

        // every picture is 'translated' to png format after load from gallery
        return this.getProfilePicName() + ".png";

    }

    public String getProfilePicName() {
        return this.username + "-profile_pic";
    }


    public Bitmap getProfPicBitmap() {
        return profPicBitmap;
    }

}
