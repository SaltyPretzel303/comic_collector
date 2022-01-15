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

    public int rating;

    private transient MutableLiveData<List<ViewComic>> createdComics;
    private transient MutableLiveData<List<ViewComic>> collectedComics;

    // mainly used for auth
    public String errorMessage;

    public ViewUser() {

    }

    public ViewUser(String errorMessage) {
        this("-Unknown-", "Unknown", "Unknown", -1, errorMessage);
    }

    public ViewUser(String userId,
                    String email,
                    String name,
                    int rating,
                    String errorMessage) {

        this.userId = userId;
        this.email = email;
        this.name = name;
        this.rating = rating;
        this.errorMessage = errorMessage;
    }

    public void setLiveProfilePic(MutableLiveData<Bitmap> livePicUri) {
        this.liveProfilePic = livePicUri;
    }

    public static ViewUser fromProfileData(ProfileData pData) {
        ViewUser vData = new ViewUser(
                pData.userId,
                pData.email,
                "notImplemented",
                pData.rating,
                "");

        MutableLiveData<Bitmap> livePic = new MutableLiveData<>();
        livePic.postValue(pData.profilePic);
        vData.setLiveProfilePic(livePic);

        return vData;
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
