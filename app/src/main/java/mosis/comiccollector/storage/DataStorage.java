package mosis.comiccollector.storage;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public interface DataStorage {

    List<Comic> getAllComicsPreview();

    List<Comic> getMoreComicsPreview();

    Comic getComic(int index);

    void registerDataRetrieveHandler(DataRetrievedHandler handler);

}
