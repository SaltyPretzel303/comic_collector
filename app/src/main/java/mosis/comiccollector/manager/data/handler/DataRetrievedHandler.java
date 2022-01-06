package mosis.comiccollector.manager.data.handler;

import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;

public interface DataRetrievedHandler {

    void onListRetrieved(List<ViewComic> retrieved_data);

    void onComicRetrieved(int index, ViewComic comic);

}
