package mosis.comiccollector.storage;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public interface DataStorage {

    // OK
    boolean getCollectedComics(int index, DataRetrievedHandler handler);

    // OK
    boolean getDiscoverComics(int index, DataRetrievedHandler handler);

    Comic getSingleComic(int index);

}
