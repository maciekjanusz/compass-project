package com.maciekjanusz.compassproject.input;

import android.text.InputFilter;
import android.text.Spanned;

import com.maciekjanusz.compassproject.util.CompassMath;

public abstract class CoordinateInputFilter implements InputFilter {

    private static final char NEGATIVE_SIGN = '-';

    protected boolean allowNegative = true;

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // allow to type negative sign
        if (allowNegative && source.length() == 1) {
            if (source.charAt(0) == NEGATIVE_SIGN) {
                return null;
            }
        }

        StringBuilder builder = new StringBuilder(dest);
        if (dstart > (dest.length() - 1)) {
            builder.append(source);
        } else {
            builder.replace(dstart, dend, source.toString().substring(start, end));
        }

        String newString = builder.toString();

            try {
                float newValue = Float.parseFloat(newString);
                if (validateInput(newValue)) {
                    return null; // Accept
                } else {
                    return ""; // Reject
                }
            } catch (NumberFormatException e) {
                return ""; // Reject input
            }

    }

    protected abstract boolean validateInput(float input);
}

class LatitudeInputFilter extends CoordinateInputFilter {

    @Override
    protected boolean validateInput(float input) {
        return CompassMath.validateLatitude(input);
    }
}

class LongitudeInputFilter extends CoordinateInputFilter {

    @Override
    protected boolean validateInput(float input) {
        return CompassMath.validateLongitude(input);
    }
}

class MinSecInputFilter extends CoordinateInputFilter {

    {
        // min / sec cannot be negative
        allowNegative = false;
    }

    @Override
    protected boolean validateInput(float input) {
        return CompassMath.validateMinSec(input);
    }
}
