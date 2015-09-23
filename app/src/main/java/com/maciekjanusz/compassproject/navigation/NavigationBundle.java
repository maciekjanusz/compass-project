package com.maciekjanusz.compassproject.navigation;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Wrapper class for values resolved by the navigation service
 */
public class NavigationBundle {

    /**
     * Device location
     */
    private final Location location;
    /**
     * Destination coordinates
     */
    private final LatLng destination;
    /**
     * Bearing to destination, in positive degrees
     */
    private final float bearing;
    /**
     * Distance to destination, in meters
     */
    private final float distance;

    public NavigationBundle(Location location, float bearing, float distance, LatLng destination) {
        this.location = location;
        this.bearing = bearing;
        this.distance = distance;
        this.destination = destination;
    }

    public Location getLocation() {
        return location;
    }

    public float getBearing() {
        return bearing;
    }

    public float getDistance() {
        return distance;
    }

    public LatLng getDestination() {
        return destination;
    }
}
