package mosis.comiccollector;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    static private Context app_context;

    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.app_context = getApplicationContext();

    }

    static public Context getAppContext() {
        return MyApplication.app_context;
    }

}
