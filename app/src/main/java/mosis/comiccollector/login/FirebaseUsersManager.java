package mosis.comiccollector.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import mosis.comiccollector.MyApplication;
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
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.putString(User.data_id_prefs_path, user.getData_id());
        prefs_edit.putString(User.username_prefs_path, user.getUsername());
        prefs_edit.putString(User.password_prefs_path, user.getPassword());

        prefs_edit.apply();

    }

    @Override
    public void reloadUser() {

        Context app_context = MyApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS
                , Context.MODE_PRIVATE);

        String data_id = prefs.getString(User.data_id_prefs_path, "unknown");
        String username = prefs.getString(User.username_prefs_path, "UnknownUsername");
        String password = prefs.getString(User.password_prefs_path, "UnknownPassword");

        this.current_user = new User(data_id, username, password);

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

        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.contains(User.username_prefs_path);

    }

    @Override
    public void clearUser() {

        // delete shared prefs
        Context app_context = MyApplication.getAppContext();
        SharedPreferences prefs = app_context.getSharedPreferences(Constants.APP_SHARED_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefs_edit = prefs.edit();

        prefs_edit.remove(User.data_id_prefs_path);
        prefs_edit.remove(User.username_prefs_path);
        prefs_edit.remove(User.password_prefs_path);

        prefs_edit.apply();

        // remove user reference
        this.current_user = null;

    }

}
