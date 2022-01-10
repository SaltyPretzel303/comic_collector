package mosis.comiccollector.repository.impl;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.PeopleRepository;

public class MyImageListener extends SimpleImageLoadingListener {

    private PeopleRepository.PicReady picReady;

    private ComicRepository.PagesHandler pagesReady;

    public MyImageListener(PeopleRepository.PicReady picReady) {
        this.picReady = picReady;
    }

    public MyImageListener(ComicRepository.PagesHandler pagesReady) {
        this.pagesReady = pagesReady;
    }


    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        if (this.picReady != null) {
            this.picReady.handlePic(loadedImage);

            return;
        }

        if (this.pagesReady != null) {
            List<Bitmap> pages = new ArrayList<>();
            pages.add(loadedImage);
            this.pagesReady.handlePages(pages);

            return;
        }

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        Log.e("ImageLoader", "Failed to load image: " + imageUri);
        Log.e("ImageLoader", "Fail reason: " + failReason.getCause().getMessage());
        if (this.picReady != null) {
            this.picReady.handlePic(null);

            return;
        }

        if (this.pagesReady != null) {
            this.pagesReady.handlePages(null);

            return;
        }

    }


}
