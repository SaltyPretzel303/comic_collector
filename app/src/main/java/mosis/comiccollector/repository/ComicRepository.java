package mosis.comiccollector.repository;

import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.ui.comic.ViewComic;

public interface ComicRepository {

    interface ComicsHandler {
        void handleComics(List<Comic> newComics);
    }

    interface PagesHandler {
        void handlePages(List<String> newPages);
    }

    void getCreatedComics(String userId, ComicsHandler handler);

    void getCollectedComics(String userId, ComicsHandler handler);

    void loadTitlePage(String comicId, PagesHandler handler);

    void loadPage(String comicId, int pageInd, PagesHandler handler);

}
