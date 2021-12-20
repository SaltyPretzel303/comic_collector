package mosis.comiccollector.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.util.Toaster;

public class MapActivity extends AppCompatActivity {

    private MapView map;

    private IMapController mapController;

    private MyLocationNewOverlay myLocationOverlay;

    private ActivityResultLauncher<String[]> permissionsRequester;

    private Drawable knownComicMarker;
    private Drawable unknownComicMarker;
    private Drawable knownPersonMarker;
    private Drawable unknownPersonMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osm advised that this should be done before setContentView(...)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);

        this.map = (MapView) this.findViewById(R.id.map);
        this.map.setTileSource(TileSourceFactory.MAPNIK);

        String[] requiredPermissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        this.permissionsRequester = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> result) -> {
                    for (String key : result.keySet()) {
                        if (result.get(key) == false) {
                            Log.e("Permission denied", "User denied permission: " + key);

                            finish();
                        } else {
                            Toaster.makeToast(this, "All permission granted ... ");
                        }
                    }
                }
        );

        List<String> notGrantedPermissions = this.getRequiredPermissions(requiredPermissions);
        if (notGrantedPermissions.size() > 0) {
            this.permissionsRequester.launch(notGrantedPermissions.toArray(new String[0]));
        }

        this.mapController = this.map.getController();

        this.mapController.setZoom(15.0);
        this.mapController.setCenter(new GeoPoint(43.3209, 21.8958));

        this.setMyLocationOverlay();
        this.setItemizedOverlay();
//        this.setClickableOverlay();

        this.loadDrawables();

    }

    private List<String> getRequiredPermissions(String[] possiblePermissions) {
        ArrayList<String> retList = new ArrayList<>();

        for (String possiblePermission : possiblePermissions) {
            if (ContextCompat.checkSelfPermission(this, possiblePermission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted

                retList.add(possiblePermission);
            }
        }

        return retList;
    }

    private void setMyLocationOverlay() {
        this.myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this),
                this.map);

        this.myLocationOverlay.enableMyLocation();
//        Location myLocation = this.myLocationOverlay.getMyLocationProvider().getLastKnownLocation();
//        Log.e("location", "You are on: " + myLocation.getLatitude() + " - " + myLocation.getLongitude());
//        this.mapController.setCenter(new GeoPoint(myLocation.getLatitude(), myLocation.getLatitude()));

        this.map.getOverlays().add(this.myLocationOverlay);
    }

    private void setItemizedOverlay() {

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        OverlayItem newItem = new OverlayItem("Title", "Description", new GeoPoint(43.3209, 21.8958));
        newItem.setMarker(getDrawable(R.drawable.marker_default_map));
        items.add(newItem);


        ItemizedOverlay<OverlayItem> overlay = new ItemizedIconOverlay<>(
                items,
                // second argument can be drawable to be displayed
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {

                        Log.e("map click", "You clicked on map");

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                },
                this);

        this.map.getOverlays().add(overlay);

    }

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

    private void loadDrawables() {
        this.unknownComicMarker = getDrawable(R.drawable.marker_unknown_comic);
        this.knownComicMarker = getDrawable(R.drawable.marker_known_comic);

        this.unknownPersonMarker = getDrawable(R.drawable.marker_unknown_person);
        this.knownPersonMarker = getDrawable(R.drawable.marker_known_person);
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}