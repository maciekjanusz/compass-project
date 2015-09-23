package com.maciekjanusz.compassproject.navigation;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * This class isolates the logic of location objects retrieval through GoogleApiClient.
 */
class LocationRetriever implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Location request time interval - with fused API 5 sec is minimum
     */
    private static final long REQUEST_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(5);

    /**
     * GoogleApiClient instance for retrieving locations through fused API
     */
    private GoogleApiClient googleApiClient;
    /**
     * LocationRequest object initialized within {@link #createLocationRequest()}
     */
    private LocationRequest locationRequest;
    /**
     * LocationListener for returning location through its callback
     */
    private final List<LocationListener> locationListeners = new CopyOnWriteArrayList<>();

    /**
     * Last retrieved location. Can be null.
     */
    @Nullable
    private volatile Location currentLocation;

    public LocationRetriever(Context context) {
        buildGoogleApiClient(context.getApplicationContext());
    }

    /**
     * Sets up GoogleApiClient. Called in constructor {@link #LocationRetriever(Context)}
     * @param context Context for GoogleApiClient.Builder
     */
    private synchronized void buildGoogleApiClient(final Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * This method initializes {@link #locationRequest} with {@link #REQUEST_INTERVAL_MILLIS}
     * as interval.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(REQUEST_INTERVAL_MILLIS);
        locationRequest.setFastestInterval(REQUEST_INTERVAL_MILLIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Starts updating location with googleApiClient.
     */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /**
     * Stops googleApiClient from updating location.
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    /**
     * Call to start retrieving location
     */
    public void startRetrievingLocation() {
        googleApiClient.connect();
    }

    /**
     * Call to stop retrieving location
     */
    public void stopRetrievingLocation() {
        if(googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // when client connects, start location updates
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // forward connection result to activity
        EventBus.getDefault().post(connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        // set current location
        currentLocation = location;
        // notify listener
        for(LocationListener locationListener : locationListeners) {
            locationListener.onLocationChanged(location);
        }
    }

    /**
     * Getter for current location
     * @return current location. Might be null
     */
    @Nullable
    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void registerLocationListener(LocationListener locationListener) {
        locationListeners.add(locationListener);
    }

    public void unregisterLocationListener(LocationListener locationListener) {
        locationListeners.remove(locationListener);
    }

}