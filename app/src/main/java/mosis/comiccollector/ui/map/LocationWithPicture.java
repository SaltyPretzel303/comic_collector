package mosis.comiccollector.ui.map;

import androidx.lifecycle.MutableLiveData;

import mosis.comiccollector.model.user.UserLocation;

public class LocationWithPicture {

    private String userId;

    private UserLocation location;
    private MutableLiveData<String> livePicUri;

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

    public MutableLiveData<String> getLivePicUri() {
        return livePicUri;
    }

    public void setLivePicUri(MutableLiveData<String> livePicUri) {
        this.livePicUri = livePicUri;
    }
}
