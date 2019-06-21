package mosis.comiccollector.manager;

import android.content.Context;
import android.content.SharedPreferences;

import mosis.comiccollector.login.FirebaseUsersManager;
import mosis.comiccollector.login.UsersManager;
import mosis.comiccollector.storage.DataStorage;
import mosis.comiccollector.storage.MemoryDataStorage;

import mosis.comiccollector.storage.model.User;

// ATTENTION SINGLETON
public class AppManager {

    private static AppManager instance;

    private DataStorage comic_storage;
    private UsersManager login_manager;

    public static AppManager getInstance() {

        if (AppManager.instance == null) {
            AppManager.instance = new AppManager();
        }

        return AppManager.instance;

    }

    private AppManager() {

        // TODO replace with FirebaseDataStorage, this one is just for testing
        this.comic_storage = new MemoryDataStorage();

        this.login_manager = new FirebaseUsersManager();

    }

    public DataStorage getStorage() {
        return this.comic_storage;
    }

    public UsersManager getUsersManager() {
        return this.login_manager;
    }

    // data storage manager

    // login manager

    // user information

    // maybe position information (map and stuff)

}
