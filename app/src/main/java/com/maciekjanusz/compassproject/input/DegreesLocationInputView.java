package com.maciekjanusz.compassproject.input;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.maciekjanusz.compassproject.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.maciekjanusz.compassproject.util.CompassMath.validateLatitude;
import static com.maciekjanusz.compassproject.util.CompassMath.validateLongitude;
import static com.maciekjanusz.compassproject.util.CompassMath.degreesToDecimal;
import static com.maciekjanusz.compassproject.util.CompassMath.validateMinSec;

public class DegreesLocationInputView extends LinearLayout implements LocationInputView {

    @Bind(R.id.latitude_degrees_edit_text) EditText latDegEditText;
    @Bind(R.id.latitude_minutes_edit_text) EditText latMinEditText;
    @Bind(R.id.latitude_seconds_edit_text) EditText latSecEditText;
    @Bind(R.id.longitude_degrees_edit_text) EditText lonDegEditText;
    @Bind(R.id.longitude_minutes_edit_text) EditText lonMinEditText;
    @Bind(R.id.longitude_seconds_edit_text) EditText lonSecEditText;

    public DegreesLocationInputView(Context context) {
        super(context);
        init(context);
    }

    public DegreesLocationInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DegreesLocationInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DegreesLocationInputView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        inflate(context, R.layout.view_degree_location_input, this);
        ButterKnife.bind(this, this);

        InputFilter[] latInputFilter = new InputFilter[] {new LatitudeInputFilter()};
        InputFilter[] lonInputFilter = new InputFilter[] {new LongitudeInputFilter()};
        InputFilter[] minSecInputFilter = new InputFilter[] {new MinSecInputFilter()};

        latDegEditText.setFilters(latInputFilter);
        lonDegEditText.setFilters(lonInputFilter);
        latMinEditText.setFilters(minSecInputFilter);
        lonMinEditText.setFilters(minSecInputFilter);
        latSecEditText.setFilters(minSecInputFilter);
        lonSecEditText.setFilters(minSecInputFilter);
    }

    @Override
    public LatLng getLatLng() throws InvalidLocationException {
        String latDegString = latDegEditText.getText().toString();
        String lonDegString = lonDegEditText.getText().toString();
        String latMinString = latMinEditText.getText().toString();
        String latSecString = latSecEditText.getText().toString();
        String lonMinString = lonMinEditText.getText().toString();
        String lonSecString = lonSecEditText.getText().toString();

        // resolve degrees, throw exception if empty
        if(latDegString.isEmpty() && lonDegString.isEmpty()) {
            throw new InvalidLocationException();
        }
        int latDeg = Integer.parseInt(latDegString);
        int lonDeg = Integer.parseInt(lonDegString);

        // resolve minutes and seconds, pass 0 if empty
        int latMin = latMinString.isEmpty() ? 0 : Integer.parseInt(latMinString);
        float latSec = latSecString.isEmpty() ? 0 : Float.parseFloat(latSecString);
        int lonMin = lonMinString.isEmpty() ? 0 : Integer.parseInt(lonMinString);
        float lonSec = lonSecString.isEmpty() ? 0 : Float.parseFloat(lonSecString);

        // convert to decimal
        float latitude = degreesToDecimal(latDeg, latMin, latSec);
        float longitude = degreesToDecimal(lonDeg, lonMin, lonSec);

        // return
        return new LatLng(latitude, longitude);
    }
}
