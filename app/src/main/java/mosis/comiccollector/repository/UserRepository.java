package mosis.comiccollector.repository;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.AuthCredential;

import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;
import mosis.comiccollector.model.user.UserResponse;

public interface UserRepository {


    MutableLiveData<UserResponse> getCurrentUser();

    MutableLiveData<UserResponse> loginWithUsername(String username, String password);

    MutableLiveData<UserResponse> registerWithUsername(String username, String password);

    MutableLiveData<UserResponse> logInWithGoogleAuth(AuthCredential credentials);

    MutableLiveData<UserResponse> clearUser();

    void updateUserProfilePic(Uri pic_uri, Bitmap newImage, JobDoneHandler onJobDone);

    void saveUserProfilePic(Bitmap pic, JobDoneHandler onJobDone);

    void loadLocalProfilePic(String pic_path, PictureReadyHandler onPicReady);

    void uploadProfilePic(Uri pic_uri, JobDoneHandler onJobDone);

    void downloadProfilePic(String pic_path, PictureReadyHandler onPicReady);

}
