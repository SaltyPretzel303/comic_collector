package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mosis.comiccollector.model.UserCollectedComicsList;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;

public class FirebaseComicRepository implements ComicRepository {

    private static final String COMICS_INFO_PATH = "comic_info";
    private static final String USER_COLLECTED_COMICS = "user_collected_comics";

    private static final String COMICS_LOCATION_PATH = "comic_locations";

    private static final String COMICS_STORAGE = "comics";

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

        new GeoFirestore(FirebaseFirestore.getInstance().collection(COMICS_LOCATION_PATH))
                .getAtLocation(
                        new GeoPoint(lat, lon),
                        rad,
                        (@Nullable List<? extends DocumentSnapshot> list, @Nullable Exception e) -> {
                            if (e != null) {
                                Log.e("comicRepo", "Failed to load createdComics in range ... ");
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            List<String> ids = new ArrayList<>();
                            for (var item : list) {
                                ids.add(item.getId());
                            }
                            if (ids.size() == 0) {
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            FirebaseFirestore.getInstance()
                                    .collection(COMICS_INFO_PATH)
                                    .whereIn(FieldPath.documentId(), ids)
                                    .whereEqualTo(Comic.AUTHOR_ID_FIELD, userId)
                                    .get()
                                    .addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
                                        if (!task.isSuccessful()) {
                                            Log.e("comicRepo", "Failed to load comic by id query ... ");
                                            handler.handleComics(Collections.emptyList());
                                            return;
                                        }

                                        handler.handleComics(task.getResult().toObjects(Comic.class));

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

                    FirebaseFirestore.getInstance()
                            .collection(COMICS_INFO_PATH)
                            .whereIn(Comic.COMIC_ID_FIELD, collectedList.comicsIds)
                            .get()
                            .addOnCompleteListener((@NonNull Task<QuerySnapshot> comicsTask) -> {

                                if (!comicsTask.isSuccessful()) {
                                    Log.e("comicRepo", "Failed to load comics info ... ");
                                    handler.handleComics(null);
                                    return;
                                }

                                handler.handleComics(comicsTask
                                        .getResult()
                                        .toObjects(Comic.class));

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
                            if (e != null) {
                                Log.e("comicRepo", "Failed to get collected comics on location ");
                                handler.handleComics(Collections.emptyList());
                                return;
                            }
                            Log.e("comicRepo", "Got nearby comics: " + list.size());
                            List<String> nearbyIds = new ArrayList<>();
                            for (var item : list) {
                                nearbyIds.add(item.getId());
                            }
                            if (nearbyIds.size() == 0) {
                                handler.handleComics(Collections.emptyList());
                                return;
                            }

                            Log.e("comicRepo", "Got nearby comics: " + nearbyIds.size());

                            FirebaseFirestore.getInstance()
                                    .collection(USER_COLLECTED_COMICS)
                                    .document(userId)
                                    .get()
                                    .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                                        if (!task.isSuccessful()
                                                || task.getResult() == null
                                                || !task.getResult().exists()) {
                                            Log.e("comicRepo", "Failed ot get collected ids ... ");
                                            handler.handleComics(Collections.emptyList());
                                            return;
                                        }

                                        UserCollectedComicsList collected = task
                                                .getResult()
                                                .toObject(UserCollectedComicsList.class);

                                        if (collected.comicsIds == null
                                                || collected.comicsIds.size() == 0) {
                                            handler.handleComics(Collections.emptyList());
                                            return;
                                        }

                                        var collectedNearby = intersection(
                                                nearbyIds,
                                                collected.comicsIds);

                                        // this will ensure that list is not empty
                                        // nad -1 for sure wont be matched with any of comic ids
                                        collectedNearby.add("-1");

                                        FirebaseFirestore.getInstance()
                                                .collection(COMICS_INFO_PATH)
                                                .whereIn(Comic.COMIC_ID_FIELD, collectedNearby)
                                                .get()
                                                .addOnCompleteListener((@NonNull Task<QuerySnapshot> queryTask) -> {

                                                    if (!queryTask.isSuccessful() || queryTask.getResult() == null) {
                                                        Log.e("comicRepo", "Failed to query near and collected ... ");
                                                        handler.handleComics(Collections.emptyList());
                                                        return;
                                                    }

                                                    handler.handleComics(queryTask
                                                            .getResult()
                                                            .toObjects(Comic.class));

                                                    return;
                                                });

                                    });

                        });


    }

    private List<String> intersection(List<String> list_1, List<String> list_2) {
        return list_1.stream().filter(list_2::contains).collect(Collectors.toList());
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
                        Log.e("comicRepo", "Failed to get page download uri ... page: " + pageInd);
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
                            Log.e("comicsRepo", "Failed to update comic ... ");
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
                        Log.e("comicRepo", "Failed to create comic info ... ");
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
                                    Log.e("comicsRepo", "Failed to update comic id ... ");
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
                                                    Log.e("comicsRepo", "Failed to update comic ... ");
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
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        Log.e("comicsRepo", "Failed to load collected comics for: " + userId);
                        handler.handleComics(null);
                        return;
                    }

                    UserCollectedComicsList collectedList = task
                            .getResult()
                            .toObject(UserCollectedComicsList.class);

                    FirebaseFirestore.getInstance()
                            .collection(COMICS_INFO_PATH)
                            .whereNotIn(Comic.COMIC_ID_FIELD, collectedList.comicsIds)
                            .get()
                            .addOnCompleteListener((@NonNull Task<QuerySnapshot> queryTask) -> {
                                if (!queryTask.isSuccessful()) {
                                    Log.e("comicRepo", "Failed to query unknown comics ... ");
                                    handler.handleComics(null);
                                    return;
                                }

                                List<Comic> comics = queryTask
                                        .getResult()
                                        .toObjects(Comic.class);
                                comics.removeIf((comic) -> {
                                    return comic.getAuthorId().equals(userId);
                                });

                                handler.handleComics(comics);

                                return;
                            });


                });
    }

    @Override
    public void getUnknownComics(String userId, double lat, double lon, double rad,
                                 ComicsHandler handler) {

        GeoPoint point = new GeoPoint(lat, lon);

        new GeoFirestore(FirebaseFirestore.getInstance().collection(COMICS_LOCATION_PATH))
                .getAtLocation(point, rad, (@Nullable List<? extends DocumentSnapshot> list, @Nullable Exception e) -> {
                    if (e != null) {
                        Log.e("comicRepo", "Failed to get unknown comics at location ... ");
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    Log.e("comicRepo", "unknown comic at location: " + list.size());

                    List<String> nearbyIds = new ArrayList<>();
                    for (var item : list) {
                        nearbyIds.add(item.getId());
                    }
                    if (nearbyIds.size() == 0) {
                        handler.handleComics(Collections.emptyList());
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection(USER_COLLECTED_COMICS)
                            .document(userId)
                            .get()
                            .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                                if (!task.isSuccessful()) {
                                    Log.e("comicsRepo", "Failed to load collected comics for: " + userId);
                                    handler.handleComics(null);
                                    return;
                                }

                                // this should be empty array but firebase doesn't support
                                // whereNotIn query on an empty array so "-1" should be Id
                                // impossible to match ...
                                List<String> collectedIds = Arrays.asList("-1");

                                if (task.getResult().exists() && task.getResult() != null) {
                                    // this person HAS some collected comics
                                    collectedIds = task
                                            .getResult()
                                            .toObject(UserCollectedComicsList.class)
                                            .comicsIds;
                                }


                                FirebaseFirestore.getInstance()
                                        .collection(COMICS_INFO_PATH)
                                        .whereNotIn(Comic.COMIC_ID_FIELD, collectedIds)
                                        .get()
                                        .addOnCompleteListener((@NonNull Task<QuerySnapshot> queryTask) -> {
                                            if (!queryTask.isSuccessful()) {
                                                Log.e("comicRepo", "Failed to query unknown comics ... ");
                                                handler.handleComics(null);
                                                return;
                                            }

                                            List<Comic> comics = queryTask
                                                    .getResult()
                                                    .toObjects(Comic.class);
                                            comics.removeIf((comic) -> {
                                                return comic.getAuthorId().equals(userId);
                                            });

                                            handler.handleComics(comics);

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
                        Log.e("comicRepo", "Failed to UPDATE collected comics list ... ");

                        FirebaseFirestore.getInstance()
                                .collection(USER_COLLECTED_COMICS)
                                .document(userId)
                                .set(new UserCollectedComicsList(userId, Arrays.asList(comicId)))
                                .addOnCompleteListener((@NonNull Task<Void> setTask) -> {

                                    if (!setTask.isSuccessful()) {
                                        Log.e("comicRepo", "Failed to SET collected comics list ... ");
                                        if (setTask.getException() != null) {
                                            handler.handle(setTask.getException().getMessage());
                                        } else {
                                            handler.handle("Unknown message");
                                        }

                                    }

                                    handler.handle(null);

                                    return;
                                });


                        return;
                    }

                    handler.handle(null);
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
                        Log.e("comicRepo", "Failed to get comic: " + comicId);
                        handler.handleComics(null);
                        return;
                    }

                    Comic comic = task.getResult().toObject(Comic.class);
                    handler.handleComics(Arrays.asList(comic));

                    return;
                });
    }

    @Override
    public void addRating(String comicId, float rating, DoneHandler handler) {

    }
}
