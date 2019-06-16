package mosis.comiccollector.login;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.UnknownServiceException;

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

                            // user exits, check password match
                            if (dataSnapshot.child("password").equals(password)) {

                                // correct password

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
                            User new_user = new User(username, password);
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

    @Override
    public void reloadUser(SharedPreferences prefs) {

        String username = prefs.getString("user_username", "UnknownUsername");
        String password = prefs.getString("user_password", "UnknownPassword");

        this.current_user = new User(username, password);

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

}
