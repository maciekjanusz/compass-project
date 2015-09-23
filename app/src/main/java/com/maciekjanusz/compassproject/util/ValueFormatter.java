package com.maciekjanusz.compassproject.util;

import android.content.Context;

import com.maciekjanusz.compassproject.R;
import com.maciekjanusz.compassproject.preferences.AppPreferences;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.maciekjanusz.compassproject.util.CompassMath.decimalToDegrees;
import static com.maciekjanusz.compassproject.util.CompassMath.metersToMiles;
import static com.maciekjanusz.compassproject.util.CompassMath.msToKmH;
import static com.maciekjanusz.compassproject.util.CompassMath.msToMpH;

/**
 * Use this class for formatting compass & navigation values. Resolves several resource strings
 * during init, checks preferences for displaying coordinates and unit system.
 * Contains {@link #stringBuilder} that enables memory allocation optimized formatting of
 * pitch & roll values.
 */
public class ValueFormatter {

    private static final char DEGREE = '\u00B0';

    /**
     * StringBuilder for formatting pitch & roll values
     */
    private final StringBuilder stringBuilder = new StringBuilder();

    /**
     * Flag indicating whether to use metric system
     */
    private boolean metricSystem;

    /**
     * Flag indicating whether to use decimal coordinates
     */
    private boolean decimalCoordinates;

    /**
     * Array with cardinal direction strings (N, NE, E...)
     */
    private String[] cardinalDirections;

    private String daysString;
    private String hoursString;
    private String minutesString;
    private String kmhUnit;
    private String kmUnit;
    private String mphUnit;
    private String mileUnit;
    private String meterUnit;
    private String decimalCoordinatesFormat;
    private String dmsCoordinatesFormat;
    private String speedFormat;
    private String durationFormat;
    private String metricDistanceFormat;
    private String imperialDistanceFormat;

    public ValueFormatter(Context context) {
        loadCardinalDirections(context);
        loadPrefs(context);
        loadUnitStrings(context);
        loadFormatStrings(context);
    }

    public void loadCardinalDirections(Context context) {
        // cardinal directions string array
        cardinalDirections = context.getResources().getStringArray(R.array.cardinal_directions);
    }

    public void loadPrefs(Context context) {
        // preferences
        metricSystem = AppPreferences.isMetricSystem(context);
        decimalCoordinates = AppPreferences.isDecimalCoordinates(context);
    }

    public void loadUnitStrings(Context context) {
        /*
            Resolution of unit strings
         */
        daysString = context.getString(R.string.days);
        hoursString = context.getString(R.string.hours);
        minutesString = context.getString(R.string.minutes);
        kmUnit = context.getString(R.string.unit_kilometer);
        mileUnit = context.getString(R.string.unit_mile);
        meterUnit = context.getString(R.string.unit_meter);
        kmhUnit = context.getString(R.string.unit_kmh);
        mphUnit = context.getString(R.string.unit_mph);
    }

    public void loadFormatStrings(Context context) {
        /*
            Resolution of format strings
         */
        decimalCoordinatesFormat = context.getString(R.string.decimal_coordinates_format);
        dmsCoordinatesFormat = context.getString(R.string.dms_coordinates_format);
        speedFormat = context.getString(R.string.speed_format);
        durationFormat = context.getString(R.string.duration_format);
        metricDistanceFormat = context.getString(R.string.metric_distance_format);
        imperialDistanceFormat = context.getString(R.string.imperial_distance_format);
    }

    /**
     * This function formats time duration: if duration is larger than a day, returns number of
     * full days. If shorter than a day, but longer than an hour, shows number of full hours.
     * If shorter than hour, shows number of full minutes.
     * @param duration duration, in seconds
     * @return formatted duration
     */
    public String formatDuration(long duration) {
        long days = TimeUnit.SECONDS.toDays(duration);
        long hours = TimeUnit.SECONDS.toHours(duration - TimeUnit.DAYS.toSeconds(days));
        long minutes = TimeUnit.SECONDS.toMinutes(duration - TimeUnit.DAYS.toSeconds(days) - TimeUnit.HOURS.toMillis(hours));

        if (days > 0) {
            if (hours > 12) {
                days++;
            }
            return String.format(Locale.getDefault(), durationFormat, days, daysString);
        } else if (hours > 0) {
            if (minutes > 30) {
                hours++;
            }
            return String.format(Locale.getDefault(), durationFormat, hours, hoursString);
        } else {
            return String.format(Locale.getDefault(), durationFormat, minutes, minutesString);
        }
    }

    /**
     * This function formats meter distance value to desired unit (km, m or mi)
     * @param distance distance [m]
     * @return formatted distance
     */
    public String formatDistance(float distance) {
        if (metricSystem) {
            if (distance >= 1000) {
                distance /= 1000;
                return String.format(Locale.getDefault(), metricDistanceFormat, (int) distance, kmUnit);
            } else {
                return String.format(Locale.getDefault(), metricDistanceFormat, (int) distance, meterUnit);
            }
        } else { // imperial
            return String.format(Locale.US, imperialDistanceFormat, metersToMiles(distance), mileUnit);
        }
    }

    /**
     * This function formats m/s speed value to desired unit (km/h or mph)
     * @param speed speed [m/s]
     * @return formatted speed
     */
    public String formatSpeed(float speed) {
        int convertedSpeed;
        String unit;
        if (metricSystem) {
            convertedSpeed = (int) msToKmH(speed);
            unit = kmhUnit;
        } else {
            convertedSpeed = (int) msToMpH(speed);
            unit = mphUnit;
        }
        return String.format(Locale.getDefault(), speedFormat, convertedSpeed, unit);
    }

    /**
     * This function checks if lat/lon should be formatted as decimal or DMS and
     * returns properly formatted string.
     * @param latitude latitude, decimal
     * @param longitude longitude, decimal
     * @return formatted string
     */
    public String formatCoordinates(double latitude, double longitude) {
        if (decimalCoordinates) {
            return String.format(Locale.getDefault(), decimalCoordinatesFormat, latitude, longitude);
        } else {
            float[] degMinSecLat = decimalToDegrees(latitude);
            float[] degMinSecLon = decimalToDegrees(longitude);
            return String.format(Locale.getDefault(), dmsCoordinatesFormat,
                    (int) degMinSecLat[0], (int) degMinSecLat[1], degMinSecLat[2],
                    (int) degMinSecLon[0], (int) degMinSecLon[1], degMinSecLon[2]);
        }
    }

    /**
     * This function converts bearing in degrees to cardinal direction symbol, ie.: 0 degrees
     * converts to "N"
     * @param bearingDegrees bearing to convert, in degrees
     * @return cardinal direction symbol string
     */
    public String getCardinalDirection(float bearingDegrees) {
        int directions = cardinalDirections.length;
        int angleStep = 360 / directions;
        int halfStep = angleStep / 2;

        for(int i = 0; i < directions - 1; i++) {
            int angle = i * angleStep;
            if(bearingDegrees >= angle - halfStep
                    && bearingDegrees < angle + halfStep) {
                return cardinalDirections[i];
            }
        }
        return cardinalDirections[directions - 1];
    }

    /**
     * Format float degree value, such as pitch or roll, by casting to int and appending degree
     * symbol.
     * This function uses stringBuilder for memory allocation optimization purposes; the pitch & roll
     * values are updated very quickly, and using String.format function everytime they are updated
     * would cause excessive string allocations.
     * @param degreeValue value to format
     * @return formatted value
     */
    public String formatDegreeValue(int degreeValue) {
        stringBuilder.setLength(0);
        stringBuilder.append(degreeValue).append(DEGREE);
        return stringBuilder.toString();
    }
}
