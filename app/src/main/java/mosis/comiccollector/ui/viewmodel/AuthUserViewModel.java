package mosis.comiccollector.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.model.user.UserAuthResponse;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class AuthUserViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private ComicRepository comicRepo;

    private MutableLiveData<ViewUser> liveUser;
    private MutableLiveData<ViewUser> liveRegUser;

    private DataMapper<UserAuthResponse, ViewUser> userMapper;

    public AuthUserViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.comicRepo = DepProvider.getComicRepository();

        this.userMapper = DepProvider.getAuthUserMapper();

    }

    public LiveData<ViewUser> getCurrentUser() {
        if (this.liveUser == null) {
            this.liveUser = new MutableLiveData<>();
        }

        UserAuthResponse authResponse = this.authRepo.getCurrentUser();

        this.liveUser.postValue(this.userMapper.mapToViewModel(authResponse));

        return this.liveUser;
    }

    public LiveData<ViewUser> removeUser() {

        if (this.liveUser == null) {
            this.liveUser = new MediatorLiveData<>();
        }

        UserAuthResponse response = this.authRepo.clearUser();
        // TODO next line might not be the best option
        // mixing user and comic repository ...
//        this.comicRepo.clearComicsCache();

        this.liveUser.postValue(this.userMapper.mapToViewModel(response));

        return this.liveUser;

    }

    public LiveData<ViewUser> loginWithEmail(String email, String password) {
        if (this.liveUser == null) {
            this.liveUser = new MutableLiveData<>();
        }

        this.authRepo.loginWithEmail(email, password,
                (UserAuthResponse response) -> {
                    liveUser.postValue(userMapper.mapToViewModel(response));
                });

        return this.liveUser;
    }

    public LiveData<ViewUser> registerWithEmail(String email, String password) {
        if (this.liveRegUser == null) {
            this.liveRegUser = new MutableLiveData<>();
        }

        this.authRepo.registerWithEmail(email, password,
                (UserAuthResponse response) -> {
                    liveRegUser.postValue(userMapper.mapToViewModel(response));
                });

        return this.liveRegUser;
    }

    public LiveData<ViewUser> loginWithGoogle(AuthCredential credentials) {
        if (this.liveUser == null) {
            this.liveUser = new MutableLiveData<>();
        }

        this.authRepo.logInWithGoogleAuth(
                credentials,
                (UserAuthResponse response) -> {
                    liveUser.postValue(userMapper.mapToViewModel(response));
                });


        return this.liveUser;
    }


}
