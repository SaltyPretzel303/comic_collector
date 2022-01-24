package mosis.comiccollector.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.location.LocalLocationReceiver;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.location.LocationService;
import mosis.comiccollector.R;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.map.UpdatableItemsOverlay;
import mosis.comiccollector.ui.map.NamedItemsOverlay;
import mosis.comiccollector.ui.map.OverlayItemWithId;
import mosis.comiccollector.ui.viewmodel.DiscoveryViewModel;
import mosis.comiccollector.ui.map.LocationWithPicture;
import mosis.comiccollector.util.Toaster;

public class DiscoverMapActivity extends AppCompatActivity implements LocationConsumer {

    private static final String PREFS_PATH = "map_prefs";
    private static final String FOLLOWING_BOOL = "is_following";
    private static final String SHOW_FRIENDS_BOOL = "is_showing_friends";
    private static final String SHOW_COMICS_BOOL = "is_showing_comics";


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

    private CheckBox followMeCb;
    private CheckBox showFriendsCb;
    private CheckBox showComicsCb;

    private BroadcastReceiver myLocationReceiver;
    private IMyLocationConsumer myLocationConsumer;

    private UpdatableItemsOverlay movingFriendsOverlay;
    private UpdatableItemsOverlay myLocationOverlay;

    private UpdatableItemsOverlay createdComicsOverlay;
    private UpdatableItemsOverlay collectedComicsOverlay;
    private UpdatableItemsOverlay unknownComicsOverlay;

    private Dialog activeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osm advised that this should be done before setContentView(...)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        this.initPermissionRequester();

        this.viewModel = new ViewModelProvider(this).get(DiscoveryViewModel.class);

        this.setupMapView();
        this.initOverlays();
        this.setupCheckBoxes();


        this.loadMyself();
        this.loadFriends();
        this.loadCreatedComics();
        this.loadCollectedComics();
        this.loadUnknownComics();

//        this.setupFollowButton();

    }

    private void setupCheckBoxes() {

        followMeCb = findViewById(R.id.follow_me_cb);
        followMeCb.setChecked(isFollowingEnabled());
        followMeCb.setOnCheckedChangeListener((CompoundButton compoundButton, boolean checked) -> {
            if (checked) {
                startFollowing();
            } else {
                stopFollowing();
            }
        });

        showFriendsCb = findViewById(R.id.show_friends_cb);
        showFriendsCb.setChecked(isShowingFriendsEnabled());
        showFriendsCb.setOnCheckedChangeListener((CompoundButton compoundButton, boolean checked) -> {
            // TODO update next lines once showing people (other than friends) gets implemented
            if (checked) {
                movingFriendsOverlay.enable();
                markToShowFriends();
            } else {
                movingFriendsOverlay.disable();
                markToNotShowFriends();
            }
        });

        showComicsCb = findViewById(R.id.show_comics_cb);
        showComicsCb.setChecked(isShowingComicsEnabled());
        showComicsCb.setOnCheckedChangeListener((CompoundButton compoundButton, boolean checked) -> {
            if (checked) {
                createdComicsOverlay.enable();
                collectedComicsOverlay.enable();
                unknownComicsOverlay.enable();

                markToShowComics();
            } else {
                createdComicsOverlay.disable();
                collectedComicsOverlay.disable();
                unknownComicsOverlay.disable();

                markToNotShowComics();
            }
        });
    }

    private void setupMapView() {
        this.map = (MapView) this.findViewById(R.id.map);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        this.mapController = this.map.getController();

        this.mapController.setZoom(15.0);
        this.mapController.setCenter(new GeoPoint(43.6845, 21.7966));

    }

    // region permissions

    private void initPermissionRequester() {
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
    }

    private List<String> getRequiredPermissions() {
        ArrayList<String> retList = new ArrayList<>();

        for (String permission : POSSIBLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not already granted

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

    // endregion

    // region following

    private void startFollowing() {
        new AlertDialog.Builder(this)
                .setMessage("You will let us follow you ?")
                .setPositiveButton("Allow", (dialogInterface, i) -> {
                    dialogInterface.dismiss();

                    requestPermissions((boolean granted) -> {
                        if (granted) {

                            startLocationService();
                            markAsFollowing();

                        } else {
                            Log.e("from permissions", "You can't follow me ... ");
                            followMeCb.setChecked(false);
                        }

                    });
                })
                .setNegativeButton("Abort", (dialogInterface, i) -> {
                    Log.e("follow button", "You can't follow me ... ");

                    dialogInterface.dismiss();
                    followMeCb.setChecked(false);
                })
                .create()
                .show();
    }

    private void stopFollowing() {
        stopLocationService();
        markAsNotFollowing();
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

    // region properties read/write

    private boolean isFollowingEnabled() {
        return getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .getBoolean(FOLLOWING_BOOL, false);
    }

    private boolean isShowingFriendsEnabled() {
        return getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .getBoolean(SHOW_FRIENDS_BOOL, false);
    }

    private boolean isShowingComicsEnabled() {
        return getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .getBoolean(SHOW_COMICS_BOOL, false);
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

    private void markToNotShowFriends() {
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SHOW_FRIENDS_BOOL, false)
                .apply();
    }

    private void markToShowFriends() {
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SHOW_FRIENDS_BOOL, true)
                .apply();
    }

    private void markToNotShowComics() {
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SHOW_COMICS_BOOL, false)
                .apply();
    }

    private void markToShowComics() {
        getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SHOW_COMICS_BOOL, true)
                .apply();
    }

    // endregions

    // region overlays

    private void initOverlays() {
        this.loadDrawables();

        this.myLocationOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.MyLocationOverlay,
                this.map,
                this.unknownPersonMarker, // TODO replace with 'myselfMarker'
                new ArrayList<>(),
                this.getEmptyClickHandler()
        );

        this.movingFriendsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.FriendsOverlay,
                this.map,
                this.knownPersonMarker,
                new ArrayList<>(),
                this.getFriendItemClickHandler());

        this.createdComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.CreatedComicsOverlay,
                this.map,
                this.knownComicMarker,
                new ArrayList<>(),
                this.getCreatedComicClickHandler());

        this.collectedComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.CollectedComicsOverlay,
                this.map,
                this.knownComicMarker,
                new ArrayList<>(),
                this.getCollectedComicClickHandler());

        this.unknownComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.UnknownComicsOverlay,
                this.map,
                this.unknownComicMarker,
                new ArrayList<>(),
                this.getUnknownComicClickHandler());

    }

    private void loadMyself() {
        this.viewModel.getMyLastLocation()
                .observe(this, (UserLocation location) -> {
                    if (myLocationOverlay.getItemsCount() == 0) {
                        // following is not enabled
                        // or following service still haven't found my location
                        // show last known location

                        myLocationOverlay.updateItem(new UserLocation(
                                location.getUserId(),
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getLastUpdate()));

                    }
                });
    }

    private void loadFriends() {
        if (!isShowingFriendsEnabled()) {
            movingFriendsOverlay.disable();
        }

        this.viewModel.getNearbyFriends(100, 100, 100)
                .observe(this, (nearbyFriends) -> {

                    // triggered whenever list of friends get updated

                    if (nearbyFriends == null) {
                        Log.e("friends", "Failed to update friends ... ");
                        return;
                    }

                    Log.e("friends", "Friends updated with: "
                            + nearbyFriends.size() + "nearby friends ... ");

                    this.movingFriendsOverlay.setItems(nearbyFriends);

                    for (LocationWithPicture friend : nearbyFriends) {
                        this.viewModel.subscribeForLocationUpdates(
                                friend.getId(),
                                movingFriendsOverlay::updateItem
                        );
                    }

                });

    }

    private void loadUnknownPeople() {
    }

    private void loadCreatedComics() {
        if (!isShowingComicsEnabled()) {
            createdComicsOverlay.disable();
        }

        viewModel.getCreatedComics(100, 100)
                .observe(this, (locations) -> {
                    if (locations == null) {
                        Log.e("discMapAct", "Got null as created comics ... ");
                        return;
                    }

                    createdComicsOverlay.setItems(locations);

                    return;
                });
    }

    private void loadCollectedComics() {
        if (!isShowingComicsEnabled()) {
            collectedComicsOverlay.disable();
        }

        viewModel.getCollectedComics(100, 100)
                .observe(this, (locations) -> {
                    if (locations == null) {
                        Log.e("discMapAct", "Got null as collected comics ... ");
                        return;
                    }

                    collectedComicsOverlay.setItems(locations);

                    return;
                });

    }

    private void loadUnknownComics() {
        if (!isShowingComicsEnabled()) {
            unknownComicsOverlay.disable();
        }

        viewModel.getUnknownComics(100, 100)
                .observe(this, (List<LocationWithPicture> locations) -> {
                    if (locations == null) {
                        Log.e("discActivity", "Got nll as unknown comics ... ");
                        return;
                    }

                    this.unknownComicsOverlay.setItems(locations);

                    return;
                });

    }

    private OnItemGestureListener<OverlayItemWithId> getFriendItemClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {

            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("Fiend click", "You clicked on friend ... ");

                activeDialog = new ShortProfileDialog(activity,
                        viewModel.getShortUser(item.getItemId()));

                activeDialog.setOnDismissListener(dialogInterface -> {
                    activeDialog = null;
                });

                activeDialog.show();

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItemWithId item) {
                return false;
            }
        };

    }

    private OnItemGestureListener<OverlayItemWithId> getEmptyClickHandler() {
        return new OnItemGestureListener<>() {

            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItemWithId item) {
                return false;
            }
        };

    }

    private OnItemGestureListener<OverlayItemWithId> getCreatedComicClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("comicClick", "You clicked on created comic");

                MutableLiveData<ViewComic> comic = viewModel.getComic(
                        item.getItemId(),
                        ShortComicDialog.TITLE_PAGE_WIDTH,
                        ShortComicDialog.TITLE_PAGE_HEIGHT);

                activeDialog = new ShortComicDialog(
                        activity,
                        comic,
                        ComicOrigin.Created);
                activeDialog.setOnDismissListener(dialogInterface -> {
                    activeDialog = null;
                });

                activeDialog.show();

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItemWithId item) {
                return false;
            }
        };
    }

    private OnItemGestureListener<OverlayItemWithId> getCollectedComicClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("comicClick", "You clicked on collected comic");

                MutableLiveData<ViewComic> comic = viewModel.getComic(
                        item.getItemId(),
                        ShortComicDialog.TITLE_PAGE_WIDTH,
                        ShortComicDialog.TITLE_PAGE_HEIGHT);

                activeDialog = new ShortComicDialog(
                        activity,
                        comic,
                        ComicOrigin.Collected);
                activeDialog.setOnDismissListener(dialogInterface -> {
                    activeDialog = null;
                });

                activeDialog.show();

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItemWithId item) {
                return false;
            }
        };
    }

    private OnItemGestureListener<OverlayItemWithId> getUnknownComicClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("comicClick", "You clicked on unknown comic");

                MutableLiveData<ViewComic> comic = viewModel.getComic(
                        item.getItemId(),
                        ShortComicDialog.TITLE_PAGE_WIDTH,
                        ShortComicDialog.TITLE_PAGE_HEIGHT);

                activeDialog = new ShortComicDialog(
                        activity,
                        comic,
                        ComicOrigin.Unknown,
                        (comicId) -> {
                            viewModel.collectComic(comicId).
                                    observe(activity, (String err) -> {
                                        if (err != null) {
                                            Log.e("discAct", "Failed to collect comic ... " + err);
                                            return;
                                        }

                                        Log.e("discAct", "Comic collected ... ");
                                        Toaster.makeToast(activity, "Collected ... you did it ... ");
                                    });
                        });
                activeDialog.setOnDismissListener(dialogInterface -> {
                    activeDialog = null;
                });

                activeDialog.show();

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

    // region location consumer implementation

    @Override
    public void updateLocation(UserLocation newLocation) {
        this.myLocationOverlay.updateItem(newLocation);

        if (activeDialog instanceof LocationConsumer) {
            ((LocationConsumer) activeDialog).updateLocation(newLocation);
        }
    }

    // endregion

}