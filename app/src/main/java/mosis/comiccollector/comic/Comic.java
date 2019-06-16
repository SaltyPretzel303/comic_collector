package mosis.comiccollector.comic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
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
    private List<Bitmap> pages;

    public Comic(String title, String author, Bitmap icon, List<Bitmap> sample_pages, int progress) {

        this.title = title;
        this.author = author;
        this.icon = icon;
        this.progress = progress;

        // if sample pages are not provided
        if (sample_pages == null) {
            this.sample_pages = new ArrayList<Bitmap>();
            this.sample_pages.add(this.icon);
        }

        this.pages = new ArrayList<Bitmap>();

    }

    // TODO REMOVE
    // default comic
    public Comic() {

        this.title = "TESTING TITLE ";
        this.icon = BitmapFactory.decodeResource(AppManager.getInstance().context.getResources(), R.drawable.main_back);
        this.author = "TEST Author";
        Random gen = new Random();
        this.progress = gen.nextInt(100);

        this.sample_pages = new ArrayList<Bitmap>();
        for (int i = 0; i < gen.nextInt(10); i++) {
            this.sample_pages.add(this.icon);
        }

    }

    public void addPage(Bitmap new_page) {

        this.pages.add(new_page);

    }


    public void addPages(List<Bitmap> new_pages) {

        this.pages.addAll(new_pages);

    }

    public List<Bitmap> getPages() {
        return this.pages;
    }

}
