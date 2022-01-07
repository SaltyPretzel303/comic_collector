package mosis.comiccollector.model.comic;

import android.location.Location;

import java.io.Serializable;

public class Comic implements Serializable {

    public static final String AUTHOR_ID_FIELD = "authorId";
    public static final String COMIC_ID_FIELD = "id";

    private String authorId;

    private String id;
    private String comicTitle;
    private String description;

    private int rating;

    private int pagesCount;

    private Location location;

    public Comic() {

    }

    public Comic(String authorId,
                 String comicTitle,
                 String description,
                 int rating,
                 int pagesCount,
                 Location location) {

        this.authorId = authorId;
        this.comicTitle = comicTitle;
        this.description = description;
        this.rating = rating;
        this.pagesCount = pagesCount;
        this.location = location;
    }

    public String getAuthorId() {
        return authorId;
    }


    public String getId() {
        return id;
    }

    public String getComicTitle() {
        return comicTitle;
    }

    public String getDescription() {
        return description;
    }

    public int getRating() {
        return rating;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public Location getLocation() {
        return location;
    }
}
