package mosis.comiccollector.model.comic;

import android.net.Uri;

import java.util.List;

public class Comic {

    private String authorId;

    private String comicTitle;

    private String description;

    private int rating;

    private Uri previewPage;

    private List<Uri> pages;

    public Comic(String authorId,
            String comicTitle,
            String description,
            int rating,
            Uri previewPage,
            List<Uri> pages) {
                
        this.authorId = authorId;
        this.comicTitle = comicTitle;
        this.description = description;
        this.rating = rating;
        this.previewPage = previewPage;
        this.pages = pages;
    }

}
