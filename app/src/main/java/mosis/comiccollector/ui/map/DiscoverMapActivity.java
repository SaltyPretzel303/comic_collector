package mosis.comiccollector.ui.map;

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
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.location.LocalLocationReceiver;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.location.LocationService;
import mosis.comiccollector.R;
import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.PermissionRequester;
import mosis.comiccollector.ui.comic.ShortComicDialog;
import mosis.comiccollector.ui.user.ShortProfileDialog;
import mosis.comiccollector.ui.comic.ComicOrigin;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.viewmodel.DiscoveryViewModel;
import mosis.comiccollector.util.Toaster;

public class DiscoverMapActivity extends AppCompatActivity implements LocationConsumer {

    public static final String FILTERS_EXTRA = "filters";

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

    private Drawable createdComicMarker;
    private Drawable collectedComicMarker;
    private Drawable unknownComicMarker;

    private Drawable myMarker;
    private Drawable knownPersonMarker;
    private Drawable unknownPersonMarker;

    private ActivityResultLauncher<String[]> permissionsRequesterLauncher;
    private PermissionRequester permissionRequester;

    private CheckBox followMeCb;

    private BroadcastReceiver myLocationReceiver;
    private IMyLocationConsumer myLocationConsumer;

    private MapFiltersState filtersState;
    private boolean transientFilters;

    private String lastComicTextFilter;
    private float lastPeopleRatingFilter;

    private String lastPeopleTextFilter;
    private float lastComicRatingFilter;

    private UpdatableItemsOverlay friendsOverlay;
    private UpdatableItemsOverlay unknownPeopleOverlay;
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

        this.loadDrawables();

        this.setupMapView();

        Intent intent = getIntent();
        if (intent.hasExtra(FILTERS_EXTRA)) {
            filtersState = (MapFiltersState) intent.getSerializableExtra(FILTERS_EXTRA);
            transientFilters = true; // will not be saved on pause
        }
    }

    private void setupFilters() {

        if (filtersState == null) {
            filtersState = MapFiltersState.read(this);
        }

        // filter state is loaded/saved in onResume/onPause

        followMeCb = findViewById(R.id.follow_me_cb);
        followMeCb.setChecked(isFollowingEnabled());
        followMeCb.setOnCheckedChangeListener((CompoundButton compoundButton, boolean checked) -> {
            if (checked) {
                startFollowing();
            } else {
                stopFollowing();
            }
        });

        findViewById(R.id.map_people_filter).setOnClickListener((v) -> {
            activeDialog = new MapFiltersDialog(
                this,
                getPeopleBooleanFilters(),
                getPeopleTextFilter(),
                getPeopleRatingFilter());

            activeDialog.setOnDismissListener((d) -> {
                activeDialog = null;
            });

            activeDialog.show();
        });

        findViewById(R.id.map_comics_filter).setOnClickListener((v) -> {
            activeDialog = new MapFiltersDialog(
                this,
                getComicsBooleanFilters(),
                getComicsTextFilter(),
                getComicsRatingFilter());

            activeDialog.setOnDismissListener((d) -> {
                activeDialog = null;
            });

            activeDialog.show();

        });

    }

    // region people filters

    private List<MapFiltersDialog.BooleanFilter> getPeopleBooleanFilters() {
        return Arrays.asList(
            new MapFiltersDialog.BooleanFilter() {
                @Override
                public boolean isActive() {
                    return filtersState.showFriends;
                }

                @Override
                public String getText() {
                    return "Show friends";
                }

                @Override
                public void handleStateChange(boolean state) {
                    filtersState.showFriends = state;
                    loadFriends();
                }
            },
            new MapFiltersDialog.BooleanFilter() {
                @Override
                public boolean isActive() {
                    return filtersState.showUnknownPeople;
                }

                @Override
                public String getText() {
                    return "Show unknown people";
                }

                @Override
                public void handleStateChange(boolean state) {
                    filtersState.showUnknownPeople = state;
                    loadUnknownPeople();
                }
            }

        );
    }

    private MapFiltersDialog.TextFilter getPeopleTextFilter() {
        return new MapFiltersDialog.TextFilter() {
            @Override
            public String lastText() {
                return lastPeopleTextFilter;
            }

            @Override
            public String getText() {
                return "Name";
            }

            @Override
            public void handleText(String text) {
                lastPeopleTextFilter = text;
                viewModel.filterPeopleByText(text);
            }
        };
    }

    private MapFiltersDialog.RatingFilter getPeopleRatingFilter() {
        return new MapFiltersDialog.RatingFilter() {

            @Override
            public float lastRating() {
                return lastPeopleRatingFilter;
            }


            @Override
            public void handlerRating(float rating) {
                lastPeopleRatingFilter = rating;
                viewModel.filterPeopleByRating(rating);
            }
        };
    }

    // endregion

    // region comics filters

    private List<MapFiltersDialog.BooleanFilter> getComicsBooleanFilters() {
        return Arrays.asList(
            new MapFiltersDialog.BooleanFilter() {
                @Override
                public boolean isActive() {
                    return filtersState.showCreatedComics;
                }

                @Override
                public String getText() {
                    return "Show created comics";
                }

                @Override
                public void handleStateChange(boolean state) {
                    filtersState.showCreatedComics = state;
                    loadCreatedComics();
                }
            },
            new MapFiltersDialog.BooleanFilter() {
                @Override
                public boolean isActive() {
                    return filtersState.showCollectedComics;
                }

                @Override
                public String getText() {
                    return "Show collected comics";
                }

                @Override
                public void handleStateChange(boolean state) {
                    filtersState.showCollectedComics = state;
                    loadCollectedComics();
                }
            },
            new MapFiltersDialog.BooleanFilter() {
                @Override
                public boolean isActive() {
                    return filtersState.showUnknownComics;
                }

                @Override
                public String getText() {
                    return "Show unknown comics";
                }

                @Override
                public void handleStateChange(boolean state) {
                    filtersState.showUnknownComics = state;
                    loadUnknownComics();
                }
            }
        );
    }

    private MapFiltersDialog.TextFilter getComicsTextFilter() {
        return new MapFiltersDialog.TextFilter() {

            @Override
            public String lastText() {
                return lastComicTextFilter;
            }

            @Override
            public String getText() {
                return "Title";
            }

            @Override
            public void handleText(String text) {
                lastComicTextFilter = text;
                viewModel.filterComicsByText(text);
            }
        };
    }

    private MapFiltersDialog.RatingFilter getComicsRatingFilter() {
        return new MapFiltersDialog.RatingFilter() {

            @Override
            public float lastRating() {
                return lastComicRatingFilter;
            }

            @Override
            public void handlerRating(float rating) {
                lastComicRatingFilter = rating;
                viewModel.filterComicsByRating(rating);
            }
        };
    }

    // endregion

    private void setupMapView() {
        this.map = (MapView) this.findViewById(R.id.map);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        this.mapController = this.map.getController();

        this.mapController.setZoom(15.0);
        this.mapController.setCenter(new GeoPoint(43.6845, 21.7966));

        this.map.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.e("mapView", "Scrolling ... ");

                loadCollectedComics();
                loadCreatedComics();
                loadUnknownComics();
                loadUnknownPeople();
                loadFriends();

                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.e("mapView", "Zooming ... ");

                loadCollectedComics();
                loadCreatedComics();
                loadUnknownComics();
                loadUnknownPeople();
                loadFriends();

                return true;
            }
        }, 500));
        // with just MapListener (not-delayed) onScroll event will be triggered
        // each time map is moved which is many times while user is "holding" map
        // with delayed listener onScroll should be triggered only once
        // approximately when user "release" map, he is not moving it anymore

    }

    private double getCenterLatitude() {
        return map.getBoundingBox().getCenterLatitude();
    }

    private double getCenterLongitude() {
        return map.getBoundingBox().getCenterLongitude();
    }

    private double getRadiusInKm() {
        return map.getBoundingBox().getDiagonalLengthInMeters() / 1000;
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

    // endregion

    // region overlays

    private void loadMyself() {
        if (this.myLocationOverlay == null) {
            this.myLocationOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.MyLocationOverlay,
                this.map,
                this.myMarker,
                new ArrayList<>(),
                this.getEmptyClickHandler()
            );


        }
        this.viewModel.getMyLastLocation()
            .observe(this, (UserLocation location) -> {
                if (myLocationOverlay.getItemsCount() == 0) {
                    // following is not enabled
                    // or following service still haven't found my location
                    // show last known location
                    myLocationOverlay.updateItem(
                        new UserLocation(
                            location.getUserId(),
                            location.getLatitude(),
                            location.getLongitude()),
                        true);

                } else {
                    Log.e("discAct", "THERE IS ALREADY SOME ITEM IN MY OVERLAY ... ");
                }
            });
    }

    private void loadFriends() {
        if (this.friendsOverlay == null) {
            this.friendsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.FriendsOverlay,
                this.map,
                this.knownPersonMarker,
                new ArrayList<>(),
                this.getFriendItemClickHandler());
        }

        if (!filtersState.showFriends) {
            // if overlay should be hidden
            friendsOverlay.disable();
            return;
        } else {
            // if overlay should be shown
            if (!friendsOverlay.isEnabled()) {
                // if it was previously hidden show it
                friendsOverlay.enable();
            }
        }

        viewModel.getNearbyFriends(
            map.getBoundingBox().getCenterLatitude(),
            map.getBoundingBox().getCenterLongitude(),
            map.getBoundingBox().getDiagonalLengthInMeters())
            .observe(this, (nearbyFriends) -> {

                // triggered whenever list of friends get updated

                if (nearbyFriends == null) {
                    return;
                }

                friendsOverlay.setItems(nearbyFriends);

            });

    }

    private void loadUnknownPeople() {
        if (unknownPeopleOverlay == null) {
            unknownPeopleOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.UnknownPeopleOverlay,
                this.map,
                this.unknownPersonMarker,
                new ArrayList<>(),
                this.getUnknownPeopleClickHandler());
        }

        if (!filtersState.showUnknownPeople) {
            unknownPeopleOverlay.disable();
            // viewModel.disableUnknownPersonUpdates
            return;
        } else {
            if (!unknownPeopleOverlay.isEnabled()) {
                Log.e("mapView", "Enabling unknown people overlay ... ");
                unknownPeopleOverlay.enable();
            }

            viewModel.getNearbyUnknownPeople(
                getCenterLatitude(),
                getCenterLongitude(),
                getRadiusInKm())
                .observe(this, (nearbyPeople) -> {

                    // triggered whenever list of nearby people get updated

                    if (nearbyPeople == null) {
                        return;
                    }

//                    Log.e("mapActivity", "Unknown people observer ... " + nearbyPeople.size());

                    this.unknownPeopleOverlay.setItems(nearbyPeople);

                    if (activeDialog != null && activeDialog instanceof PersonFollower) {
                        String id = ((PersonFollower) activeDialog).getPersonId();
                        if (nearbyPeople.stream().anyMatch((person) -> person.getId().equals(id))) {
                            Location loc = nearbyPeople.stream()
                                .filter((person) -> person.getId().equals(id))
                                .findFirst()
                                .get()
                                .getLocation();
                            ((PersonFollower) activeDialog).consumePersonLocation(
                                new UserLocation(id, loc.latitude, loc.longitude));
                        }
                    }

                });
        }

    }

    private void loadCreatedComics() {
        if (this.createdComicsOverlay == null) {
            this.createdComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.CreatedComicsOverlay,
                this.map,
                this.createdComicMarker,
                new ArrayList<>(),
                this.getCreatedComicClickHandler());
        }

        if (!filtersState.showCreatedComics) {
            createdComicsOverlay.disable();
            return;
        } else {
            if (!createdComicsOverlay.isEnabled()) {
                Log.e("mapActivity", "Created comics enabled ... ");
                createdComicsOverlay.enable();
            }
        }

        viewModel.getCreatedComics(
            100,
            100,
            getCenterLatitude(),
            getCenterLongitude(),
            getRadiusInKm())
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
        if (this.collectedComicsOverlay == null) {
            this.collectedComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.CollectedComicsOverlay,
                this.map,
                this.collectedComicMarker,
                new ArrayList<>(),
                this.getCollectedComicClickHandler());
        }

        if (!filtersState.showCollectedComics) {
            collectedComicsOverlay.disable();
            return;
        } else {
            if (!collectedComicsOverlay.isEnabled()) {
                collectedComicsOverlay.enable();
            }
        }

        viewModel.getCollectedComics(
            100,
            100,
            getCenterLatitude(),
            getCenterLongitude(),
            getRadiusInKm())
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
        if (this.unknownComicsOverlay == null) {
            this.unknownComicsOverlay = new UpdatableItemsOverlay(
                this,
                NamedItemsOverlay.Names.UnknownComicsOverlay,
                this.map,
                this.unknownComicMarker,
                new ArrayList<>(),
                this.getUnknownComicClickHandler());
        }

        if (!filtersState.showUnknownComics) {
            unknownComicsOverlay.disable();
            return;
        } else {
            if (!unknownComicsOverlay.isEnabled()) {
                Log.e("mapActivity", "Unknown comics enabled ... ");
                unknownComicsOverlay.enable();
            }
        }

        viewModel.getUnknownComics(
            100,
            100,
            getCenterLatitude(),
            getCenterLongitude(),
            getRadiusInKm())
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

    private OnItemGestureListener<OverlayItemWithId> getUnknownPeopleClickHandler() {
        AppCompatActivity activity = this;
        return new OnItemGestureListener<>() {

            @Override
            public boolean onItemSingleTapUp(int index, OverlayItemWithId item) {
                Log.e("Fiend click", "You clicked on unknown person ... ");

                activeDialog = new ShortProfileDialog(
                    activity,
                    viewModel.getShortUser(item.getItemId()),
                    viewModel.getUserLocation(item.getItemId()),
                    viewModel.getMyLastLocation(),
                    (person) -> friendRequest(person));

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
                    viewModel.getMyLastLocation(),
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
        this.myMarker = AppCompatResources.getDrawable(this, R.drawable.my_marker);

        this.createdComicMarker = AppCompatResources.getDrawable(this, R.drawable.created_comic_marker);
        this.collectedComicMarker = AppCompatResources.getDrawable(this, R.drawable.collected_comic_marker);
        this.unknownComicMarker = AppCompatResources.getDrawable(this, R.drawable.unknown_comic_marker);

        this.unknownPersonMarker = AppCompatResources.getDrawable(this, R.drawable.unknown_person_marker);
        this.knownPersonMarker = AppCompatResources.getDrawable(this, R.drawable.known_person_marker);
    }

    private void friendRequest(String personId) {
        viewModel.makeFriends(personId);
        if (activeDialog != null) {
            activeDialog.dismiss();
        }
    }

    // endregion

    @Override
    public void onResume() {
        super.onResume();

        if (filtersState == null) {
            filtersState = MapFiltersState.read(this);
        }

        this.setupFilters();

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

        this.loadMyself();
        this.loadFriends();
        this.loadUnknownPeople();
        this.loadCreatedComics();
        this.loadCollectedComics();
        this.loadUnknownComics();

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

        if (filtersState != null && !transientFilters) {
            MapFiltersState.write(this, filtersState);
        }

        viewModel.stopFollowingPeople();
    }

    // region location consumer implementation

    @Override
    public void consumeMyLocation(UserLocation newLocation) {
        this.myLocationOverlay.updateItem(newLocation, true);

        // these are loaded based on my current location
        loadFriends();
        loadCreatedComics();
        loadCollectedComics();
        loadUnknownComics();

        if (activeDialog instanceof LocationConsumer) {
            ((LocationConsumer) activeDialog).consumeMyLocation(newLocation);
        }
    }

    // endregion

}