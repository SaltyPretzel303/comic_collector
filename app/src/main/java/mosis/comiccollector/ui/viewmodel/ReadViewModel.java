package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.util.DepProvider;

public class ReadViewModel extends AndroidViewModel {

    private ComicRepository comicsRepo;

    private List<IndexedBitmapPage> pages;
    private ViewComic comic;

    public ReadViewModel(@NonNull Application application) {
        super(application);

        this.pages = new ArrayList<>();
        this.comicsRepo = DepProvider.getComicRepository();
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

}
