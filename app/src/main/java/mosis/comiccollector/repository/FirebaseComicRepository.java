package mosis.comiccollector.repository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mosis.comiccollector.ui.comic.Comic;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public class FirebaseComicRepository implements ComicRepository {

    private Executor taskExecutor;

    public FirebaseComicRepository() {

        this.taskExecutor = Executors.newCachedThreadPool();

    }

    // comics specific

    @Override
    public List<Comic> getCollectedComics() {
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
    public void clearComicsCache() {

    }

}
