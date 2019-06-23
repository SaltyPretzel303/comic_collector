package mosis.comiccollector.manager.data.handler;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public interface DataRetrievedHandler {

    void onListRetrieved(List<Comic> retrieved_data);

    void onComicRetrieved(int index, Comic comic);

}
