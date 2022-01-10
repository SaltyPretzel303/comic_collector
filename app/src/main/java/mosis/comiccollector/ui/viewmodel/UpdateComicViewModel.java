package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.compose.ui.semantics.LiveRegionMode;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.DoubleSummaryStatistics;
import java.util.List;

import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.util.DepProvider;

public class UpdateComicViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private ComicRepository comicsRepo;

    private MutableLiveData<List<Bitmap>> pages;

    public UpdateComicViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.comicsRepo = DepProvider.getComicRepository();
    }

    public MutableLiveData<List<Bitmap>> getPages() {

        return null;
    }

    public MutableLiveData<Long> updateComic(String comicId, int totalCnt, List<String> pagesUris) {
        MutableLiveData<Long> liveResult = new MutableLiveData<>();

        comicsRepo.addPages(comicId, totalCnt, pagesUris, liveResult::postValue);

        return liveResult;
    }

    public MutableLiveData<Long> createComic(String title,
                                             String description,
                                             Location location,
                                             List<String> pages) {
        MutableLiveData<Long> liveUri = new MutableLiveData<>();

        Log.e("updateViewModel","Creating comic ... ");

        String authorId = authRepo.getCurrentUser().user.getUserId();
        Comic newComic = new Comic(
                authorId,
                title,
                description,
                0,
                pages.size(),
                location);

        comicsRepo.createComic(newComic, pages, (long uploadSize) -> {
            // this will be called for each page that gets updated
            liveUri.postValue(uploadSize);
        });

        return liveUri;
    }

}
