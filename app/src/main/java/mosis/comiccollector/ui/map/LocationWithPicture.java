package mosis.comiccollector.ui.map;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.user.UserLocation;

public class LocationWithPicture {

    private String id;

    private Location location;
    private MutableLiveData<Bitmap> livePic;

    public LocationWithPicture(UserLocation location) {
        this.id = location.getUserId();
        this.location = new Location(location.getLatitude(), location.getLongitude());
    }

    public LocationWithPicture(String id, Location location){
        this.id = id;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public void updateLocation(UserLocation newLocation) {
        this.location = new Location(newLocation.getLatitude(), newLocation.getLongitude());
    }

    public MutableLiveData<Bitmap> getLivePic() {
        return livePic;
    }

    public void setLivePic(MutableLiveData<Bitmap> livePic) {
        this.livePic = livePic;
    }
}
