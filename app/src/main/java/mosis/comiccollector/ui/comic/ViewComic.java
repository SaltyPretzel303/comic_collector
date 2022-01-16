package mosis.comiccollector.ui.comic;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.util.List;

import kotlin.jvm.Transient;
import mosis.comiccollector.model.Location;

public class ViewComic implements Serializable {

    public String modelId;

    public String title;
    public String description;
    public String authorId;

    public transient MutableLiveData<Bitmap> liveTitlePage;

    public Location location;

    public int pagesCount;

    public int rating;

    public ViewComic() {

    }


    public ViewComic(String modelId,
                     String title,
                     String description,
                     String authorId,
                     Location location,
                     int rating,
                     int pagesCount) {

        this.modelId = modelId;

        this.title = title;
        this.description = description;
        this.authorId = authorId;

        this.location = location;
        this.pagesCount = pagesCount;

        this.rating = rating;
    }

}
