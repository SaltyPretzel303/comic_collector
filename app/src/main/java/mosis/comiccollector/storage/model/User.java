package mosis.comiccollector.storage.model;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User {

    static public String data_id_prefs_path = "data_id";
    static public String username_prefs_path = "user_username";
    static public String password_prefs_path = "user_password";
    static public String prof_pic_prefs_path = "user_prof_pic";

    private String data_id;

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

    public User(String data_id, String username, String password) {

        this.data_id = data_id;
        this.username = username;
        this.password = password;

        this.prof_picture_ref = "undefined";
        this.my_commics = new ArrayList<String>();
        this.collected_commics = new ArrayList<String>();

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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProf_pic(Bitmap prof_pic) {
        this.prof_pic = prof_pic;
    }

    public void setMy_commics(List<String> my_commics) {
        this.my_commics = my_commics;
    }

    public void setCollected_commics(List<String> collected_commics) {
        this.collected_commics = collected_commics;
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }

    static public User parseMap(Map<String, String> map_sample) {

        User new_user = new User();

        new_user.setData_id(map_sample.get("data_id"));
        new_user.setUsername(map_sample.get("username"));
        new_user.setPassword(map_sample.get("password"));
        new_user.setProf_picture_ref(map_sample.get("prof_picture_ref"));

        return new_user;

    }

    @Override
    public String toString() {
        return "\nData_id: " + this.data_id + "\nUsername: " + this.username + "\nPassword: " + this.password;
    }
}
