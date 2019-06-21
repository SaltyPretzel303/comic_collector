package mosis.comiccollector.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

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
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.manager.Constants;
import mosis.comiccollector.storage.model.User;

public class FirebaseUsersManager implements UsersManager {

    private DatabaseReference database;

    private User current_user;

    public FirebaseUsersManager() {

        this.database = FirebaseDatabase.getInstance().getReference("users");

    }


    @Override
    public void login(String username, final String password, final OnResponseAction response_callback) {

        // check does user exists
        this.database.orderByChild("username")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            // first (and the only in this case) entry from the map
                            User user = User.parseMap(((Map<String, Map<String, String>>) dataSnapshot.getValue()).entrySet()
                                                              .iterator()
                                                              .next()
                                                              .getValue());


                            if (user != null) {
                                Log.e("FirebaseLogin", user.toString());
                            }

                            // user exits, check password match
                            if (user.getPassword().equals(password)) {
                                // correct password

                                setCurrenUser(user);

                                response_callback.execute(LoginResponseType.Success);

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

                    }
                });

    }

    @Override
    public void register(final String username, final String password, final OnResponseAction response_callback) {

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

                            response_callback.execute(LoginResponseType.Success);

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

    private void setCurrenUser(User user) {

        this.current_user = user;

        // save user data to te local storage
        Context app_context = MyApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.putString(User.data_id_prefs_path, user.getUserId());
        prefs_edit.putString(User.username_prefs_path, user.getUsername());
        prefs_edit.putString(User.password_prefs_path, user.getPassword());

        prefs_edit.apply();

    }

    @Override
    public void reloadUser() throws InvalidUserInfoLoaded {

        // TODO show loading screen
        // TODO extract this in separate thread

        Context app_context = MyApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH
                , Context.MODE_PRIVATE);

        User tempUser = new User();

        final String unknown_value = "unknown";

        // dataId
        String dataId = prefs.getString(User.data_id_prefs_path, "unknown");
        if (dataId.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Invalid dataId (userId)");

        tempUser.setUserId(dataId);

        // username
        String username = prefs.getString(User.username_prefs_path, "unknown");
        if (username.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Invalid username ... ");

        tempUser.setUsername(username);


        // password
        String password = prefs.getString(User.password_prefs_path, "unknown");
        if (password.equals(unknown_value))
            throw new InvalidUserInfoLoaded("Unknown password ... ");

        tempUser.setPassword(password);

        // TODO picture load should be the last task

        Bitmap profilePic = this.loadLocalProfilePic(tempUser.getLocalProfilePicName());
        if (profilePic != null) {

            Log.e("FirebaseUserManager", "Profile pic FOUND locally ... ");

            tempUser.setProfPicBitmap(profilePic);

            this.current_user = tempUser;

        } else {

            // Basically this cant't happen (but still, let it live)
            // this branch means that user has local data, but doesn't have picture locally
            // when the user is registered or logged-in for the first time, default image is saved locally

            Log.e("FirebaseUserManager", "Profile pic NOT found locally ... ");

            // fetch user picture from firebase

            String pic_name = tempUser.getProfilePicName();
            // TODO create callback interface to pass
//            this.fetchProfilePic(pic_name);

            // TODO replace next code with previous method call
            StorageReference reference = FirebaseStorage.getInstance().getReference("profile_pics/" + pic_name);

            try {

                final File temp_file = File.createTempFile("prof_pic", "png");
                reference.getFile(temp_file)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                Log.e("FirebaseUserManager", "onSuccess: FETCHING DONE ");
                                Bitmap pic = BitmapFactory.decodeFile(temp_file.getPath());
                                Log.e("FirebaseUserManager", "onSuccess: Translated to bitmap ");

                                current_user.setProfPicBitmap(pic);

                            }
                        });


            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private Bitmap loadLocalProfilePic(String pic_path) {

        File path = MyApplication.getAppContext().getFilesDir();
        File pic = new File(path, pic_path);

        if (pic.exists()) {

            return BitmapFactory.decodeFile(pic.getPath());

        } else {

            return null;
        }

    }

    private Bitmap fetchProfilePic(String name) {


        return null;

    }

    @Override
    public void fetchUser(String username) {

        this.database.orderByChild("username")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        current_user = dataSnapshot.getValue(User.class);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public boolean hasUser() {

        Context app_context = MyApplication.getAppContext();

        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);
        return prefs.contains(User.username_prefs_path);

    }

    @Override
    public void clearUser() {

        // delete shared prefs
        Context app_context = MyApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS_PATH,
                                                                   Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.remove(User.data_id_prefs_path);
        prefs_edit.remove(User.username_prefs_path);
        prefs_edit.remove(User.password_prefs_path);

        prefs_edit.apply();

        this.removeLocalProfilePic(current_user.getLocalProfilePicName());

        // remove user reference
        this.current_user = null;

    }

    private void removeLocalProfilePic(String pic_name) {

        File path = MyApplication.getAppContext().getFilesDir();
        File pic = new File(path, pic_name);

        if (pic.exists()) {

            Log.e("FirebaseUserManager", "removeLocalProfilePic: Successful");
            pic.delete();

        } else {

            Log.e("FirebaseUserManager", "removeLocalProfilePic: can't remove it, doesn't exists");

        }

    }

    @Override
    public void saveUserProfilePic(Bitmap bitmap) {

        Log.e("FirebaseUserManager", "saveUserProfilePic: SAVING PIC TO LOCAL STORAGE");

        String pic_name = this.current_user.getLocalProfilePicName();
        File path = MyApplication.getAppContext().getFilesDir();
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.e("FirebaseUserManager", "saveUserProfilePic: write done");

            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // TODO MOVE THIS METHOD TO THE DATA STORAGE !!!!!!!!
    @Override
    public boolean uploadProfilePic(Uri pic_uri, final OnResponseAction responseAction) {

        Log.e("FirebaseUserManager", "uploadProfilePic: UPLOADING IMAGE ");

        StorageReference storage = FirebaseStorage.getInstance().getReference("profile_pics");

        String username = AppManager.getInstance().getUsersManager().getCurrentUser().getUsername();
        String name = username + "-profile_pic";

        StorageReference child_ref = storage.child(name);
        child_ref.putFile(pic_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                responseAction.execute(null);

            }
        });

        return true;
    }

}
