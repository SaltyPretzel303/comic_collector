package mosis.comiccollector.manager.data;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.storage.model.User;

public class SavePicLocally implements Runnable {

    private JobDoneHandler onJobDone;

    private Bitmap pic;

    public SavePicLocally(Bitmap pic, JobDoneHandler onJobDone) {
        this.onJobDone = onJobDone;
        this.pic = pic;
    }

    @Override
    public void run() {

        Log.e("FirebaseUserManager", "saveUserProfilePic: SAVING PIC TO LOCAL STORAGE");

        User current_user = AppManager.getInstance().getUsersManager().getCurrentUser();

        String pic_name = current_user.getLocalProfilePicName();
        File path = MyApplication.getInstance().getActivityContext().getFilesDir();
        File picFile = new File(path, pic_name);

        if (!picFile.exists()) {
            Log.e("FireabaseUserManger", "saveUserProfilePic: Pic file does NOT exists, creating new ");

            try {
                picFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.e("FireabaseUserManager", "saveUserProfilePic: File already exists ");
        }

        try {

            FileOutputStream fos = new FileOutputStream(picFile);
            pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.e("FirebaseUserManager", "saveUserProfilePic: write done");

            fos.close();

            onJobDone.execute("success");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
