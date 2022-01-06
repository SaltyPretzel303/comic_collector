package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class UserProfileViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private ComicRepository comicRepo;
    private PeopleRepository peopleRepo;

    private MutableLiveData<ViewUser> liveUserData;
    private MutableLiveData<String> liveProfilePic;

    private DataMapper<User, ViewUser> userMapper;

    public UserProfileViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.comicRepo = DepProvider.getComicRepository();
        this.userMapper = DepProvider.getUserModelMapper();
        this.peopleRepo = DepProvider.getPeopleRepository();
    }

    public MutableLiveData<ViewUser> loadUser(String userId) {
        if (this.liveUserData == null) {
            this.liveUserData = new MutableLiveData<>();
        }

        this.peopleRepo.getUser(userId, (people) -> {
            // I guess people arg can't be null
            // where did i got id if that user doesn't exists ...
            User user = people.get(0);
            ViewUser viewUser = this.userMapper.mapToViewModel(user);
            viewUser.setLivePicUri(this.loadProfilePic(userId));

            this.liveUserData.postValue(viewUser);
        });

        return this.liveUserData;
    }

    public MutableLiveData<String> saveProfilePic(String picUri) {
        if (this.liveProfilePic == null) {
            this.liveProfilePic = new MutableLiveData<>();
        }

        // I can upload picture only for myself
        String userId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.uploadProfilePic(
                userId,
                picUri,
                (String uri) -> {

                    peopleRepo.updatePicUri(userId, uri, (retUri) -> {
                        // TODO maybe do something ...
                    });
                    authRepo.updatePicUri(uri);

//                    liveProfilePic.postValue(uri);
                    // this can't be done
                    // returned uri is firebaseDownlaodUri
                    // imageView wont't be able to display that
                    // imageView require local file uri which is crated in repo.LoadProfilePic
                });

        return this.liveProfilePic;
    }

    public MutableLiveData<String> loadProfilePic(String userId) {

        if (this.liveProfilePic == null) {
            this.liveProfilePic = new MutableLiveData<>();
        }

        this.peopleRepo.loadProfilePic(userId, (String picUri) -> {
            liveProfilePic.postValue(picUri);
        });

        return this.liveProfilePic;
    }

}
