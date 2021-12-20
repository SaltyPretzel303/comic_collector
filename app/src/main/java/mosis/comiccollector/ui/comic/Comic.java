package mosis.comiccollector.ui.comic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.R;

public class Comic {

    public static class Position {
        public double x;
        public double y;

        public Position() {

        }

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }

    public String modelId;

    public String title;
    public String description;
    public Bitmap icon;
    public String author;

    public Position position;

    public boolean discovered;

    public int progress;

    public Bitmap previewPage;
    public List<Bitmap> samplePages;
    private List<Bitmap> pages;

    public Comic(String modelId,
                 String title,
                 String description,
                 String author,
                 Bitmap icon,
                 List<Bitmap> samplePages,
                 Bitmap previewPage,
                 int progress) {

        this.modelId = modelId;

        this.title = title;
        this.author = author;
        this.icon = icon;
        this.previewPage = previewPage;
        this.progress = progress;

        // if sample pages are not provided
        if (samplePages == null) {
            this.samplePages = new ArrayList<Bitmap>();
            this.samplePages.add(this.icon);
        }

        this.pages = new ArrayList<Bitmap>();

    }

    // TODO REMOVE
    // default comic
    public Comic() {

        this.title = "TESTING TITLE ";
        this.icon = BitmapFactory.decodeResource(
                MyApplication
                        .getInstance()
                        .getActivityContext()
                        .getResources(),
                R.drawable.main_back);

        this.author = "TEST Author";
        Random gen = new Random();
        this.progress = gen.nextInt(100);

        this.samplePages = new ArrayList<Bitmap>();
        for (int i = 0; i < gen.nextInt(10); i++) {
            this.samplePages.add(this.icon);
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
