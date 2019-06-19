package mosis.comiccollector.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;

import mosis.comiccollector.storage.model.User;

public interface UsersManager {

    void login(String username, String password, OnResponseAction response_callback);

    void register(String username, String password, OnResponseAction response_callback);

    User getCurrentUser();

    void reloadUser();

    void fetchUser(String username);

    boolean hasUser();

    void clearUser();

    void saveUserProfilePic(Bitmap bitmap);

    boolean uploadProfilePic(Uri pic_uri,OnResponseAction responseAction);

}
