package mosis.comiccollector.ui.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

public class IndexedBitmapPage {

    public int index;
    public MutableLiveData<Bitmap> livePage;

    public boolean isNew;
    public String localUri;

    public IndexedBitmapPage(int index, MutableLiveData<Bitmap> livePage) {
        this.index = index;
        this.livePage = livePage;

        this.isNew = false;
    }

    public IndexedBitmapPage(int index, String localUri) {
        this.index = index;
        this.localUri = localUri;

        this.isNew = true;
    }

}
