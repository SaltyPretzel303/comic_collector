package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class ListViewModel extends AndroidViewModel {

    public interface SortComicCriteria {
        int compare(ViewComic comic1, ViewComic comic2);
    }

    public interface SortPeopleCriteria {
        int compare(ViewUser user1, ViewUser user2);
    }

    private AuthRepository authRepo;
    private PeopleRepository peopleRepo;
    private ComicRepository comicsRepo;

    private DataMapper<Comic, ViewComic> comicMapper;
    private DataMapper<User, ViewUser> userMapper;

    private MutableLiveData<List<ViewUser>> livePeople;
    private MutableLiveData<List<ViewComic>> liveComics;

    private String peopleFilter;

    private String comicsFilter;

    public ListViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
        this.comicsRepo = DepProvider.getComicRepository();

        this.comicMapper = DepProvider.getComicModelMapper();
        this.userMapper = DepProvider.getUserModelMapper();

    }

    private String getMyId() {
        return authRepo.getCurrentUser().user.getUserId();
    }

    public MutableLiveData<List<ViewUser>> getUnknownPeople() {
        if (livePeople == null) {
            livePeople = new MutableLiveData<>();
        }

        queryPeople();

        return livePeople;
    }

    private void queryPeople() {
        peopleRepo.getUnknownPeople(
            getMyId(),
            (people) -> {
                if (people == null) {
                    livePeople.postValue(Collections.emptyList());
                    return;
                }

                if (peopleFilter != null && !peopleFilter.isEmpty()) {
                    people.removeIf((person) -> !person.getName().contains(peopleFilter));
                }

                livePeople.postValue(mapUsers(people));
            }
        );
    }

    public ViewUser getUserAt(int index) {
        if (livePeople != null
            && livePeople.getValue() != null
            && livePeople.getValue().size() >= index) {

            var user = livePeople.getValue().get(index);

            if (user.liveProfilePic == null) {
                user.liveProfilePic = loadProfilePic(user.userId);
            }

            return user;
        }

        return null;
    }

    public ViewUser getUser(String id) {
        if (livePeople != null
            && livePeople.getValue() != null) {
            for (var person : livePeople.getValue()) {
                if (person.userId.equals(id)) {
                    return person;
                }
            }
        }

        return null;
    }


    public MutableLiveData<List<ViewComic>> getUnknownComics() {
        if (liveComics == null) {
            liveComics = new MutableLiveData<>();
        }

        queryComics();

        return liveComics;
    }

    private void queryComics() {
        comicsRepo.getUnknownComics(
            getMyId(),
            (comics) -> {
                if (comics == null) {
                    liveComics.postValue(Collections.emptyList());
                    return;
                }

                if (comicsFilter != null && !comicsFilter.isEmpty()) {
                    comics.removeIf((comic) -> !comic.getComicTitle().contains(comicsFilter));
                }

                liveComics.postValue(mapComics(comics));
            }
        );
    }

    public ViewComic getComicAt(int index) {
        if (liveComics != null
            && liveComics.getValue() != null
            && liveComics.getValue().size() >= index) {

            var vComic = liveComics.getValue().get(index);

            if (vComic.liveTitlePage == null) {
                vComic.liveTitlePage = loadTitlePage(vComic.comicId);
            }

            if (vComic.liveAuthor == null) {
                vComic.liveAuthor = loadAuthor(vComic.authorId);
            }

            return vComic;
        }

        return null;
    }

    public ViewComic getComic(String id) {
        if (liveComics != null
            && liveComics.getValue() != null) {
            for (var comic : liveComics.getValue()) {
                if (comic.comicId.equals(id)) {
                    return comic;
                }
            }
        }

        return null;
    }


    private List<ViewUser> mapUsers(List<User> inputUsers) {
        List<ViewUser> vUsers = new ArrayList<>();

        for (var user : inputUsers) {
            vUsers.add(userMapper.mapThis(user));
        }

        return vUsers;
    }

    private List<ViewComic> mapComics(List<Comic> inputComics) {
        List<ViewComic> vComics = new ArrayList<>();

        for (var comic : inputComics) {
            vComics.add(comicMapper.mapThis(comic));
        }

        return vComics;
    }

    private MutableLiveData<Bitmap> loadProfilePic(String userId) {
        MutableLiveData<Bitmap> liveData = new MutableLiveData<>();
        peopleRepo.loadProfilePic(userId, liveData::postValue);
        return liveData;
    }

    private MutableLiveData<Bitmap> loadTitlePage(String comicId) {
        var liveData = new MutableLiveData<Bitmap>();
        comicsRepo.loadTitlePage(comicId, 100, 100, (pages) -> {
            if (pages != null && pages.size() > 0) {
                liveData.postValue(pages.get(0));
            } else {
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    private MutableLiveData<ViewUser> loadAuthor(String userId) {
        var liveData = new MutableLiveData<ViewUser>();
        peopleRepo.getUser(userId, (people) -> {
            if (people != null && people.size() > 0) {
                var vUser = userMapper.mapThis(people.get(0));
                vUser.liveProfilePic = loadProfilePic(userId);

                liveData.postValue(vUser);
            } else {
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    public int getPeopleCount() {
        if (livePeople != null
            && livePeople.getValue() != null) {
            return livePeople.getValue().size();
        }

        return 0;
    }

    public int getComicsCount() {
        if (liveComics != null
            && liveComics.getValue() != null) {
            return liveComics.getValue().size();
        }

        return 0;
    }

    public void sortComics(SortComicCriteria sortCriteria) {
        if (liveComics != null && liveComics.getValue() != null) {
            liveComics.getValue().sort(sortCriteria::compare);
        }
    }

    public void sortPeople(SortPeopleCriteria sortCriteria) {
        if (livePeople != null && livePeople.getValue() != null) {
            livePeople.getValue().sort(sortCriteria::compare);
        }
    }

    public void setPeopleTextFilter(String filter) {
        Log.e("listViewModel", "Set people filter: " + filter);
        if (peopleFilter == null || !peopleFilter.equals(filter)) {
            peopleFilter = filter;
            queryPeople();
        }
    }

    public void setComicsTextFilter(String filter) {
        Log.e("listViewModel", "Set comic filter: " + filter);
        if (comicsFilter == null || !comicsFilter.equals(filter)) {
            comicsFilter = filter;
            queryComics();
        }
    }

    public MutableLiveData<UserLocation> getLastLocation(String userId) {
        var liveData = new MutableLiveData<UserLocation>();
        peopleRepo.getLastLocation(userId, (locations) -> {
            if (locations != null) {
                liveData.postValue(locations.get(0));
            }
        });
        return liveData;
    }

}
