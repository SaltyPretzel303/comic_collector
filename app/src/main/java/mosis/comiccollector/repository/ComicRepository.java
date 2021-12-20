package mosis.comiccollector.repository;

import java.util.List;

import mosis.comiccollector.ui.comic.Comic;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public interface ComicRepository {

    // comics specific

    List<Comic> getCollectedComics();

    List<Comic> getDiscoverComics();

    List<Comic> getQueuedComics();

    List<Comic> getMyComics();

    boolean fetchCollectedComics(int index, DataRetrievedHandler handler);

    boolean fetchDiscoverComics(int index, DataRetrievedHandler handler);

    Comic getComicAt(int index);

    Comic getComic(String name, String author);

    void downloadCollectedPaged(int index);

    void clearComicsCache();

}
