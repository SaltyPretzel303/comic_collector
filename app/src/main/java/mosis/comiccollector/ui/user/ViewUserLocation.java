package mosis.comiccollector.ui.user;

import java.util.Date;

public class ViewUserLocation {

    public double latitude;
    public double longitude;

    public Date lastSeen;

    public ViewUserLocation(double lat, double longit, Date lastSeen) {
        this.latitude = lat;
        this.longitude = longit;

        this.lastSeen = lastSeen;
    }


}
