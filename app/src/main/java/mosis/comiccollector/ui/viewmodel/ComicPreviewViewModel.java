package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.ui.comic.IndexedUriPage;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class ComicPreviewViewModel extends AndroidViewModel {

    public class UploadProgress {
        public String docId;
        public long progress;

        public UploadProgress(String id, long progress) {
            this.docId = id;
            this.progress = progress;
        }
    }

    private final AuthRepository authRepo;
    private final PeopleRepository peopleRepo;
    private final ComicRepository comicsRepo;

    private ViewComic currentComic;

    private DataMapper<User, ViewUser> userMapper;

    private final List<IndexedBitmapPage> pages;

    public ComicPreviewViewModel(@NonNull Application application) {
        super(application);

        this.currentComic = new ViewComic("", "", "", "", null, 0, 0);
        this.pages = new ArrayList<>();

        this.authRepo = DepProvider.getAuthRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
        this.comicsRepo = DepProvider.getComicRepository();

        this.userMapper = DepProvider.getUserModelMapper();
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

            comicsRepo.loadPage(currentComic.comicId, width, height, pageIndex,
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
        pages.add(newPage);
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
            currentComic.comicId,
            currentComic.pagesCount,
            getNewPages(),
            (String docId, int pageIndex, long uploadValue) -> {
                markAsOld(pageIndex);
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

        Comic newComic = DepProvider.getViewComicMapper().mapThis(currentComic);
//        Comic newComic = new Comic(
//                authorId,
//                this.currentComic.title,
//                this.currentComic.description,
//                this.currentComic.rating,
//                this.currentComic.pagesCount,
//                this.currentComic.location);

        comicsRepo.createComic(
            newComic,
            this.getNewPages(),
            (String docId, int pageIndex, long uploadSize) -> {
                this.currentComic.comicId = docId;
                markAsOld(pageIndex);

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

    public MutableLiveData<ViewUser> getAuthor() {
        var liveResult = new MutableLiveData<ViewUser>();

        peopleRepo.getUser(currentComic.authorId, (people) -> {
            if (people == null || people.size() == 0) {
                Log.e("previewViewModel", "Failed to load comic author ... ");
                liveResult.postValue(null);

                return;
            }

            var viewUser = userMapper.mapThis(people.get(0));

            viewUser.liveProfilePic = new MutableLiveData<>();
            peopleRepo.loadProfilePic(viewUser.userId, viewUser.liveProfilePic::postValue);

            liveResult.postValue(viewUser);
        });

        return liveResult;
    }

    private void markAsOld(int index) {
        for (var page : pages) {
            if (page.isNew && page.index == index) {
                page.isNew = false;
                return;
            }

        }

    }

    public LiveData<Float> getRating() {
        var liveData = new MutableLiveData<Float>();
        comicsRepo.getRating(currentComic.comicId, liveData::postValue);
        return liveData;
    }


}
