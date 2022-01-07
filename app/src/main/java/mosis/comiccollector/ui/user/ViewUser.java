package mosis.comiccollector.ui.user;

import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;

public class ViewUser implements Serializable {

    public String userId;

    public String email;
    public String name;

    public MutableLiveData<String> liveLocalPicUri;

    public int rating;

    private MutableLiveData<List<ViewComic>> createdComics;
    private MutableLiveData<List<ViewComic>> collectedComics;

    // mainly used for auth
    public String errorMessage;

    public ViewUser() {

    }

    public ViewUser(String errorMessage) {
        this(null, null, null, -1, errorMessage);
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

    public void setLivePicUri(MutableLiveData<String> livePicUri) {
        this.liveLocalPicUri = livePicUri;
    }

    public static ViewUser fromProfileData(ProfileData pData) {
        ViewUser vData = new ViewUser(
                pData.userId,
                pData.email,
                "notImplemented",
                pData.rating,
                "");

        MutableLiveData<String> livePic = new MutableLiveData<>();
        livePic.postValue(pData.picUri);
        vData.setLivePicUri(livePic);

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
