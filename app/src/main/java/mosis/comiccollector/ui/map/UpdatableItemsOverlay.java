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

import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.user.UserLocation;

public class UpdatableItemsOverlay {

    private Context context;

    private NamedItemsOverlay.Names overlayName;

    private MapView map;
    private Drawable icon;
    private List<LocationWithPicture> locations;
    private ItemizedIconOverlay.OnItemGestureListener<OverlayItemWithId> clickHandler;

    private boolean enabled;

    public UpdatableItemsOverlay(
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

        this.enabled = true;

        this.generateOverlay();
    }

    public void enable() {
        this.enabled = true;
        generateOverlay();
    }

    public void disable() {
        this.enabled = false;
        this.removeOverlay();
        this.map.postInvalidate();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void updateItem(UserLocation newItem, boolean follow) {

        Predicate<LocationWithPicture> filterItem = (location) -> {
            return location.getId().equals(newItem.getUserId());
        };

        if (this.locations.stream().anyMatch(filterItem)) {
            this.locations.stream()
                    .filter(filterItem)
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

            this.addItem(newLocation);
        }

        if (enabled) {
            this.generateOverlay();
            if (follow) {
                this.map.getController().animateTo(
                        new GeoPoint(
                                newItem.getLatitude(),
                                newItem.getLongitude()));
            }
        }
    }

    public void setItems(List<LocationWithPicture> newLocations) {
        this.locations = newLocations;

        if (enabled) {
            this.generateOverlay();
        }
    }

    public void addItem(LocationWithPicture newLocation) {
        this.locations.add(newLocation);

        if (enabled) {
            this.generateOverlay();
        }
    }

    public int getItemsCount() {
        return this.locations.size();
    }

    private void generateOverlay() {

        for (LocationWithPicture item : this.locations) {
            if (!item.getLivePic().hasObservers()) {
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
                    item.getId(),
                    "",
                    "",
                    getPoint(item.getLocation()));

            if (item.getLivePic().getValue() == null) {
                // set default marker
                // newItem.setMarker(this.icon);
            } else {
                // TODO create marker using profile pic ...
//                Log.e("MovingOverlay", "This item has pic ... ");
            }

            // TODO remove this after upper if gets implemented
            newItem.setMarker(this.icon);

            overlayItems.add(newItem);
        }

        Overlay newOverlay = new NamedItemsOverlay<>(
                this.overlayName,
                this.context,
                overlayItems,
                this.clickHandler);

        this.addOverlay(newOverlay);

        this.map.postInvalidate();
    }

    private void addOverlay(Overlay newOverlay) {
        if (existInOverlays()) {
            replaceWith(newOverlay);
        } else {
            map.getOverlays().add(newOverlay);
        }
    }

    private GeoPoint getPoint(Location loc) {
        return new GeoPoint(loc.latitude, loc.longitude);
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

    private void removeOverlay() {
        this.map.getOverlays().removeIf((overlay) -> {
            return (overlay instanceof NamedItemsOverlay
                    && ((NamedItemsOverlay) overlay).name == this.overlayName);
        });
    }

}
