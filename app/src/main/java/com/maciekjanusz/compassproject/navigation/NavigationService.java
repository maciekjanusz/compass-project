package com.maciekjanusz.compassproject.navigation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.maciekjanusz.compassproject.ui.CompassActivity;
import com.maciekjanusz.compassproject.R;

import de.greenrobot.event.EventBus;

import static com.maciekjanusz.compassproject.util.CompassMath.calculateBearing;
import static com.maciekjanusz.compassproject.util.CompassMath.calculateDistance;

public class NavigationService extends Service implements LocationListener {

    private static final String TAG = "NavService";

    public static final String EXTRA_DESTINATION = "extra_destination";

    private LocationRetriever locationRetriever;
    private double destLat, destLon;
    private volatile ServiceState currentState = ServiceState.NAVIGATION_STOPPED;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationRetriever = new LocationRetriever(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        if(intent != null && intent.hasExtra(EXTRA_DESTINATION)) {
            LatLng latLng = intent.getParcelableExtra(EXTRA_DESTINATION);
            destLat = latLng.latitude;
            destLon = latLng.longitude;
        } else {
            stopSelf();
        }

        locationRetriever.registerLocationListener(this);
        locationRetriever.startRetrievingLocation();

        startForeground(startId, createServiceRunningNotification());
        currentState = ServiceState.NAVIGATION_RUNNING;
        EventBus.getDefault().post(currentState);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        locationRetriever.stopRetrievingLocation();
        locationRetriever.unregisterLocationListener(this);
        currentState = ServiceState.NAVIGATION_STOPPED;
        EventBus.getDefault().post(currentState);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // prepare and post navigation bundle to anyone who cares
        double fromLon = location.getLongitude();
        double fromLat = location.getLatitude();
        float bearing = (float) calculateBearing(fromLat, fromLon, destLat, destLon);
        float distance = (float) calculateDistance(fromLat, fromLon, destLat, destLon);

        EventBus.getDefault().post(new NavigationBundle(location, bearing, distance,
                new LatLng(destLat, destLon)));
    }

    private Notification createServiceRunningNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getApplicationContext(), 0,
                        new Intent(getApplicationContext(),
                                CompassActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setSmallIcon(R.drawable.ic_navigation_white_48dp)
                .setContentIntent(pendingIntent)
                .setContentTitle(String.format(getString(R.string.navigation_service_content_title_format), destLat, destLon))
                .setContentText(getString(R.string.navigation_service_content_text));
        return builder.build();
    }

    @SuppressWarnings("unused")
    public void onEvent(ServiceMessage serviceMessage) {
        switch (serviceMessage) {
            case REQUEST_NAVIGATION_SERVICE_STATE:
                // post current state
                EventBus.getDefault().post(currentState);

                // if current location is known, go through onLocationChanged
                // and eventually post the navigationBundle
                Location currentLocation = locationRetriever.getCurrentLocation();
                if(currentLocation != null) {
                    onLocationChanged(currentLocation);
                }

                break;
        }
    }
}