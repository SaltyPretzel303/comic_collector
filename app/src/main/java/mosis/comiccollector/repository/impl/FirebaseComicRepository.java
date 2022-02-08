package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mosis.comiccollector.model.UserCollectedComicsList;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DoneHandler;
import mosis.comiccollector.ui.comic.IndexedUriPage;

public class FirebaseComicRepository implements ComicRepository {

    private static final String COMICS_INFO_PATH = "comic_info";
    private static final String USER_COLLECTED_COMICS = "user_collected_comics";

    private static final String COMICS_LOCATION_PATH = "comic_locations";

    private static final String COMICS_STORAGE = "comics";

    // firebase supports up to 10 items in whereIn query
    private static final int FIREBASE_WHEREIN_LIMIT = 10;

    @Override
    public void getCreatedComics(String userId, ComicsHandler handler) {

        FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH)
            .whereEqualTo(Comic.AUTHOR_ID_FIELD, userId)
            .get()
            .addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("comicRepo", "Failed to load created comics for: " + userId);
                    handler.handleComics(null);
                    return;
                }

                handler.handleComics(task.getResult().toObjects(Comic.class));

                return;
            });
    }

    @Override
    public void getCreatedComics(String userId, double lat, double lon, double rad,
                                 ComicsHandler handler) {

        new GeoFirestore(FirebaseFirestore.getInstance()
            .collection(COMICS_LOCATION_PATH))
            .getAtLocation(
                new GeoPoint(lat, lon),
                rad,
                (@Nullable List<? extends DocumentSnapshot> list, @Nullable Exception e) -> {
                    if (e != null || list == null) {
                        Log.e("comicRepo", "Failed to query createdComics in range ... ");
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    if (list.size() == 0) {
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    List<String> ids = list.stream()
                        .map(DocumentSnapshot::getId)
                        .collect(Collectors.toList());

                    var idSegments = splitSegments(ids, FIREBASE_WHEREIN_LIMIT);

                    var tasks = new ArrayList<Task<QuerySnapshot>>();
                    for (var segment : idSegments) {
                        tasks.add(FirebaseFirestore.getInstance()
                            .collection(COMICS_INFO_PATH)
                            .whereIn(FieldPath.documentId(), segment)
                            .whereEqualTo(Comic.AUTHOR_ID_FIELD, userId)
                            .get());
                    }

                    Tasks.whenAllComplete(tasks)
                        .addOnCompleteListener((@NonNull Task<List<Task<?>>> task) -> {
                            if (!task.isSuccessful() || task.getResult() == null) {
                                Log.e("comicRepo", "Failed to query created comics ... ");
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            List<Comic> data = joinTaskResults(task.getResult(), Comic.class);
                            handler.handleComics(data);

                            return;
                        });

                });

    }

    @Override
    public void getCollectedComics(String userId, ComicsHandler handler) {

        FirebaseFirestore.getInstance()
            .collection(USER_COLLECTED_COMICS)
            .document(userId)
            .get()
            .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {

                if (!task.isSuccessful() || !task.getResult().exists()) {
                    Log.e("comicsRepo", "Failed to load collected comics for: " + userId);
                    handler.handleComics(null);
                    return;
                }

                UserCollectedComicsList collectedList = task
                    .getResult()
                    .toObject(UserCollectedComicsList.class);

                if (collectedList == null || collectedList.comicsIds.size() == 0) {
                    handler.handleComics(Collections.emptyList());
                    return;
                }

                List<List<String>> idSegments = splitSegments(
                    collectedList.comicsIds,
                    FIREBASE_WHEREIN_LIMIT);

                var tasks = new ArrayList<Task<QuerySnapshot>>();

                for (var segment : idSegments) {
                    tasks.add(FirebaseFirestore.getInstance()
                        .collection(COMICS_INFO_PATH)
                        .whereIn(Comic.COMIC_ID_FIELD, segment)
                        .get());
                }

                Tasks
                    .whenAllComplete(tasks)
                    .addOnCompleteListener((@NonNull Task<List<Task<?>>> segmentTask) -> {
                        if (!segmentTask.isSuccessful() || segmentTask.getResult() == null) {
                            Log.e("comicRepo", "Failed to query collected comics segments .. ");
                            handler.handleComics(Collections.emptyList());
                            return;
                        }

                        List<Comic> data = joinTaskResults(
                            segmentTask.getResult(),
                            Comic.class);

                        handler.handleComics(data);
                        return;

                    });

            });
    }

    @Override
    public void getCollectedComics(String userId, double lat, double lon, double rad,
                                   ComicsHandler handler) {

        var point = new GeoPoint(lat, lon);

        new GeoFirestore(FirebaseFirestore.getInstance().collection(COMICS_LOCATION_PATH))
            .getAtLocation(
                point,
                rad,
                (@Nullable List<? extends DocumentSnapshot> list, @Nullable Exception e) -> {
                    if (e != null || list == null) {
                        Log.e("comicRepo", "Failed to get collected comics on location ");
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    if (list.size() == 0) {
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    List<String> nearbyIds = list.stream()
                        .map(DocumentSnapshot::getId)
                        .collect(Collectors.toList());

                    FirebaseFirestore.getInstance()
                        .collection(USER_COLLECTED_COMICS)
                        .document(userId)
                        .get()
                        .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                            if (!task.isSuccessful()
                                || task.getResult() == null
                                || !task.getResult().exists()) {

                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            UserCollectedComicsList collected = task
                                .getResult()
                                .toObject(UserCollectedComicsList.class);

                            if (collected == null
                                || collected.comicsIds == null
                                || collected.comicsIds.size() == 0) {
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            var collectedNearby = intersection(
                                nearbyIds,
                                collected.comicsIds);

                            if (collectedNearby.size() == 0) {
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            var idSegments = splitSegments(
                                collectedNearby,
                                FIREBASE_WHEREIN_LIMIT);

                            var tasks = new ArrayList<Task<QuerySnapshot>>();
                            for (var segment : idSegments) {
                                tasks.add(FirebaseFirestore.getInstance()
                                    .collection(COMICS_INFO_PATH)
                                    .whereIn(Comic.COMIC_ID_FIELD, segment)
                                    .get());
                            }

                            Tasks
                                .whenAllComplete(tasks)
                                .addOnCompleteListener((@NonNull Task<List<Task<?>>> segmentTask) -> {
                                    if (!segmentTask.isSuccessful() || segmentTask.getResult() == null) {
                                        handler.handleComics(Collections.emptyList());
                                        return;
                                    }

                                    List<Comic> data = joinTaskResults(
                                        segmentTask.getResult(),
                                        Comic.class);
                                    handler.handleComics(data);

                                    return;
                                });

                        });

                });

    }

    @Override
    public void loadTitlePage(String comicId,
                              int width,
                              int height,
                              PagesHandler handler) {
        loadPage(comicId, width, height, 0, handler);
    }

    @Override
    public void loadPage(String comicId,
                         int width,
                         int height,
                         int pageInd,
                         PagesHandler handler) {

        FirebaseStorage.getInstance()
            .getReference(COMICS_STORAGE)
            .child(comicId)
            .child("" + pageInd)
            .getDownloadUrl()
            .addOnCompleteListener((@NonNull Task<Uri> task) -> {
                if (!task.isSuccessful()) {
                    handler.handlePages(null);
                    return;
                }

                String uri = task.getResult().toString();

                ImageLoader.getInstance().loadImage(
                    uri,
                    new ImageSize(width, height),
                    new MyImageListener(handler));

            });

    }

    @Override
    public void addPages(String comicId,
                         int newCount,
                         List<IndexedUriPage> pageUris,
                         @NotNull UploadHandler handler) {

        FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH)
            .document(comicId)
            .update(Comic.PAGES_COUNT_FIELD, newCount)
            .addOnCompleteListener((@NonNull Task<Void> updateTask) -> {
                // TODO I guess do something ...
            });


        for (IndexedUriPage indexedUri : pageUris) {
            FirebaseStorage.getInstance()
                .getReference(COMICS_STORAGE)
                .child(comicId)
                .child("" + indexedUri.index)
                .putFile(Uri.parse(indexedUri.pageUri))
                .addOnCompleteListener((@NonNull Task<UploadTask.TaskSnapshot> task) -> {
                    if (!task.isSuccessful()) {
                        handler.handleSingleUpload(comicId, indexedUri.index, 0);
                        return;
                    }

                    handler.handleSingleUpload(
                        comicId,
                        indexedUri.index,
                        task.getResult().getBytesTransferred());

                });

        }

    }

    @Override
    public void createComic(Comic newComic,
                            List<IndexedUriPage> pages,
                            @NonNull UploadHandler handler) {
        CollectionReference comicCollection = FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH);

        comicCollection
            .add(newComic)
            .addOnCompleteListener((@NonNull Task<DocumentReference> createTask) -> {
                if (!createTask.isSuccessful()) {
                    handler.handleSingleUpload("", 0, -1);
                    return;
                }

                String docId = createTask.getResult().getId();

                new GeoFirestore(FirebaseFirestore.getInstance()
                    .collection(COMICS_LOCATION_PATH))
                    .setLocation(docId, new GeoPoint(
                        newComic.getLocation().latitude,
                        newComic.getLocation().longitude));
                // TODO somehow wait for the upper query to finish first ... ?

                createTask.getResult()
                    .update(Comic.COMIC_ID_FIELD, docId)
                    .addOnCompleteListener((@NonNull Task<Void> updateTask) -> {
                        if (!updateTask.isSuccessful()) {
                            handler.handleSingleUpload(docId, 0, -1);
                            return;
                        }

                        for (IndexedUriPage page : pages) {
                            FirebaseStorage.getInstance()
                                .getReference(COMICS_STORAGE)
                                .child(docId)
                                .child("" + page.index)
                                .putFile(Uri.parse(page.pageUri))
                                .addOnCompleteListener((@NonNull Task<UploadTask.TaskSnapshot> task) -> {
                                    if (!task.isSuccessful()) {
                                        handler.handleSingleUpload(
                                            docId,
                                            page.index,
                                            0);
                                        return;
                                    }

                                    handler.handleSingleUpload(
                                        docId,
                                        page.index,
                                        task.getResult().getBytesTransferred());

                                });
                        }

                    });

            });
    }

    @Override
    public void getUnknownComics(String userId, ComicsHandler handler) {
        FirebaseFirestore.getInstance()
            .collection(USER_COLLECTED_COMICS)
            .document(userId)
            .get()
            .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {

                List<String> collectedIds = new ArrayList<>();
                collectedIds.add("-1"); // never matching id

                if (task.isSuccessful()
                    && task.getResult().exists()
                    && task.getResult() != null) {

                    var collectedObj = task
                        .getResult()
                        .toObject(UserCollectedComicsList.class);

                    if (collectedObj != null && collectedObj.comicsIds != null) {
                        collectedIds.addAll(collectedObj.comicsIds);
                    }

                }

                var idSegments = splitSegments(
                    collectedIds,
                    FIREBASE_WHEREIN_LIMIT);

                List<Task<?>> tasks = new ArrayList<>();

                for (var idsSegment : idSegments) {
                    tasks.add(FirebaseFirestore.getInstance()
                        .collection(COMICS_INFO_PATH)
                        .whereNotIn(Comic.COMIC_ID_FIELD, idsSegment)
                        .get());
                }

                Tasks
                    .whenAllComplete(tasks)
                    .addOnCompleteListener((@NonNull Task<List<Task<?>>> segmentTask) -> {
                        if (!segmentTask.isSuccessful() || segmentTask.getResult() == null) {
                            Log.e("comicRepo", "Failed to get unknown comics ... ");
                            handler.handleComics(Collections.emptyList());
                            return;
                        }

                        List<Comic> data = joinTaskResults(
                            segmentTask.getResult(),
                            Comic.class);
                        data.removeIf((input) -> input.getAuthorId().equals(userId));

                        handler.handleComics(data);
                        return;
                    });

            });
    }

    @Override
    public void getUnknownComics(String userId, double lat, double lon, double rad,
                                 ComicsHandler handler) {

        GeoPoint point = new GeoPoint(lat, lon);

        new GeoFirestore(FirebaseFirestore.getInstance().collection(COMICS_LOCATION_PATH))
            .getAtLocation(
                point,
                rad,
                (@Nullable List<? extends DocumentSnapshot> list, @Nullable Exception e) -> {
                    if (e != null || list == null) {
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    if (list.size() == 0) {
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    List<String> nearbyIds = list.stream()
                        .map(DocumentSnapshot::getId)
                        .collect(Collectors.toList());

                    FirebaseFirestore.getInstance()
                        .collection(USER_COLLECTED_COMICS)
                        .document(userId)
                        .get()
                        .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                            List<String> collectedIds = new ArrayList<>();
                            if (task.isSuccessful()
                                && task.getResult().exists()
                                && task.getResult() != null) {

                                var collectedObj = task
                                    .getResult()
                                    .toObject(UserCollectedComicsList.class);

                                if (collectedObj != null && collectedObj.comicsIds != null) {
                                    collectedIds.addAll(collectedObj.comicsIds);
                                }

                            }

                            nearbyIds.removeIf(collectedIds::contains);

                            List<List<String>> nearbyIdSegments = splitSegments(
                                nearbyIds,
                                FIREBASE_WHEREIN_LIMIT);

                            List<Task<?>> tasks = new ArrayList<>();

                            for (var singleSegment : nearbyIdSegments) {
                                tasks.add(FirebaseFirestore.getInstance()
                                    .collection(COMICS_INFO_PATH)
                                    .whereIn(Comic.COMIC_ID_FIELD, singleSegment)
                                    .get());
                            }

                            Tasks.whenAllComplete(tasks)
                                .addOnCompleteListener((@NonNull Task<List<Task<?>>> segmentTask) -> {
                                    if (!segmentTask.isSuccessful()
                                        || segmentTask.getResult() == null) {

                                        handler.handleComics(Collections.emptyList());
                                        return;
                                    }

                                    List<Comic> data = joinTaskResults(
                                        segmentTask.getResult(),
                                        Comic.class);
                                    data.removeIf(comic -> comic.getAuthorId().equals(userId));

                                    handler.handleComics(data);
                                    return;
                                });

                        });

                });

    }

    @Override
    public void collectComic(String userId, String comicId, DoneHandler handler) {
        FirebaseFirestore.getInstance()
            .collection(USER_COLLECTED_COMICS)
            .document(userId)
            .update(UserCollectedComicsList.COLLECTED_LIST_FIELD, FieldValue.arrayUnion(comicId))
            .addOnCompleteListener((@NonNull Task<Void> task) -> {
                if (!task.isSuccessful()) {

                    FirebaseFirestore.getInstance()
                        .collection(USER_COLLECTED_COMICS)
                        .document(userId)
                        .set(new UserCollectedComicsList(userId, Arrays.asList(comicId)))
                        .addOnCompleteListener((@NonNull Task<Void> setTask) -> {

                            if (!setTask.isSuccessful()) {
                                if (setTask.getException() != null) {
                                    handler.handleDone(setTask.getException().getMessage());
                                } else {
                                    handler.handleDone("Unknown message");
                                }

                            }

                            handler.handleDone(null);

                            return;
                        });


                    return;
                }

                handler.handleDone(null);
                return;
            });
    }

    @Override
    public void getComic(String comicId, ComicsHandler handler) {
        FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH)
            .document(comicId)
            .get().
            addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                if (!task.isSuccessful()) {
                    handler.handleComics(null);
                    return;
                }

                Comic comic = task.getResult().toObject(Comic.class);
                handler.handleComics(Arrays.asList(comic));

                return;
            });
    }

    @Override
    public void updateRating(String comicId, float newRating, DoneHandler onDone) {
        FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH)
            .document(comicId)
            .update(Comic.RATING_FIELD, newRating)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    var err = "Unknown err ... ";
                    if (task.getException() != null) {
                        err = task.getException().getMessage();
                    }

                    onDone.handleDone(err);
                    return;
                }

                onDone.handleDone(null);
            });
    }

    @Override
    public void getRating(String comicId, RatingHandler handler) {
        FirebaseFirestore.getInstance()
            .collection(COMICS_INFO_PATH)
            .document(comicId)
            .get()
            .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                if (!task.isSuccessful()) {
                    handler.handleRating(0);
                    return;
                }

                float rating = task.getResult().toObject(Comic.class).getRating();
                handler.handleRating(rating);
            });
    }

    private List<List<String>> splitSegments(List<String> input, int size) {
        List<List<String>> regions = new ArrayList<>();

        int starting = 0;
        // second index in list.sublist(starting, ending) is not included in resulting list
        int lastIndex = input.size();

        int ending;
        while (starting < lastIndex) {
            ending = starting + size;
            if (ending > lastIndex) {
                ending = lastIndex;
            }

            regions.add(input.subList(starting, ending));

            starting += size;
        }

        return regions;

    }

    private <T> List<T> joinTaskResults(List<Task<?>> results, Class T) {
        List<T> data = new ArrayList<>();

        for (var resultTask : results) {
            if (!resultTask.isSuccessful() || resultTask.getResult() == null) {
            } else {
                data.addAll(((Task<QuerySnapshot>) resultTask).getResult().toObjects(T));
            }
        }

        return data;
    }

    private List<String> intersection(List<String> list_1, List<String> list_2) {
        return list_1.stream().filter(list_2::contains).collect(Collectors.toList());
    }
}
