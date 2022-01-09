package mosis.comiccollector.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import mosis.comiccollector.model.user.UserLocation;

public class MovingItemsOverlay {

    private Context context;

    private NamedItemsOverlay.Names overlayName;

    private MapView map;
    private Drawable icon;
    private List<LocationWithPicture> locations;
    private ItemizedIconOverlay.OnItemGestureListener<OverlayItemWithId> clickHandler;

    public MovingItemsOverlay(
            Context context,
            NamedItemsOverlay.Names overlayName,
            MapView map,
            Drawable icon,
            List<LocationWithPicture> locations,
            ItemizedIconOverlay.OnItemGestureListener<OverlayItemWithId> clickHandler) {

        this.context = context;

        this.overlayName = overlayName;

        this.map = map;
        this.icon = icon;
        this.locations = locations;

        this.clickHandler = clickHandler;

        this.generateOverlay();
    }

    public void updateItem(UserLocation newItem) {

        Predicate<LocationWithPicture> filter = (location) -> {
            return location.getUserId().equals(newItem.getUserId());
        };

        if (this.locations.stream().anyMatch(filter)) {
            this.locations.stream()
                    .filter(filter)
                    .findFirst()
                    .get()
                    .updateLocation(newItem);
        } else {
            // made mainly for the purpose of displaying my location
            // this "overlayAdapter" is created in initOverlays method
            // on each location update this method is called
            LocationWithPicture newLocation = new LocationWithPicture(newItem);
            MutableLiveData<Bitmap> fakeLivePic = new MutableLiveData<>();
            fakeLivePic.postValue(null);
            newLocation.setLivePic(fakeLivePic);

            this.addItem(new LocationWithPicture(newItem));
        }


        this.generateOverlay();
    }

    public void setItems(List<LocationWithPicture> newLocations) {
        this.locations = newLocations;
        this.generateOverlay();
    }

    public void addItem(LocationWithPicture newLocation) {
        this.locations.add(newLocation);
        this.generateOverlay();
    }

    private void generateOverlay() {

        for (LocationWithPicture item : this.locations) {
            if (!item.getLivePic().hasObservers()) {
                Log.e("Moving overlay", "Adding pic observer ... ");
                item.getLivePic().observe((LifecycleOwner) context, (Bitmap uri) -> {
                    if (uri == null) {
                        // if uri is null there is nothing to be updated
                        return;
                    }

                    generateOverlay();
                });
            }
        }

        List<OverlayItemWithId> overlayItems = new ArrayList<>();
        for (LocationWithPicture item : this.locations) {
            OverlayItemWithId newItem = new OverlayItemWithId(
                    item.getUserId(),
                    "",
                    "",
                    getPoint(item.getLocation()));

            if (item.getLivePic().getValue() == null) {
                // set default marker
                // newItem.setMarker(this.icon);
            } else {
                // TODO create marker using profile pic ...
                Log.e("MovingOverlay", "This item has pic ... ");
            }

            // TODO remove this after upper if gets implemented
            newItem.setMarker(this.icon);

            overlayItems.add(newItem);
        }

        Log.e("MovingOverlay", "Overlay items: " + overlayItems.size());

        Overlay newOverlay = new NamedItemsOverlay<OverlayItemWithId>(
                this.overlayName,
                this.context,
                overlayItems,
                this.clickHandler);

        this.addOverlay(newOverlay);

        this.map.postInvalidate();
    }

    private void addOverlay(Overlay newOverlay) {
        if (existInOverlays()) {
            this.replaceWith(newOverlay);
        } else {
            this.map.getOverlays().add(newOverlay);
        }
    }

    private GeoPoint getPoint(UserLocation loc) {
        return new GeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private boolean isThisOverlay(Overlay overlay) {
        return (overlay instanceof NamedItemsOverlay
                && ((NamedItemsOverlay) overlay).name == this.overlayName);
    }

    private boolean existInOverlays() {
        return this.map.getOverlays().stream().anyMatch(this::isThisOverlay);
    }

    private void replaceWith(Overlay newOverlay) {
        this.map.getOverlays().replaceAll((overlay) -> {
            if (isThisOverlay(overlay)) {
                return newOverlay;
            }

            return overlay;
        });
    }

}
