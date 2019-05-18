package mosis.comiccollector.storage;

import java.util.List;

import mosis.comiccollector.comic.Comic;

public interface DataRetrievedHandler {

    void execute(List<Comic> retrieved_data);

}
