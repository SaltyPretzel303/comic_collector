package mosis.comiccollector.model;

import java.io.Serializable;

public class Location implements Serializable {
    public double latitude;
    public double longitude;

    public Location() {

    }

    public Location(double lat, double lgt) {
        this.latitude = lat;
        this.longitude = lgt;
    }

}
