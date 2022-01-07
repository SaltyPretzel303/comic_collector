package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.UserCollectedComicsList;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.ComicRepository;

public class FirebaseComicRepository implements ComicRepository {

    private static final String PAGE_CACHE_PREFIX = "comic_page_";
    private static final String PAGE_CACHE_SUFFIX = "_comic_";

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
        final File tempFile;
        try {
            tempFile = File.createTempFile(
                    PAGE_CACHE_PREFIX,
                    PAGE_CACHE_SUFFIX + comicId);

            Log.e("loadPic", "Created temp file: " + tempFile.getName());
            tempFile.deleteOnExit();

            FirebaseStorage.getInstance()
                    .getReference(COMICS_STORAGE)
                    .child(comicId)
                    .child("" + pageInd)
                    .getFile(tempFile)
                    .addOnCompleteListener((@NonNull Task<FileDownloadTask.TaskSnapshot> task) -> {
                        if (!task.isSuccessful()) {
                            Log.e("comicRepo",
                                    "Failed to load comic page ... "
                                            + task.getException().getMessage());
                            handler.handlePages(null);
                            return;
                        }

                        List<String> uris = new ArrayList<>();
                        uris.add(Uri.fromFile(tempFile).toString());
                        handler.handlePages(uris);

                        return;
                    });

        } catch (Exception e) {
            Log.e("comicRepo", "Exc while loading page ... " + e.getMessage());
            handler.handlePages(null);
            return;
        }
    }
}
