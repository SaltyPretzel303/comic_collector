package mosis.comiccollector.ui.user;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.map.PersonFollower;
import mosis.comiccollector.util.Distance;

public class ShortProfileDialog
        extends Dialog
        implements LocationConsumer, PersonFollower {

    public interface FriendRequestHandler {
        void friendRequest(String userId);
    }

    private static final String VIEW_PROFILE_STRING = "View Profile";
    private static final String BE_FRIENDS_STRING = "Be Friends";

    private static final int MAX_BE_FRIENDS_DISTANCE_M = 20; // 20,

    private enum LoadStages {
        DataLoaded, ImageLoaded
    }

    private final Context context;

    private final List<LoadStages> loadStages;

    private final LiveData<ViewUser> liveUserData;

    private Button actionButton;

    private LiveData<UserLocation> myLiveLocation;
    private UserLocation myLastLocation;

    private LiveData<UserLocation> personLiveLocation;
    private UserLocation personLastLocation;

    private FriendRequestHandler friendReqHandler;

    private boolean areFriends;

    public ShortProfileDialog(
            @NonNull Context context,
            LiveData<ViewUser> liveData) {
        super(context);

        this.context = context;
        this.liveUserData = liveData;
        this.loadStages = new ArrayList<>();

        this.areFriends = true;
    }

    public ShortProfileDialog(
            @NonNull Context context,
            LiveData<ViewUser> liveData,
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

        if (areFriends) {
            actionButton.setText(VIEW_PROFILE_STRING);
            actionButton.setOnClickListener(this::showProfileCLick);
        } else {
            actionButton.setText(BE_FRIENDS_STRING);
            actionButton.setOnClickListener(this::beFriendsClick);
        }

    }

    private void initViews(ViewUser user) {
        this.loadStages.add(LoadStages.DataLoaded);

        user.liveProfilePic.observe((LifecycleOwner) context, this::setupProfilePic);

        ((TextView) findViewById(R.id.username_people_map)).setText(user.name);

        ((ProgressBar) findViewById(R.id.rating_people_map_pb)).setProgress(user.rating);
        ((TextView) findViewById(R.id.rating_people_map_tv)).setText(user.rating + "/100");

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
            ((ImageView) findViewById(R.id.profile_pic_people_map)).setImageBitmap(pic);
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
        context.startActivity(profileIntent);
    }

    private void beFriendsClick(View v) {
        if (friendReqHandler != null
                && liveUserData != null
                && liveUserData.getValue() != null) {
            friendReqHandler.friendRequest(liveUserData.getValue().userId);
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