package mosis.comiccollector.ui.map;

import mosis.comiccollector.model.user.UserLocation;

public interface PersonFollower {

    String getPersonId();

    void consumePersonLocation(UserLocation newLocation);
}
