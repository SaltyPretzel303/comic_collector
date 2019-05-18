package mosis.comiccollector.manager;

import android.content.Context;

// SINGLETON
public class AppManager {

    // TODO REMOVE
    public Context context;

    private static AppManager instance;

    public static AppManager getInstance() {

        if (AppManager.instance == null) {
            AppManager.instance = new AppManager();
        }

        return AppManager.instance;

    }

    private AppManager() {


    }

    // data storage

    // user information

    // maybe position information (map and stuff)

}
