package mosis.comiccollector.manager.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;
import mosis.comiccollector.manager.data.tasks.LoadLocalPicTask;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;
import mosis.comiccollector.storage.model.User;

public class FirebaseDataManager implements DataManager {

    private Executor taskExecutor;

    public FirebaseDataManager() {

        this.taskExecutor = Executors.newCachedThreadPool();


    }

    // comics specific

    @Override
    public List<Comic> getCollectedComcis() {
        return null;
    }

    @Override
    public List<Comic> getDiscoverComics() {
        return null;
    }

    @Override
    public List<Comic> getQueuedComics() {
        return null;
    }

    @Override
    public List<Comic> getMyComics() {
        return null;
    }

    @Override
    public boolean fetchCollectedComics(int index, DataRetrievedHandler handler) {
        return false;
    }

    @Override
    public boolean fetchDiscoverComics(int index, DataRetrievedHandler handler) {
        return false;
    }

    @Override
    public Comic getComicAt(int index) {
        return null;
    }

    @Override
    public Comic getComic(String name, String author) {
        return null;
    }

    @Override
    public void downloadCollectedPaged(int index) {

    }

    // user profile page specific

    @Override
    public void saveUserProfilePic(Bitmap pic, JobDoneHandler onJobDone) {

        this.taskExecutor.execute(new SavePicLocally(pic, onJobDone));

    }

    @Override
    public void loadLocalProfilePic(String pic_path, PictureReadyHandler onPicReady) {

        this.taskExecutor.execute(new LoadLocalPicTask(pic_path, onPicReady));

    }

    @Override
    public void uploadProfilePic(Uri pic_uri, final JobDoneHandler onJobDone) {

        Log.w("FirebaseUserManager", "uploadProfilePic: UPLOADING IMAGE ");

        StorageReference storage = FirebaseStorage.getInstance().getReference("profile_pics");

        String name = AppManager.getInstance().getUsersManager().getCurrentUser().getProfilePicName();

        StorageReference child_ref = storage.child(name);
        child_ref.putFile(pic_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                onJobDone.execute("success");

            }
        });

    }


    @Override
    public void downloadProfilePic(String pic_path, final PictureReadyHandler onPicReady) {


        StorageReference reference = FirebaseStorage.getInstance().getReference("profile_pics/" + pic_path);

        try {

            final File temp_file = File.createTempFile("prof_pic", "png");
            reference.getFile(temp_file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Log.e("FirebaseUserManager", "onSuccess: FETCHING DONE ");
                            Bitmap pic = BitmapFactory.decodeFile(temp_file.getPath());
                            Log.e("FirebaseUserManager", "onSuccess: Translated to bitmap ");

                            temp_file.delete();

                            onPicReady.execute(pic);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.e("DOWnLOAD ", "onFailure: MY ERROR , FAILURE IN DOWNLOAD ");

                    onPicReady.execute(null);

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
