package mosis.comiccollector.repository;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.ui.comic.IndexedUriPage;

public interface ComicRepository {

    interface ComicsHandler {
        void handleComics(List<Comic> newComics);
    }

    interface PagesHandler {
        void handlePages(List<Bitmap> newPages);
    }

    interface UploadHandler {
        void handleSingleUpload(String docId, long uploadSize);
    }

    void getCreatedComics(String userId, ComicsHandler handler);

    void getCollectedComics(String userId, ComicsHandler handler);

    void loadTitlePage(String comicId, int width, int height, PagesHandler handler);

    void loadPage(String comicId, int width, int height, int pageInd, PagesHandler handler);

    void addPages(String comicId,
                  int newCount,
                  List<IndexedUriPage> pageUris,
                  @NotNull UploadHandler handler);

    void createComic(Comic comicInfo,
                     List<IndexedUriPage> pagesUris,
                     @NotNull UploadHandler handler);

}
