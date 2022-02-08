package mosis.comiccollector.repository.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import mosis.comiccollector.model.Rating;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DoneHandler;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.RatingsRepository;
import mosis.comiccollector.util.DepProvider;

public class FirebaseRatingRepository implements RatingsRepository {

    public static final String RATINGS_COLLECTION = "ratings";

    private ComicRepository comicRepo;
    private PeopleRepository peopleRepo;

    public FirebaseRatingRepository() {
        this.comicRepo = DepProvider.getComicRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
    }

    @Override
    public void addRating(
        String userId,
        String authorId,
        float authorRating,
        String comicId,
        float comicRating,
        DoneHandler onDone) {

        Rating newRating = new Rating(userId, authorId, authorRating, comicId, comicRating);

        int tasksCount = 3;
        var liveTask = new MutableLiveData<Integer>(0);

        liveTask.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                if (value >= tasksCount) {
                    Log.e("ratingRepo", "rating update done ... ");
                    onDone.handleDone(null);

                    liveTask.removeObserver(this);

                    return;
                }
                Log.e("ratingRepo", "single task done ... " + value);
            }
        });

        FirebaseFirestore.getInstance()
            .collection(RATINGS_COLLECTION)
            .add(newRating)
            .addOnCompleteListener((@NonNull Task<DocumentReference> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("ratingRepo", "Failed to add new rating ... ");
                    String err = "Unknown error ... ";
                    if (task.getException() != null) {
                        err = task.getException().getMessage();
                    }
                    onDone.handleDone(err);
                    return;
                }

                Log.e("ratingRepo", "Added new rating ... ");
                int current = liveTask.getValue();
                liveTask.postValue(current + 1);
            });

        getAuthorRating(authorId, (rating) -> {
            peopleRepo.updateRating(authorId, rating, (unused) -> {
                Log.e("ratingRepo", "Updated author ... ");
                int current = liveTask.getValue();
                liveTask.postValue(current + 1);
            });

        });

        getComicsRating(comicId, (rating) -> {
            comicRepo.updateRating(comicId, rating, (unused) -> {
                Log.e("ratingRepo", "Updated comic ... ");
                int current = liveTask.getValue();
                liveTask.postValue(current + 1);
            });
        });


    }

    @Override
    public void getAuthorRating(String authorId, RatingValueReady onRead) {
        FirebaseFirestore.getInstance()
            .collection(RATINGS_COLLECTION)
            .whereEqualTo(Rating.AUTHOR_FIELD, authorId)
            .get()
            .addOnCompleteListener((@NonNull Task<QuerySnapshot> queryTask) -> {
                if (!queryTask.isSuccessful()) {
                    Log.e("ratingRepo", "Failed to query author ratings ... ");
                    String err = "Unknown err";
                    if (queryTask.getException() != null) {
                        err = queryTask.getException().getMessage();
                    }
                    Log.e("ratingRepo", "Err: " + err);

                    onRead.handle(0);
                    return;
                }

                if (queryTask.getResult() == null) {

                    onRead.handle(0);
                    return;
                }

                var ratings = queryTask.getResult().toObjects(Rating.class);

                float sum = 0;
                for (var singleRating : ratings) {
                    sum += singleRating.authorRating;
                }

                onRead.handle(sum / ratings.size());

            });
    }

    @Override
    public void getComicsRating(String comicId, RatingValueReady onRead) {
        FirebaseFirestore.getInstance()
            .collection(RATINGS_COLLECTION)
            .whereEqualTo(Rating.COMIC_FIELD, comicId)
            .get()
            .addOnCompleteListener((@NonNull Task<QuerySnapshot> queryTask) -> {
                if (!queryTask.isSuccessful()) {
                    Log.e("ratingRepo", "Failed to query comic ratings ... ");
                    String err = "Unknown err";
                    if (queryTask.getException() != null) {
                        err = queryTask.getException().getMessage();
                    }
                    Log.e("ratingRepo", "Err: " + err);

                    onRead.handle(0);
                    return;
                }

                if (queryTask.getResult() == null) {

                    onRead.handle(0);
                    return;
                }

                var ratings = queryTask.getResult().toObjects(Rating.class);

                float sum = 0;
                for (var singleRating : ratings) {
                    sum += singleRating.comicRating;
                }

                onRead.handle(sum / ratings.size());
            });
    }

    @Override
    public void getRating(String whoRatedId, String comicId, RatingReady onRead) {
        FirebaseFirestore.getInstance()
            .collection(RATINGS_COLLECTION)
            .whereEqualTo(Rating.WHO_RATED_FIELD, whoRatedId)
            .whereEqualTo(Rating.COMIC_FIELD, comicId)
            .get()
            .addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("ratingRepo", "Failed to get rating ... ");
                    onRead.handle(null);

                    return;
                }

                if (task.getResult() != null && task.getResult().size() > 0) {
                    onRead.handle(task.getResult().toObjects(Rating.class).get(0));
                } else {
                    onRead.handle(null);
                }

                return;
            });
    }


}
