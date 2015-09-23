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

public class DecimalLocationInputView extends LinearLayout implements LocationInputView {

    @Bind(R.id.latitude_edit_text)
    EditText latitudeEditText;

    @Bind(R.id.longitude_edit_text)
    EditText longitudeEditText;

    public DecimalLocationInputView(Context context) {
        super(context);
        init(context);
    }

    public DecimalLocationInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DecimalLocationInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DecimalLocationInputView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_decimal_location_input, this);
        ButterKnife.bind(this, this);

        InputFilter[] latInputFilter = new InputFilter[] {new LatitudeInputFilter()};
        InputFilter[] lonInputFilter = new InputFilter[] {new LongitudeInputFilter()};

        latitudeEditText.setFilters(latInputFilter);
        longitudeEditText.setFilters(lonInputFilter);
    }

    @Override
    public LatLng getLatLng() throws InvalidLocationException {
        String latString = latitudeEditText.getText().toString();
        String lonString = longitudeEditText.getText().toString();

        // if lat or lon is empty, throw exception
        if(latString.isEmpty() || lonString.isEmpty()) {
            throw new InvalidLocationException();
        }

        // input filter ensures valid values
        float latitude = Float.parseFloat(latString);
        float longitude = Float.parseFloat(lonString);
        // return latlng
        return new LatLng(latitude, longitude);
    }
}
