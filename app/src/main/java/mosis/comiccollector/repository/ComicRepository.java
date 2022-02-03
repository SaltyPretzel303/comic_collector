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
        void handleSingleUpload(String docId, int pageIndex, long uploadSize);
    }

    interface DoneHandler {
        void handle(String error);
    }

    void getCreatedComics(String userId, ComicsHandler handler);

    void getCreatedComics(String userId, double lat, double lon, double rad,
                          ComicsHandler handler);

    void getCollectedComics(String userId, ComicsHandler handler);

    void getCollectedComics(String userId, double lat, double lon, double rad,
                            ComicsHandler handler);

    void loadTitlePage(String comicId, int width, int height, PagesHandler handler);

    void loadPage(String comicId, int width, int height, int pageInd, PagesHandler handler);

    void addPages(String comicId,
                  int newCount,
                  List<IndexedUriPage> pageUris,
                  @NotNull UploadHandler handler);

    void createComic(Comic comicInfo,
                     List<IndexedUriPage> pagesUris,
                     @NotNull UploadHandler handler);

    void getUnknownComics(String userId, ComicsHandler handler);

    void getUnknownComics(String userId, double lat, double lon, double rad,
                          ComicsHandler handler);

    void collectComic(String userId, String comicId, DoneHandler handler);

    void getComic(String comicId, ComicsHandler handler);

    void addRating(String comicId, float rating, DoneHandler handler);

}
