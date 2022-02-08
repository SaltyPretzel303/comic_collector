package mosis.comiccollector.ui.comic;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;

import mosis.comiccollector.model.Location;
import mosis.comiccollector.ui.user.ViewUser;

public class ViewComic implements Serializable {

    public String comicId;

    public String title;
    public String description;

    public String authorId;
    public transient MutableLiveData<ViewUser> liveAuthor;
    public transient MutableLiveData<Bitmap> liveTitlePage;

    public Location location;

    public int pagesCount;

    public float rating;

    public ViewComic() {

    }

    public ViewComic(String modelId,
                     String authorId,
                     String title,
                     String description,
                     Location location,
                     int pagesCount,
                     float rating) {

        this.comicId = modelId;
        this.authorId = authorId;

        this.title = title;
        this.description = description;

        this.location = location;
        this.pagesCount = pagesCount;

        this.rating = rating;
    }

}
