package mosis.comiccollector.repository;

import android.graphics.Bitmap;

import com.google.firebase.firestore.GeoPoint;

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
        void handlePic(Bitmap pic);
    }

    interface LocationsReady {
        void handleLocations(List<UserLocation> locations);
    }

    interface PeopleUpdateHandler {
        void personIn(String id, GeoPoint loc);

        void personOut(String id);

        void personMoved(String id, GeoPoint loc);

        void everyoneLoaded();

        void error(String err);
    }

    interface DoneHandler {
        void handleDone(String err);
    }

    void getLastLocation(String userId, @NotNull LocationsReady handleLocation);

    void getNearbyFriendsLocations(String userId, GeoPoint point, double range,
                                   @NotNull PeopleUpdateHandler onFriendsUpdate);

    void updateFriendsRadius(GeoPoint point, double radius);


    void getNearbyPeopleLocations(String userId, GeoPoint point, double range,
                                  @NotNull PeopleUpdateHandler onPeopleUpdate);

    void updatePeopleRadius(GeoPoint point, double radius);

    void getFriends(String userId, @NotNull PeopleReady peopleHandler);

    void updateLocation(String userId, UserLocation newLocation);

    void createUser(User newUser, @NotNull PeopleReady doneHandler);

    void getUser(String userId, @NotNull PeopleReady doneHandler);

    void loadProfilePic(String userId, @NotNull PicReady picHandler);

    void uploadProfilePic(String userId, String picUri, @NotNull UriReady uriHandler);

    void updatePicUri(String userId, String picUri, @NotNull UriReady uriHandler);

    UnsubscribeProvider subscribeForLocUpdates(String userId, PeopleLocationConsumer locHandler);

    void makeFriends(String user_1, String user_2, DoneHandler doneHandler);

    void sendFriendRequest(String sender, String receiver, DoneHandler doneHandler);

}
