package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserAuthResponse;
import mosis.comiccollector.model.user.UserAuthResponseType;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.util.DepProvider;
import mosis.comiccollector.util.FirebaseTypesMapper;

public class FirebaseAuthRepository implements AuthRepository {

    private static final String PROFILE_PIC_PATH = "profile_pics";
    private static final String FIREBASE_PATTERN = "firebasestorage.googleapis.com";

    private Executor taskExecutor;

    public FirebaseAuthRepository() {
        this.taskExecutor = Executors.newCachedThreadPool();
    }


    @Override
    public UserAuthResponse getCurrentUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        UserAuthResponse response = new UserAuthResponse();

        if (fUser == null) {
            response.user = null;
        } else {
            response.user = mapToUser(fUser);
        }

        response.responseType = UserAuthResponseType.Success;

        return response;
    }

    @Override
    public void loginWithEmail(String username, String password,
                               AuthResultHandler resultHandler) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(username, password)
                .addOnCompleteListener((Task<AuthResult> resultTask) -> {

                    UserAuthResponse response = new UserAuthResponse();

                    if (resultTask.isSuccessful()) {

                        response.user = mapToUser(FirebaseAuth.getInstance()
                                .getCurrentUser());

                        response.responseType = UserAuthResponseType.Success;

                        Log.e("username login", "login response good ... ");
                    } else {

                        response.user = null;
                        response.responseType = FirebaseTypesMapper.getUserResponse(
                                resultTask.getException());

                        Log.e("username login", "failed to login with username: "
                                + resultTask.getException().getMessage() + "\nExc:"
                                + resultTask.getException().getClass().toString());

                    }

                    resultHandler.handleResult(response);

                    return;
                });

    }

    @Override
    public void registerWithUsername(String username, String password,
                                     AuthResultHandler resultHandler) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener((Task<AuthResult> task) -> {
                    UserAuthResponse response = new UserAuthResponse();

                    if (task.isSuccessful()) {
                        response.user = mapToUser(task.getResult().getUser());
                        response.responseType = UserAuthResponseType.Success;

                        DepProvider
                                .getPeopleRepository()
                                .addUser(response.user, (user) -> {
                                    // TODO do something ...
                                });

                    } else {
                        Log.e("usernameRegister", "failed to register: " + task.getException().getMessage());
                        response.user = null;
                        response.responseType = FirebaseTypesMapper.getUserResponse(
                                task.getException());
                    }

                    resultHandler.handleResult(response);
                });
    }

    @Override
    public void logInWithGoogleAuth(AuthCredential credentials, AuthResultHandler resultHandler) {
        FirebaseAuth.getInstance()
                .signInWithCredential(credentials)
                .addOnCompleteListener((Task<AuthResult> task) -> {
                    UserAuthResponse response = new UserAuthResponse();

                    task.getResult();

                    if (task.isSuccessful()) {
                        Log.e("googleLogin", "login good ... ");
                        response.user = mapToUser(task.getResult().getUser());
                        response.responseType = UserAuthResponseType.Success;
                    } else {
                        Log.e("googleLogin", "login bad ... " + task.getException().getMessage());

                        response.user = null;
                        response.responseType = FirebaseTypesMapper.getUserResponse(
                                task.getException());

                    }

                    resultHandler.handleResult(response);
                });
    }

    @Override
    public UserAuthResponse clearUser() {
        FirebaseAuth.getInstance().signOut();

        return new UserAuthResponse(null, UserAuthResponseType.Success);
    }

    @Override
    public void updatePicUri(String newUri) {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest.Builder reqBuilder = new UserProfileChangeRequest.Builder();
        reqBuilder.setPhotoUri(Uri.parse(newUri));
        cUser.updateProfile(reqBuilder.build());
    }

    public void uploadProfilePic(Uri picUri, PicResultHandler onJobDone) {
        Log.w("FirebaseUserManager", "uploadProfilePic: UPLOADING IMAGE ");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        StorageReference storage = FirebaseStorage.getInstance()
                .getReference(PROFILE_PIC_PATH)
                .child(userId);

        storage.putFile(picUri).addOnCompleteListener(
                (Task<UploadTask.TaskSnapshot> task) -> {
                    if (task.isSuccessful()) {

                        Log.e("pic upload", "Pic uploaded successfully ... ");

                        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest.Builder reqBuilder = new UserProfileChangeRequest.Builder();
                        reqBuilder.setPhotoUri(task.getResult().getUploadSessionUri());
                        cUser.updateProfile(reqBuilder.build());

                        storage.getDownloadUrl().addOnCompleteListener((uriTask) -> {
                            if (uriTask.isSuccessful()) {
                                onJobDone.handlePic(uriTask.getResult());
                            } else {
                                onJobDone.handlePic(null);
                            }
                        });

                    } else {

                        Log.e("pic upload", "Failed to upload pic... ");
                        onJobDone.handlePic(null);
                    }
                });
    }


    public User mapToUser(FirebaseUser user) {
        if (user != null) {
            return new User(user.getUid(), user.getEmail(), user.getDisplayName(),
                    user.getPhotoUrl(),
                    -1);
        }

        return null;

    }

}
