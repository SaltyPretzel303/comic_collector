package mosis.comiccollector.ui.comic;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public interface ComicsLoader {

    MutableLiveData<List<ViewComic>> getCreatedComics(String userId);

    MutableLiveData<List<ViewComic>> getCollectedComics(String userId);

}
