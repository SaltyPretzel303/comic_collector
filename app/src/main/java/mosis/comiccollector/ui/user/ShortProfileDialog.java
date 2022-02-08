package mosis.comiccollector.ui.user;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.map.PersonFollower;
import mosis.comiccollector.ui.map.ShortMapDialog;
import mosis.comiccollector.util.Distance;

public class ShortProfileDialog
    extends Dialog
    implements LocationConsumer, PersonFollower {

    public interface FriendRequestHandler {
        void friendRequest(String userId);
    }

    private static final String VIEW_PROFILE_STRING = "View Profile";
    private static final String BE_FRIENDS_STRING = "Be Friends";
    private static final String SHOW_ON_MAP_STRING = "Show on Map";

    private static final int MAX_BE_FRIENDS_DISTANCE_M = 20; // 20,

    private enum LoadStages {
        DataLoaded, ImageLoaded
    }

    private final Context context;

    private final List<LoadStages> loadStages;

    private final MutableLiveData<ViewUser> liveUserData;

    private Button actionButton;

    private LiveData<UserLocation> myLiveLocation;
    private UserLocation myLastLocation;

    private LiveData<UserLocation> personLiveLocation;
    private UserLocation personLastLocation;

    private FriendRequestHandler friendReqHandler;

    private boolean areFriends;
    private LiveData<UserLocation> liveUserLocation;

    public ShortProfileDialog(@NonNull Context context,
                              ViewUser user,
                              LiveData<UserLocation> userLocation) {
        super(context);

        this.context = context;
        this.liveUserData = new MutableLiveData<>();
        this.liveUserData.postValue(user);
        this.loadStages = new ArrayList<>();

        this.liveUserLocation = userLocation;
    }

    public ShortProfileDialog(
        @NonNull Context context,
        MutableLiveData<ViewUser> liveData) {
        super(context);

        this.context = context;
        this.liveUserData = liveData;
        this.loadStages = new ArrayList<>();

        this.areFriends = true;
    }

    public ShortProfileDialog(
        @NonNull Context context,
        MutableLiveData<ViewUser> liveData,
        LiveData<UserLocation> personLocation,
        LiveData<UserLocation> myLocation,
        FriendRequestHandler reqHandler) {
        this(context, liveData);

        this.myLiveLocation = myLocation;
        this.personLiveLocation = personLocation;
        this.friendReqHandler = reqHandler;

        this.areFriends = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.people_from_map_dialog);

        this.actionButton = findViewById(R.id.open_profile_people_map);
        this.actionButton.setEnabled(false);

        this.liveUserData.observe((LifecycleOwner) context, this::initViews);

        if (myLiveLocation != null) {
            myLiveLocation.observe((LifecycleOwner) context, (loc) -> {
                Log.e("shortProfile", "Got my last location ... ");
                if (myLastLocation == null) {
                    // location service still has not computed my location
                    consumeMyLocation(loc);
                }
            });
        }

        if (personLiveLocation != null) {
            personLiveLocation.observe((LifecycleOwner) context, (loc) -> {
                Log.e("shortProfile", "Got person last location ... ");
                if (personLastLocation == null) {
                    consumePersonLocation(loc);
                }
            });
        }

        if (liveUserLocation != null) {
            actionButton.setText(SHOW_ON_MAP_STRING);
            actionButton.setEnabled(false);

            liveUserLocation.observe((LifecycleOwner) context, (userLocation) -> {
                actionButton.setOnClickListener(this::showOnMapClick);
                actionButton.setEnabled(true);
            });

        } else {

            if (areFriends) {
                actionButton.setText(VIEW_PROFILE_STRING);
                actionButton.setOnClickListener(this::showProfileCLick);
            } else {
                actionButton.setText(BE_FRIENDS_STRING);
                actionButton.setOnClickListener(this::beFriendsClick);
            }
        }

    }

    private void initViews(ViewUser user) {
        this.loadStages.add(LoadStages.DataLoaded);

        Bitmap defaultBitmap = BitmapFactory.decodeResource(
            context.getResources(),
            R.drawable.profile_pic);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
            context.getResources(),
            defaultBitmap);
        roundedBitmapDrawable.setCircular(true);
        ((ImageView) findViewById(R.id.profile_pic_people_map))
            .setImageDrawable(roundedBitmapDrawable);

        user.liveProfilePic.observe((LifecycleOwner) context, this::setupProfilePic);

        ((TextView) findViewById(R.id.username_people_map)).setText(user.name);

        ((RatingBar) findViewById(R.id.people_from_map_rating_rb)).setRating(user.rating);

//        ((ProgressBar) findViewById(R.id.rating_people_map_pb)).setProgress(user.rating);
//        ((TextView) findViewById(R.id.rating_people_map_tv)).setText(user.rating + "/100");

        if (this.loadStages.contains(LoadStages.DataLoaded)
            && this.loadStages.contains(LoadStages.ImageLoaded)
            && areFriends) {

            // if areFriends != true button will be activated from location consumer methods

            this.actionButton.setEnabled(true);
        }
    }

    private void setupProfilePic(Bitmap pic) {
        this.loadStages.add(LoadStages.ImageLoaded);

        if (pic != null) {

            ImageView imageView = findViewById(R.id.profile_pic_people_map);
            RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(
                context.getResources(),
                pic);
            rbd.setCircular(true);
            imageView.setImageDrawable(rbd);

//            ((ImageView) findViewById(R.id.profile_pic_people_map)).setImageBitmap(pic);
        }

        if (this.loadStages.contains(LoadStages.DataLoaded)
            && this.loadStages.contains(LoadStages.ImageLoaded)
            && areFriends) {

            this.actionButton.setEnabled(true);
        }

    }

    private void showProfileCLick(View v) {
        ViewUser vUser = this.liveUserData.getValue();

        Intent profileIntent = new Intent(context, ProfileActivity.class);
        profileIntent.putExtra(ProfileActivity.USER_DATA_EXTRA, vUser);
        profileIntent.putExtra(ProfileActivity.IS_FRIEND_EXTRA, areFriends);
        context.startActivity(profileIntent);
    }

    private void beFriendsClick(View v) {
        if (friendReqHandler != null
            && liveUserData != null
            && liveUserData.getValue() != null) {
            friendReqHandler.friendRequest(liveUserData.getValue().userId);
        }
    }

    private void showOnMapClick(View v) {
        if (liveUserLocation.getValue() != null) {
            var loc = new Location(
                liveUserLocation.getValue().getLatitude(),
                liveUserLocation.getValue().getLongitude());

            var mapDialog = new ShortMapDialog(context, loc);
            mapDialog.show();
        }
    }

    @Override
    public void consumeMyLocation(UserLocation newLocation) {
        this.myLastLocation = newLocation;
        if (!areFriends && actionButton != null) {
            double distance = ourDistance();
            actionButton.setEnabled(ourDistance() < MAX_BE_FRIENDS_DISTANCE_M);
        }
    }

    @Override
    public String getPersonId() {
        if (liveUserData.getValue() != null) {
            return liveUserData.getValue().userId;
        }

        return "-1"; // impossible id
    }

    @Override
    public void consumePersonLocation(UserLocation newLocation) {
        this.personLastLocation = newLocation;
        if (!areFriends && actionButton != null) {
            double distance = ourDistance();
            actionButton.setEnabled(ourDistance() < MAX_BE_FRIENDS_DISTANCE_M);
        }
    }

    private double ourDistance() {
        if (myLastLocation != null && personLastLocation != null) {
            double distance = Distance.calculateInKm(
                myLastLocation.getLatitude(),
                myLastLocation.getLongitude(),
                personLastLocation.getLatitude(),
                personLastLocation.getLongitude());

            Log.e("shortUser", "Distance: " + distance);

            return distance;
        }

        // just some value that wont trigger collect button activation
        return 100 * MAX_BE_FRIENDS_DISTANCE_M;
    }
}
