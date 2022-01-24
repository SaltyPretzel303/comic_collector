package mosis.comiccollector.repository;

import android.net.Uri;

import com.google.firebase.auth.AuthCredential;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserAuthResponse;
import mosis.comiccollector.model.user.UserLocation;

public interface AuthRepository {

    interface AuthResultHandler {
        void handleResult(UserAuthResponse response);
    }

    interface PicResultHandler {
        void handlePic(Uri picUri);
    }

    UserAuthResponse getCurrentUser();

    void loginWithEmail(String username, String password,
                        AuthResultHandler resultHandler);

    void registerWithEmail(String email,
                           String username,
                           String password,
                           AuthResultHandler resultHandler);

    void logInWithGoogleAuth(AuthCredential credentials, AuthResultHandler resultHandler);

    UserAuthResponse clearUser();

    // TODO this one should have doneHandler as well ...
    void updatePicUri(String newUri);

}
