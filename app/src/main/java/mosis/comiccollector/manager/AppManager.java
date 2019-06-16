package mosis.comiccollector.manager;

import android.content.Context;

import mosis.comiccollector.login.FakeLoginManager;
import mosis.comiccollector.login.LoginManager;
import mosis.comiccollector.storage.DataStorage;
import mosis.comiccollector.storage.MemoryDataStorage;

// ATTENTION SINGLETON
public class AppManager {

    // TODO REMOVE; just for testing
    // used in no-argument Comic constructor
    public Context context;

    private static AppManager instance;

    private DataStorage comic_storage;
    private LoginManager login_manager;

    public static AppManager getInstance() {

        if (AppManager.instance == null) {
            AppManager.instance = new AppManager();
        }

        return AppManager.instance;

    }

    private AppManager() {

        // TODO replace with FirebaseDataStorage, this one is just for testing
        this.comic_storage = new MemoryDataStorage();

        // TODO replace with FirebaseDataStorage
        this.login_manager = new FakeLoginManager();

    }

    public DataStorage getStorage() {
        return this.comic_storage;
    }

    public LoginManager getLoginManager() {
        return this.login_manager;
    }

    // data storage

    // user information

    // maybe position information (map and stuff)

}
