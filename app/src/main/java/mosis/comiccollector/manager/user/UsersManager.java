package mosis.comiccollector.manager.user;

import android.graphics.Bitmap;
import android.net.Uri;

import mosis.comiccollector.login.InvalidUserInfoLoaded;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.LoginResponseHandler;
import mosis.comiccollector.storage.model.User;

public interface UsersManager {

    void login(String username, String password, LoginResponseHandler response_callback);

    void register(String username, String password, LoginResponseHandler response_callback);

    boolean hasUser();

    // in memory user
    User getCurrentUser();

    void clearUser();

    // load user from the local storage
    void reloadUser(JobDoneHandler onJobDone) throws InvalidUserInfoLoaded;

    void updateUserProfilePic(Uri pic_uri, Bitmap newImage, JobDoneHandler onJobDone);

}
