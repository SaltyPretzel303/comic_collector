package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.PeopleLocationConsumer;
import mosis.comiccollector.repository.UnsubscribeProvider;
import mosis.comiccollector.ui.map.PersonFollower;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.map.LocationWithPicture;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class DiscoveryViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private PeopleRepository peopleRepo;
    private ComicRepository comicRepo;

    private MutableLiveData<List<LocationWithPicture>> nearbyFriends;
    private MutableLiveData<List<LocationWithPicture>> nearbyPeople;
    private List<UnsubscribeProvider> unsubscribeProviders;

    private MutableLiveData<List<LocationWithPicture>> createdComics;
    private MutableLiveData<List<LocationWithPicture>> collectedComics;
    private MutableLiveData<List<LocationWithPicture>> unknownComics;

    private DataMapper<User, ViewUser> viewUserMapper;
    private DataMapper<Comic, ViewComic> viewComicMapper;

    public DiscoveryViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();
        this.comicRepo = DepProvider.getComicRepository();

        this.viewUserMapper = DepProvider.getUserModelMapper();
        this.viewComicMapper = DepProvider.getComicModelMapper();

        this.unsubscribeProviders = new ArrayList<>();
        this.peopleFollowers = new ArrayList<>();
    }

    private List<PersonFollower> peopleFollowers;

    // region people

    public String getMyId() {
        return authRepo.getCurrentUser().user.getUserId();
    }

    public MutableLiveData<List<LocationWithPicture>> getNearbyFriends(
            double lat,
            double lon,
            double range) {

        if (nearbyFriends == null) {
            nearbyFriends = new MutableLiveData<>();
        }

        String userId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.getNearbyFriendsLocations(
                userId,
                new GeoPoint(lat, lon),
                range,
                new PeopleRepository.PeopleUpdateHandler() {
                    @Override
                    public void personIn(String id, GeoPoint loc) {

                        List<LocationWithPicture> currentUsers = nearbyFriends.getValue();
                        if (currentUsers == null) {
                            currentUsers = new ArrayList<>();
                        }

                        if (currentUsers.stream().noneMatch((l) -> l.getId().equals(id))) {

                            LocationWithPicture newUser = new LocationWithPicture(id, loc);
                            newUser.setLivePic(loadUserPic(id));

                            currentUsers.add(newUser);
                            nearbyFriends.postValue(currentUsers);
                        }

                    }

                    @Override
                    public void personOut(String id) {
                        List<LocationWithPicture> users = nearbyFriends.getValue();

                        users.removeIf((user) -> user.getId().equals(id));

                        nearbyFriends.postValue(users);
                    }

                    @Override
                    public void personMoved(String id, GeoPoint loc) {
                        for (var friend : nearbyFriends.getValue()) {
                            if (friend.getId().equals(id)) {

                                friend.updateLocation(loc);
                                nearbyFriends.postValue(nearbyFriends.getValue());

                                return;
                            }
                        }
                    }

                    @Override
                    public void everyoneLoaded() {
                    }

                    @Override
                    public void error(String err) {
                        return;
                    }
                });

        return nearbyFriends;
    }

    public MutableLiveData<List<LocationWithPicture>> getNearbyUnknownPeople(
            double lat,
            double lon,
            double range) {

        if (nearbyPeople == null) {
            nearbyPeople = new MutableLiveData<>();
        }

        String userId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.getNearbyPeopleLocations(
                userId,
                new GeoPoint(lat, lon),
                range,
                new PeopleRepository.PeopleUpdateHandler() {
                    @Override
                    public void personIn(String id, GeoPoint loc) {

                        List<LocationWithPicture> currentPeople = nearbyPeople.getValue();
                        if (currentPeople == null) {
                            currentPeople = new ArrayList<>();
                        }

                        if (currentPeople.stream().noneMatch((l) -> l.getId().equals(id))) {

                            LocationWithPicture newUser = new LocationWithPicture(id, loc);
                            newUser.setLivePic(loadUserPic(id));

                            currentPeople.add(newUser);
                            nearbyPeople.postValue(currentPeople);
                        }

                    }

                    @Override
                    public void personOut(String id) {
                        List<LocationWithPicture> users = nearbyPeople.getValue();

                        users.removeIf((user) -> user.getId().equals(id));

                        nearbyPeople.postValue(users);
                    }

                    @Override
                    public void personMoved(String id, GeoPoint loc) {
                        for (var person : nearbyPeople.getValue()) {
                            if (person.getId().equals(id)) {

                                person.updateLocation(loc);
                                nearbyPeople.postValue(nearbyPeople.getValue());

                                return;
                            }
                        }
                    }

                    @Override
                    public void everyoneLoaded() {
                        return;
                    }

                    @Override
                    public void error(String err) {
                        return;
                    }
                });

        return nearbyPeople;
    }

    private MutableLiveData<Bitmap> loadUserPic(String userId) {
        MutableLiveData<Bitmap> livePicUri = new MutableLiveData<>();

        peopleRepo.loadProfilePic(userId, (Bitmap pic) -> {
            livePicUri.postValue(pic);
        });

        return livePicUri;
    }

    public void subscribeForLocationUpdates(String userId,
                                            PeopleLocationConsumer locationConsumer) {

        UnsubscribeProvider unsubProvider =
                this.peopleRepo.subscribeForLocUpdates(userId, locationConsumer);

        this.unsubscribeProviders.add(unsubProvider);
    }

    public void unsubscribeForLocationUpdates(String userId) {
        this.unsubscribeProviders.removeIf((unsubscribeProvider) -> {
            if (unsubscribeProvider.getItemId().equals(userId)) {
                unsubscribeProvider.unsubscribe();
                return true;
            }
            return false;
        });
    }

    public MutableLiveData<ViewUser> getShortUser(String userId) {
        MutableLiveData<ViewUser> shortUser = new MutableLiveData<>();

        this.peopleRepo.getUser(userId, (List<User> people) -> {
            if (people != null && people.size() > 0 && people.get(0) != null) {

                ViewUser viewUser = this.viewUserMapper.mapToViewModel(people.get(0));
                viewUser.setLiveProfilePic(getLiveLocalPic(userId));

                shortUser.postValue(viewUser);
            }
        });

        return shortUser;
    }

    private MutableLiveData<Bitmap> getLiveLocalPic(String userId) {

        // it has to be there ... you can't request data for user that has not been displayed

        if (nearbyFriends != null && nearbyFriends.getValue() != null) {
            for (var locWithPicture : nearbyFriends.getValue()) {
                if (locWithPicture.getId().equals(userId)) {
                    return locWithPicture.getLivePic();
                }
            }
        }

        if (nearbyPeople != null && nearbyPeople.getValue() != null) {
            for (var locWithPicture : nearbyPeople.getValue()) {
                if (locWithPicture.getId().equals(userId)) {
                    return locWithPicture.getLivePic();
                }
            }
        }

        return null;
    }

    public MutableLiveData<UserLocation> getMyLastLocation() {
        MutableLiveData<UserLocation> liveData = new MutableLiveData<>();

        String myId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.getLastLocation(myId, (List<UserLocation> locations) -> {
            if (locations == null || locations.size() == 0 || locations.get(0) == null) {
                Log.e("discViewModel", "Failed to get my last known location ... ");
                return;
            }

            liveData.postValue(locations.get(0));
        });

        return liveData;
    }

    public MutableLiveData<UserLocation> getUserLocation(String userId) {
        var liveData = new MutableLiveData<UserLocation>();

        peopleRepo.getLastLocation(userId, (locations) -> {
            if (locations == null || locations.size() == 0) {
                Log.e("discViewModel", "Failed to get person's last location");
                liveData.postValue(null);
                return;
            }

            liveData.postValue(locations.get(0));
        });

        return liveData;
    }

    public MutableLiveData<ViewUser> getAuthor(String id) {
        MutableLiveData<ViewUser> liveAuthor = new MutableLiveData<>();

        peopleRepo.getUser(id, (List<User> people) -> {
            if (people == null) {
                Log.e("mapViewModel", "Failed to get author ... ");
                return;
            }

            ViewUser vAuthor = viewUserMapper.mapToViewModel(people.get(0));
            vAuthor.liveProfilePic = loadUserPic(vAuthor.userId);

            liveAuthor.postValue(vAuthor);

            return;
        });

        return liveAuthor;
    }

    public MutableLiveData<String> makeFriends(String personId) {
        var liveResult = new MutableLiveData<String>();

        String myId = authRepo.getCurrentUser().user.getUserId();

        peopleRepo.makeFriends(myId, personId, (err) -> {
            if (err != null) {
                Log.e("discViewModel", "Failed to be friend ... ");
                liveResult.postValue(err);
                return;
            }

            LocationWithPicture person = removeUnknownPerson(personId);
            addFriend(person);

        });

        return liveResult;
    }

    private LocationWithPicture removeUnknownPerson(String id) {
        if (nearbyPeople != null && nearbyPeople.getValue() != null) {
            var people = nearbyPeople.getValue();

            for (int i = 0; i < people.size(); i++) {
                LocationWithPicture person = people.get(i);
                if (person.getId().equals(id)) {

                    people.remove(i);
                    nearbyPeople.postValue(people);

                    return person;
                }
            }
        }

        return null;
    }

    private void addFriend(LocationWithPicture person) {
        if (nearbyFriends == null) {
            nearbyFriends = new MutableLiveData<>();
        }
        if (nearbyFriends.getValue() == null) {
            nearbyFriends.postValue(Arrays.asList(person));
        } else {
            var friends = nearbyFriends.getValue();
            friends.add(person);
            nearbyFriends.postValue(friends);
        }
    }

    // endregion

    // region comics

    public MutableLiveData<List<LocationWithPicture>> getCreatedComics(
            int width,
            int height,
            double lat,
            double lon,
            double radius) {

        if (this.createdComics == null) {
            this.createdComics = new MutableLiveData<>();
        }

        String myId = this.getMyId();

        this.comicRepo.getCreatedComics(myId, lat, lon, radius, (List<Comic> dataComics) -> {
            if (dataComics == null) {
                Log.e("discViewModel", "got err as createdComics ... ");
                createdComics.postValue(Collections.emptyList());
                return;
            }

            createdComics.postValue(mapComics(
                    createdComics.getValue(),
                    dataComics,
                    width,
                    height));

            return;
        });

        return this.createdComics;
    }

    public MutableLiveData<List<LocationWithPicture>> getCollectedComics(
            int width,
            int height,
            double lat,
            double lon,
            double radius) {

        if (this.collectedComics == null) {
            this.collectedComics = new MutableLiveData<>();
        }

        String myId = this.getMyId();

        this.comicRepo.getCollectedComics(myId, lat, lon, radius, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("discViewModel", "Failed to get collected comics ... ");
                collectedComics.postValue(new ArrayList<>());
                return;
            }

            Log.e("discViewModel", "Got collected comics: " + newComics.size());

            if (collectedComics.getValue() != null) {
                // if I collected some comic before other collected comics are retrieved ...
                collectedComics.postValue(union(
                        collectedComics.getValue(),
                        mapComics(collectedComics.getValue(),
                                newComics,
                                width,
                                height)));

            } else {
                collectedComics.postValue(mapComics(
                        collectedComics.getValue(),
                        newComics,
                        width,
                        height));
            }

            return;
        });

        return this.collectedComics;
    }

    private List<LocationWithPicture> union(
            List<LocationWithPicture> list_1,
            List<LocationWithPicture> list_2) {

        List<LocationWithPicture> retList = new ArrayList<>(list_1);

        for (LocationWithPicture loc : list_2) {
            if (list_1.stream().noneMatch((loc_1) -> loc_1.getId().equals(loc.getId()))) {
                retList.add(loc);
            }
        }

        return retList;
    }

    private boolean isDiff(
            List<LocationWithPicture> oldList,
            List<LocationWithPicture> newList) {

        if (oldList.size() != newList.size()) {
            return true;
        } else {
            for (var newItem : newList) {
                if (oldList.stream().noneMatch((oldItem) -> oldItem.getId().equals(newItem.getId()))) {
                    return true;
                }
            }
        }

        return false;
    }

    public MutableLiveData<List<LocationWithPicture>> getUnknownComics(
            int width,
            int height,
            double lat,
            double lon,
            double radius) {

        if (this.unknownComics == null) {
            this.unknownComics = new MutableLiveData<>();
        }

        String myId = this.getMyId();

        this.comicRepo.getUnknownComics(myId, lat, lon, radius, (List<Comic> newComics) -> {
            if (newComics == null || newComics.size() == 0) {
                Log.e("discViewModel", "Failed to get unknown comics ... ");
                return;
            }

            unknownComics.postValue(mapComics(
                    unknownComics.getValue(),
                    newComics,
                    width,
                    height));

            return;
        });

        return this.unknownComics;
    }

    public MutableLiveData<ViewComic> getComic(String id, int width, int height) {

        MutableLiveData<ViewComic> liveComic = new MutableLiveData<>();

        comicRepo.getComic(id, (List<Comic> newComics) -> {
            if (newComics == null) {
                Log.e("discViewModel", "Failed to get comic ... ");
                liveComic.postValue(null);
                return;
            }

            ViewComic vComic = viewComicMapper.mapToViewModel(newComics.get(0));
            vComic.liveTitlePage = new MutableLiveData<>();

            comicRepo.loadTitlePage(id, width, height, (List<Bitmap> pages) -> {
                if (pages == null) {
                    Log.e("discViewModel", "Failed to load title page ... ");
                    return;
                }

                vComic.liveTitlePage.postValue(pages.get(0));

                return;
            });

            vComic.liveAuthor = getAuthor(newComics.get(0).getAuthorId());


            liveComic.postValue(vComic);

            return;
        });

        return liveComic;
    }

    public MutableLiveData<String> collectComic(String comicId) {
        MutableLiveData<String> liveDone = new MutableLiveData<>();

        String myId = getMyId();
        // if err is null (no err) it is just gonna be propagated to the "view"
        comicRepo.collectComic(myId, comicId, (String err) -> {
            if (err != null) {
                Log.e("mapViewModel", "Failed to collect comic ... ");
                liveDone.postValue(err);
                return;
            }

            moveToCollected(comicId);
            liveDone.postValue(null);

            return;
        });

        return liveDone;
    }

    private void moveToCollected(String comicId) {
        // unknownComics can't be null
        // unknownComics.getValue can't be null
        // what did i clicked on if it is null ...

        int indexInUnknown = indexOfUnknown(comicId);
        LocationWithPicture locWPic = unknownComics.getValue().remove(indexInUnknown);

        if (collectedComics != null) {

            if (collectedComics.getValue() == null) {
                collectedComics.postValue(Arrays.asList(locWPic));
            } else {
                List<LocationWithPicture> oldValues = collectedComics.getValue();
                oldValues.add(locWPic);
                collectedComics.postValue(oldValues);
            }

//            collectedComics.getValue().add(locWPic);
//            collectedComics.postValue(collectedComics.getValue());
//
            unknownComics.postValue(unknownComics.getValue());

            Log.e("mapViewModel", "Comics rearranged ... ");
        } else {
            Log.e("mapViewModel", "Something is null for rearranging ... ");
        }
    }

    private int indexOfUnknown(String comicId) {
        for (int i = 0; i < unknownComics.getValue().size(); i++) {
            if (unknownComics.getValue().get(i).getId().equals(comicId)) {
                return i;
            }
        }
        return -1;
    }

    // endregion

    @Override
    protected void onCleared() {
        // will be called once activity/lifecycleOwner is destroyed

        for (UnsubscribeProvider provider : this.unsubscribeProviders) {
            provider.unsubscribe();
        }
        this.unsubscribeProviders.clear();
    }

    private List<LocationWithPicture> mapComics(
            List<LocationWithPicture> oldComics,
            List<Comic> comics,
            int titleWidth,
            int titleHeight) {

        Log.e("discViewModel", "Mapping new collected comics ... ");

        List<LocationWithPicture> locations = new ArrayList<>();

        for (var comic : comics) {

            Predicate<LocationWithPicture> filter = (oldItem) -> {
                return oldItem.getId().equals(comic.getId());
            };

            if (oldComics != null && oldComics.stream().anyMatch(filter)) {
                // reuse old comic
                locations.add(oldComics.stream().filter(filter).findFirst().get());
            } else {

                LocationWithPicture locWithPic = new LocationWithPicture(
                        comic.getId(),
                        comic.getLocation());

                locWithPic.setLivePic(new MutableLiveData<>());

                comicRepo.loadTitlePage(
                        comic.getId(),
                        titleWidth,
                        titleHeight,
                        (List<Bitmap> pages) -> {
                            if (pages == null || pages.size() == 0) {
                                Log.e("discViewModel", "Failed to load title page ... ");
                                return;
                            }

                            locWithPic.getLivePic().postValue(pages.get(0));
                            return;
                        });

                locations.add(locWithPic);
            }

        }

        return locations;
    }

}
