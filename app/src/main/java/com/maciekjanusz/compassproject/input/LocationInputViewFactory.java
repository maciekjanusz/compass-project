package com.maciekjanusz.compassproject.input;

import android.content.Context;
import android.view.View;

import com.maciekjanusz.compassproject.preferences.AppPreferences;

class LocationInputViewFactory {

    public View getLocationInputView(Context context) {

        if (AppPreferences.isDecimalCoordinates(context)) {
            return new DecimalLocationInputView(context);
        }
        else return new DegreesLocationInputView(context);
    }
}
