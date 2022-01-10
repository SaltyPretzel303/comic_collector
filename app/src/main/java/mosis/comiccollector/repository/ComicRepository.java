package mosis.comiccollector.repository;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.ui.comic.ViewComic;

public interface ComicRepository {

    interface ComicsHandler {
        void handleComics(List<Comic> newComics);
    }

    interface PagesHandler {
        void handlePages(List<Bitmap> newPages);
    }

    interface UploadHandler {
        void handleResult(long uploadSize);
    }

    void getCreatedComics(String userId, ComicsHandler handler);

    void getCollectedComics(String userId, ComicsHandler handler);

    void loadTitlePage(String comicId, PagesHandler handler);

    void loadPage(String comicId, int pageInd, PagesHandler handler);

    void addPages(String comicId,
                  int newCount,
                  List<String> pageUris,
                  @NotNull UploadHandler handler);

    void createComic(Comic comicInfo,
                     List<String> pagesUris,
                     @NotNull UploadHandler handler);

}
