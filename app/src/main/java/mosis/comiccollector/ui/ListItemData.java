package mosis.comiccollector.ui;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;

public class ListItemData {

    public String id;

    public LiveData<Bitmap> liveImage;
    public String text;
    public float rating;

    public ListItemData(String id,
                        LiveData<Bitmap> liveImage,
                        String text,
                        float rating) {

        this.id = id;

        this.liveImage = liveImage;
        this.text = text;
        this.rating = rating;
    }


}
