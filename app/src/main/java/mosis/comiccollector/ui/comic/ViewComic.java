package mosis.comiccollector.ui.comic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.R;
import mosis.comiccollector.model.Location;

public class ViewComic {

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
    public String author;

    public MutableLiveData<String> liveTitlePageUri;
    private MutableLiveData<List<String>> pagesUris;

    public Position location;

    public ViewComic(String modelId,
                     String title,
                     String description,
                     String author,
                     Location location,
                     int progress) {

        this.modelId = modelId;

        this.title = title;
        this.author = author;

    }

}
