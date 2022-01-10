package mosis.comiccollector.repository.impl;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.List;

import mosis.comiccollector.model.UserCollectedComicsList;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.ComicRepository;

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

                    if (!task.isSuccessful()) {
                        Log.e("comicsRepo", "Failed to load collected comics for: " + userId);
                        handler.handleComics(null);
                        return;
                    }

                    List<String> ids = task
                            .getResult()
                            .toObject(UserCollectedComicsList.class)
                            .comicsIds;

                    FirebaseFirestore.getInstance()
                            .collection(COMICS_INFO_PATH)
                            .whereIn(Comic.COMIC_ID_FIELD, ids)
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
    public void loadTitlePage(String comicId, PagesHandler handler) {
        loadPage(comicId, 0, handler);
    }

    @Override
    public void loadPage(String comicId, int pageInd, PagesHandler handler) {

        FirebaseStorage.getInstance()
                .getReference(COMICS_STORAGE)
                .child(comicId)
                .child("" + pageInd)
                .getDownloadUrl()
                .addOnCompleteListener((@NonNull Task<Uri> task) -> {
                    if (!task.isSuccessful()) {
                        Log.e("comicRepo", "Failed to get page download uri ... ");
                        handler.handlePages(null);
                        return;
                    }

                    String uri = task.getResult().toString();

                    ImageLoader.getInstance()
                            .loadImage(uri, new MyImageListener(handler));

                });

//        final File tempFile;
//        try {
//            tempFile = File.createTempFile(
//                    PAGE_CACHE_PREFIX,
//                    PAGE_CACHE_SUFFIX + comicId);
//
//            Log.e("loadPic", "Created temp file: " + tempFile.getName());
//            tempFile.deleteOnExit();
//
//            FirebaseStorage.getInstance()
//                    .getReference(COMICS_STORAGE)
//                    .child(comicId)
//                    .child("" + pageInd)
//                    .getFile(tempFile)
//                    .addOnCompleteListener((@NonNull Task<FileDownloadTask.TaskSnapshot> task) -> {
//                        if (!task.isSuccessful()) {
//                            Log.e("comicRepo",
//                                    "Failed to load comic page ... "
//                                            + task.getException().getMessage());
//                            handler.handlePages(null);
//                            return;
//                        }
//
//                        List<String> uris = new ArrayList<>();
//                        uris.add(Uri.fromFile(tempFile).toString());
//                        handler.handlePages(uris);
//
//                        return;
//                    });
//
//        } catch (Exception e) {
//            Log.e("comicRepo", "Exc while loading page ... " + e.getMessage());
//            handler.handlePages(null);
//            return;
//        }
    }

    @Override
    public void addPages(String comicId,
                         int newCount,
                         List<String> pageUris,
                         @NotNull UploadHandler handler) {

        FirebaseFirestore.getInstance()
                .collection(COMICS_INFO_PATH)
                .document(comicId)
                .update(Comic.PAGES_COUNT_FIELD, newCount)
                .addOnCompleteListener((@NonNull Task<Void> updateTask) -> {
                    // TODO do something I guess ...
                });


        int index = newCount - pageUris.size();
        for (String uri : pageUris) {
            FirebaseStorage.getInstance()
                    .getReference(COMICS_STORAGE)
                    .child(comicId)
                    .child("" + index)
                    .putFile(Uri.parse(uri))
                    .addOnCompleteListener((@NonNull Task<UploadTask.TaskSnapshot> task) -> {
                        if (!task.isSuccessful()) {
                            Log.e("comicsRepo", "Failed to update comic ... ");
                            handler.handleResult(-1);
                            return;
                        }

                        handler.handleResult(task.getResult().getBytesTransferred());

                    });

            index++;
        }

    }

    @Override
    public void createComic(Comic newComic, List<String> pagesUris, @NonNull UploadHandler handler) {
        CollectionReference comicCollection = FirebaseFirestore.getInstance()
                .collection(COMICS_INFO_PATH);

        comicCollection
                .add(newComic)
                .addOnCompleteListener((@NonNull Task<DocumentReference> createTask) -> {
                    if (!createTask.isSuccessful()) {
                        Log.e("comicRepo", "Failed to create comic info ... ");
                        handler.handleResult(-1);
                        return;
                    }

                    String docId = createTask.getResult().getId();
                    createTask.getResult()
                            .update(Comic.COMIC_ID_FIELD, docId)
                            .addOnCompleteListener((@NonNull Task<Void> updateTask) -> {
                                if (!updateTask.isSuccessful()) {
                                    Log.e("comicsRepo", "Failed to update comic id ... ");
                                    handler.handleResult(-1);
                                    return;
                                }

                                int index = newComic.getPagesCount() - pagesUris.size();
                                for (String uri : pagesUris) {
                                    FirebaseStorage.getInstance()
                                            .getReference(COMICS_STORAGE)
                                            .child(docId)
                                            .child("" + index)
                                            .putFile(Uri.parse(uri))
                                            .addOnCompleteListener((@NonNull Task<UploadTask.TaskSnapshot> task) -> {
                                                if (!task.isSuccessful()) {
                                                    Log.e("comicsRepo", "Failed to update comic ... ");
                                                    handler.handleResult(-1);
                                                    return;
                                                }

                                                handler.handleResult(task.getResult().getBytesTransferred());

                                            });

                                    index++;
                                }

                            });

                });
    }
}
