package mosis.comiccollector.manager;

import android.content.Context;

import mosis.comiccollector.storage.DataStorage;
import mosis.comiccollector.storage.FirebaseDataStorage;

// SINGLETON
public class AppManager {

    // TODO REMOVE
    public Context context;

    private static AppManager instance;

    private DataStorage comic_storage;

    public static AppManager getInstance() {

        if (AppManager.instance == null) {
            AppManager.instance = new AppManager();
        }

        return AppManager.instance;

    }

    private AppManager() {

        this.comic_storage = new FirebaseDataStorage();

    }

    public DataStorage getStorage() {
        return this.comic_storage;
    }

    // data storage

    // user information

    // maybe position information (map and stuff)

}
