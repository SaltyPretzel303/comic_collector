package mosis.comiccollector.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import mosis.comiccollector.ui.LoadingScreenDialog;

public class LoadingScreenManager {

    private static LoadingScreenDialog loadingScreen;

    // TODO extract to strings
    private static final String DEFAULT_MESSAGE = "Loading ... ";

    private static void createLoadingScreen(Context newContext) {

        if (loadingScreen != null && loadingScreen.isShowing()) {
            loadingScreen.cancel();
        }

        loadingScreen = new LoadingScreenDialog(newContext);
    }

    // TODO add text message under the loading screen
    // e.g. checking user
    // loading comics
    // please wait ... etc
    public static void ShowLoadingScreen(Context context, String message) {

        if (loadingScreen == null) {
            createLoadingScreen(context);
        }

        Log.e("lsManager", "Gonna show loading with: " + message);
        new Handler(Looper.getMainLooper()).post(() -> {
            loadingScreen.showDialog(message);
        });
    }

    public static void ShowLoadingScreen(Context context) {
        ShowLoadingScreen(context, DEFAULT_MESSAGE);
    }

    public static void HideLoadingScreen() {
        if (loadingScreen == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (loadingScreen == null) {
                return;
            }
            loadingScreen.hideDialog();
            loadingScreen.cancel();
            loadingScreen = null;
        });
    }

}

