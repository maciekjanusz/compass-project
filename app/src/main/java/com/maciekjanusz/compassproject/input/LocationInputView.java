package com.maciekjanusz.compassproject.input;

import com.google.android.gms.maps.model.LatLng;

interface LocationInputView {

    LatLng getLatLng() throws InvalidLocationException;
}
