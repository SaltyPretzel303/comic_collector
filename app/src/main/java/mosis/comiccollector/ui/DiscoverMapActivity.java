package mosis.comiccollector.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.location.LocalLocationReceiver;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.location.LocationService;
import mosis.comiccollector.R;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.map.MovingItemsOverlay;
import mosis.comiccollector.ui.map.NamedItemsOverlay;
import mosis.comiccollector.ui.map.OverlayItemWithId;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.DiscoveryViewModel;
import mosis.comiccollector.ui.map.LocationWithPicture;

public class DiscoverMapActivity extends AppCompatActivity implements LocationConsumer, IMyLocationProvider {

    interface PermissionRequester {
        void handlePermissionResult(boolean allGranted);
    }

    private static final String PREFS_PATH = "map_prefs";
    private static final String FOLLOWING_BOOL = "is_following";

    private static final String[] POSSIBLE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private DiscoveryViewModel viewModel;

    private MapView map;
    private IMapController mapController;

    private Drawable knownComicMarker;
    private Drawable unknownComicMarker;
    private Drawable knownPersonMarker;
    private Drawable unknownPersonMarker;

    private ActivityResultLauncher<String[]> permissionsRequesterLauncher;
    private PermissionRequester permissionRequester;

    private FloatingActionButton followButton;
    private View.OnClickListener startFollowingClick;
    private View.OnClickListener stopFollowingClick;

    private BroadcastReceiver myLocationReceiver;
    private IMyLocationConsumer myLocationConsumer;

    private MovingItemsOverlay movingFriendsOverlay;
    private MovingItemsOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osm advised that this should be done before setContentView(...)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        this.permissionsRequesterLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> result) -> {

                    for (String key : result.keySet()) {

                        if (result.get(key) == false) {
                            Log.e("Permission denied", "User denied permission: " + key);
                            if (permissionRequester != null) {
                                permissionRequester.handlePermissionResult(false);
                                permissionRequester = null;
                            }
                            return;
                        }
                    }

                    Log.e("permissions", "All permissions granted ... ");

                    if (permissionRequester != null) {
                        permissionRequester.handlePermissionResult(true);
                        permissionRequester = null;
                    }

                    return;
                });


        this.viewModel = new ViewModelProvider(this).get(DiscoveryViewModel.class);

        this.map = (MapView) this.findViewById(R.id.map);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        this.mapController = this.map.getController();

        this.mapController.setZoom(15.0);
        this.mapController.setCenter(new GeoPoint(43.6845, 21.7966));

        this.initOverlays();

        // this one doesn't require any permission
        this.loadFriends();

        this.setupFollowButton();

        this.requestPermissions((boolean granted) -> {
            if (granted) {
                // TODO setup my location overlay with location provider
            } else {
                // TODO add one more icon with my last known location to itemized overlay
            }
        });

    }

    // region permissions

    private List<String> getRequiredPermissions() {
        ArrayList<String> retList = new ArrayList<>();

        for (String permission : POSSIBLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted

                retList.add(permission);
            }
        }

        return retList;
    }

    private void requestPermissions(PermissionRequester requester) {

        this.permissionRequester = requester;

        List<String> notGrantedPermissions = this.getRequiredPermissions();
        if (notGrantedPermissions.size() > 0) {
            this.permissionsRequesterLauncher.launch(notGrantedPermissions.toArray(new String[0]));
            // toArray(new String[0]) is how you convert List<String> to String[]
        } else {
            if (this.permissionRequester != null) {
                this.permissionRequester.handlePermissionResult(true);
                this.permissionRequester = null;
            }
        }

    }

    // endregions

    // region setup following
    private void setupFollowButton() {
        this.followButton = findViewById(R.id.follow_button);

        if (isFollowingEnabled()) {
            this.followButton.setOnClickListener(this.stopFollowingClick);
        } else {
            this.followButton.setOnClickListener(this.startFollowingClick);
        }
    }

    {
        this.startFollowingClick = (View v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("You will let us follow you ?");

            builder.setPositiveButton("Allow", (dialogInterface, i) -> {
                dialogInterface.dismiss();

                requestPermissions((boolean granted) -> {
                    if (granted) {

                        startLocationService();

                        followButton.setOnClickListener(stopFollowingClick);
                        // TODO switch button icon

                        markAsFollowing();
                    } else {
                        Log.e("from permissions", "You can't follow me ... ");
                    }

                });
            });
            builder.setNegativeButton("Abort", (dialogInterface, i) -> {
                Log.e("follow button", "You can't follow me ... ");

                dialogInterface.dismiss();
                // position of other players and comics is gonna be displayed
                // but your position wont' be (displayed and updated ... )
            });

            builder.create().show();
        };

        this.stopFollowingClick = (View v) -> {

            stopLocationService();

            followButton.setOnClickListener(startFollowingClick);
            // TODO switch button icon

            markAsNotFollowing();
        };
    }

    private boolean isFollowingEnabled() {
        return getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .getBoolean(FOLLOWING_BOOL, false);
    }

    private void markAsFollowing() {
        Log.e("SharedPrefs", "Marked as FOLLOWING ... ");
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(FOLLOWING_BOOL, true)
                .apply();
    }

    private void markAsNotFollowing() {
        Log.e("SharedPrefs", "Marked as NOT FOLLOWING ... ");
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(FOLLOWING_BOOL, false)
                .apply();
    }

    private void startLocationService() {
        Intent startIntent = new Intent(this, LocationService.class);
        startService(startIntent);

        IntentFilter bcastFilter = new IntentFilter();
        bcastFilter.addAction(LocationService.LOCATION_BCAST_FILTER);

        String id = viewModel.getMyId();

        this.myLocationReceiver = new LocalLocationReceiver(id, (LocationConsumer) this);
        registerReceiver(this.myLocationReceiver, bcastFilter);

        Log.e("start", "Rcvr registered ... ");
    }

    private void stopLocationService() {

        Log.e("start", "Rcvr UN registered and locService stop ... ");

        if (this.myLocationReceiver != null) {
            unregisterReceiver(this.myLocationReceiver);
            this.myLocationReceiver = null;
        }

        Intent stopIntent = new Intent(DiscoverMapActivity.this, LocationService.class);
        stopService(stopIntent);

    }

    // endregion

    // region overlays

    private void initOverlays() {
        this.loadDrawables();

        this.movingFriendsOverlay = new MovingItemsOverlay(
                this,
                NamedItemsOverlay.Names.FriendsOverlay,
                this.map,
                this.knownPersonMarker,
                new ArrayList<>(),
                this.getFriendItemClickHandler());

        this.viewModel.getMyLastLocation()
                .observe((LifecycleOwner) this, (LocationWithPicture locationWithPicture) -> {
                    List<LocationWithPicture> item = new ArrayList<>();
                    item.add(locationWithPicture);

                    myLocationOverlay = new MovingItemsOverlay(
                            this,
                            NamedItemsOverlay.Names.MyLocationOverlay,
                            this.map,
                            this.unknownPersonMarker, // TODO replace with 'myselfMarker'
                            item,
                            new OnItemGestureListener<OverlayItemWithId>() {
                                // TODO empty click handler
                                @Override
                                public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                                    return false;
                                }

                                @Override
                                public boolean onItemLongPress(int index, OverlayItemWithId item) {
                                    return false;
                                }
                            });

                    // TODO questionable logic
                    // can't turn on following before I read my last location from firebase
                    setupFollowButton();

                });


        // TODO add comics overlay
        // TODO add otherPeople-notFriends overlay

    }

    private void loadFriends() {

        this.viewModel.getNearbyFriends(100, 100, 100)
                .observe(this, (nearbyFriends) -> {

                    // triggered whenever list of friends get updated

                    if (nearbyFriends == null) {
                        Log.e("friends", "Friends updated with  NO nearby friends ... ");
                        return;
                    }

                    Log.e("friends", "Friends updated with: "
                            + nearbyFriends.size() + "nearby friends ... ");

                    this.movingFriendsOverlay.setItems(nearbyFriends);

                    for (LocationWithPicture friend : nearbyFriends) {
                        this.viewModel.subscribeForLocationUpdates(
                                friend.getUserId(),
                                movingFriendsOverlay::updateItem
                        );
                    }

                });

    }

    private OnItemGestureListener<OverlayItemWithId> getFriendItemClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {

            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("Fiend click", "You clicked on friend ... ");

                Dialog dialog = new PeopleMapDialog(activity,
                        viewModel.getShortUser(item.getItemId()));

                dialog.show();

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItemWithId item) {
                return false;
            }
        };

    }

    private void loadDrawables() {
        this.unknownComicMarker = AppCompatResources.getDrawable(this, R.drawable.marker_unknown_comic);
        this.knownComicMarker = AppCompatResources.getDrawable(this, R.drawable.marker_known_comic);

        this.unknownPersonMarker = AppCompatResources.getDrawable(this, R.drawable.marker_unknown_person);
        this.knownPersonMarker = AppCompatResources.getDrawable(this, R.drawable.marker_known_person);
    }

    // endregion

    @Override
    public void onResume() {
        super.onResume();
        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        // SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(this);
        // Configuration.getInstance().load(this,
        // PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); // needed for compass, my location overlays, v6.0.0 and up

        // re-register bcast listener
        if (isFollowingEnabled() && this.myLocationReceiver == null) {
            IntentFilter registerIntent = new IntentFilter();
            registerIntent.addAction(LocationService.LOCATION_BCAST_FILTER);

            String id = viewModel.getMyId();

            this.myLocationReceiver = new LocalLocationReceiver(id, (LocationConsumer) this);
            registerReceiver(this.myLocationReceiver, registerIntent);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        // SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(this);
        // Configuration.getInstance().save(this, prefs);
        map.onPause(); // needed for compass, my location overlays, v6.0.0 and up

        if (this.myLocationReceiver != null) {
            unregisterReceiver(this.myLocationReceiver);
            this.myLocationReceiver = null;
        }

    }

    // left just for reference
    private void setClickableOverlay() {

        MapEventsOverlay overlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                Log.e("events overlay", "YOU CLICKED OUTSIDE ITEM ... ");
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });

        this.map.getOverlays().add(overlay);
    }

    // region location consumer implementation

    @Override
    public void updateLocation(UserLocation newLocation) {
        this.myLocationOverlay.updateItem(newLocation);
    }

    // endregion

    // region IMyLocationProvider implementation

    // attempt to implement custom myLocationOverlay
    // it think this is safe to be removed

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        this.myLocationConsumer = myLocationConsumer;
        return true;
    }

    @Override
    public void stopLocationProvider() {
        this.myLocationConsumer = null;
    }

    @Override
    public Location getLastKnownLocation() {
        return null; // TODO use people repo
    }

    @Override
    public void destroy() {
        this.myLocationConsumer = null;
    }

    // endregion

}