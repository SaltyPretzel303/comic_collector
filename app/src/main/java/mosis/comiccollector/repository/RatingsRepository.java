package mosis.comiccollector.repository;

import mosis.comiccollector.model.Rating;

public interface RatingsRepository {

    interface RatingValueReady {
        void handle(float rating);
    }

    interface RatingReady {
        void handle(Rating rating);
    }

    void addRating(String userId,
                   String authorId,
                   float authorRating,
                   String comicId,
                   float comicRating,
                   DoneHandler onDone);

    void getAuthorRating(String authorId, RatingValueReady onRead);

    void getComicsRating(String comicId, RatingValueReady onRead);

    void getRating(String whoRatedId, String comicId, RatingReady onRead);

}
