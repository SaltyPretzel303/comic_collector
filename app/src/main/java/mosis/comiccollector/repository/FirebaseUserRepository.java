package mosis.comiccollector.repository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mosis.comiccollector.manager.data.tasks.LoadLocalPicTask;
import mosis.comiccollector.manager.data.user.SavePicLocally;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserResponse;
import mosis.comiccollector.model.user.UserResponseType;
import mosis.comiccollector.repository.UserRepository;
import mosis.comiccollector.util.FirebaseTypesMapper;

public class FirebaseUserRepository implements UserRepository {

    private Executor taskExecutor;


    public FirebaseUserRepository() {
        this.taskExecutor = Executors.newCachedThreadPool();
    }


    @Override
    public MutableLiveData<UserResponse> getCurrentUser() {
        MutableLiveData<UserResponse> liveData = new MediatorLiveData<>();

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        UserResponse response = new UserResponse();

        if (fUser == null) {
            response.user = null;
        } else {
            response.user = new User(
                    fUser.getUid(),
                    fUser.getEmail(),
                    fUser.getDisplayName());
        }

        response.responseType = UserResponseType.Success;

        liveData.postValue(response);

        return liveData;
    }

    @Override
    public MutableLiveData<UserResponse> loginWithUsername(String username, String password) {
        MutableLiveData<UserResponse> liveResponse = new MediatorLiveData<>();

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(username, password)
                .addOnCompleteListener((Task<AuthResult> resultTask) -> {

                    UserResponse response = new UserResponse();

                    if (resultTask.isSuccessful()) {

                        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                        response.user = new User(
                                fUser.getUid(),
                                fUser.getEmail(),
                                fUser.getDisplayName());

                        response.responseType = UserResponseType.Success;

                        Log.e("username login", "login response good ... ");
                    } else {

                        response.user = null;
                        response.responseType = FirebaseTypesMapper
                                .getUserResponse(resultTask.getException());

                        Log.e("username login", "failed to login with username: "
                                + resultTask.getException().getMessage() + "\nExc:"
                                + resultTask.getException().getClass().toString());

                    }

                    liveResponse.postValue(response);

                    return;
                });

        return liveResponse;
    }

    @Override
    public MutableLiveData<UserResponse> registerWithUsername(String username, String password) {
        MutableLiveData<UserResponse> liveData = new MediatorLiveData<>();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener((Task<AuthResult> task) -> {
                    UserResponse response = new UserResponse();

                    if (task.isSuccessful()) {
                        response.user = FirebaseTypesMapper.getUser(
                                task.getResult().getUser());
                        response.responseType = UserResponseType.Success;
                    } else {
                        Log.e("failed to register", "exc: " + task.getException().getMessage() + "\n" + task.getException().toString());
                        response.user = null;
                        response.responseType = FirebaseTypesMapper.getUserResponse(
                                task.getException());
                    }

                    liveData.postValue(response);
                });

        return liveData;
    }

    @Override
    public MutableLiveData<UserResponse> logInWithGoogleAuth(AuthCredential credentials) {
        MutableLiveData<UserResponse> liveData = new MediatorLiveData<>();

        FirebaseAuth.getInstance().signInWithCredential(credentials)
                .addOnCompleteListener((Task<AuthResult> task) -> {
                    UserResponse response = new UserResponse();

                    if (task.isSuccessful()) {
                        response.user = FirebaseTypesMapper.getUser(
                                task.getResult().getUser());
                        response.responseType = UserResponseType.Success;
                    } else {
                        Log.e("failed to register", "exc: " + task.getException().getMessage() + "\n" + task.getException().toString());
                        response.user = null;
                        response.responseType = FirebaseTypesMapper.getUserResponse(
                                task.getException());
                    }

                    liveData.postValue(response);
                });

        return liveData;
    }

    @Override
    public MutableLiveData<UserResponse> clearUser() {
        MutableLiveData<UserResponse> liveData = new MediatorLiveData<>();

        FirebaseAuth.getInstance().signOut();

        UserResponse response = new UserResponse();
        response.user = null;
        response.responseType = UserResponseType.Success;

        liveData.postValue(response);

        return liveData;
    }

    // TODO review implementation of bellow method related to profile pic load/save
    @Override
    public void updateUserProfilePic(Uri pic_uri, Bitmap newImage, JobDoneHandler onJobDone) {

    }

    @Override
    public void saveUserProfilePic(Bitmap pic, JobDoneHandler onJobDone) {
        this.taskExecutor.execute(new SavePicLocally(pic, onJobDone));
    }

    @Override
    public void loadLocalProfilePic(String pic_path, PictureReadyHandler onPicReady) {
        this.taskExecutor.execute(new LoadLocalPicTask(pic_path, onPicReady));
    }

    @Override
    public void uploadProfilePic(Uri pic_uri, JobDoneHandler onJobDone) {
        Log.w("FirebaseUserManager", "uploadProfilePic: UPLOADING IMAGE ");

        StorageReference storage = FirebaseStorage.getInstance().getReference("profile_pics");

//        String name = DepProvider.getUserRepository().getCurrentUser().getValue().getProfilePicName();

//        StorageReference child_ref = storage.child(name);
//        child_ref.putFile(pic_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                onJobDone.execute("success");
//
//            }
//        });
    }

    @Override
    public void downloadProfilePic(String pic_path, PictureReadyHandler onPicReady) {
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
