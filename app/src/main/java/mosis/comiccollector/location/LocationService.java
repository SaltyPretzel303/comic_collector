package mosis.comiccollector.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.DiscoverMapActivity;
import mosis.comiccollector.util.DateTime;
import mosis.comiccollector.util.DepProvider;

public class LocationService extends Service {

    public static final String LOCATION_BCAST_FILTER = "comiccollector.location_update";

    public static final String LOC_EXTRA_KEY = "location";

    public static final String NOTIF_CHANNEL_ID = "location_channel";
    public static final String NOTIF_NAME = "Location_notification";

    public static final int FG_SERVICE_ID = 102;

    public FusedLocationProviderClient locationClient;
    public LocationCallback locationListener;

    public HandlerThread locHandlerThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("locService", "Hello from service, you are in onStartCommand ... ");

        Notification notif = this.buildNotification();
        startForeground(FG_SERVICE_ID, notif);

        // TODO review this once more at some point ...
        // https://github.com/android/location-samples/blob/main/LocationUpdatesForegroundService/app/src/main/java/com/google/android/gms/location/sample/locationupdatesforegroundservice/LocationUpdatesService.java
        this.locHandlerThread = new HandlerThread("locHandlerThread");
        this.locHandlerThread.start();

        this.locationClient = LocationServices.getFusedLocationProviderClient(this);

        // TODO read this values from intent passed from map activity
        LocationRequest lReq = LocationRequest.create();
        lReq.setInterval(1000);
        lReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        this.locationListener = new LocationHandler(this);

        this.locationClient.requestLocationUpdates(
                lReq,
                this.locationListener,
                this.locHandlerThread.getLooper());

        Log.e("fgService", "Location requested ... ");

        // old "wrong" implementation
//        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        this.locationListener = new LocationHandler(this);
//
//        this.locationManager.requestLocationUpdates(
////                LocationManager.FUSED_PROVIDER,
//                LocationManager.GPS_PROVIDER,
//                500,
//                100,
//                locationListener);

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification() {

        // TODO customize this a little bit so it looks decent

        Intent notifIntent = new Intent(this, DiscoverMapActivity.class);
        PendingIntent notifPendIntent = PendingIntent.getActivity(
                this,
                0,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationChannel notifChannel = new NotificationChannel(
                NOTIF_CHANNEL_ID,
                NOTIF_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(notifChannel);

        Notification notif = new Notification.Builder(this, NOTIF_CHANNEL_ID)
                .setContentTitle("Title ")
                .setContentText("Content text ... ")
                .setContentIntent(notifPendIntent)
                .build();

        return notif;
    }


    static class LocationHandler extends LocationCallback {

        private Context serviceContext;

        public LocationHandler(Context context) {
            this.serviceContext = context;
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {

            Location location = locationResult.getLastLocation();

            Intent bcastIntent = new Intent();
            bcastIntent.setAction(LOCATION_BCAST_FILTER);
            bcastIntent.putExtra(LOC_EXTRA_KEY, location);

            String myId = DepProvider
                    .getAuthRepository()
                    .getCurrentUser()
                    .user.getUserId();

            DepProvider
                    .getPeopleRepository()
                    .updateLocation(myId, new UserLocation(
                            myId,
                            location.getLatitude(),
                            location.getLongitude(),
                            DateTime.now()));

            Log.e("LocationService", "bcasting: ( "
                    + "lat->" + location.getLatitude()
                    + " long->" + location.getLongitude()
                    + " )");

            serviceContext.sendBroadcast(bcastIntent);
        }
    }

    @Override
    public void onDestroy() {
        Log.e("location service", "On destroy location service ... ");

        if (this.locationClient != null) {
            this.locationClient.removeLocationUpdates(this.locationListener);
        }

        if (this.locHandlerThread != null && !this.locHandlerThread.isInterrupted()) {
            this.locHandlerThread.interrupt();
        }

    }

}
