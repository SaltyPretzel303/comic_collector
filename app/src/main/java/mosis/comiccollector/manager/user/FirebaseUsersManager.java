package mosis.comiccollector.manager.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.login.InvalidUserInfoLoaded;
import mosis.comiccollector.login.LoginResponseType;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.LoginResponseHandler;
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.manager.Constants;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;
import mosis.comiccollector.storage.model.User;

public class FirebaseUsersManager implements UsersManager {

    private DatabaseReference database;

    private User current_user;

    public FirebaseUsersManager() {

        this.database = FirebaseDatabase.getInstance().getReference("users");

    }

    @Override
    public void login(final String username, final String password, final LoginResponseHandler response_callback) {

        // check does user exists
        this.database.orderByChild("username")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            // first (and the only in this case) entry from the map
                            Map<String, String> user_in_map = ((Map<String, Map<String, String>>) dataSnapshot.getValue()).entrySet()
                                    .iterator()
                                    .next()
                                    .getValue();

                            User user = User.parseMap(user_in_map);
                            Log.w("FirebaseLogin", user.toString());

                            // user exists, check password match
                            if (user.getPassword().equals(password)) {

                                setCurrentUser(user, new JobDoneHandler() {
                                    @Override
                                    public void execute(String message) {

                                        response_callback.execute(LoginResponseType.Success);

                                    }
                                });

                            } else {
                                // wrong password

                                response_callback.execute(LoginResponseType.InvalidPassword);

                            }

                        } else {

                            // account with 'username' not found
                            response_callback.execute(LoginResponseType.NoSuchUser);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        response_callback.execute(LoginResponseType.UnknownError);
                    }
                });

    }

    @Override
    public void register(final String username, final String password, final LoginResponseHandler response_callback) {

        this.database.orderByChild("username")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // this username is already in use

                            response_callback.execute(LoginResponseType.UsernameAlreadyInUse);

                        } else {

                            // this username is not in use
                            // create account

                            String new_key = database.push().getKey();

                            User new_user = new User(new_key, username, password);
                            database.child(new_key).setValue(new_user);

                            setCurrentUser(new_user, new JobDoneHandler() {

                                @Override
                                public void execute(String message) {

                                    response_callback.execute(LoginResponseType.Success);

                                }

                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public User getCurrentUser() {
        return this.current_user;
    }

    private void setCurrentUser(User user, final JobDoneHandler onJobDone) {

        this.current_user = user;

        // save user data to te local storage
        Context app_context = MyApplication.getInstance().getActivityContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.putString(User.data_id_prefs_path, user.getUserId());
        prefs_edit.putString(User.username_prefs_path, user.getUsername());
        prefs_edit.putString(User.password_prefs_path, user.getPassword());

        // TODO save comics
        //collected: [
        //      comic_1: {
        //
        //              title: some_title,
        //              pages: 1, 3, 19, 11...
        //
        //      }
        // ]

        // TODO check and save profile pic
        prefs_edit.apply();

        Log.w("SET_DATA", "setCurrentUser: Partion write done ... ");

        AppManager.getInstance().getStorage().downloadProfilePic(user.getProfilePicName(), new PictureReadyHandler() {
            @Override
            public void execute(Bitmap pic) {

                if (pic != null) {

                    Log.w("Download picture ", "execute: PIc is NOT NULL");

                    current_user.setProfPicBitmap(pic);

                    AppManager.getInstance().getStorage().saveUserProfilePic(pic, new JobDoneHandler() {
                        @Override
                        public void execute(String message) {

                            onJobDone.execute(message);


                        }
                    });

                } else {

                    Log.e("FirebaseUserManager", "execute: user does not have profile pic");
                    onJobDone.execute("noPic");

                }
            }

        });

    }

    @Override
    public void reloadUser(final JobDoneHandler onJobDone) throws InvalidUserInfoLoaded {

        Context app_context = MyApplication.getInstance().getActivityContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH
                , Context.MODE_PRIVATE);

        this.current_user = new User();

        final String unknown_value = "unknown";

        // dataId
        String dataId = prefs.getString(User.data_id_prefs_path, unknown_value);
        if (dataId.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Invalid dataId (userId)");

        this.current_user.setUserId(dataId);

        // username
        String username = prefs.getString(User.username_prefs_path, unknown_value);
        if (username.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Invalid username ... ");

        this.current_user.setUsername(username);


        // password
        String password = prefs.getString(User.password_prefs_path, unknown_value);
        if (password.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Unknown password ... ");

        this.current_user.setPassword(password);

        AppManager.getInstance().getStorage().loadLocalProfilePic(this.current_user.getLocalProfilePicName(), new PictureReadyHandler() {

            @Override
            public void execute(Bitmap pic) {

                if (pic != null) {

                    Log.e("FirebaseUserManager", "Profile pic FOUND locally ... ");

                    current_user.setProfPicBitmap(pic);

                    onJobDone.execute("success");

                } else {

                    Log.w("LoadImage", "reloadUser: USER DOESNT HAVE IMAGE");

                    onJobDone.execute("failure");

                }
            }

        });


    }

    @Override
    public void clearUser() {

        // delete shared prefs
        Context app_context = MyApplication.getInstance().getActivityContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.remove(User.data_id_prefs_path);
        prefs_edit.remove(User.username_prefs_path);
        prefs_edit.remove(User.password_prefs_path);

        // remove comics lists

        prefs_edit.apply();

        this.removeLocalProfilePic(current_user.getLocalProfilePicName());

        // remove user reference
        this.current_user = null;

    }

    @Override
    public void updateUserProfilePic(final Uri pic_uri, final Bitmap new_image, final JobDoneHandler onJobDone) {

        // update pic reference in user class
        current_user.setProfPicBitmap(new_image);

        // make local copy
        AppManager.getInstance().getStorage().uploadProfilePic(pic_uri, new JobDoneHandler() {
            @Override
            public void execute(String message) {

                // update pic in to the firebase
                AppManager.getInstance().getStorage().saveUserProfilePic(new_image, new JobDoneHandler() {
                    @Override
                    public void execute(String message) {

                        onJobDone.execute(message);

                    }
                });

            }
        });


    }

    @Override
    public boolean hasUser() {

        Context app_context = MyApplication.getInstance().getActivityContext();

        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);
        return prefs.contains(User.username_prefs_path);

    }

    private void removeLocalProfilePic(String pic_name) {

        File path = MyApplication.getInstance().getActivityContext().getFilesDir();
        File pic = new File(path, pic_name);

        if (pic.exists()) {

            Log.e("FirebaseUserManager", "removeLocalProfilePic: Successful");
            pic.delete();

        } else {

            Log.e("FirebaseUserManager", "removeLocalProfilePic: can't remove it, doesn't exists");

        }

    }


}
