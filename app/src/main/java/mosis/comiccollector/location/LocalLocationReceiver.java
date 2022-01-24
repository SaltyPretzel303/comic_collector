package mosis.comiccollector.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.util.DateTime;
import mosis.comiccollector.util.DepProvider;

public class LocalLocationReceiver extends BroadcastReceiver {

    private String myId;
    private LocationConsumer consumer;

    public LocalLocationReceiver(String myId, LocationConsumer consumer) {
        this.consumer = consumer;
        this.myId = myId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

//        Log.e("bcastRcver", "Local location rcvr got newLocation ... ");

        Location newLocation = (Location) intent
                .getExtras()
                .getParcelable(LocationService.LOC_EXTRA_KEY);

        if (newLocation == null) {
            Log.e("bcastRcver", "Location is null ... ");
            return;
        }
//        Log.e("bCast receiver", "rcvdLocation:"
//                + " lat: " + newLocation.getLatitude()
//                + " - "
//                + "long: " + newLocation.getLongitude());

        UserLocation userLocation = new UserLocation(
                this.myId,
                newLocation.getLatitude(),
                newLocation.getLongitude(),
                DateTime.now());

        this.consumer.updateLocation(userLocation);

    }
}
