package mosis.comiccollector.ui.user;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;

public class ViewUser implements Serializable {

    public String userId;

    public String email;
    public String name;

    public transient MutableLiveData<Bitmap> liveProfilePic;

    private transient MutableLiveData<List<ViewComic>> createdComics;
    private transient MutableLiveData<List<ViewComic>> collectedComics;

    public float rating;

    // mainly used for auth
    public String errorMessage;

    public ViewUser() {

    }

    public ViewUser(String errorMessage) {
        this("-Unknown-", "Unknown", "Unknown", 0, errorMessage);
    }

    public ViewUser(String userId,
                    String email,
                    String name,
                    float rating,
                    String errorMessage) {

        this.userId = userId;
        this.email = email;
        this.name = name;
        this.errorMessage = errorMessage;
        this.rating = rating;
    }

    public void setLiveProfilePic(MutableLiveData<Bitmap> livePicUri) {
        this.liveProfilePic = livePicUri;
    }


    public MutableLiveData<List<ViewComic>> getCreatedComics() {
        return createdComics;
    }

    public void setCreatedComics(MutableLiveData<List<ViewComic>> createdComics) {
        this.createdComics = createdComics;
    }

    public MutableLiveData<List<ViewComic>> getCollectedComics() {
        return collectedComics;
    }

    public void setCollectedComics(MutableLiveData<List<ViewComic>> collectedComics) {
        this.collectedComics = collectedComics;
    }

}
