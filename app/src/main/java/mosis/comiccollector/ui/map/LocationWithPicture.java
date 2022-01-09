package mosis.comiccollector.ui.map;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import mosis.comiccollector.model.user.UserLocation;

public class LocationWithPicture {

    private String userId;

    private UserLocation location;
    private MutableLiveData<Bitmap> livePic;

    public LocationWithPicture(UserLocation location) {
        this.userId = location.getUserId();
        this.location = location;
    }

    public UserLocation getLocation() {
        return location;
    }

    public String getUserId() {
        return userId;
    }

    public void updateLocation(UserLocation newLocation) {
        this.location = newLocation;
    }

    public MutableLiveData<Bitmap> getLivePic() {
        return livePic;
    }

    public void setLivePic(MutableLiveData<Bitmap> livePic) {
        this.livePic = livePic;
    }
}
