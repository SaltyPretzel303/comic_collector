package mosis.comiccollector.manager.data.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;

public class LoadLocalPicTask implements Runnable {

    private PictureReadyHandler onPictureReady;
    private String pic_path;

    public LoadLocalPicTask(String pic_path, PictureReadyHandler onPictureReady) {

        this.onPictureReady = onPictureReady;

        this.pic_path = pic_path;

    }

    @Override
    public void run() {


        File path = MyApplication.getInstance().getActivityContext().getFilesDir();
        File pic = new File(path, pic_path);

        if (pic.exists()) {

            Log.w("LoadLocalPic", "loadLocalProfilePic: Local pic exists ...");

            Bitmap bitmap = BitmapFactory.decodeFile(pic.getPath());

            this.onPictureReady.execute(bitmap);

        } else {

            Log.e("FirebaseDataManager", "loacLocalProfilePic: error in local pic loading ");
            this.onPictureReady.execute(null);
        }

    }


}
