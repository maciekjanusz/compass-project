package com.maciekjanusz.compassproject.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.maciekjanusz.compassproject.R;

public enum AppPreferences {;

    public static final String WIDGET_SERVICE_RUNNING = "widget_service_running";
    public static final String SHOW_WIDGET_INFO_PREF = "show_widget_info";

    public static boolean isMetricSystem(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultUnitSystemPref = context.getString(R.string.length_unit_default);
        String unitSystemPref = sharedPreferences
                .getString(context.getString(R.string.length_unit_list_preference_key), defaultUnitSystemPref);

        return unitSystemPref.equals(defaultUnitSystemPref);
    }

    public static boolean isDecimalCoordinates(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultInputModePref = context.getString(R.string.coordinates_input_mode_default);
        String inputModePref = sharedPreferences.getString(context
                .getString(R.string.coordinates_input_mode_preference_key), defaultInputModePref);

        return inputModePref.equals(defaultInputModePref);
    }

    public static boolean isWidgetServiceRunning(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(WIDGET_SERVICE_RUNNING, false);
    }
}
