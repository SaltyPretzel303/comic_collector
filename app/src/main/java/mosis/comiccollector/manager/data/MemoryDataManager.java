package mosis.comiccollector.manager.data;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public class MemoryDataManager implements ComicRepository {

    private List<ViewComic> collected_cache;
    private int collected_part_size; // how many items to load with one 'load more' request

    private List<ViewComic> discover_cache;
    private int discover_part_size; // how many items to load with one 'load more' request

    // lazy load from local storage
    private List<ViewComic> queued_cache;

    // lazy load from local storage
    private List<ViewComic> my_comics;

    private DataRetrievedHandler data_handler;

    private StorageReference storage_ref;

    public MemoryDataManager() {

        // TODO don't left it hardcoded
        this.collected_part_size = 5;
        this.collected_cache = new ArrayList<ViewComic>();

        // TODO same as above...
        this.discover_part_size = 5;
        this.discover_cache = new ArrayList<ViewComic>();

        this.storage_ref = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public List<ViewComic> getCollectedComics() {
        return this.collected_cache;
    }

    @Override
    public List<ViewComic> getDiscoverComics() {
        return this.discover_cache;
    }

    @Override
    public List<ViewComic> getQueuedComics() {

        if (this.queued_cache == null) {
            this.loadQueuedComics();
        }

        return queued_cache;
    }

    // TODO implement
    private void loadQueuedComics() {
        // load queued comics from local storage
    }

    @Override
    public List<ViewComic> getMyComics() {

        if (this.my_comics == null) {
            this.loadMyComics();
        }

        return this.my_comics;
    }

    // TODO implement
    private void loadMyComics() {
        // load my comics from local storage
    }

    @Override
    public boolean fetchCollectedComics(int index, DataRetrievedHandler handler) {

        // TODO fake database access
        if (this.collected_cache.size() < index + this.collected_part_size) {

            this.fetchMoreCollectedComics(index + this.collected_part_size - this.collected_cache.size());

        }
        handler.onListRetrieved(this.collected_cache.subList(index, index + this.collected_part_size));

        return true;
    }

    @Override
    public boolean fetchDiscoverComics(int index, DataRetrievedHandler handler) {

        if (this.collected_cache.size() < index + this.collected_part_size) {

            this.fetchMoreCollectedComics(index + this.collected_part_size - this.collected_cache.size());

        }
        handler.onListRetrieved(this.collected_cache.subList(index, index + this.collected_part_size));

        return true;
    }

    // TODO JUST FOR TEST, REMOVE
    private void fetchMoreCollectedComics(int count) {

        for (int i = 0; i < count; i++) {

            this.collected_cache.add(new ViewComic());

        }

    }

    @Override
    public ViewComic getComicAt(int index) {
        return this.collected_cache.get(index);
    }

    @Override
    public ViewComic getComic(String title, String author) {

        for (ViewComic selected : this.collected_cache) {
            if (selected.title.equals(title) && selected.author.equals(author)) {
                return selected;
            }
        }

        return null;
    }

    // TODO implement
    @Override
    public void downloadCollectedPaged(int index) {

    }

    @Override
    public void clearComicsCache() {

    }


}
