package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.RatingsRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class UserProfileViewModel extends AndroidViewModel {

    public interface SortComicCriteria {
        int compare(ViewComic comic1, ViewComic comic2);
    }

    public interface SortPeopleCriteria {
        int compare(ViewUser user1, ViewUser user2);
    }

    private AuthRepository authRepo;
    private ComicRepository comicRepo;
    private PeopleRepository peopleRepo;
    private RatingsRepository ratingRepo;

    private MutableLiveData<ViewUser> liveUserData;
    private MutableLiveData<Bitmap> liveProfilePic;

    private MutableLiveData<List<ViewComic>> liveCreatedComics;
    private MutableLiveData<List<ViewComic>> liveCollectedComics;
    private MutableLiveData<List<ViewUser>> liveFriends;

    private DataMapper<User, ViewUser> userMapper;
    private DataMapper<Comic, ViewComic> comicMapper;

    public UserProfileViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.comicRepo = DepProvider.getComicRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
        this.ratingRepo = DepProvider.getRatingRepository();

        this.userMapper = DepProvider.getUserModelMapper();
        this.comicMapper = DepProvider.getComicModelMapper();

    }

    // region user info

    public String getMyId() {
        return authRepo.getCurrentUser().user.getUserId();
    }

    public MutableLiveData<ViewUser> getUser(String userId, int width, int height) {
        if (this.liveUserData == null) {
            this.liveUserData = new MutableLiveData<>();
        }

        this.peopleRepo.getUser(userId, (people) -> {
            // I guess people arg can't be null
            // where did I got id if that user doesn't exists ...
            User user = people.get(0);
            ViewUser viewUser = this.userMapper.mapThis(user);

            viewUser.setLiveProfilePic(loadProfilePic(userId, width, height));

            this.liveUserData.postValue(viewUser);
        });

        return this.liveUserData;
    }

    public MutableLiveData<String> saveProfilePic(String picUri) {
        MutableLiveData<String> uploadUri = new MutableLiveData<>();

        // I can upload picture only for myself
        String userId = this.getMyId();

        this.peopleRepo.uploadProfilePic(userId, picUri, (String uri) -> {

            peopleRepo.updatePicUri(userId, uri, (retUri) -> {
                // TODO maybe do something ...
            });
            authRepo.updatePicUri(uri);

            uploadUri.postValue(uri);
        });

        return uploadUri;
    }

    private MutableLiveData<Bitmap> loadProfilePic(String userId, int width, int height) {

        MutableLiveData<Bitmap> livePic = new MutableLiveData<>();
//        if (this.liveProfilePic == null) {
//            this.liveProfilePic = new MutableLiveData<>();
//        }

        this.peopleRepo.loadProfilePic(userId, (Bitmap picUri) -> {
//            liveProfilePic.postValue(picUri);
            livePic.postValue(picUri);
        });

//        return this.liveProfilePic;
        return livePic;
    }

    private MutableLiveData<Float> getUserRating(String userId) {
        var liveData = new MutableLiveData<Float>();

        ratingRepo.getAuthorRating(userId, liveData::postValue);


        return liveData;
    }

    // endregion

    // region created comics

    public MutableLiveData<List<ViewComic>> loadCreatedComics(String userId) {
        if (this.liveCreatedComics == null) {
            this.liveCreatedComics = new MutableLiveData<>();
        }

        this.comicRepo.getCreatedComics(userId, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("viewModelProfile", "Failed to load created comics for: " + userId);
                liveCreatedComics.postValue(null);
                return;
            }

            List<ViewComic> vComicL = new ArrayList<>();
            for (Comic comic : newComics) {
                ViewComic vComic = comicMapper.mapThis(comic);

                vComicL.add(vComic);
            }

            liveCreatedComics.postValue(vComicL);
        });

        return this.liveCreatedComics;
    }

    public void updateMyCreatedComics(String id) {
        comicRepo.getComic(id, (newComics) -> {
            if (newComics == null || newComics.size() == 0) {
                Log.e("profileViewModel", "Failed to retrieve new created comics ... ");
                return;
            }

            if (liveCreatedComics == null) {
                liveCreatedComics = new MutableLiveData<>();
            }

            ViewComic newComic = comicMapper.mapThis(newComics.get(0));

            if (liveCreatedComics.getValue() == null) {
                liveCreatedComics.postValue(Arrays.asList(newComic));
            } else {
                var comics = liveCreatedComics.getValue();
                comics.add(newComic);

                liveCreatedComics.postValue(comics);
            }

        });
    }

    public void updatePagesCount(String comicId, int newCount) {
        getCreatedComic(comicId).pagesCount = newCount;
    }

    public ViewComic getCreatedComic(String id) {
        if (liveCreatedComics != null && liveCreatedComics.getValue() != null) {
            for (ViewComic comic : liveCreatedComics.getValue()) {
                if (comic.comicId.equals(id)) {
                    return comic;
                }
            }
        }
        return null;
    }

    public ViewComic getCreatedComicAt(int index, int width, int height) {
        if (liveCreatedComics != null
            && liveCreatedComics.getValue() != null
            && liveCreatedComics.getValue().size() >= index) {

            ViewComic comic = liveCreatedComics.getValue().get(index);
            if (comic.liveTitlePage == null) {
                comic.liveTitlePage = loadTitlePage(comic.comicId, width, height);
            }

            return comic;
        }

        return null;
    }

    public int getCreatedCount() {
        if (liveCreatedComics != null
            && liveCreatedComics.getValue() != null) {
            return liveCreatedComics.getValue().size();
        }

        return 0;
    }

    // endregion

    // region collected comics

    private MutableLiveData<Bitmap> loadTitlePage(String comicId, int width, int height) {
        MutableLiveData<Bitmap> livePic = new MutableLiveData<>();

        comicRepo.loadTitlePage(comicId, width, height, (List<Bitmap> newPages) -> {
            if (newPages == null) {
                Log.e("viewModelProfile", "Failed to load titlePage for: " + comicId);
                livePic.postValue(null);

                return;
            }

            livePic.postValue(newPages.get(0));

        });

        return livePic;
    }

    public MutableLiveData<List<ViewComic>> loadCollectedComics(String userId) {
        if (this.liveCollectedComics == null) {
            this.liveCollectedComics = new MutableLiveData<>();
        }

        this.comicRepo.getCollectedComics(userId, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("viewModelProfile", "Failed to load collected comics ... ");
                liveCollectedComics.postValue(null);
                return;
            }

            List<ViewComic> vComics = new ArrayList<>();
            for (Comic comic : newComics) {
                ViewComic vComic = this.comicMapper.mapThis(comic);

                vComics.add(vComic);
            }

            this.liveCollectedComics.postValue(vComics);
        });

        return this.liveCollectedComics;
    }

    public ViewComic getCollectedComic(String id) {
        if (liveCollectedComics != null && liveCollectedComics.getValue() != null) {
            for (ViewComic comic : liveCollectedComics.getValue()) {
                if (comic.comicId.equals(id)) {
                    return comic;
                }
            }
        }

        return null;
    }

    public ViewComic getCollectedComicAt(int index, int width, int height) {
        if (liveCollectedComics != null
            && liveCollectedComics.getValue() != null
            && liveCollectedComics.getValue().size() >= index) {

            ViewComic comic = liveCollectedComics.getValue().get(index);
            if (comic.liveTitlePage == null) {
                comic.liveTitlePage = this.loadTitlePage(
                    comic.comicId,
                    width,
                    height);
            }

            return comic;
        }

        return null;
    }

    public int getCollectedCount() {

        if (liveCollectedComics != null && liveCollectedComics.getValue() != null) {
            return liveCollectedComics.getValue().size();
        }

        return 0;
    }

    // endregion

    // region friends

    public MutableLiveData<List<ViewUser>> loadFriends(String userId) {
        if (liveFriends == null) {
            liveFriends = new MutableLiveData<>();
        }

        peopleRepo.getFriends(userId, (List<User> people) -> {
            if (people == null) {
                Log.e("profileViewModel", "Failed to get friends for user: " + userId);
                liveFriends.postValue(null);
                return;
            }

            List<ViewUser> vPeople = new ArrayList<>();
            for (User uPeople : people) {
                ViewUser vUser = userMapper.mapThis(uPeople);
                vPeople.add(vUser);
            }
            liveFriends.postValue(vPeople);
        });

        return liveFriends;
    }

    public ViewUser getFriend(String id) {
        if (liveFriends != null && liveFriends.getValue() != null) {
            for (ViewUser friend : this.liveFriends.getValue()) {
                if (friend.userId.equals(id)) {

                    return friend;
                }
            }
        }

        return null;
    }

    public ViewUser getFriendAt(int index, int width, int height) {
        if (liveFriends != null
            && liveFriends.getValue() != null
            && liveFriends.getValue().size() >= index) {

            ViewUser friend = liveFriends.getValue().get(index);
            if (friend.liveProfilePic == null) {
                friend.liveProfilePic = loadProfilePic(friend.userId, width, height);
            }

            return friend;
        }
        return null;
    }

    public int getFriendsCount() {
        if (liveFriends != null && liveFriends.getValue() != null) {
            return liveFriends.getValue().size();
        }
        return 0;
    }

    // endregion

    // region sorts

    public void sortCreatedComics(SortComicCriteria criteria) {
        this.liveCreatedComics.getValue().sort(criteria::compare);
    }

    public void sortCollectedComics(SortComicCriteria criteria) {
        this.liveCollectedComics.getValue().sort(criteria::compare);
    }

    public void sortPeople(SortPeopleCriteria criteria) {
        this.liveFriends.getValue().sort(criteria::compare);
    }

    // endregion

}
