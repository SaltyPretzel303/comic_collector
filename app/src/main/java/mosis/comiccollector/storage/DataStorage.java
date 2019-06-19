package mosis.comiccollector.storage;

import android.net.Uri;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public interface DataStorage {

    List<Comic> getCollectedComcis();

    List<Comic> getDiscoverComics();

    List<Comic> getQueuedComics();

    List<Comic> getMyComics();

    boolean fetchCollectedComics(int index, DataRetrievedHandler handler);

    boolean fetchDiscoverComics(int index, DataRetrievedHandler handler);

    Comic getComicAt(int index);

    Comic getComic(String name, String author);

    void downloadCollectedPaged(int index);

    void saveProfilePic(Uri image_uri);

}
