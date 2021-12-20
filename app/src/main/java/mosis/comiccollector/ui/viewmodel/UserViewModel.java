package mosis.comiccollector.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.UserRepository;
import mosis.comiccollector.model.user.UserResponse;
import mosis.comiccollector.model.user.UserResponseType;
import mosis.comiccollector.util.DepProvider;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepo;
    private ComicRepository comicRepo;

    private MutableLiveData<UserResponse> liveUser;

    private MutableLiveData<UserResponse> regUser;

    public UserViewModel(@NonNull Application application) {
        super(application);

        this.userRepo = DepProvider.getUserRepository();
        this.comicRepo = DepProvider.getComicRepository();
    }

    public LiveData<UserResponse> getCurrentUser() {
        if (this.liveUser == null) {
            this.liveUser = this.userRepo.getCurrentUser();
        }

        return this.liveUser;
    }

    public LiveData<UserResponse> removeUser() {

        this.userRepo.clearUser();
        // TODO next line might not be the best option
        // mixing user and comic repository ...
        this.comicRepo.clearComicsCache();

        if (this.liveUser == null) {
            this.liveUser = new MediatorLiveData<>();
        }

        this.liveUser.postValue(new UserResponse(null, UserResponseType.Success));
        // just to trigger observers

        return this.liveUser;

    }

    public LiveData<UserResponse> loginWithEmail(String email, String password) {
        if (this.liveUser == null) {
            this.liveUser = this.userRepo.loginWithUsername(email, password);
        }
        return this.liveUser;
    }

    public LiveData<UserResponse> registerWithEmail(String email, String password) {
        if (this.regUser == null) {
            this.regUser = this.userRepo.registerWithUsername(email, password);
        }

        return this.regUser;
    }

    public LiveData<UserResponse> loginWithGoogle(AuthCredential credentials) {
        if (this.liveUser == null) {
            this.liveUser = this.userRepo.logInWithGoogleAuth(credentials);
        }

        return this.liveUser;
    }


}
