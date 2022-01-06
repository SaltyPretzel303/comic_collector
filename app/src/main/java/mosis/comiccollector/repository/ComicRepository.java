package mosis.comiccollector.repository;

import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public interface ComicRepository {

    // comics specific

    List<ViewComic> getCollectedComics();

    List<ViewComic> getDiscoverComics();

    List<ViewComic> getQueuedComics();

    List<ViewComic> getMyComics();

    boolean fetchCollectedComics(int index, DataRetrievedHandler handler);

    boolean fetchDiscoverComics(int index, DataRetrievedHandler handler);

    ViewComic getComicAt(int index);

    ViewComic getComic(String name, String author);

    void downloadCollectedPaged(int index);

    void clearComicsCache();

}
