package mosis.comiccollector.manager;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import mosis.comiccollector.ActivityChangeListener;
import mosis.comiccollector.MyApplication;
import mosis.comiccollector.activity.LoadingScreen;
import mosis.comiccollector.manager.data.FirebaseDataManager;
import mosis.comiccollector.manager.data.MemoryDataManager;
import mosis.comiccollector.manager.user.FirebaseUsersManager;
import mosis.comiccollector.manager.user.UsersManager;
import mosis.comiccollector.manager.data.DataManager;

// ATTENTION SINGLETON
public class AppManager {

    private static AppManager instance;

    private DataManager comic_storage;
    private UsersManager login_manager;

    private Dialog loadingScreen;

    private Handler handler;

    // singleton specific
    public static AppManager getInstance() {

        if (AppManager.instance == null) {
            AppManager.instance = new AppManager();
        }

        return AppManager.instance;

    }

    private AppManager() {

        // TODO replace with FirebaseDataManager, this one is just for testing
//        this.comic_storage = new MemoryDataManager();
        this.comic_storage = new FirebaseDataManager();
//            this.comic_storage=new MemoryDataManager();

        this.login_manager = new FirebaseUsersManager();

        MyApplication.getInstance().registerActivityChangeListener(this.onActivityChange);

        this.createLoadingScreen();

        this.handler = new Handler(Looper.getMainLooper());

    }

    public DataManager getStorage() {
        return this.comic_storage;
    }

    public UsersManager getUsersManager() {
        return this.login_manager;
    }

    private ActivityChangeListener onActivityChange = new ActivityChangeListener() {
        @Override
        public void notify(Context current_context) {
            createLoadingScreen();
        }
    };

    private void createLoadingScreen() {

        if (this.loadingScreen != null && this.loadingScreen.isShowing()) {
            this.loadingScreen.cancel();
        }

        this.loadingScreen = new LoadingScreen(MyApplication.getInstance().getActivityContext());

    }

    public void showLoadingScreen() {

        this.handler.post(new Runnable() {
            @Override
            public void run() {

                loadingScreen.show();

            }
        });

    }

    public void hideLoadingScreen() {


        this.handler.post(new Runnable() {
            @Override
            public void run() {

                loadingScreen.hide();

            }
        });

    }

    public void makeToast(final String message) {

        this.handler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MyApplication.getInstance().getActivityContext(), message, Toast.LENGTH_SHORT).show();

            }
        });

    }

    // data storage manager

    // login manager

    // user information

    // maybe position information (map and stuff)

}
