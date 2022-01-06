package mosis.comiccollector.repository.impl;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public class FirebaseComicRepository implements ComicRepository {

    private Executor taskExecutor;

    public FirebaseComicRepository() {

        this.taskExecutor = Executors.newCachedThreadPool();

    }

    // comics specific

    @Override
    public List<ViewComic> getCollectedComics() {
        return null;
    }

    @Override
    public List<ViewComic> getDiscoverComics() {
        return null;
    }

    @Override
    public List<ViewComic> getQueuedComics() {
        return null;
    }

    @Override
    public List<ViewComic> getMyComics() {
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
    public ViewComic getComicAt(int index) {
        return null;
    }

    @Override
    public ViewComic getComic(String name, String author) {
        return null;
    }

    @Override
    public void downloadCollectedPaged(int index) {

    }

    @Override
    public void clearComicsCache() {

    }

}
