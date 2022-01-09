package mosis.comiccollector.repository.impl;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import mosis.comiccollector.repository.PeopleRepository;

public class MyImageListener extends SimpleImageLoadingListener {

    private PeopleRepository.PicReady picReady;

    public MyImageListener(PeopleRepository.PicReady picReady) {
        this.picReady = picReady;
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        this.picReady.handlePic(loadedImage);
    }
}
