package mosis.comiccollector.location;

import mosis.comiccollector.model.user.UserLocation;

public interface LocationConsumer {
    void updateLocation(UserLocation newLocation);
}
