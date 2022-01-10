package mosis.comiccollector.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.repository.AuthRepository;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.PeopleLocationConsumer;
import mosis.comiccollector.repository.UnsubscribeProvider;
import mosis.comiccollector.ui.map.LocationWithPicture;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.DepProvider;

public class DiscoveryViewModel extends AndroidViewModel {

    private AuthRepository authRepo;
    private PeopleRepository peopleRepo;

    private MutableLiveData<List<LocationWithPicture>> nearbyFriends;
    private List<UnsubscribeProvider> unsubscribeProviders;

    private DataMapper<User, ViewUser> viewUserMapper;

    public DiscoveryViewModel(@NonNull Application application) {
        super(application);

        this.authRepo = DepProvider.getAuthRepository();
        this.peopleRepo = DepProvider.getPeopleRepository();

        this.viewUserMapper = DepProvider.getUserModelMapper();

        this.unsubscribeProviders = new ArrayList<>();
    }

    public String getMyId() {
        return authRepo.getCurrentUser().user.getUserId();
    }

    public MutableLiveData<List<LocationWithPicture>> getNearbyFriends(
            double lat, double lgt, double range) {

        if (this.nearbyFriends == null) {
            nearbyFriends = new MutableLiveData<>();
        }

        String userId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.getNearbyFriendsLocations(userId, lat, lgt, range,
                (List<UserLocation> friendLocks) -> {

                    if (friendLocks == null) {
                        Log.e("mapViewmodel", "Failed to retrieved friends for: " + userId);
                        nearbyFriends.postValue(null);
                        return;
                    }

                    List<LocationWithPicture> viewPeople = new ArrayList<>();

                    for (UserLocation userLoc : friendLocks) {

                        LocationWithPicture locWithPic = new LocationWithPicture(userLoc);
                        locWithPic.setLivePic(this.loadUserPic(userLoc.getUserId()));

                        viewPeople.add(locWithPic);
                    }

                    nearbyFriends.postValue(viewPeople);

                });

//        this.peopleRepo.

        return nearbyFriends;
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
            if (people != null && people.size() > 0) {

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
            for (LocationWithPicture locWithPicture : nearbyFriends.getValue()) {
                if (locWithPicture.getUserId().equals(userId)) {
                    return locWithPicture.getLivePic();
                }
            }
        }

        // TODO search trough other people list when i get implemented
        // not just friends

        return null;
    }

    public MutableLiveData<LocationWithPicture> getMyLastLocation() {
        MutableLiveData<LocationWithPicture> liveData = new MutableLiveData<>();

        String myId = authRepo.getCurrentUser().user.getUserId();

        this.peopleRepo.getLastLocation(myId, (List<UserLocation> locations) -> {
            if (locations != null && locations.size() > 0) {
                MutableLiveData<Bitmap> livePic = new MutableLiveData<>();
                livePic.postValue(null); // there is no need to display my pic on map

                LocationWithPicture location = new LocationWithPicture(locations.get(0));
                location.setLivePic(livePic);

                liveData.postValue(location);
            }
        });

        return liveData;
    }

    @Override
    protected void onCleared() {
        // will be called once activity/lifecycleOwner is destroyed

        for (UnsubscribeProvider provider : this.unsubscribeProviders) {
            provider.unsubscribe();
        }
        this.unsubscribeProviders.clear();
    }


}
