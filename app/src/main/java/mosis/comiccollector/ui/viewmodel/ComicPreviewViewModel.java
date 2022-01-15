package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.util.DepProvider;

public class ComicPreviewViewModel extends AndroidViewModel {

    public class IndexedBitmapPage {
        public int index;
        public MutableLiveData<Bitmap> livePage;

        public boolean isNew;
        public String localUri;

        public IndexedBitmapPage(int index, MutableLiveData<Bitmap> livePage) {
            this.index = index;
            this.livePage = livePage;

            this.isNew = false;
        }

        public IndexedBitmapPage(int index, String localUri) {
            this.index = index;
            this.localUri = localUri;

            this.isNew = true;
        }

    }

    public class UploadProgress {
        public String docId;
        public long progress;

        public UploadProgress(String id, long progress) {
            this.docId = id;
            this.progress = progress;
        }
    }

    private AuthRepository authRepo;
    private ComicRepository comicsRepo;

    private ViewComic currentComic;

    private List<IndexedBitmapPage> pages;

    public ComicPreviewViewModel(@NonNull Application application) {
        super(application);

        this.currentComic = new ViewComic("", "", "", "", null, 0);
        this.pages = new ArrayList<>();

        this.authRepo = DepProvider.getAuthRepository();
        this.comicsRepo = DepProvider.getComicRepository();
    }

    public void setComic(ViewComic comic) {
        this.currentComic = comic;
    }

    public ViewComic getComic() {
        return this.currentComic;
    }

    public int getPagesCount() {
        return this.currentComic.pagesCount;
    }

    public boolean isNewPage(int index) {
        for (IndexedBitmapPage page : this.pages) {
            if (page.index == index) {
                return page.isNew;
            }
        }

        return false;
    }

    public int getNewCount() {
        int counter = 0;
        for (IndexedBitmapPage page : this.pages) {
            if (page.isNew) {
                counter++;
            }
        }
        return counter;
    }

    public IndexedBitmapPage getPage(int pageIndex, int width, int height) {
        Log.e("err", "requesting page: " + pageIndex);

        if (pageIndex >= currentComic.pagesCount) {
            Log.e("err", "We don't have that many pages ... ");
            return null;
        }

        IndexedBitmapPage targetPage = searchLoadedPages(pageIndex);

        if (targetPage == null) {
            MutableLiveData<Bitmap> livePage = new MutableLiveData<>();
            targetPage = new IndexedBitmapPage(pageIndex, livePage);
            this.pages.add(targetPage);

            comicsRepo.loadPage(currentComic.modelId, width, height, pageIndex,
                    (List<Bitmap> newPages) -> {
                        if (newPages == null || newPages.size() == 0) {
                            Log.e("updateViewModel", "Comic repo returned null as page ... ");
                            livePage.postValue(null);
                            return;
                        }

                        livePage.postValue(newPages.get(0));

                        return;
                    });
        }

        return targetPage;
    }

    private IndexedBitmapPage searchLoadedPages(int pageIndex) {
        for (IndexedBitmapPage page : this.pages) {
            if (page.index == pageIndex) {
                return page;
            }
        }

        return null;
    }

    public int addPage(String uri) {
        MutableLiveData<Bitmap> livePage = new MutableLiveData<>();
        IndexedBitmapPage newPage = new IndexedBitmapPage(currentComic.pagesCount, uri);
        newPage.livePage = livePage;
        this.pages.add(newPage);
        currentComic.pagesCount++;

        ImageLoader.getInstance().loadImage(uri, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                livePage.postValue(loadedImage);
            }
        });

        Log.e("err", "Pages count: " + currentComic.pagesCount);
        pages.forEach((page) -> {
            Log.e("err", "page: " + page.index);
        });

        return currentComic.pagesCount - 1;
    }

    public void removePage(int index) {
        pages.removeIf((page) -> {
            return page.index == index;
        });

        pages.forEach((page) -> {
            if (page.index > index) {
                page.index--;
            }
        });

        currentComic.pagesCount--;

        Log.e("err", "Pages count: " + currentComic.pagesCount);
        pages.forEach((page) -> {
            Log.e("err", "page: " + page.index);
        });

    }

    public MutableLiveData<Long> updateComic() {
        MutableLiveData<Long> liveResult = new MutableLiveData<>();

        comicsRepo.addPages(
                currentComic.modelId,
                currentComic.pagesCount,
                this.getNewPages(),
                (String docId, long uploadValue) -> {
                    // mark all added pages as not-new
                    pages.forEach((page) -> page.isNew = false);
                    liveResult.postValue(uploadValue);
                });

        return liveResult;
    }

    public MutableLiveData<UploadProgress> createComic(String title,
                                                       String description,
                                                       Location location) {

        MutableLiveData<UploadProgress> liveUri = new MutableLiveData<>();

        Log.e("updateViewModel", "Creating comic ... ");

        String authorId = authRepo.getCurrentUser().user.getUserId();

        this.currentComic = new ViewComic();
        this.currentComic.title = title;
        this.currentComic.description = description;
        this.currentComic.location = location;
        this.currentComic.pagesCount = this.getNewCount();
        this.currentComic.authorId = authorId;

        Comic newComic = new Comic(
                this.currentComic.authorId,
                this.currentComic.title,
                this.currentComic.description,
                0,
                this.currentComic.pagesCount,
                this.currentComic.location);

        comicsRepo.createComic(
                newComic,
                this.getNewPages(),
                (String docId, long uploadSize) -> {
                    this.currentComic.modelId = docId;
                    // this will be called for each page that gets updated
                    liveUri.postValue(new UploadProgress(docId, uploadSize));
                });

        return liveUri;
    }

    private List<IndexedUriPage> getNewPages() {
        List<IndexedUriPage> uris = new ArrayList<>();
        for (IndexedBitmapPage page : this.pages) {
            if (page.isNew) {
                uris.add(new IndexedUriPage(page.index, page.localUri));
            }
        }
        return uris;
    }


}
