package mosis.comiccollector;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// SINGLETON
public class MyApplication extends Application {

    static private MyApplication instance;

    private Context activity_context;

    private List<ActivityChangeListener> listeners;

    static public MyApplication getInstance() {

        if (MyApplication.instance == null) {
            Log.w("MyApplication", "getInstance: First MyApplication instance request... ");

            MyApplication.instance = new MyApplication();

        }

        return MyApplication.instance;

    }

    // constructors

    public MyApplication() {

        this.listeners = new ArrayList<>();

    }

    // methods

    public Context getActivityContext() {
        return this.activity_context;
    }

    public void registerActivityContext(Context new_context) {

        // e.g. called in onCreate and onStart from the same activity
        if (this.activity_context == new_context)
            return;

        this.activity_context = new_context;

        for (ActivityChangeListener listener : this.listeners) {

            Log.e("MyApplication", "registerActivityContext: Activity change notify ... ");

            listener.notify(this.activity_context);

        }

    }

    public void registerActivityChangeListener(ActivityChangeListener listener) {

        if (!this.listeners.contains(listener)) {

            this.listeners.add(listener);

        } else {

            Log.e("MyApplication", "registerActivityChangeListener: This listener is already registered ... ");

        }

    }

    public void removeActivityChangeListener(ActivityChangeListener listener) {

        if (this.listeners.contains(listener)) {

            this.listeners.remove(listener);

        } else {

            Log.e("MyApplication", "removeActivityChangeListener: This listener is not registered ... ");

        }

    }


}
