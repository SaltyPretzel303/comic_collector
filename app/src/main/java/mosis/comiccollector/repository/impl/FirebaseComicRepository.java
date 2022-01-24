package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import mosis.comiccollector.model.UserCollectedComicsList;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;

public class FirebaseComicRepository implements ComicRepository {

//    private static final String PAGE_CACHE_PREFIX = "comic_page_";
//    private static final String PAGE_CACHE_SUFFIX = "_comic_";

    private static final String COMICS_INFO_PATH = "comic_info";
    private static final String USER_COLLECTED_COMICS = "user_collected_comics";

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
                            handler.handleSingleUpload(comicId, -1);
                            return;
                        }

                        handler.handleSingleUpload(
                                comicId,
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
                        handler.handleSingleUpload("", -1);
                        return;
                    }

                    String docId = createTask.getResult().getId();

                    createTask.getResult()
                            .update(Comic.COMIC_ID_FIELD, docId)
                            .addOnCompleteListener((@NonNull Task<Void> updateTask) -> {
                                if (!updateTask.isSuccessful()) {
                                    Log.e("comicsRepo", "Failed to update comic id ... ");
                                    handler.handleSingleUpload(docId, -1);
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
                                                    handler.handleSingleUpload(docId, -1);
                                                    return;
                                                }

                                                handler.handleSingleUpload(
                                                        docId,
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
    public void collectComic(String userId, String comicId, DoneHandler handler) {
        FirebaseFirestore.getInstance()
                .collection(USER_COLLECTED_COMICS)
                .document(userId)
                .update(UserCollectedComicsList.COLLECTED_LIST_FIELD, FieldValue.arrayUnion(comicId))
                .addOnCompleteListener((@NonNull Task<Void> task) -> {
                    if (!task.isSuccessful()) {
                        Log.e("comicRepo", "Failed to update collected comics list ... ");
                        handler.handle(task.getException().getMessage());
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
}
