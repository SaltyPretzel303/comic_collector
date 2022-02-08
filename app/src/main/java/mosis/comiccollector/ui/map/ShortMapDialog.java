package mosis.comiccollector.ui.map;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mosis.comiccollector.R;
import mosis.comiccollector.model.Location;
import mosis.comiccollector.util.Toaster;

public class ShortMapDialog extends Dialog {

    public interface PlaceReady {
        void handlePlace(Location location);
    }

    private final Context context;
    private PlaceReady placeHandler;

    private Location lastPicked;

    private Location locationToShow;

    public ShortMapDialog(@NotNull Context context, Location location) {
        super(context);

        this.context = context;
        this.locationToShow = location;
    }

    public ShortMapDialog(@NonNull Context context, PlaceReady placeHandler) {
        super(context);

        this.context = context;
        this.placeHandler = placeHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // osm advised that this should be done before setContentView(...)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        setContentView(R.layout.pick_a_place_dialog);

        initMap();
        if (locationToShow == null) {
            findViewById(R.id.pick_a_place_pick_button).setOnClickListener(this::placeHereClick);
        } else {
            findViewById(R.id.pick_a_place_pick_button).setVisibility(View.GONE);
        }
    }

    private void initMap() {

        MapView map = findViewById(R.id.pick_a_place_map);

        Overlay overlay = null;

        if (locationToShow == null) {
            map.getController().setCenter(new GeoPoint(43.6845, 21.7966));
            overlay = new MapEventsOverlay(new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    Log.e("mapOverlay", "You clicked on the map ... ");

                    String toText = tryToGeocode(p.getLatitude(), p.getLongitude());
                    Log.e("Geocode", toText);
                    String text = toText + "\n"
                        + "lat: " + p.getLatitude() + " lon: " + p.getLongitude();

                    ((TextView) findViewById(R.id.place_to_text_tv)).setText(text);

                    lastPicked = new Location(p.getLatitude(), p.getLongitude());

                    return true;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            });

        } else {

            var point = new GeoPoint(
                locationToShow.latitude,
                locationToShow.longitude);

            map.getController().setCenter(point);

            var list = new ArrayList<OverlayItem>();
            list.add(new OverlayItem("", "", point));

            overlay = new ItemizedIconOverlay<OverlayItem>(
                list,
                AppCompatResources.getDrawable(context, R.drawable.marker_default_map),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                },
                context);
        }

        map.getOverlays().add(overlay);
        map.getController().setZoom(15.0);

    }

    private String tryToGeocode(double lat, double lgt) {
        // TODO this might be some long synchronous thing ...
        // and it is not necessary so ... might as well remove it
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lgt, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
                }
                return strReturnedAddress.toString();
            } else {
                return "Unknown location";
            }
        } catch (IOException e) {
            Log.e("pickAPlace", "Exc while geocoding: " + e.getMessage());
            return "Unknown location";
        }
    }

    private void placeHereClick(View v) {
        if (lastPicked != null) {
            placeHandler.handlePlace(lastPicked);
            dismiss();
        } else {
            Toaster.makeToast(context, "Please click somewhere first ... ");
        }
    }

}
