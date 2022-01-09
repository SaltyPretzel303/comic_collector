package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class UserProfileViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private ComicRepository comicRepo;
    private PeopleRepository peopleRepo;

    private MutableLiveData<ViewUser> liveUserData;
    private MutableLiveData<Bitmap> liveProfilePic;

    private MutableLiveData<List<ViewComic>> liveCreatedComics;
    private MutableLiveData<List<ViewComic>> liveCollectedComics;
    private MutableLiveData<List<ViewUser>> liveFriends;

    private DataMapper<User, ViewUser> userMapper;
    private DataMapper<Comic, ViewComic> comicMapper;

    public UserProfileViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.comicRepo = DepProvider.getComicRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();

        this.userMapper = DepProvider.getUserModelMapper();
        this.comicMapper = DepProvider.getComicModelMapper();

    }

    public String getMyId() {
        return authRepo.getCurrentUser().user.getUserId();
    }

    public MutableLiveData<ViewUser> getUser(String userId) {
        if (this.liveUserData == null) {
            this.liveUserData = new MutableLiveData<>();
        }

        this.peopleRepo.getUser(userId, (people) -> {
            // I guess people arg can't be null
            // where did I got id if that user doesn't exists ...
            User user = people.get(0);
            ViewUser viewUser = this.userMapper.mapToViewModel(user);
            viewUser.setLiveProfilePic(this.loadProfilePic(userId));

            this.liveUserData.postValue(viewUser);
        });

        return this.liveUserData;
    }

    public MutableLiveData<String> saveProfilePic(String picUri) {
        MutableLiveData<String> uploadUri = new MutableLiveData<>();

        // I can upload picture only for myself
        String userId = this.getMyId();

        this.peopleRepo.uploadProfilePic(userId, picUri, (String uri) -> {

            peopleRepo.updatePicUri(userId, uri, (retUri) -> {
                // TODO maybe do something ...
            });
            authRepo.updatePicUri(uri);

            uploadUri.postValue(uri);
        });

        return uploadUri;
    }

    private MutableLiveData<Bitmap> loadProfilePic(String userId) {

        MutableLiveData<Bitmap> livePic = new MutableLiveData<>();
//        if (this.liveProfilePic == null) {
//            this.liveProfilePic = new MutableLiveData<>();
//        }

        this.peopleRepo.loadProfilePic(userId, (Bitmap picUri) -> {
//            liveProfilePic.postValue(picUri);
            livePic.postValue(picUri);
        });

//        return this.liveProfilePic;
        return livePic;
    }

    public MutableLiveData<List<ViewComic>> loadCreatedComics(String userId) {
        if (this.liveCreatedComics == null) {
            this.liveCreatedComics = new MutableLiveData<>();
        }

        this.comicRepo.getCreatedComics(userId, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("viewModelProfile", "Failed to load created comics for: " + userId);
                liveCreatedComics.postValue(null);
                return;
            }

            List<ViewComic> vComicL = new ArrayList<>();
            for (Comic comic : newComics) {
                ViewComic vComic = comicMapper.mapToViewModel(comic);
                vComic.liveTitlePageUri = loadTitlePage(comic.getId());

                vComicL.add(vComic);
            }

            liveCreatedComics.postValue(vComicL);
        });

        return this.liveCreatedComics;
    }

    private MutableLiveData<String> loadTitlePage(String comicId) {
        MutableLiveData<String> liveUri = new MutableLiveData<>();

        comicRepo.loadTitlePage(comicId, (List<String> newPages) -> {
            if (newPages == null) {
                Log.e("viewModelProfile", "Failed to load titlePage for: " + comicId);
                liveUri.postValue(null);

                return;
            }

            liveUri.postValue(newPages.get(0));

        });

        return liveUri;
    }

    public MutableLiveData<List<ViewComic>> loadCollectedComics(String userId) {
        if (this.liveCollectedComics == null) {
            this.liveCollectedComics = new MutableLiveData<>();
        }

        this.comicRepo.getCollectedComics(userId, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("viewModelProfile", "Failed to load collected comics ... ");
                liveCollectedComics.postValue(null);
                return;
            }

            List<ViewComic> vComics = new ArrayList<>();
            for (Comic comic : newComics) {
                ViewComic vComic = this.comicMapper.mapToViewModel(comic);
                vComic.liveTitlePageUri = loadTitlePage(userId);

                vComics.add(vComic);
            }

            this.liveCollectedComics.postValue(vComics);
        });

        return this.liveCollectedComics;
    }

    public MutableLiveData<List<ViewUser>> loadFriends(String userId) {
        MutableLiveData<List<ViewUser>> liveFriends = new MutableLiveData<>();

        peopleRepo.getFriends(userId, (List<User> people) -> {
            List<ViewUser> vPeople = new ArrayList<>();
            for (User uPeople : people) {
                ViewUser vUser = userMapper.mapToViewModel(uPeople);
                vUser.setLiveProfilePic(this.loadProfilePic(userId));
                vPeople.add(vUser);
            }
            liveFriends.postValue(vPeople);
        });

        return liveFriends;
    }

}
