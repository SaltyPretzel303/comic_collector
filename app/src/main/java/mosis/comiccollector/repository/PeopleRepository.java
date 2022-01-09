package mosis.comiccollector.repository;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;

public interface PeopleRepository {

    interface PeopleReady {
        void handlePeople(List<User> people);
    }

    interface UriReady {
        void handleUri(String uri);
    }

    interface PicReady {
        void handlePic(Bitmap picUri);
    }

    interface LocationsReady {
        void handleLocations(List<UserLocation> locations);
    }

    void getLastLocation(String userId, @NotNull LocationsReady handleLocation);

    void getNearbyFriendsLocations(String userId,
                                   double lat, double lgt, double range,
                                   @NotNull LocationsReady handleLocations);

    void getFriends(String userId, @NotNull PeopleReady peopleHandler);

    void updateLocation(String userId, UserLocation newLocation);

    void createUser(User newUser, @NotNull PeopleReady doneHandler);

    void getUser(String userId, @NotNull PeopleReady doneHandler);

    void loadProfilePic(String userId, @NotNull PicReady picHandler);

    void uploadProfilePic(String userId, String picUri, @NotNull UriReady uriHandler);

    void updatePicUri(String userId, String picUri, @NotNull UriReady uriHandler);

    UnsubscribeProvider subscribeForLocUpdates(String userId, PeopleLocationConsumer locHandler);

}
