package com.maciekjanusz.compassproject.util;

import android.view.Surface;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * This utility contains several mathematical functions for performing processing of values such as
 * bearing, roll, pitch, coordinates, speed, distance etc.
 */
public enum CompassMath {;

    /**
     * The absolute value of maximum valid latitude, in degrees
     */
    public static final float MAX_LATITUDE = 90f;
    /**
     * The absolute value of maximum valid longitude, in degrees
     */
    public static final float MAX_LONGITUDE = 180f;
    /**
     * Maximum value of coordinate minutes or seconds
     */
    private static final float MAX_MIN_SEC = 60;
    /**
     * Earth's radius, in meters
     */
    private static final long EARTH_RADIUS = 6371000;

    /**
     * This function calculates the initial bearing (forward azimuth) from given location
     * to destination.
     *
     * @param fromLat latitude of starting location, in degrees
     * @param fromLon longitude of starting location, in degrees
     * @param toLat latitude of destination, in degrees
     * @param toLon longitude of destination, in degrees
     * @return initial bearing from given location to destination, in degrees
     */
    public static double calculateBearing(double fromLat, double fromLon,
                                          double toLat, double toLon) {
        // convert degree angles to radians
        fromLat = toRadians(fromLat);
        fromLon = toRadians(fromLon);
        toLat = toRadians(toLat);
        toLon = toRadians(toLon);

        // calculate bearing
        double y = sin(toLon - fromLon) * cos(toLat);
        double x = cos(fromLat) * sin(toLat)
                - sin(fromLat) * cos(toLat) * cos(toLon - fromLon);
        // back to degrees
        double bearing = toDegrees(atan2(y, x));
        // ensure no "overflow" angle
        return (bearing + 360f) % 360f;
    }

    public static double calculateDistance(double fromLat, double fromLon,
                                           double toLat, double toLon) {
        // convert degree angles to radians
        double deltaLat = toRadians(toLat - fromLat);
        double deltaLon = toRadians(toLon - fromLon);
        fromLat = toRadians(fromLat);
        toLat = toRadians(toLat);

        // "haversine"
        double a = pow(sin(deltaLat/2.0), 2)
                + cos(fromLat) * cos(toLat)
                * pow(sin(deltaLon/2.0), 2);
        double c = 2 * atan2(sqrt(a), sqrt(1.0-a));
        return EARTH_RADIUS * c;
    }

    /**
     * Function to calculate (estimate) reach time given current speed and
     * distance to destination
     * @param speed speed in m/s
     * @param distance distance in meters
     * @return estimated reach time delta, in seconds
     */
    public static long calculateTimeToReach(double speed, double distance) {
        double time = distance / speed;
        return (long) time;
    }

    /**
     * This function is used to adjust raw bearing according to screen rotation and
     * device roll. If the device is upside down, bearing is inverted. Then, current rotation
     * of the device is added to the bearing.
     *
     * @param bearing raw bearing in degrees, as returned from compass instance
     * @param roll roll in degrees (-180 : 180), as returned from compass instance
     * @param rotation {@link Surface#ROTATION_0}, {@link Surface#ROTATION_90},
     *                 {@link Surface#ROTATION_180}, {@link Surface#ROTATION_270}
     * @return adjusted bearing in degrees
     */
    public static float adjustBearing(float bearing, float roll, int rotation) {
        // if device is more or less upside down, inverse the bearing
        if(roll > 90 || roll <= -90) bearing = -bearing;
        // ensure that bearing is properly adjusted according to current screen rotation
        switch(rotation) {
            case Surface.ROTATION_90:
                bearing = (bearing + 90) % 360;
                break;
            case Surface.ROTATION_180:
                bearing = (bearing + 180) % 360;
                break;
            case Surface.ROTATION_270:
                bearing = (bearing + 270) % 360;
                break;
            // default rotation (ROTATION_0 - upright portrait mode) requires no further adjustment.
        }
        // bearing must be inverted for display
        return bearing;
    }

    /**
     * This function checks if the passed latitude value lies between -{@link #MAX_LATITUDE} and
     * {@link #MAX_LATITUDE} (-90 : 90)
     * @param latitude latitude, in degrees
     * @return true if valid, false otherwise
     */
    public static boolean validateLatitude(float latitude) {
        return latitude >= -MAX_LATITUDE && latitude <= MAX_LATITUDE;
    }

    /**
     * This function checks if the passed longitude value lies between -{@link #MAX_LONGITUDE} and
     * {@link #MAX_LONGITUDE} (-180 : 180)
     * @param longitude longitude, in degrees
     * @return true if valid, false otherwise
     */
    public static boolean validateLongitude(float longitude) {
        return longitude >= -MAX_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    /**
     * This function checks if the passeed value qualify as 0 <= x < {@link #MAX_MIN_SEC}
     * @param minSec value to check
     * @return true if valid
     */
    public static boolean validateMinSec(float minSec) {
        return minSec >= 0 && minSec < MAX_MIN_SEC;
    }

    /**
     * Converts value in meters/second to kilometers/hour.
     * @param speedMs value [m/s]
     * @return value [km/h]
     */
    public static float msToKmH(float speedMs) {
        return speedMs * 3.6f;
    }

    /**
     * Converts value in meters/second to miles per hour.
     * @param speedMs value [m/s]
     * @return value [mph]
     */
    public static float msToMpH(float speedMs) {
        return speedMs * 2.23694f;
    }

    /**
     * Converts value in meters to miles
     * @param meters value [m]
     * @return value [mi]
     */
    public static float metersToMiles(float meters) {
        return meters * 0.00062137f;
    }

    /**
     * Converts coordinates in DMS (degree, minute, second) format to decimal format.
     * @param degrees degrees of coordinate
     * @param minutes minutes of coordinate
     * @param seconds seconds of coordinate
     * @return coordinate in decimal format
     */
    public static float degreesToDecimal(int degrees, int minutes, float seconds) {
        return degrees + (minutes / 60f) + (seconds / 3600f);
    }

    /**
     * Converts coordinates in decimal format to DMS (degree, minute, second) format.
     * @param decimal coordinate in decimal format
     * @return coordinate in DMS format
     */
    public static float[] decimalToDegrees(double decimal) {
        int deg = (int) (floor(decimal * 1000000f) / 1000000f);
        int min = (int) ((floor(abs(decimal) * 1000000f * 60f) / 1000000f) % 60f);
        float sec = (float) ((abs(decimal) * 3600f) % 60f);

        return new float[] {deg, min, sec};
    }

}
