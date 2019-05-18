package mosis.comiccollector.comic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;
import java.util.Random;

import mosis.comiccollector.R;
import mosis.comiccollector.manager.AppManager;

public class Comic {

    public String title;
    public Bitmap icon;
    public String author;

    public int progress;

    public List<Bitmap> sample_pages;

    public Comic(String title, String author, Bitmap icon, List<Bitmap> sample_pages, int progress) {

        this.title = title;
        this.author = author;
        this.icon = icon;
        this.sample_pages = sample_pages;
        this.progress = progress;

    }

    // TODO REMOVE
    // default comic
    public Comic() {

        this.title = "TESTING TITLE ";
        this.icon = BitmapFactory.decodeResource(AppManager.getInstance().context.getResources(), R.drawable.main_back);
        this.author = "TEST Author";
        Random gen = new Random();
        this.progress = gen.nextInt(100);

    }


}
