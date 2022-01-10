package mosis.comiccollector.repository.impl;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    @Override
    public void getLastLocation(String userId, @NonNull LocationsReady handleLocation) {
        FirebaseFirestore.getInstance()
                .collection(USER_LOCATIONS_PATH)
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("PeopleRepo", "Failed to get last location for: " + userId);
                            handleLocation.handleLocations(new ArrayList<>());
                            return;
                        }

                        List<UserLocation> locations = new ArrayList<>();
                        locations.add(task.getResult().toObject(UserLocation.class));
                        handleLocation.handleLocations(locations);
                    }
                });
    }

    @Override
    public void getNearbyFriendsLocations(String userId,
                                          double lat,
                                          double lgt,
                                          double range,
                                          @NotNull LocationsReady locationHandler) {

        FirebaseFirestore.getInstance()
                .collection(USER_FRIENDS_PATH)
                .document(userId)
                .get()
                .addOnCompleteListener((docSnapshotTask) -> {

                    if (!docSnapshotTask.isSuccessful()) {
                        // TODO handle failure
                        locationHandler.handleLocations(null);
                        return;
                    }

                    UserFriendsList friendsObj = docSnapshotTask
                            .getResult()
                            .toObject(UserFriendsList.class);
                    Log.e("peopleRepo", "He got: " + friendsObj.friendsIds.size() + " friends ... ");

                    FirebaseFirestore.getInstance()
                            .collection(USER_LOCATIONS_PATH)
                            .whereIn(UserLocation.USER_ID_FIELD, friendsObj.friendsIds)
                            .get()
                            .addOnCompleteListener((querySnapshotTask) -> {

                                if (!querySnapshotTask.isSuccessful()) {
                                    // TODO handle failure
                                    Log.e("peopleRepo", "Failed to query ... ");
                                    locationHandler.handleLocations(new ArrayList<>());

                                    return;
                                }

                                int size = querySnapshotTask
                                        .getResult()
                                        .size();

                                List<UserLocation> userLocations = querySnapshotTask
                                        .getResult()
                                        .toObjects(UserLocation.class);

                                locationHandler.handleLocations(userLocations);
                            });

                });

    }

    @Override
    public void getFriends(String userId, @NonNull PeopleReady peopleHandler) {

        FirebaseFirestore.getInstance()
                .collection(USER_FRIENDS_PATH)
                .document(userId)
                .get()
                .addOnCompleteListener((Task<DocumentSnapshot> task) -> {

                    if (!task.isSuccessful()) {

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
                            });

                });

    }

    @Override
    public void updateLocation(String userId, UserLocation newLocation) {
        FirebaseFirestore.getInstance()
                .collection(USER_LOCATIONS_PATH)
                .document(userId)
                .set(newLocation);

//        FirebaseDatabase
//                .getInstance()
//                .getReference(USER_LOCATIONS_PATH)
//                .child(userId)
//                .setValue(newLocation);
    }

    @Override
    public void createUser(User newUser, @NotNull PeopleReady doneHandler) {
        FirebaseFirestore
                .getInstance()
                .collection(USER_INFO_PATH)
                .document(newUser.getUserId())
                .set(newUser)
                .addOnCompleteListener(
                        (task) -> {
                            if (task.isSuccessful()) {
                                Log.e("peopleRepo", "New doc added for new user ... ");
                                List<User> retList = new ArrayList<User>();
                                retList.add(newUser);

                                doneHandler.handlePeople(retList);
                            } else {
                                Log.e("peopleRepo", "Failed with firestore ... "
                                        + task.getException().getMessage());

                                doneHandler.handlePeople(null);
                            }
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

}
