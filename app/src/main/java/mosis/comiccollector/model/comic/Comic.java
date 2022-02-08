package mosis.comiccollector.model.comic;

import java.io.Serializable;

import mosis.comiccollector.model.Location;

public class Comic implements Serializable {

    public static final String AUTHOR_ID_FIELD = "authorId";
    public static final String COMIC_ID_FIELD = "id";
    public static final String PAGES_COUNT_FIELD = "pagesCount";

    public static final String RATING_FIELD = "rating";

    private String authorId;

    private String id;
    private String comicTitle;
    private String description;

    private int pagesCount;

    private Location location;

    private float rating;

    public Comic() {

    }

    public Comic(String authorId,
                 String comicTitle,
                 String description,
                 int pagesCount,
                 Location location,
                 float rating) {

        this.authorId = authorId;
        this.comicTitle = comicTitle;
        this.description = description;
        this.pagesCount = pagesCount;
        this.location = location;
        this.rating = rating;
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

    public int getPagesCount() {
        return pagesCount;
    }

    public Location getLocation() {
        return location;
    }

    public float getRating() {
        return rating;
    }
}
