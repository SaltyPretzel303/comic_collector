package mosis.comiccollector.repository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;

public interface PeopleRepository {

    interface SearchCondition {
        boolean shouldInclude(User user);
    }

    interface PeopleReady {
        void handlePeople(List<User> people);
    }

    interface PicReady {
        void handlePic(String picUri);
    }

    interface LocationsReady {
        void handleLocations(List<UserLocation> locations);
    }

    void getLastLocation(String userId,@NotNull LocationsReady handleLocation);

    void getNearbyFriendsLocations(String userId,
                                   double lat, double lgt, double range,
                                   @NotNull LocationsReady handleLocations);

    void updateLocation(String userId, UserLocation newLocation);

    void addUser(User newUser, @NotNull PeopleReady doneHandler);

    void getUser(String userId, @NotNull PeopleReady doneHandler);

    void loadProfilePic(String userId, @NotNull PicReady picHandler);

    void uploadProfilePic(String userId, String picUri, @NotNull PicReady picHandler);

    void updatePicUri(String userId, String picUri, @NotNull PicReady picHandler);

    UnsubscribeProvider subscribeForLocUpdates(String userId, PeopleLocationConsumer locHandler);

}
