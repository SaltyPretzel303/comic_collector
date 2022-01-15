package mosis.comiccollector.ui;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.Serializable;

import mosis.comiccollector.repository.impl.MyImageListener;

public class PreviewItemData implements Serializable {
    private String id;

    private String upperText;
    private String lowerText;

    private LiveData<Bitmap> liveBitmap;
    private String uri;

    public PreviewItemData() {

    }

    public PreviewItemData(String id, String upperText, String bottomText, LiveData<Bitmap> bitmap) {
        this.id = id;

        this.upperText = upperText;
        this.lowerText = bottomText;
        this.liveBitmap = bitmap;
    }

    // TODO this one should be removed at some point
    public PreviewItemData(String id, String upperText, String bottomText, String uri) {
        this.id = id;

        this.upperText = upperText;
        this.lowerText = bottomText;
        this.uri = uri;
    }

    public LiveData<Bitmap> getBitmap() {
        return liveBitmap;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public boolean hasUri() {
        return uri != null;
    }

    public boolean hasBitmap() {
        return liveBitmap != null;
    }

    public String getUpperText() {
        return upperText;
    }
}
