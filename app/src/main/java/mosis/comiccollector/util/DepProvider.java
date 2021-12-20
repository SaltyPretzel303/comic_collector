package mosis.comiccollector.util;

import android.os.Handler;
import android.os.Looper;

import mosis.comiccollector.repository.FirebaseComicRepository;
import mosis.comiccollector.repository.FirebaseUserRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.UserRepository;

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

    public static UserRepository getUserRepository() {
        return new FirebaseUserRepository();
    }

    public static ComicRepository getComicRepository() {
        return new FirebaseComicRepository();
    }
}
