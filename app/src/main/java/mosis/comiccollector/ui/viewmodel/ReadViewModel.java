package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DoneHandler;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.RatingsRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.util.DepProvider;

public class ReadViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private PeopleRepository peopleRepo;
    private ComicRepository comicsRepo;

    private RatingsRepository ratingRepo;

    private List<IndexedBitmapPage> pages;
    private ViewComic comic;

    public ReadViewModel(@NonNull Application application) {
        super(application);

        this.pages = new ArrayList<>();
        this.authRepo = DepProvider.getAuthRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
        this.comicsRepo = DepProvider.getComicRepository();
        this.ratingRepo = DepProvider.getRatingRepository();
    }

    public void setComic(ViewComic newComic) {
        this.comic = newComic;
    }

    public IndexedBitmapPage getPage(int index, int width, int height) {
        if (index > comic.pagesCount) {
            Log.e("readViewModel", "Requesting page out of bound ... ");
            return null;
        }

        IndexedBitmapPage retPage = searchLoadedPages(index);

        if (retPage == null) {
            MutableLiveData<Bitmap> livePage = new MutableLiveData<>();
            retPage = new IndexedBitmapPage(index, livePage);
            this.pages.add(retPage);

            comicsRepo.loadPage(comic.comicId, width, height, index, (newPages) -> {
                if (newPages == null || newPages.size() == 0) {
                    Log.e("readViewModel", "Failed to load page ... ind: " + index);
                    livePage.postValue(null);
                    return;
                }

                livePage.postValue(newPages.get(0));

                return;
            });

        }

        return retPage;

    }

    private IndexedBitmapPage searchLoadedPages(int index) {
        return pages.stream()
            .filter((page) -> page.index == index)
            .findFirst()
            .orElse(null);
    }

    public int getPagesCount() {
        return comic.pagesCount;
    }

    public LiveData<Void> addRating(float comicRating, float authorRating) {
        var retData = new MutableLiveData<Void>();

        ratingRepo.addRating(
            authRepo.getCurrentUser().user.getUserId(),
            comic.authorId,
            authorRating,
            comic.comicId,
            comicRating,
            (err) -> {

                // TODO I guess this is gonna be used just to hide loading screen
                // with void I kinda can't see if there was an error
                retData.postValue(null);
                Log.e("readViewModel", "New rating added ... ");

            });

        return retData;
    }

    public boolean isMine() {
        return authRepo.getCurrentUser().user.getUserId().equals(comic.authorId);
    }

    public LiveData<Boolean> shouldRate() {
        var liveData = new MutableLiveData<Boolean>();

        if (isMine()) {
            Log.e("readViewModel", "Comic is mine ... ");
            liveData.postValue(false);
        } else {
            Log.e("readViewModel", "Comic is NOT mine, checking prev. rating ... ");

            var myId = authRepo.getCurrentUser().user.getUserId();

            ratingRepo.getRating(myId, comic.comicId, (value) -> {
                Log.e("readViewModel", "Got " + (value != null) + " from repo ... ");

                liveData.postValue(value == null);
            });

        }

        return liveData;
    }

}
