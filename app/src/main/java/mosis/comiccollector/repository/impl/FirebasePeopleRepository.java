package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mosis.comiccollector.model.UserFriendsList;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.repository.DoneHandler;
import mosis.comiccollector.repository.PeopleRepository;

public class FirebasePeopleRepository implements PeopleRepository {

    private static final String USER_INFO_PATH = "user_info";
    private static final String PROFILE_PIC_PATH = "profile_pics";

    private static final String USER_FRIENDS_PATH = "user_friends";

    private static final String USER_LOCATIONS_PATH = "user_locations";

    private static final String GEO_FIRESTORE_POINT_FIELD = "l";

    // firebase supports up to 10 items in whereIn query
    private static final int FIREBASE_WHEREIN_LIMIT = 10;

    // this looks bad ... static and all that ...


    private static ListenerRegistration friendsListenerRemover;
    private static EventListener<DocumentSnapshot> friendsListener;

    private static List<String> friends;

    private static GeoQuery peopleQuery;
    private static GeoQueryEventListener peopleQueryListener;

    private static PeopleUpdateHandler friendsHandler;
    private static PeopleUpdateHandler unknownPeopleHandler;

    @Override
    public void getLastLocation(String userId, @NonNull LocationsReady handleLocation) {

        FirebaseFirestore.getInstance()
            .collection(USER_LOCATIONS_PATH)
            .document(userId)
            .get()
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()
                    || !task.getResult().exists()
                    || task.getResult() == null) {

                    Log.e("PeopleRepo", "Failed to get last location for: " + userId);
                    handleLocation.handleLocations(new ArrayList<>());
                    return;
                }

                GeoPoint point = (GeoPoint) task.getResult().get(GEO_FIRESTORE_POINT_FIELD);
                handleLocation.handleLocations(Arrays.asList(
                    new UserLocation(
                        userId,
                        point.getLatitude(),
                        point.getLongitude())
                ));
            });
    }

    private void setupPeopleFollowing(String userId, GeoPoint point, double range) {

        if (friendsListener == null) {

            friendsListenerRemover = FirebaseFirestore.getInstance()
                .collection(USER_FRIENDS_PATH)
                .document(userId)
                .addSnapshotListener(this::friendsUpdate);

            friendsListener = this::friendsUpdate;
        }

        if (peopleQuery == null) {

            peopleQuery = new GeoFirestore(FirebaseFirestore
                .getInstance()
                .collection(USER_LOCATIONS_PATH))
                .queryAtLocation(point, range);

            peopleQueryListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(@NonNull String s, @NonNull GeoPoint geoPoint) {
                    if (isFriend(s) && friendsHandler != null) {
                        friendsHandler.personIn(s, geoPoint);
                    } else if (!s.equals(userId) && unknownPeopleHandler != null) {
                        unknownPeopleHandler.personIn(s, geoPoint);
                    }
                }

                @Override
                public void onKeyExited(@NonNull String s) {
                    if (isFriend(s) && friendsHandler != null) {
                        friendsHandler.personOut(s);
                    } else if (!s.equals(userId) && unknownPeopleHandler != null) {
                        unknownPeopleHandler.personOut(s);
                    }
                }

                @Override
                public void onKeyMoved(@NonNull String s, @NonNull GeoPoint geoPoint) {
                    if (isFriend(s) && friendsHandler != null) {
                        friendsHandler.personMoved(s, geoPoint);
                    } else if (!s.equals(userId) && unknownPeopleHandler != null) {
                        unknownPeopleHandler.personMoved(s, geoPoint);
                    }
                }

                @Override
                public void onGeoQueryReady() {
                    if (friendsHandler != null) {
                        friendsHandler.everyoneLoaded();
                    }
                    if (unknownPeopleHandler != null) {
                        unknownPeopleHandler.everyoneLoaded();
                    }
                }

                @Override
                public void onGeoQueryError(@NonNull Exception e) {
                    Log.e("peopleRepo", "Query error: " + e.getMessage());
                    if (friendsHandler != null) {
                        friendsHandler.error(e.toString());
                    }
                    if (unknownPeopleHandler != null) {
                        unknownPeopleHandler.error(e.toString());
                    }
                }
            };

            peopleQuery.addGeoQueryEventListener(peopleQueryListener);
        }


        if (peopleQuery.getCenter() != point) {
            peopleQuery.setCenter(point);
        }

        if (peopleQuery.getRadius() != range) {
            peopleQuery.setRadius(range);
        }

    }

    public void getNearbyFriends(String userId, GeoPoint point, double range,
                                 @NotNull PeopleUpdateHandler onFriendsUpdate) {

        friendsHandler = onFriendsUpdate;

        setupPeopleFollowing(userId, point, range);
    }

    @Override
    public void getNearbyUnknownPeople(String userId, GeoPoint point, double range,
                                       @NonNull PeopleUpdateHandler onPeopleUpdate) {

        unknownPeopleHandler = onPeopleUpdate;

        setupPeopleFollowing(userId, point, range);
    }

    private void friendsUpdate(
        @Nullable DocumentSnapshot value,
        @Nullable FirebaseFirestoreException error) {

        if (error != null || value == null) {
            Log.e("peopleRepo", "Got value as friends list update ... ");
            return;
        }

        var friendsObj = value.toObject(UserFriendsList.class);
        if (friendsObj == null) {
            return;
        }

        friends = friendsObj.friendsIds;


        if (peopleQuery != null
            && peopleQueryListener != null
            && friendsHandler != null) {
            // if friends handler is null there is no point triggering this
            // because nobody is listening for friends

            Log.e("peopleRepo", "Friends updated, reloading query handlers ... ");

            // this SHOULD trigger geoQueryListener for each user inside given region
            peopleQuery.removeGeoQueryEventListener(peopleQueryListener);
            peopleQuery.addGeoQueryEventListener(peopleQueryListener);
        }

    }

    @Override
    public void updatePeopleRadius(GeoPoint point, double radius) {
        if (peopleQuery != null) {
            peopleQuery.setLocation(point, radius);
        }
    }

    @Override
    public void stopFollowingPeople(String userId) {
        if (friendsListenerRemover != null) {
            friendsListenerRemover.remove();

            friendsListenerRemover = null;
            friendsListener = null;
        }

        if (friends != null) {
            friends.clear();
        }

        if (peopleQuery != null) {
            peopleQuery.removeAllListeners();
            peopleQuery = null;
            peopleQueryListener = null;
        }

    }

    private boolean isFriend(String id) {
        return friends != null
            && friends.size() > 0
            && friends.stream().anyMatch((friend) -> friend.equals(id));
    }

    @Override
    public void getFriends(String userId, @NonNull PeopleReady peopleHandler) {

        FirebaseFirestore.getInstance()
            .collection(USER_FRIENDS_PATH)
            .document(userId)
            .get()
            .addOnCompleteListener((Task<DocumentSnapshot> task) -> {

                if (!task.isSuccessful()
                    || !task.getResult().exists()
                    || task.getResult() == null) {

                    Log.e("peopleRepo", "Failed to get friendsIds for: " + userId);
                    peopleHandler.handlePeople(Collections.emptyList());

                    return;
                }

                UserFriendsList friendsObj = task
                    .getResult()
                    .toObject(UserFriendsList.class);

                if (friendsObj == null
                    || friendsObj.friendsIds == null
                    || friendsObj.friendsIds.size() == 0) {

                    peopleHandler.handlePeople(Collections.emptyList());
                    return;
                }

                var idSegments = splitSegments(
                    friendsObj.friendsIds,
                    FIREBASE_WHEREIN_LIMIT);

                List<Task<QuerySnapshot>> segmentTasks = new ArrayList<>();
                for (var idSegment : idSegments) {
                    segmentTasks.add(
                        FirebaseFirestore.getInstance()
                            .collection(USER_INFO_PATH)
                            .whereIn(User.USER_ID_FIELD, friendsObj.friendsIds)
                            .get());
                }

                Tasks
                    .whenAllComplete(segmentTasks)
                    .addOnCompleteListener((@NonNull Task<List<Task<?>>> segmentTask) -> {
                        if (!segmentTask.isSuccessful()) {
                            peopleHandler.handlePeople(Collections.emptyList());
                            return;
                        }

                        List<User> data = joinTaskResults(
                            segmentTask.getResult(),
                            User.class);
                        peopleHandler.handlePeople(data);

                        return;
                    });

            });

    }

    @Override
    public void getUnknownPeople(String userId, @NonNull PeopleReady peopleHandler) {
        FirebaseFirestore.getInstance()
            .collection(USER_FRIENDS_PATH)
            .document(userId)
            .get()
            .addOnCompleteListener((@NonNull Task<DocumentSnapshot> friendsTask) -> {

                List<String> friendIds = new ArrayList<>();
                friendIds.add("-1"); // never matching id

                if (friendsTask.isSuccessful()
                    && friendsTask.getResult().exists()
                    && friendsTask.getResult() != null) {

                    var friendsObj = friendsTask
                        .getResult()
                        .toObject(UserFriendsList.class);

                    friendIds.addAll(friendsObj.friendsIds);
                }

                var idSegments = splitSegments(
                    friendIds,
                    FIREBASE_WHEREIN_LIMIT);

                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (var idSegment : idSegments) {
                    tasks.add(
                        FirebaseFirestore.getInstance()
                            .collection(USER_INFO_PATH)
                            .whereNotIn(User.USER_ID_FIELD, idSegment)
                            .get());
                }

                Tasks
                    .whenAllComplete(tasks)
                    .addOnCompleteListener((@NonNull Task<List<Task<?>>> resultTask) -> {
                        if (!resultTask.isSuccessful()) {
                            peopleHandler.handlePeople(Collections.emptyList());
                            return;
                        }

                        List<User> people = joinTaskResults(
                            resultTask.getResult(),
                            User.class);
                        people.removeIf((person) -> person.getUserId().equals(userId));

                        peopleHandler.handlePeople(people);

                        return;
                    });
            });

    }

    @Override
    public void updateLocation(String userId, UserLocation newLocation) {

        new GeoFirestore(FirebaseFirestore.getInstance().collection(USER_LOCATIONS_PATH))
            .setLocation(userId, new GeoPoint(
                newLocation.getLatitude(),
                newLocation.getLongitude()));

//        FirebaseFirestore.getInstance()
//                .collection(USER_LOCATIONS_PATH)
//                .document(userId)
//                .set(newLocation);
    }

    @Override
    public void createUser(User newUser, @NotNull PeopleReady doneHandler) {
        FirebaseFirestore
            .getInstance()
            .collection(USER_INFO_PATH)
            .document(newUser.getUserId())
            .set(newUser)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e("peopleRepo", "Failed with firestore ... "
                        + task.getException().getMessage());

                    doneHandler.handlePeople(null);

                    return;
                }

                Log.e("peopleRepo", "New doc added for new user ... ");
                List<User> retList = new ArrayList<User>();
                retList.add(newUser);

                doneHandler.handlePeople(retList);

                return;
            });

    }

    @Override
    public void getUser(String userId, @NotNull PeopleReady doneHandler) {
        FirebaseFirestore
            .getInstance()
            .collection(USER_INFO_PATH)
            .document(userId)
            .get()
            .addOnCompleteListener((task) -> {
                if (task.isSuccessful()) {

                    List<User> retList = new ArrayList<User>();
                    retList.add(task.getResult().toObject(User.class));

                    doneHandler.handlePeople(retList);

                } else {
                    Log.e("peopleRepo", "Failed to get user ... ");
                    doneHandler.handlePeople(null);
                }
            });
    }

    @Override
    public void loadProfilePic(String userId, @NotNull PicReady onPicReady) {

        // .child is notNull
//        StorageReference reference = FirebaseStorage
//                .getInstance()
//                .getReference(PROFILE_PIC_PATH)
//                .child(userId);

        this.getUser(userId, (List<User> people) -> {

            if (people == null) {
                Log.e("peopleRepo", "Failed to load profile pic ... ");
                onPicReady.handlePic(null);
                return;
            }

            String uri = people.get(0).getProfilePicUri();

            // TODO maybe encapsulate imageLoader in custom interface
            // so it can be provided with depProvider
            ImageLoader.getInstance().loadImage(uri, new MyImageListener(onPicReady));

        });

        return;

//        try {
//
//            final File tempFile = File.createTempFile(
//                    LOCAL_PIC_CACHE_PREFIX,
//                    LOCAL_PIC_CACHE_SUFFIX + userId);
//            Log.e("loadPic", "Created temp file: " + tempFile.getName());
//            tempFile.deleteOnExit();
//
//            reference.getFile(tempFile).addOnCompleteListener(
//                    (Task<FileDownloadTask.TaskSnapshot> task) -> {
//
//                        if (task.isSuccessful()) {
//                            Log.e("FirebaseUserManager", "onSuccess: FETCHING DONE ");
//
////                            onPicReady.handlePic(Uri.fromFile(tempFile).toString());
//                        } else {
//                            Log.e("FirebaseUserManager", "failed to load profile pic ... "
//                                    + task.getException().toString());
//
//                            tempFile.delete();
//                            onPicReady.handlePic(null);
//                        }
//                    });
//
//
//        } catch (IOException e) {
//            Log.e("picLoad", "IOExc while loading pic: " + e.getMessage());
//
//            onPicReady.handlePic(null);
//        } catch (Exception e) {
//            Log.e("picLoad", "Exc while loading pic: " + e.getMessage());
//
//            onPicReady.handlePic(null);
//        }

    }

    @Override
    public void uploadProfilePic(String userId, String picUri, @NonNull UriReady uriHandler) {
        Log.w("FirebaseUserManager", "uploadProfilePic: UPLOADING IMAGE ");

        Uri uri = Uri.parse(picUri);

        StorageReference storage = FirebaseStorage.getInstance()
            .getReference(PROFILE_PIC_PATH)
            .child(userId);

        storage.putFile(uri).addOnCompleteListener(
            (Task<UploadTask.TaskSnapshot> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("pic upload", "Failed to upload pic... ");
                    uriHandler.handleUri(null);
                    return;
                }

                Log.e("pic upload", "Pic uploaded successfully ... ");

                storage.getDownloadUrl().addOnCompleteListener((@NonNull Task<Uri> task1) -> {
                    if (!task1.isSuccessful()) {
                        Log.e("PeopleRepo", "Failed to get profile pic download uri ... ");
                        uriHandler.handleUri(null);
                        return;
                    }

                    uriHandler.handleUri(task1.getResult().toString());

                });

            });
    }

    @Override
    public void updatePicUri(String userId, String picUri, @NonNull UriReady uriHandler) {

        CollectionReference userCollection = FirebaseFirestore
            .getInstance()
            .collection(USER_INFO_PATH);


        userCollection
            .document(userId)
            .update(User.PROFILE_PIC_FIELD, picUri)
            .addOnCompleteListener((@NonNull Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e("PeopleRepo", "Failed to update poc uri in user info ... ");
                    uriHandler.handleUri(null);
                    return;
                }

                uriHandler.handleUri(picUri);
            });
    }

    @Override
    public void makeFriends(String user_1, String user_2, DoneHandler doneHandler) {

        updateFriends(user_1, user_2, (err_1) -> {
            if (err_1 != null) {
                Log.e("peopleRepo", "Err occurred while updating friends ... ");
                doneHandler.handleDone(err_1);
                return;
            }

            updateFriends(user_2, user_1, (err_2) -> {
                if (err_2 != null) {
                    Log.e("peopleRepo", "Err occurred while updating friends ... ");
                    doneHandler.handleDone(err_2);
                    return;
                }

                doneHandler.handleDone(null);
            });

        });

    }

    private void updateFriends(String user, String newFriend, DoneHandler handler) {
        FirebaseFirestore.getInstance()
            .collection(USER_FRIENDS_PATH)
            .document(user)
            .update(UserFriendsList.FRIENDS_IDS_FIELD, FieldValue.arrayUnion(newFriend))
            .addOnCompleteListener((task_1) -> {
                if (!task_1.isSuccessful()) {
                    Log.e("peopleRepo", "Failed to UPDATE friends list ... ");

                    FirebaseFirestore.getInstance()
                        .collection(USER_FRIENDS_PATH)
                        .document(user)
                        .set(new UserFriendsList(user, Arrays.asList(newFriend)))
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.e("peopleRepo", "Failed to CREATE friends list ... ");
                                if (task.getException() != null) {
                                    handler.handleDone(task.getException().getMessage());
                                } else {
                                    handler.handleDone("Some err ... ");
                                }
                                return;
                            }

                            handler.handleDone(null);
                        });

                    return;
                }

                handler.handleDone(null);
            });
    }

    @Override
    public void sendFriendRequest(String sender, String receiver, DoneHandler doneHandler) {

    }

    @Override
    public void updateRating(String userId, float newRating, DoneHandler onDone) {
        FirebaseFirestore.getInstance()
            .collection(USER_INFO_PATH)
            .document(userId)
            .update(User.RATING_FIELD, newRating)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e("peopleRepo", "Failed to update author rating ... ");
                    var err = "Unknown err ... ";
                    if (task.getException() != null) {
                        err = task.getException().getMessage();
                    }
                    onDone.handleDone(err);
                    return;
                }

                onDone.handleDone(null);
            });
    }

    private List<List<String>> splitSegments(List<String> input, int size) {
        List<List<String>> regions = new ArrayList<>();

        int starting = 0;
        // second index in list.sublist(starting, ending) is not included in resulting list
        int lastIndex = input.size();

        int ending;
        while (starting < lastIndex) {
            ending = starting + size;
            if (ending > lastIndex) {
                ending = lastIndex;
            }

            regions.add(input.subList(starting, ending));

            starting += size;
        }

        return regions;

    }

    private <T> List<T> joinTaskResults(List<Task<?>> results, Class T) {
        List<T> data = new ArrayList<>();

        for (var resultTask : results) {
            if (!resultTask.isSuccessful() || resultTask.getResult() == null) {
            } else {
                data.addAll(((Task<QuerySnapshot>) resultTask).getResult().toObjects(T));
            }
        }

        return data;
    }

}
