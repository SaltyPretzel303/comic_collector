package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

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
    private MutableLiveData<String> liveProfilePic;

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

    public MutableLiveData<ViewUser> loadUser(String userId) {
        if (this.liveUserData == null) {
            this.liveUserData = new MutableLiveData<>();
        }

        this.peopleRepo.getUser(userId, (people) -> {
            // I guess people arg can't be null
            // where did i got id if that user doesn't exists ...
            User user = people.get(0);
            ViewUser viewUser = this.userMapper.mapToViewModel(user);
            viewUser.setLivePicUri(this.loadProfilePic(userId));

            this.liveUserData.postValue(viewUser);
        });

        return this.liveUserData;
    }

    public MutableLiveData<String> saveProfilePic(String picUri) {
        if (this.liveProfilePic == null) {
            this.liveProfilePic = new MutableLiveData<>();
        }

        // I can upload picture only for myself
        String userId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.uploadProfilePic(
                userId,
                picUri,
                (String uri) -> {

                    peopleRepo.updatePicUri(userId, uri, (retUri) -> {
                        // TODO maybe do something ...
                    });
                    authRepo.updatePicUri(uri);

//                    liveProfilePic.postValue(uri);
                    // this can't be done
                    // returned uri is firebaseDownlaodUri
                    // imageView wont't be able to display that
                    // imageView require local file uri which is crated in repo.LoadProfilePic
                });

        return this.liveProfilePic;
    }

    public MutableLiveData<String> loadProfilePic(String userId) {

        if (this.liveProfilePic == null) {
            this.liveProfilePic = new MutableLiveData<>();
        }

        this.peopleRepo.loadProfilePic(userId, (String picUri) -> {
            liveProfilePic.postValue(picUri);
        });

        return this.liveProfilePic;
    }

    public MutableLiveData<List<ViewComic>> loadCreatedComics(String userId) {
        MutableLiveData<List<ViewComic>> liveComics = new MutableLiveData<>();

        this.comicRepo.getCreatedComics(userId, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("viewModelProfile", "Failed to load created comics for: " + userId);
                liveComics.postValue(null);
                return;
            }

            List<ViewComic> vComicL = new ArrayList<>();
            for (Comic comic : newComics) {
                ViewComic vComic = comicMapper.mapToViewModel(comic);
                vComic.titlePageUri = loadTitlePage(comic.getId());

                vComicL.add(vComic);
            }

            liveComics.postValue(vComicL);
        });

        return liveComics;
    }

    public MutableLiveData<String> loadTitlePage(String comicId) {
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

    public void loadCollectedComics(String userId) {

    }

    public MutableLiveData<List<ViewUser>> loadFriends(String userId) {
        MutableLiveData<List<ViewUser>> liveFriends = new MutableLiveData<>();

        peopleRepo.getFriends(userId, (List<User> people) -> {
            List<ViewUser> vPeople = new ArrayList<>();
            for (User uPeople : people) {
                ViewUser vUser = userMapper.mapToViewModel(uPeople);
                vUser.setLivePicUri(this.loadProfilePic(userId));
                vPeople.add(vUser);
            }
            liveFriends.postValue(vPeople);
        });

        return liveFriends;
    }

}
