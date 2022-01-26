package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.List;

import mosis.comiccollector.model.UserFriendsList;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.repository.PeopleRepository;
import mosis.comiccollector.repository.UnsubscribeProvider;
import mosis.comiccollector.repository.PeopleLocationConsumer;

public class FirebasePeopleRepository implements PeopleRepository {

    private static final String USER_INFO_PATH = "user_info";
    private static final String PROFILE_PIC_PATH = "profile_pics";

    private static final String USER_FRIENDS_PATH = "user_friends";

    private static final String USER_LOCATIONS_PATH = "user_locations";

    private static final String GEO_FIRESTORE_POINT_FIEL = "l";

    // this looks bad ... static and all that ...
    private static List<String> friends;
    private static GeoQuery peopleQuery;

    @Override
    public void getLastLocation(String userId, @NonNull LocationsReady handleLocation) {

//        new GeoFirestore(FirebaseFirestore.getInstance().collection(USER_LOCATIONS_PATH))
//                // "srz2xcpg22" - geo cache
//                .getLocation(userId, (@Nullable GeoPoint geoPoint, @Nullable Exception e) -> {
//                    if (e != null) {
//                        Log.e("peopleRepo", "Failed to get MY last location: " + e.getMessage());
//                        handleLocation.handleLocations(Collections.emptyList());
//                        return;
//                    }
//
//                    handleLocation.handleLocations(Arrays.asList(
//                            new UserLocation(
//                                    userId,
//                                    geoPoint.getLatitude(),
//                                    geoPoint.getLongitude()
//                            )));
//                });

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

                    GeoPoint point = (GeoPoint) task.getResult().get(GEO_FIRESTORE_POINT_FIEL);
                    handleLocation.handleLocations(Arrays.asList(
                            new UserLocation(
                                    userId,
                                    point.getLatitude(),
                                    point.getLongitude())
                    ));
                });
    }

    @Override
    public void getNearbyFriendsLocations(String userId,
                                          GeoPoint point,
                                          double range,
                                          @NotNull PeopleUpdateHandler onFriendsUpdate) {

        FirebaseFirestore.getInstance()
                .collection(USER_FRIENDS_PATH)
                .document(userId)
                .get()
                .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                    if (!task.isSuccessful()
                            || !task.getResult().exists()
                            || task.getResult() == null) {

                        Log.e("peopleRepo", "Failed to load friends ids ... ");
                        onFriendsUpdate.error("Some random error ... ");
                        return;
                    }

                    friends = task.getResult()
                            .toObject(UserFriendsList.class)
                            .friendsIds;

                    peopleQuery = new GeoFirestore(FirebaseFirestore
                            .getInstance()
                            .collection(USER_LOCATIONS_PATH))
                            .queryAtLocation(point, range);

                    peopleQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(@NonNull String s, @NonNull GeoPoint geoPoint) {
                            if (isFriend(s)) {
                                onFriendsUpdate.personIn(s, geoPoint);
                            }
                        }

                        @Override
                        public void onKeyExited(@NonNull String s) {
                            if (isFriend(s)) {
                                onFriendsUpdate.personOut(s);
                            }
                        }

                        @Override
                        public void onKeyMoved(@NonNull String s, @NonNull GeoPoint geoPoint) {
                            if (isFriend(s)) {
                                onFriendsUpdate.personMoved(s, geoPoint);
                            }

                        }

                        @Override
                        public void onGeoQueryReady() {
                            onFriendsUpdate.everyoneLoaded();
                        }

                        @Override
                        public void onGeoQueryError(@NonNull Exception e) {
                            onFriendsUpdate.error(e.getMessage());
                        }
                    });

                });

    }

    @Override
    public void updateFriendsRadius(GeoPoint point, double radius) {
        // TODO do the same for unknown people
        if (peopleQuery != null) {
            peopleQuery.setLocation(point, radius);
        }
    }

    @Override
    public void getNearbyPeopleLocations(String userId, GeoPoint point, double range,
                                         @NonNull PeopleUpdateHandler onPeopleUpdate) {

        FirebaseFirestore.getInstance()
                .collection(USER_FRIENDS_PATH)
                .document(userId)
                .get()
                .addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
                    if (!task.isSuccessful()) {
                        Log.e("peopleRepo", "Failed to load friends ids ... ");
                        onPeopleUpdate.error("Some random error ... ");
                        return;
                    }

                    // this should be empty array but firebase doesn't support
                    // whereNotIn query on an empty array so "-1" should be Id
                    // impossible to match ...
                    friends = Arrays.asList("-1");

                    if (task.getResult().exists() && task.getResult() != null) {
                        friends = task.getResult()
                                .toObject(UserFriendsList.class)
                                .friendsIds;
                    }

                    peopleQuery = new GeoFirestore(FirebaseFirestore
                            .getInstance()
                            .collection(USER_LOCATIONS_PATH))
                            .queryAtLocation(point, range);

                    peopleQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        @Override
                        public void onKeyEntered(@NonNull String s, @NonNull GeoPoint geoPoint) {
                            if (!isFriend(s) && !s.equals(userId)) {
                                onPeopleUpdate.personIn(s, geoPoint);
                            }
                        }

                        @Override
                        public void onKeyExited(@NonNull String s) {
                            if (!isFriend(s) && !s.equals(userId)) {
                                onPeopleUpdate.personOut(s);
                            }
                        }

                        @Override
                        public void onKeyMoved(@NonNull String s, @NonNull GeoPoint geoPoint) {
                            if (!isFriend(s) && !s.equals(userId)) {
                                onPeopleUpdate.personMoved(s, geoPoint);
                            }

                        }

                        @Override
                        public void onGeoQueryReady() {
                            onPeopleUpdate.everyoneLoaded();
                        }

                        @Override
                        public void onGeoQueryError(@NonNull Exception e) {
                            onPeopleUpdate.error(e.getMessage());
                        }
                    });

                });
    }

    @Override
    public void updatePeopleRadius(GeoPoint point, double radius) {

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

                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        Log.e("peopleRepo", "Failed to get friendsIds for: " + userId);
                        peopleHandler.handlePeople(null);

                        return;
                    }

                    UserFriendsList friendsObj = task
                            .getResult()
                            .toObject(UserFriendsList.class);

                    FirebaseFirestore.getInstance()
                            .collection(USER_INFO_PATH)
                            .whereIn(User.USER_ID_FIELD, friendsObj.friendsIds)
                            .get()
                            .addOnCompleteListener((Task<QuerySnapshot> friendsTask) -> {
                                if (!friendsTask.isSuccessful()) {
                                    Log.e("peopleRepo", "Failed to retrieve friends for: " + userId);
                                    peopleHandler.handlePeople(null);
                                    return;
                                }

                                peopleHandler.handlePeople(friendsTask
                                        .getResult()
                                        .toObjects(User.class));

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

    // TODO should be removed when geoFirestore get fully implemented
    @Override
    public UnsubscribeProvider subscribeForLocUpdates(String userId, @NotNull PeopleLocationConsumer locHandler) {

        ListenerRegistration lReg = FirebaseFirestore.getInstance()
                .collection(USER_LOCATIONS_PATH)
                .document(userId)
                .addSnapshotListener(
                        (@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) -> {

                            if (error != null) {
                                Log.e("peopleRepo", "Location update failed ... "
                                        + error.getMessage());
                                locHandler.updateUserLocation(null);

                                return;
                            }

                            locHandler.updateUserLocation(value.toObject(UserLocation.class));

                        });

        return new FirebaseUnsubProvider(userId, lReg);
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
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
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
                                    }
                                });

                        return;
                    }

                    handler.handleDone(null);
                });
    }


    @Override
    public void sendFriendRequest(String sender, String receiver, DoneHandler doneHandler) {

    }

}
