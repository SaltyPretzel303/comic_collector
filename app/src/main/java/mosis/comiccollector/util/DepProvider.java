package mosis.comiccollector.util;

import android.os.Handler;
import android.os.Looper;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserAuthResponse;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.impl.FirebaseComicRepository;
import mosis.comiccollector.repository.impl.FirebasePeopleRepository;
import mosis.comiccollector.repository.impl.FirebaseAuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.mapper.AuthUserMapper;
import mosis.comiccollector.ui.viewmodel.mapper.ComicModelMapper;
import mosis.comiccollector.ui.viewmodel.mapper.UserModelMapper;
import mosis.comiccollector.ui.viewmodel.mapper.ViewComicMapper;

// singleton
public class DepProvider {

    private static DepProvider instance;

    private ComicRepository comicStorage;
//    private UserRepository loginManager;

    private Handler handler;

    // singleton specific
    public static DepProvider getInstance() {

        if (DepProvider.instance == null) {
            DepProvider.instance = new DepProvider();
        }

        return DepProvider.instance;

    }

    private DepProvider() {


//        this.comicStorage = new FirebaseComicRepository();

//        this.loginManager = new FirebaseUsersManager();

//        MyApplication.getInstance().registerActivityChangeListener(this.onActivityChange);

        this.handler = new Handler(Looper.getMainLooper());

    }

    public static AuthRepository getAuthRepository() {
        return new FirebaseAuthRepository();
    }

    public static ComicRepository getComicRepository() {
        return new FirebaseComicRepository();
    }

    public static PeopleRepository getPeopleRepository() {
        return new FirebasePeopleRepository();
    }

    public static DataMapper<UserAuthResponse, ViewUser> getAuthUserMapper() {
        return new AuthUserMapper();
    }

    public static DataMapper<User, ViewUser> getUserModelMapper() {
        return new UserModelMapper();
    }

    public static DataMapper<Comic, ViewComic> getComicModelMapper() {
        return new ComicModelMapper();
    }

    public static DataMapper<ViewComic, Comic> getViewComicMapper() {
        return new ViewComicMapper();
    }

}
