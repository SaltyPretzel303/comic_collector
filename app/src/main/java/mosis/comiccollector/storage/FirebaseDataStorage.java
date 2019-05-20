package mosis.comiccollector.storage;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.comic.Comic;

public class FirebaseDataStorage implements DataStorage {

    private DatabaseReference database;

    private List<Comic> collected_cache;
    private int collected_part_size;

    private List<Comic> discover_cache;
    private int discover_part_size;

    private DataRetrievedHandler data_handler;

    public FirebaseDataStorage() {

        // TODO don't left it hardcoded
        this.collected_part_size = 5;
        this.collected_cache = new ArrayList<>();

        // TODO this too ...
        this.discover_part_size = 5;
        this.discover_cache = new ArrayList<Comic>();

        this.database = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public boolean getCollectedComics(int index, DataRetrievedHandler handler) {

        // TODO fake database access
        if (this.collected_cache.size() < index + this.collected_part_size) {

            this.fetchMoreCollectedComics(index + this.collected_part_size - this.collected_cache.size());

        }
        handler.onListRetrieved(this.collected_cache.subList(index, index + this.collected_part_size));

        return true;
    }

    @Override
    public boolean getDiscoverComics(int index, DataRetrievedHandler handler) {

        if (this.collected_cache.size() < index + this.collected_part_size) {

            this.fetchMoreCollectedComics(index + this.collected_part_size - this.collected_cache.size());

        }
        handler.onListRetrieved(this.collected_cache.subList(index, index + this.collected_part_size));

        return true;
    }

    // TODO JUST FOR TEST, REMOVE
    private void fetchMoreCollectedComics(int count) {

        for (int i = 0; i < count; i++) {

            this.collected_cache.add(new Comic());

        }

    }

    @Override
    public Comic getSingleComic(int index) {
        return this.collected_cache.get(index);
    }

}
