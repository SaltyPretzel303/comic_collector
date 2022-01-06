package mosis.comiccollector.repository;

import mosis.comiccollector.model.user.UserLocation;

public interface PeopleLocationConsumer {
    void updateUserLocation(UserLocation newLocation);
}
