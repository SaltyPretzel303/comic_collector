package mosis.comiccollector.model.user;

public class UserPosition {

    public String userId;

    public double lastLongitude;
    public double lastLatitude;


    public UserPosition(double lastLongitude, double lastLatitude) {
        this.lastLongitude = lastLongitude;
        this.lastLatitude = lastLatitude;
    }
}
