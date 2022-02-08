package mosis.comiccollector.model;

import java.io.Serializable;

public class Rating implements Serializable {

    public static final String WHO_RATED_FIELD = "userId";
    public static final String AUTHOR_FIELD = "authorId";
    public static final String COMIC_FIELD = "comicId";

    public String userId;

    public String authorId;
    public float authorRating;

    public String comicId;
    public float comicRating;

    public Rating() {
    }

    public Rating(String userId,
                  String authorId,
                  float authorRating,
                  String comicId,
                  float comicRating) {

        this.userId = userId;
        this.authorId = authorId;
        this.authorRating = authorRating;
        this.comicId = comicId;
        this.comicRating = comicRating;
    }
}
