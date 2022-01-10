package mosis.comiccollector.ui;

import android.graphics.Bitmap;

public class ImageWithId {
    private String id;

    private Bitmap bitmap;
    private String uri;

    public ImageWithId() {

    }

    public ImageWithId(String id, Bitmap bitmap) {
        this.id = id;
        this.bitmap = bitmap;
    }

    public ImageWithId(String id, String uri) {
        this.id = id;
        this.uri = uri;
    }

    public Bitmap getBitmap() {
        return bitmap;
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
        return bitmap != null;
    }

}
