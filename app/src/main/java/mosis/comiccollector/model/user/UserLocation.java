package mosis.comiccollector.model.user;

import mosis.comiccollector.model.Location;

public class UserLocation {

    public static final String USER_ID_FIELD = "userId";

    private String userId;

    private double latitude;
    private double longitude;

    public UserLocation() {
        // required for serialization
    }


    public UserLocation(String userId,
                        double latitude,
                        double longitude) {

        this.userId = userId;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    // get set ...

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public Location getLocation() {
        return new Location(latitude, longitude);
    }

}
