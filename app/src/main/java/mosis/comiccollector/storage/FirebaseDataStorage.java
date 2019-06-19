package mosis.comiccollector.storage;

import android.net.Uri;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public class FirebaseDataStorage implements DataStorage {
    @Override
    public List<Comic> getCollectedComcis() {
        return null;
    }

    @Override
    public List<Comic> getDiscoverComics() {
        return null;
    }

    @Override
    public List<Comic> getQueuedComics() {
        return null;
    }

    @Override
    public List<Comic> getMyComics() {
        return null;
    }

    @Override
    public boolean fetchCollectedComics(int index, DataRetrievedHandler handler) {
        return false;
    }

    @Override
    public boolean fetchDiscoverComics(int index, DataRetrievedHandler handler) {
        return false;
    }

    @Override
    public Comic getComicAt(int index) {
        return null;
    }

    @Override
    public Comic getComic(String name, String author) {
        return null;
    }

    @Override
    public void downloadCollectedPaged(int index) {

    }

    @Override
    public void saveProfilePic(Uri image_uri) {

    }
}
